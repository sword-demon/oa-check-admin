package com.oa.admin.approval.listener;

import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalTaskCreateListener implements TaskListener {
    private final BizApprovalInstanceMapper instanceMapper;
    private final BizApprovalTaskMapper taskMapper;

    @Override
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        String assignee = delegateTask.getAssignee();

        // Find the matching business instance
        BizApprovalInstance instance = instanceMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizApprovalInstance>()
                .eq(BizApprovalInstance::getFlowableProcessInstanceId, processInstanceId)
        );
        if (instance == null) {
            log.warn("No business instance found for process: {}", processInstanceId);
            return;
        }

        BizApprovalTask task = new BizApprovalTask();
        task.setApprovalInstanceId(instance.getId());
        task.setFlowableTaskId(delegateTask.getId());
        task.setAssigneeUserId(Long.parseLong(assignee));
        task.setTaskName(delegateTask.getName());
        taskMapper.insert(task);

        log.info("Created approval task: instanceId={}, taskId={}, assignee={}, name={}",
            instance.getId(), delegateTask.getId(), assignee, delegateTask.getName());
    }
}
