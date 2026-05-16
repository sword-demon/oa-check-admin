package com.oa.admin.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.dto.DashboardStatsVO;
import com.oa.admin.approval.dto.InstanceDiagramVO;
import com.oa.admin.approval.entity.BizApprovalCc;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.entity.BizProcessNodeConfig;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizApprovalCcMapper;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import com.oa.admin.approval.constant.ApprovalConstants;
import com.oa.admin.approval.constant.FlowableConstants;
import com.oa.admin.approval.enums.ApprovalInstanceStatus;
import com.oa.admin.approval.enums.ApprovalTaskResult;
import com.oa.admin.approval.enums.TemplateStatus;
import com.oa.admin.approval.service.ApprovalCcService;
import com.oa.admin.approval.service.ApprovalService;
import com.oa.admin.approval.service.ApprovalTemplateService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.common.result.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl extends ServiceImpl<BizApprovalInstanceMapper, BizApprovalInstance> implements ApprovalService {
    private final ApprovalTemplateService templateService;
    private final BizApprovalTaskMapper taskMapper;
    private final BizApprovalCcMapper ccMapper;
    private final ApprovalCcService ccService;
    private final RuntimeService runtimeService;
    private final TaskService flowableTaskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public BizApprovalInstance submit(Long templateId, String title, String formData) {
        BizProcessTemplate template = templateService.getById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (!template.getStatus().equals(TemplateStatus.PUBLISHED.getCode())) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_PUBLISHED);
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
        ApprovalTaskResult taskResult;
        try {
            taskResult = ApprovalTaskResult.of(result);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

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

        task.setTaskResult(taskResult.getCode());
        task.setTaskComment(comment);
        task.setCompletedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        Map<String, Object> variables = new HashMap<>();
        variables.put(FlowableConstants.VAR_APPROVED, taskResult == ApprovalTaskResult.APPROVED);

        // Get task definition key before completing (needed for CC lookup)
        String taskDefinitionKey = null;
        try {
            org.flowable.task.api.Task flowableTask = flowableTaskService.createTaskQuery()
                .taskId(task.getFlowableTaskId())
                .singleResult();
            if (flowableTask != null) {
                taskDefinitionKey = flowableTask.getTaskDefinitionKey();
            }
        } catch (Exception e) {
            log.warn("Failed to get task definition key: {}", e.getMessage());
        }

        flowableTaskService.complete(task.getFlowableTaskId(), variables);

        // Trigger CC if configured for this node
        if (taskDefinitionKey != null) {
            BizApprovalInstance instance = this.getById(task.getApprovalInstanceId());
            if (instance != null) {
                triggerCcByNodeKey(taskDefinitionKey, instance);
            }
        }
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

        // Clean up orphaned pending tasks
        tasks.stream()
            .filter(t -> t.getTaskResult() == null)
            .forEach(t -> {
                t.setTaskResult(ApprovalTaskResult.CANCELLED.getCode());
                t.setTaskComment(ApprovalConstants.WITHDRAW_REASON);
                t.setCompletedAt(LocalDateTime.now());
                taskMapper.updateById(t);
            });
    }

    @Override
    public List<BizApprovalTask> instanceTasks(Long instanceId) {
        return taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getApprovalInstanceId, instanceId)
                .orderByAsc(BizApprovalTask::getCreatedAt)
        );
    }

    @Override
    public PageResult<BizApprovalInstance> myApplications(String title, Integer status, long page, long pageSize) {
        long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<BizApprovalInstance> wrapper = new LambdaQueryWrapper<BizApprovalInstance>()
            .eq(BizApprovalInstance::getInitiatorUserId, userId);

        if (title != null && !title.isBlank()) {
            wrapper.like(BizApprovalInstance::getInstanceTitle, title);
        }
        if (status != null) {
            wrapper.eq(BizApprovalInstance::getStatus, status);
        }
        wrapper.orderByDesc(BizApprovalInstance::getCreatedAt);

        Page<BizApprovalInstance> result = this.page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public BizApprovalInstance getInstanceDetail(Long instanceId) {
        BizApprovalInstance instance = this.getById(instanceId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND);
        }
        return instance;
    }

    @Override
    public InstanceDiagramVO getInstanceDiagram(Long instanceId) {
        BizApprovalInstance instance = this.getById(instanceId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND);
        }

        String processInstanceId = instance.getFlowableProcessInstanceId();

        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(processInstanceId)
            .finished()
            .list();

        List<String> completedNodeIds = activities.stream()
            .map(HistoricActivityInstance::getActivityId)
            .distinct()
            .collect(Collectors.toList());

        List<Task> activeTasks = flowableTaskService.createTaskQuery()
            .processInstanceId(processInstanceId)
            .list();

        List<String> currentNodeIds = activeTasks.stream()
            .map(Task::getTaskDefinitionKey)
            .distinct()
            .collect(Collectors.toList());

        String bpmnXml;
        try {
            var historicInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
            if (historicInstance != null) {
                String processDefinitionId = historicInstance.getProcessDefinitionId();
                var pd = repositoryService.getProcessDefinition(processDefinitionId);
                var resourceStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
                bpmnXml = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
                resourceStream.close();
            } else {
                bpmnXml = "";
            }
        } catch (Exception e) {
            log.warn("Failed to get BPMN XML for instance {}: {}", instanceId, e.getMessage());
            bpmnXml = "";
        }

        return InstanceDiagramVO.builder()
            .bpmnXml(bpmnXml)
            .completedNodeIds(completedNodeIds)
            .currentNodeIds(currentNodeIds)
            .build();
    }

    @Override
    public DashboardStatsVO dashboardStats() {
        long userId = StpUtil.getLoginIdAsLong();

        long todoCount = taskMapper.selectCount(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getAssigneeUserId, userId)
                .isNull(BizApprovalTask::getTaskResult)
        );

        long doneCount = taskMapper.selectCount(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getAssigneeUserId, userId)
                .isNotNull(BizApprovalTask::getTaskResult)
        );

        long templateCount = templateService.count(
            new LambdaQueryWrapper<BizProcessTemplate>()
                .eq(BizProcessTemplate::getStatus, TemplateStatus.PUBLISHED.getCode())
        );

        long unreadCcCount = ccMapper.selectCount(
            new LambdaQueryWrapper<BizApprovalCc>()
                .eq(BizApprovalCc::getCcUserId, userId)
                .isNull(BizApprovalCc::getReadAt)
        );

        List<BizApprovalTask> recentTasks = taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getAssigneeUserId, userId)
                .isNotNull(BizApprovalTask::getTaskResult)
                .orderByDesc(BizApprovalTask::getCompletedAt)
                .last("LIMIT 10")
        );

        return DashboardStatsVO.builder()
            .todoCount(todoCount)
            .doneCount(doneCount)
            .templateCount(templateCount)
            .unreadCcCount(unreadCcCount)
            .recentActivities(recentTasks)
            .build();
    }

    private void triggerCcByNodeKey(String nodeKey, BizApprovalInstance instance) {
        try {
            List<BizProcessNodeConfig> configs = templateService.getNodeConfigs(instance.getProcessTemplateId());
            for (BizProcessNodeConfig config : configs) {
                if (config.getNodeId().equals(nodeKey) && config.getCcConfig() != null && !config.getCcConfig().isBlank()) {
                    List<Long> ccUserIds = objectMapper.readValue(config.getCcConfig(),
                        new TypeReference<List<Long>>() {});
                    ccService.createCc(instance.getId(), ccUserIds, "审批节点[" + config.getNodeName() + "]完成抄送");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to trigger CC for node {}: {}", nodeKey, e.getMessage());
        }
    }
}
