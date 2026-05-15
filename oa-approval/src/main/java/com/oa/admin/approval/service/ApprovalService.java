package com.oa.admin.approval.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService extends ServiceImpl<BizApprovalInstanceMapper, BizApprovalInstance> {
    private final ApprovalTemplateService templateService;
    private final BizApprovalTaskMapper taskMapper;
    private final RuntimeService runtimeService;
    private final TaskService flowableTaskService;
    private final ObjectMapper objectMapper;

    @Transactional
    public BizApprovalInstance submit(Long templateId, String title, String formData) {
        BizProcessTemplate template = templateService.getById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }

        long userId = StpUtil.getLoginIdAsLong();

        // Start Flowable process by template key (matching BPMN process id)
        Map<String, Object> variables = new HashMap<>();
        variables.put("initiator", userId);

        // Extract form fields as process variables for gateway conditions
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
            String.valueOf(userId), // business key
            variables
        );

        // Create business instance
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setProcessTemplateId(templateId);
        instance.setInstanceTitle(title);
        instance.setFlowableProcessInstanceId(processInstance.getId());
        instance.setInitiatorUserId(userId);
        instance.setStatus(1); // pending
        instance.setFormData(formData);
        this.save(instance);

        return instance;
    }

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

        // Complete Flowable task with approved variable for gateway routing
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", result == 1);
        flowableTaskService.complete(task.getFlowableTaskId(), variables);
    }

    public List<BizApprovalTask> myTodo() {
        long userId = StpUtil.getLoginIdAsLong();
        return taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getAssigneeUserId, userId)
                .isNull(BizApprovalTask::getTaskResult)
                .orderByDesc(BizApprovalTask::getCreatedAt)
        );
    }

    public List<BizApprovalTask> myDone() {
        long userId = StpUtil.getLoginIdAsLong();
        return taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getAssigneeUserId, userId)
                .isNotNull(BizApprovalTask::getTaskResult)
                .orderByDesc(BizApprovalTask::getCompletedAt)
        );
    }

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
        if (instance.getStatus() != 1) {
            throw new BusinessException(ErrorCode.CANNOT_WITHDRAW);
        }

        // Check if first task is unprocessed
        List<BizApprovalTask> tasks = taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getApprovalInstanceId, instanceId)
                .orderByAsc(BizApprovalTask::getCreatedAt)
        );
        boolean anyProcessed = tasks.stream().anyMatch(t -> t.getTaskResult() != null);
        if (anyProcessed) {
            throw new BusinessException(ErrorCode.CANNOT_WITHDRAW);
        }

        instance.setStatus(4); // withdrawn
        this.updateById(instance);

        // Delete Flowable process instance
        runtimeService.deleteProcessInstance(instance.getFlowableProcessInstanceId(), "发起人撤回");
    }

    public List<BizApprovalTask> instanceTasks(Long instanceId) {
        return taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getApprovalInstanceId, instanceId)
                .orderByAsc(BizApprovalTask::getCreatedAt)
        );
    }
}
