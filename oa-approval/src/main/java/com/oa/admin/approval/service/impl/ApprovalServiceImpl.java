package com.oa.admin.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import com.oa.admin.approval.constant.ApprovalConstants;
import com.oa.admin.approval.constant.FlowableConstants;
import com.oa.admin.approval.enums.ApprovalInstanceStatus;
import com.oa.admin.approval.enums.ApprovalTaskResult;
import com.oa.admin.approval.service.ApprovalService;
import com.oa.admin.approval.service.ApprovalTemplateService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl extends ServiceImpl<BizApprovalInstanceMapper, BizApprovalInstance> implements ApprovalService {
    private final ApprovalTemplateService templateService;
    private final BizApprovalTaskMapper taskMapper;
    private final RuntimeService runtimeService;
    private final TaskService flowableTaskService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public BizApprovalInstance submit(Long templateId, String title, String formData) {
        BizProcessTemplate template = templateService.getById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }

        long userId = StpUtil.getLoginIdAsLong();

        Map<String, Object> variables = new HashMap<>();
        variables.put(FlowableConstants.VAR_INITIATOR, userId);

        if (formData != null && !formData.isBlank()) {
            try {
                Map<String, Object> formFields = objectMapper.readValue(formData,
                    new TypeReference<Map<String, Object>>() {});
                variables.putAll(formFields);
            } catch (Exception e) {
                log.warn("Failed to parse formData as JSON for variables: {}", e.getMessage());
            }
        }

        var processInstance = runtimeService.startProcessInstanceByKey(
            template.getTemplateKey(),
            String.valueOf(userId),
            variables
        );

        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setProcessTemplateId(templateId);
        instance.setInstanceTitle(title);
        instance.setFlowableProcessInstanceId(processInstance.getId());
        instance.setInitiatorUserId(userId);
        instance.setStatus(ApprovalInstanceStatus.PENDING.getCode());
        instance.setFormData(formData);
        this.save(instance);

        return instance;
    }

    @Override
    @Transactional
    public void approve(Long taskId, int result, String comment) {
        BizApprovalTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        if (task.getTaskResult() != null) {
            throw new BusinessException(ErrorCode.ALREADY_APPROVED);
        }

        long userId = StpUtil.getLoginIdAsLong();
        if (!task.getAssigneeUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        task.setTaskResult(result);
        task.setTaskComment(comment);
        task.setCompletedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        Map<String, Object> variables = new HashMap<>();
        variables.put(FlowableConstants.VAR_APPROVED, result == ApprovalTaskResult.APPROVED.getCode());
        flowableTaskService.complete(task.getFlowableTaskId(), variables);
    }

    @Override
    public List<BizApprovalTask> myTodo() {
        long userId = StpUtil.getLoginIdAsLong();
        return taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getAssigneeUserId, userId)
                .isNull(BizApprovalTask::getTaskResult)
                .orderByDesc(BizApprovalTask::getCreatedAt)
        );
    }

    @Override
    public List<BizApprovalTask> myDone() {
        long userId = StpUtil.getLoginIdAsLong();
        return taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getAssigneeUserId, userId)
                .isNotNull(BizApprovalTask::getTaskResult)
                .orderByDesc(BizApprovalTask::getCompletedAt)
        );
    }

    @Override
    @Transactional
    public void withdraw(Long instanceId) {
        BizApprovalInstance instance = this.getById(instanceId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND);
        }

        long userId = StpUtil.getLoginIdAsLong();
        if (!instance.getInitiatorUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!Integer.valueOf(ApprovalInstanceStatus.PENDING.getCode()).equals(instance.getStatus())) {
            throw new BusinessException(ErrorCode.CANNOT_WITHDRAW);
        }

        List<BizApprovalTask> tasks = taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getApprovalInstanceId, instanceId)
                .orderByAsc(BizApprovalTask::getCreatedAt)
        );
        boolean anyProcessed = tasks.stream().anyMatch(t -> t.getTaskResult() != null);
        if (anyProcessed) {
            throw new BusinessException(ErrorCode.CANNOT_WITHDRAW);
        }

        instance.setStatus(ApprovalInstanceStatus.WITHDRAWN.getCode());
        this.updateById(instance);

        runtimeService.deleteProcessInstance(instance.getFlowableProcessInstanceId(), ApprovalConstants.WITHDRAW_REASON);
    }

    @Override
    public List<BizApprovalTask> instanceTasks(Long instanceId) {
        return taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getApprovalInstanceId, instanceId)
                .orderByAsc(BizApprovalTask::getCreatedAt)
        );
    }
}
