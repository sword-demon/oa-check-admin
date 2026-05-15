package com.oa.admin.approval.listener;

import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessEndEventListener implements FlowableEventListener {
    private final BizApprovalInstanceMapper instanceMapper;

    @Override
    public void onEvent(FlowableEvent event) {
        if (event instanceof FlowableEngineEntityEvent entityEvent) {
            if (entityEvent.getEntity() instanceof ExecutionEntity execution) {
                String processInstanceId = execution.getProcessInstanceId();
                BizApprovalInstance instance = instanceMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizApprovalInstance>()
                        .eq(BizApprovalInstance::getFlowableProcessInstanceId, processInstanceId)
                );
                if (instance != null) {
                    instance.setStatus(2);
                    instance.setEndAt(LocalDateTime.now());
                    instanceMapper.updateById(instance);
                    log.info("Process completed: instanceId={}", instance.getId());
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
