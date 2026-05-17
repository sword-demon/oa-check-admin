package com.oa.admin.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.dto.AdminMetricsVO;
import com.oa.admin.approval.dto.DashboardStatsVO;
import com.oa.admin.approval.dto.InstanceDiagramVO;
import com.oa.admin.approval.dto.InstanceVO;
import com.oa.admin.approval.dto.TaskVO;
import com.oa.admin.approval.entity.BizApprovalCc;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.entity.BizProcessNodeConfig;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizApprovalCcMapper;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import com.oa.admin.approval.constant.ApprovalConstants;
import com.oa.admin.approval.constant.AuditConstants;
import com.oa.admin.approval.constant.FlowableConstants;
import com.oa.admin.approval.enums.ApprovalInstanceStatus;
import com.oa.admin.approval.enums.ApprovalTaskResult;
import com.oa.admin.approval.enums.ApprovalTaskType;
import com.oa.admin.approval.enums.NotificationType;
import com.oa.admin.approval.enums.TemplateStatus;
import com.oa.admin.approval.service.ApprovalCcService;
import com.oa.admin.approval.service.ApprovalFormSchemaService;
import com.oa.admin.approval.service.ApprovalService;
import com.oa.admin.approval.service.ApprovalTemplateService;
import com.oa.admin.approval.service.AuditLogService;
import com.oa.admin.approval.service.NotificationService;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
/**
 * @author wxvirus
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl extends ServiceImpl<BizApprovalInstanceMapper, BizApprovalInstance> implements ApprovalService {
    private static final String BPMN_DIAGRAM_MARKER = "BPMNDiagram";
    private static final String LEAVE_PROCESS_ID = "leave_request";
    private static final String LEAVE_PROCESS_ID_ATTRIBUTE = "id=\"leave_request\"";
    private static final String BPMN_DI_NAMESPACES =
        "xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"\n" +
        "             xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\"\n" +
        "             xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\"\n" +
        "             ";
    private static final String LEAVE_PROCESS_DIAGRAM = """

  <bpmndi:BPMNDiagram id="BPMNDiagram_leave_request">
    <bpmndi:BPMNPlane id="BPMNPlane_leave_request" bpmnElement="leave_request">
      <bpmndi:BPMNShape id="BPMNShape_startEvent" bpmnElement="startEvent">
        <omgdc:Bounds x="120" y="160" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_task1" bpmnElement="task1">
        <omgdc:Bounds x="300" y="138" width="120" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_task2" bpmnElement="task2">
        <omgdc:Bounds x="480" y="138" width="120" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_endEvent" bpmnElement="endEvent">
        <omgdc:Bounds x="660" y="160" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="BPMNEdge_flow1" bpmnElement="flow1">
        <omgdi:waypoint x="156" y="178"/>
        <omgdi:waypoint x="300" y="178"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="BPMNEdge_flow2" bpmnElement="flow2">
        <omgdi:waypoint x="420" y="178"/>
        <omgdi:waypoint x="480" y="178"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="BPMNEdge_flow3" bpmnElement="flow3">
        <omgdi:waypoint x="600" y="178"/>
        <omgdi:waypoint x="660" y="178"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
""";

    private final ApprovalTemplateService templateService;
    private final BizApprovalTaskMapper taskMapper;
    private final BizApprovalCcMapper ccMapper;
    private final ApprovalCcService ccService;
    private final RuntimeService runtimeService;
    private final TaskService flowableTaskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final ApprovalFormSchemaService formSchemaService;

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

        Map<String, Object> formFields = parseFormData(formData);
        formSchemaService.validateSubmission(template.getFormConfig(), formFields);
        variables.putAll(formFields);

        if (template.getFlowableProcessDefinitionId() == null || template.getFlowableProcessDefinitionId().isBlank()) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_PUBLISHED);
        }

        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setProcessTemplateId(templateId);
        instance.setInstanceTitle(title);
        instance.setInitiatorUserId(userId);
        instance.setStatus(ApprovalInstanceStatus.PENDING.getCode());
        instance.setFormData(formData);
        this.save(instance);

        variables.put(FlowableConstants.VAR_APPROVAL_INSTANCE_ID, instance.getId());

        var processInstance = runtimeService.startProcessInstanceById(
            template.getFlowableProcessDefinitionId(),
            String.valueOf(instance.getId()),
            variables
        );

        instance.setFlowableProcessInstanceId(processInstance.getId());
        this.updateById(instance);

        auditLogService.log(AuditConstants.MODULE_APPROVAL, AuditConstants.ACTION_SUBMIT,
            AuditConstants.TARGET_INSTANCE, instance.getId(),
            "{\"title\":\"" + title + "\",\"templateId\":" + templateId + "}");

        return instance;
    }

    private Map<String, Object> parseFormData(String formData) {
        if (formData == null || formData.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(formData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new BusinessException(
                ErrorCode.PARAM_ERROR.getCode(),
                "表单数据必须是合法 JSON 对象"
            );
        }
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

        String auditAction = taskResult == ApprovalTaskResult.APPROVED
            ? AuditConstants.ACTION_APPROVE : AuditConstants.ACTION_REJECT;
        auditLogService.log(AuditConstants.MODULE_APPROVAL, auditAction,
            AuditConstants.TARGET_TASK, taskId,
            "{\"result\":" + result + ",\"comment\":\"" + (comment != null ? comment : "") + "\"}");

        // Notify initiator about approval/rejection result
        BizApprovalInstance inst = this.getById(task.getApprovalInstanceId());
        if (inst != null) {
            String notifType = taskResult == ApprovalTaskResult.APPROVED
                ? NotificationType.APPROVED.getCode() : NotificationType.REJECTED.getCode();
            String notifTitle = taskResult == ApprovalTaskResult.APPROVED
                ? "审批已通过" : "审批已驳回";
            notificationService.send(inst.getInitiatorUserId(), notifType, notifTitle,
                inst.getInstanceTitle() + " - " + notifTitle,
                "/approval/instance/" + inst.getId());
        }

        // Trigger CC if configured for this node
        if (taskDefinitionKey != null) {
            if (inst != null) {
                triggerCcByNodeKey(taskDefinitionKey, inst);
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

        auditLogService.log(AuditConstants.MODULE_APPROVAL, AuditConstants.ACTION_WITHDRAW,
            AuditConstants.TARGET_INSTANCE, instanceId, null);

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
        BizApprovalInstance instance = this.getById(instanceId);
        if (instance != null) {
            syncActiveTasks(instance);
        }
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
                bpmnXml = ensureDisplayDiagram(new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8));
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

    private void syncActiveTasks(BizApprovalInstance instance) {
        if (instance.getFlowableProcessInstanceId() == null
            || !Integer.valueOf(ApprovalInstanceStatus.PENDING.getCode()).equals(instance.getStatus())) {
            return;
        }

        List<Task> activeTasks;
        try {
            activeTasks = flowableTaskService.createTaskQuery()
                .processInstanceId(instance.getFlowableProcessInstanceId())
                .list();
        } catch (Exception e) {
            log.warn("Failed to sync active tasks for instance {}: {}", instance.getId(), e.getMessage());
            return;
        }
        if (activeTasks == null || activeTasks.isEmpty()) {
            return;
        }

        List<BizApprovalTask> existingTasks = taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getApprovalInstanceId, instance.getId())
        );
        Set<String> existingFlowableTaskIds = existingTasks.stream()
            .map(BizApprovalTask::getFlowableTaskId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        for (Task activeTask : activeTasks) {
            if (existingFlowableTaskIds.contains(activeTask.getId())) {
                continue;
            }
            String assignee = activeTask.getAssignee();
            if (assignee == null || assignee.isBlank()) {
                log.warn("Skip syncing unassigned active task {} for instance {}",
                    activeTask.getId(), instance.getId());
                continue;
            }

            long assigneeUserId;
            try {
                assigneeUserId = Long.parseLong(assignee);
            } catch (NumberFormatException e) {
                log.warn("Skip syncing active task {} with invalid assignee {}",
                    activeTask.getId(), assignee);
                continue;
            }

            BizApprovalTask task = new BizApprovalTask();
            task.setApprovalInstanceId(instance.getId());
            task.setFlowableTaskId(activeTask.getId());
            task.setAssigneeUserId(assigneeUserId);
            task.setTaskName(activeTask.getName());
            task.setTaskType(ApprovalTaskType.NORMAL.getCode());
            taskMapper.insert(task);
            existingFlowableTaskIds.add(activeTask.getId());
            log.info("Synced missing approval task: instanceId={}, taskId={}, assignee={}",
                instance.getId(), activeTask.getId(), assignee);
        }
    }

    private String ensureDisplayDiagram(String bpmnXml) {
        if (bpmnXml == null || bpmnXml.isBlank() || bpmnXml.contains(BPMN_DIAGRAM_MARKER)) {
            return bpmnXml;
        }
        if (!bpmnXml.contains(LEAVE_PROCESS_ID_ATTRIBUTE)) {
            return bpmnXml;
        }

        String xmlWithNamespaces = bpmnXml;
        if (!xmlWithNamespaces.contains("xmlns:bpmndi")) {
            xmlWithNamespaces = xmlWithNamespaces.replaceFirst("<definitions\\s+", "<definitions " + BPMN_DI_NAMESPACES);
        }
        return xmlWithNamespaces.replace("</definitions>", LEAVE_PROCESS_DIAGRAM + "</definitions>");
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

    @Override
    public PageResult<TaskVO> myTodoPaged(String title, long page, long pageSize) {
        long userId = StpUtil.getLoginIdAsLong();

        List<Long> instanceIds = null;
        if (title != null && !title.isBlank()) {
            List<BizApprovalInstance> matching = this.list(new LambdaQueryWrapper<BizApprovalInstance>()
                .like(BizApprovalInstance::getInstanceTitle, title)
                .select(BizApprovalInstance::getId));
            instanceIds = matching.stream().map(BizApprovalInstance::getId).toList();
            if (instanceIds.isEmpty()) {
                return new PageResult<>(List.of(), 0, page, pageSize);
            }
        }

        LambdaQueryWrapper<BizApprovalTask> wrapper = new LambdaQueryWrapper<BizApprovalTask>()
            .eq(BizApprovalTask::getAssigneeUserId, userId)
            .isNull(BizApprovalTask::getTaskResult);

        if (instanceIds != null) {
            wrapper.in(BizApprovalTask::getApprovalInstanceId, instanceIds);
        }
        wrapper.orderByDesc(BizApprovalTask::getCreatedAt);

        Page<BizApprovalTask> taskPage = taskMapper.selectPage(new Page<>(page, pageSize), wrapper);

        List<TaskVO> vos = enrichTasksWithInstance(taskPage.getRecords());
        return new PageResult<>(vos, taskPage.getTotal(), page, pageSize);
    }

    @Override
    public PageResult<TaskVO> myDonePaged(String title, long page, long pageSize) {
        long userId = StpUtil.getLoginIdAsLong();

        List<Long> instanceIds = null;
        if (title != null && !title.isBlank()) {
            List<BizApprovalInstance> matching = this.list(new LambdaQueryWrapper<BizApprovalInstance>()
                .like(BizApprovalInstance::getInstanceTitle, title)
                .select(BizApprovalInstance::getId));
            instanceIds = matching.stream().map(BizApprovalInstance::getId).toList();
            if (instanceIds.isEmpty()) {
                return new PageResult<>(List.of(), 0, page, pageSize);
            }
        }

        LambdaQueryWrapper<BizApprovalTask> wrapper = new LambdaQueryWrapper<BizApprovalTask>()
            .eq(BizApprovalTask::getAssigneeUserId, userId)
            .isNotNull(BizApprovalTask::getTaskResult);

        if (instanceIds != null) {
            wrapper.in(BizApprovalTask::getApprovalInstanceId, instanceIds);
        }
        wrapper.orderByDesc(BizApprovalTask::getCompletedAt);

        Page<BizApprovalTask> taskPage = taskMapper.selectPage(new Page<>(page, pageSize), wrapper);

        List<TaskVO> vos = enrichTasksWithInstance(taskPage.getRecords());
        return new PageResult<>(vos, taskPage.getTotal(), page, pageSize);
    }

    private List<TaskVO> enrichTasksWithInstance(List<BizApprovalTask> tasks) {
        if (tasks.isEmpty()) return List.of();

        Set<Long> instIds = tasks.stream()
            .map(BizApprovalTask::getApprovalInstanceId)
            .collect(Collectors.toSet());

        Map<Long, BizApprovalInstance> instanceMap = new HashMap<>();
        this.listByIds(instIds).forEach(inst -> instanceMap.put(inst.getId(), inst));

        return tasks.stream().map(task -> {
            BizApprovalInstance instance = instanceMap.get(task.getApprovalInstanceId());
            String summary = null;
            if (instance != null && instance.getFormData() != null && !instance.getFormData().isBlank()) {
                summary = instance.getFormData().length() > 100
                    ? instance.getFormData().substring(0, 100) + "..."
                    : instance.getFormData();
            }
            return TaskVO.builder()
                .id(task.getId())
                .approvalInstanceId(task.getApprovalInstanceId())
                .flowableTaskId(task.getFlowableTaskId())
                .assigneeUserId(task.getAssigneeUserId())
                .taskName(task.getTaskName())
                .taskType(task.getTaskType())
                .taskResult(task.getTaskResult())
                .taskComment(task.getTaskComment())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .instanceTitle(instance != null ? instance.getInstanceTitle() : null)
                .initiatorUserId(instance != null ? instance.getInitiatorUserId() : null)
                .instanceStatus(instance != null ? instance.getStatus() : null)
                .formDataSummary(summary)
                .build();
        }).toList();
    }

    @Override
    @Transactional
    public void transfer(Long taskId, Long targetUserId, String reason) {
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
        if (targetUserId == null || targetUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // Mark current task as transferred
        task.setTaskResult(ApprovalTaskResult.TRANSFERRED.getCode());
        task.setTaskComment(reason);
        task.setCompletedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // Create new task for target user
        BizApprovalTask newTask = new BizApprovalTask();
        newTask.setApprovalInstanceId(task.getApprovalInstanceId());
        newTask.setFlowableTaskId(task.getFlowableTaskId());
        newTask.setAssigneeUserId(targetUserId);
        newTask.setTaskName(task.getTaskName());
        newTask.setTaskType(task.getTaskType());
        taskMapper.insert(newTask);

        // Reassign in Flowable
        flowableTaskService.setAssignee(task.getFlowableTaskId(), String.valueOf(targetUserId));

        // Audit
        auditLogService.log(AuditConstants.MODULE_APPROVAL, AuditConstants.ACTION_TRANSFER,
            AuditConstants.TARGET_TASK, taskId,
            "{\"targetUserId\":" + targetUserId + ",\"reason\":\"" + (reason != null ? reason : "") + "\"}");

        // Notify target user
        notificationService.send(targetUserId, NotificationType.TASK_TRANSFERRED.getCode(),
            "收到转办任务", "任务已转办给您, 请及时处理",
            "/approval/my-todo");
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

    // ========== Admin operations ==========

    @Override
    public PageResult<InstanceVO> adminInstances(String title, Integer status, Long templateId,
                                                  Long initiatorUserId, String startTime, String endTime,
                                                  long page, long pageSize) {
        LambdaQueryWrapper<BizApprovalInstance> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isBlank()) {
            wrapper.like(BizApprovalInstance::getInstanceTitle, title);
        }
        if (status != null) {
            wrapper.eq(BizApprovalInstance::getStatus, status);
        }
        if (templateId != null) {
            wrapper.eq(BizApprovalInstance::getProcessTemplateId, templateId);
        }
        if (initiatorUserId != null) {
            wrapper.eq(BizApprovalInstance::getInitiatorUserId, initiatorUserId);
        }
        if (startTime != null && !startTime.isBlank()) {
            wrapper.ge(BizApprovalInstance::getCreatedAt, startTime);
        }
        if (endTime != null && !endTime.isBlank()) {
            wrapper.le(BizApprovalInstance::getCreatedAt, endTime);
        }
        wrapper.orderByDesc(BizApprovalInstance::getCreatedAt);

        Page<BizApprovalInstance> result = this.page(new Page<>(page, pageSize), wrapper);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<InstanceVO> vos = result.getRecords().stream().map(inst -> InstanceVO.builder()
            .id(inst.getId())
            .processTemplateId(inst.getProcessTemplateId())
            .instanceTitle(inst.getInstanceTitle())
            .initiatorUserId(inst.getInitiatorUserId())
            .status(inst.getStatus())
            .formData(inst.getFormData())
            .createdAt(inst.getCreatedAt() != null ? inst.getCreatedAt().format(fmt) : null)
            .endAt(inst.getEndAt() != null ? inst.getEndAt().format(fmt) : null)
            .build()
        ).toList();

        return new PageResult<>(vos, result.getTotal(), page, pageSize);
    }

    @Override
    @Transactional
    public void terminateInstance(Long instanceId) {
        BizApprovalInstance instance = this.getById(instanceId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND);
        }
        if (!Integer.valueOf(ApprovalInstanceStatus.PENDING.getCode()).equals(instance.getStatus())) {
            throw new BusinessException(ErrorCode.CANNOT_TERMINATE);
        }

        instance.setStatus(ApprovalInstanceStatus.CANCELLED.getCode());
        instance.setEndAt(LocalDateTime.now());
        this.updateById(instance);

        try {
            runtimeService.deleteProcessInstance(instance.getFlowableProcessInstanceId(), "Admin terminated");
        } catch (Exception e) {
            log.warn("Failed to delete Flowable process instance for termination: {}", e.getMessage());
        }

        List<BizApprovalTask> tasks = taskMapper.selectList(
            new LambdaQueryWrapper<BizApprovalTask>()
                .eq(BizApprovalTask::getApprovalInstanceId, instanceId)
                .isNull(BizApprovalTask::getTaskResult)
        );
        tasks.forEach(t -> {
            t.setTaskResult(ApprovalTaskResult.CANCELLED.getCode());
            t.setTaskComment("管理员终止");
            t.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(t);
        });

        auditLogService.log(AuditConstants.MODULE_APPROVAL, AuditConstants.ACTION_TERMINATE,
            AuditConstants.TARGET_INSTANCE, instanceId, null);

        // Notify initiator
        notificationService.send(instance.getInitiatorUserId(),
            NotificationType.INSTANCE_TERMINATED.getCode(),
            "审批已被管理员终止",
            instance.getInstanceTitle() + " 已被管理员终止",
            "/approval/instance/" + instanceId);
    }

    @Override
    @Transactional
    public void reassignTask(Long taskId, Long targetUserId) {
        BizApprovalTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        if (task.getTaskResult() != null) {
            throw new BusinessException(ErrorCode.TASK_ALREADY_PROCESSED);
        }
        if (targetUserId == null || targetUserId.equals(task.getAssigneeUserId())) {
            throw new BusinessException(ErrorCode.INVALID_TRANSFER_TARGET);
        }

        Long originalAssignee = task.getAssigneeUserId();
        task.setAssigneeUserId(targetUserId);
        taskMapper.updateById(task);

        flowableTaskService.setAssignee(task.getFlowableTaskId(), String.valueOf(targetUserId));

        auditLogService.log(AuditConstants.MODULE_APPROVAL, AuditConstants.ACTION_REASSIGN,
            AuditConstants.TARGET_TASK, taskId,
            "{\"from\":" + originalAssignee + ",\"to\":" + targetUserId + "}");
    }

    @Override
    public AdminMetricsVO metrics() {
        long total = this.count();
        long pending = this.count(new LambdaQueryWrapper<BizApprovalInstance>()
            .eq(BizApprovalInstance::getStatus, ApprovalInstanceStatus.PENDING.getCode()));
        long approved = this.count(new LambdaQueryWrapper<BizApprovalInstance>()
            .eq(BizApprovalInstance::getStatus, ApprovalInstanceStatus.APPROVED.getCode()));
        long rejected = this.count(new LambdaQueryWrapper<BizApprovalInstance>()
            .eq(BizApprovalInstance::getStatus, ApprovalInstanceStatus.REJECTED.getCode()));
        long withdrawn = this.count(new LambdaQueryWrapper<BizApprovalInstance>()
            .eq(BizApprovalInstance::getStatus, ApprovalInstanceStatus.WITHDRAWN.getCode()));

        List<BizApprovalInstance> completedInstances = this.list(new LambdaQueryWrapper<BizApprovalInstance>()
            .isNotNull(BizApprovalInstance::getEndAt)
            .isNotNull(BizApprovalInstance::getCreatedAt));

        double avgHours = 0;
        if (!completedInstances.isEmpty()) {
            double totalHours = completedInstances.stream()
                .filter(i -> i.getEndAt() != null && i.getCreatedAt() != null)
                .mapToLong(i -> java.time.Duration.between(i.getCreatedAt(), i.getEndAt()).toMinutes())
                .average().orElse(0);
            avgHours = totalHours / 60.0;
        }

        List<BizProcessTemplate> templates = templateService.list(
            new LambdaQueryWrapper<BizProcessTemplate>()
                .eq(BizProcessTemplate::getStatus, TemplateStatus.PUBLISHED.getCode()));

        List<AdminMetricsVO.TemplateMetric> templateMetrics = templates.stream().map(tpl -> {
            LambdaQueryWrapper<BizApprovalInstance> tplWrapper = new LambdaQueryWrapper<BizApprovalInstance>()
                .eq(BizApprovalInstance::getProcessTemplateId, tpl.getId());
            long tplTotal = this.count(tplWrapper);
            long tplPending = this.count(new LambdaQueryWrapper<BizApprovalInstance>()
                .eq(BizApprovalInstance::getProcessTemplateId, tpl.getId())
                .eq(BizApprovalInstance::getStatus, ApprovalInstanceStatus.PENDING.getCode()));
            long tplApproved = this.count(new LambdaQueryWrapper<BizApprovalInstance>()
                .eq(BizApprovalInstance::getProcessTemplateId, tpl.getId())
                .eq(BizApprovalInstance::getStatus, ApprovalInstanceStatus.APPROVED.getCode()));
            long tplRejected = this.count(new LambdaQueryWrapper<BizApprovalInstance>()
                .eq(BizApprovalInstance::getProcessTemplateId, tpl.getId())
                .eq(BizApprovalInstance::getStatus, ApprovalInstanceStatus.REJECTED.getCode()));
            return AdminMetricsVO.TemplateMetric.builder()
                .templateId(tpl.getId())
                .templateName(tpl.getTemplateName())
                .total(tplTotal).pending(tplPending).approved(tplApproved).rejected(tplRejected)
                .build();
        }).toList();

        return AdminMetricsVO.builder()
            .totalInstances(total)
            .pendingInstances(pending)
            .approvedInstances(approved)
            .rejectedInstances(rejected)
            .withdrawnInstances(withdrawn)
            .avgDurationHours(Math.round(avgHours * 100.0) / 100.0)
            .templateMetrics(templateMetrics)
            .build();
    }
}
