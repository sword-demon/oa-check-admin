package com.oa.admin.approval.listener;

import com.oa.admin.approval.constant.FlowableConstants;
import com.oa.admin.approval.enums.ApprovalTaskType;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;
/**
 * @author wxvirus
 */

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
        if (assignee == null || assignee.isBlank()) {
            log.warn("Task created without assignee for process: {}", processInstanceId);
            return;
        }

        BizApprovalInstance instance = instanceMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizApprovalInstance>()
                .eq(BizApprovalInstance::getFlowableProcessInstanceId, processInstanceId)
        );
        if (instance == null) {
            log.warn("No business instance found for process: {}", processInstanceId);
            return;
        }

        int taskType = detectTaskType(delegateTask);

        long assigneeUserId;
        try {
            assigneeUserId = Long.parseLong(assignee);
        } catch (NumberFormatException e) {
            log.error("Invalid assignee format for task {}: expected numeric, got '{}'",
                delegateTask.getId(), assignee);
            return;
        }

        BizApprovalTask task = new BizApprovalTask();
        task.setApprovalInstanceId(instance.getId());
        task.setFlowableTaskId(delegateTask.getId());
        task.setAssigneeUserId(assigneeUserId);
        task.setTaskName(delegateTask.getName());
        task.setTaskType(taskType);
        taskMapper.insert(task);

        log.info("Created approval task: instanceId={}, taskId={}, assignee={}, name={}, type={}",
            instance.getId(), delegateTask.getId(), assignee, delegateTask.getName(), taskType);
    }

    /**
     * Detect multi-instance task type from task variables.
     * Flowable sets nrOfInstances/nrOfActiveInstances/nrOfCompletedInstances
     * as local variables on tasks within a multi-instance activity.
     */
    private int detectTaskType(DelegateTask delegateTask) {
        Object nrOfInstances = delegateTask.getVariableLocal("nrOfInstances");
        if (nrOfInstances == null) {
            nrOfInstances = delegateTask.getVariable("nrOfInstances");
        }
        if (nrOfInstances == null) {
            return ApprovalTaskType.NORMAL.getCode();
        }
        Object completionCondition = delegateTask.getVariable("completionCondition");
        if (completionCondition != null) {
            String condition = completionCondition.toString();
            if (condition.contains(FlowableConstants.UEL_OR_SIGN_CONDITION)) {
                return ApprovalTaskType.OR_SIGN.getCode();
            }
        }
        return ApprovalTaskType.COUNTERSIGN.getCode();
    }
}
