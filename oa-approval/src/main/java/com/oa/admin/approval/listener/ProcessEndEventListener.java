package com.oa.admin.approval.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.admin.approval.enums.ApprovalInstanceStatus;
import com.oa.admin.approval.enums.ApprovalTaskResult;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessEndEventListener implements FlowableEventListener {
    private final BizApprovalInstanceMapper instanceMapper;
    private final BizApprovalTaskMapper taskMapper;

    @Override
    public void onEvent(FlowableEvent event) {
        if (event instanceof FlowableEngineEntityEvent entityEvent) {
            if (entityEvent.getEntity() instanceof ExecutionEntity execution) {
                String processInstanceId = execution.getProcessInstanceId();
                BizApprovalInstance instance = instanceMapper.selectOne(
                    new LambdaQueryWrapper<BizApprovalInstance>()
                        .eq(BizApprovalInstance::getFlowableProcessInstanceId, processInstanceId)
                );
                if (instance != null) {
                    // Determine final status from business task results
                    List<BizApprovalTask> tasks = taskMapper.selectList(
                        new LambdaQueryWrapper<BizApprovalTask>()
                            .eq(BizApprovalTask::getApprovalInstanceId, instance.getId())
                    );

                    boolean anyRejected = tasks.stream()
                        .anyMatch(t -> t.getTaskResult() != null && t.getTaskResult().equals(ApprovalTaskResult.REJECTED.getCode()));

                    instance.setStatus(anyRejected ? ApprovalInstanceStatus.REJECTED.getCode() : ApprovalInstanceStatus.APPROVED.getCode());
                    instance.setEndAt(LocalDateTime.now());
                    instanceMapper.updateById(instance);
                    log.info("Process completed: instanceId={}, status={}", instance.getId(), anyRejected ? "rejected" : "approved");
                }
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return true;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
