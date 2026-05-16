package com.oa.admin.approval.service;

import com.oa.admin.approval.service.impl.ApprovalServiceImpl;
import com.oa.admin.approval.enums.TemplateStatus;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.admin.approval.dto.DashboardStatsVO;
import com.oa.admin.approval.dto.InstanceDiagramVO;
import com.oa.admin.approval.entity.BizApprovalCc;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizApprovalCcMapper;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.common.result.PageResult;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricActivityInstanceQuery;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalTemplateService templateService;

    @Mock
    private BizApprovalTaskMapper taskMapper;

    @Mock
    private BizApprovalInstanceMapper instanceMapper;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private TaskService flowableTaskService;

    @Mock
    private BizApprovalCcMapper ccMapper;

    @Mock
    private HistoryService historyService;

    @Mock
    private RepositoryService repositoryService;

    private ApprovalService approvalService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        approvalService = new ApprovalServiceImpl(templateService, taskMapper, ccMapper, runtimeService, flowableTaskService, historyService, repositoryService, objectMapper);
        injectBaseMapper(approvalService, instanceMapper);
    }

    static void injectBaseMapper(Object service, Object mapper) throws Exception {
        Class<?> c = service.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField("baseMapper");
                f.setAccessible(true);
                f.set(service, mapper);
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new RuntimeException("baseMapper field not found");
    }

    private BizProcessTemplate buildTemplate(Long id, String key) {
        BizProcessTemplate t = new BizProcessTemplate();
        t.setId(id);
        t.setTemplateKey(key);
        t.setTemplateName("Test Template");
        t.setStatus(TemplateStatus.PUBLISHED.getCode());
        return t;
    }

    private BizApprovalTask buildTask(Long id, Long assigneeId, Integer result) {
        BizApprovalTask task = new BizApprovalTask();
        task.setId(id);
        task.setAssigneeUserId(assigneeId);
        task.setTaskResult(result);
        task.setFlowableTaskId("flowable-" + id);
        task.setApprovalInstanceId(100L);
        return task;
    }

    @Test
    void submit_withNonexistentTemplate_throwsTemplateNotFound() {
        when(templateService.getById(999L)).thenReturn(null);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.submit(999L, "title", "{}"));
            assertEquals(ErrorCode.TEMPLATE_NOT_FOUND.getCode(), ex.getCode());
        }
    }

    @Test
    void submit_withValidTemplate_createsInstance() {
        BizProcessTemplate template = buildTemplate(1L, "leave_request");
        when(templateService.getById(1L)).thenReturn(template);

        ProcessInstance pi = mock(ProcessInstance.class);
        when(pi.getId()).thenReturn("proc-123");
        when(runtimeService.startProcessInstanceByKey(anyString(), anyString(), anyMap()))
                .thenReturn(pi);
        when(instanceMapper.insert(any(BizApprovalInstance.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            BizApprovalInstance result = approvalService.submit(1L, "请假", "{\"days\":3}");
            assertNotNull(result);
            assertEquals(1, result.getStatus());
            assertEquals("proc-123", result.getFlowableProcessInstanceId());
        }
    }

    @Test
    void approve_withNonexistentTask_throwsTaskNotFound() {
        when(taskMapper.selectById(999L)).thenReturn(null);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.approve(999L, 1, "ok"));
            assertEquals(ErrorCode.TASK_NOT_FOUND.getCode(), ex.getCode());
        }
    }

    @Test
    void approve_alreadyProcessedTask_throwsAlreadyApproved() {
        BizApprovalTask task = buildTask(1L, 1L, 1);
        when(taskMapper.selectById(1L)).thenReturn(task);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.approve(1L, 1, "ok"));
            assertEquals(ErrorCode.ALREADY_APPROVED.getCode(), ex.getCode());
        }
    }

    @Test
    void approve_wrongAssignee_throwsForbidden() {
        BizApprovalTask task = buildTask(1L, 2L, null);
        when(taskMapper.selectById(1L)).thenReturn(task);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.approve(1L, 1, "ok"));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        }
    }

    @Test
    void approve_validTask_completesSuccessfully() {
        BizApprovalTask task = buildTask(1L, 1L, null);
        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskMapper.updateById(any(BizApprovalTask.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            approvalService.approve(1L, 1, "approved");

            verify(taskMapper).updateById(any(BizApprovalTask.class));
            verify(flowableTaskService).complete(eq("flowable-1"), argThat((java.util.Map<String, Object> vars) ->
                vars != null && Boolean.TRUE.equals(vars.get("approved"))
            ));
        }
    }

    @Test
    void approve_rejection_passesApprovedFalse() {
        BizApprovalTask task = buildTask(1L, 1L, null);
        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskMapper.updateById(any(BizApprovalTask.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            approvalService.approve(1L, 2, "rejected");

            verify(flowableTaskService).complete(eq("flowable-1"), argThat((java.util.Map<String, Object> vars) ->
                vars != null && Boolean.FALSE.equals(vars.get("approved"))
            ));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void submit_extractsFormFieldsAsVariables() {
        BizProcessTemplate template = buildTemplate(1L, "leave_request_v2");
        when(templateService.getById(1L)).thenReturn(template);

        ProcessInstance pi = mock(ProcessInstance.class);
        when(pi.getId()).thenReturn("proc-456");
        when(runtimeService.startProcessInstanceByKey(anyString(), anyString(), anyMap()))
                .thenReturn(pi);
        when(instanceMapper.insert(any(BizApprovalInstance.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            approvalService.submit(1L, "请假", "{\"leave_days\":5,\"leave_type\":\"sick\"}");

            verify(runtimeService).startProcessInstanceByKey(
                eq("leave_request_v2"),
                eq("1"),
                argThat(vars -> {
                    Object days = vars.get("leave_days");
                    Object type = vars.get("leave_type");
                    return vars.get("initiator").equals(1L)
                        && days != null && type != null;
                })
            );
        }
    }

    @Test
    void withdraw_nonexistentInstance_throwsInstanceNotFound() {
        when(instanceMapper.selectById(999L)).thenReturn(null);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.withdraw(999L));
            assertEquals(ErrorCode.INSTANCE_NOT_FOUND.getCode(), ex.getCode());
        }
    }

    @Test
    void withdraw_wrongInitiator_throwsForbidden() {
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setId(1L);
        instance.setInitiatorUserId(2L);
        instance.setStatus(1);
        when(instanceMapper.selectById(1L)).thenReturn(instance);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.withdraw(1L));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        }
    }

    @Test
    void withdraw_alreadyProcessedStatus_throwsCannotWithdraw() {
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setId(1L);
        instance.setInitiatorUserId(1L);
        instance.setStatus(2);
        when(instanceMapper.selectById(1L)).thenReturn(instance);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.withdraw(1L));
            assertEquals(ErrorCode.CANNOT_WITHDRAW.getCode(), ex.getCode());
        }
    }

    @Test
    void withdraw_withProcessedTasks_throwsCannotWithdraw() {
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setId(1L);
        instance.setInitiatorUserId(1L);
        instance.setStatus(1);
        when(instanceMapper.selectById(1L)).thenReturn(instance);

        BizApprovalTask processedTask = buildTask(1L, 2L, 1);
        when(taskMapper.selectList(any())).thenReturn(List.of(processedTask));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.withdraw(1L));
            assertEquals(ErrorCode.CANNOT_WITHDRAW.getCode(), ex.getCode());
        }
    }

    @Test
    void myTodo_returnsUnprocessedTasksForCurrentUser() {
        BizApprovalTask task = buildTask(1L, 1L, null);
        when(taskMapper.selectList(any())).thenReturn(List.of(task));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            List<BizApprovalTask> result = approvalService.myTodo();
            assertEquals(1, result.size());
        }
    }

    @Test
    void myDone_returnsProcessedTasksForCurrentUser() {
        BizApprovalTask task = buildTask(1L, 1L, 1);
        when(taskMapper.selectList(any())).thenReturn(List.of(task));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            List<BizApprovalTask> result = approvalService.myDone();
            assertEquals(1, result.size());
        }
    }

    // ========== myApplications tests ==========

    @Test
    void myApplications_returnsPagedInstancesForCurrentUser() {
        BizApprovalInstance instance1 = new BizApprovalInstance();
        instance1.setId(1L);
        instance1.setInitiatorUserId(1L);
        instance1.setInstanceTitle("Test 1");

        BizApprovalInstance instance2 = new BizApprovalInstance();
        instance2.setId(2L);
        instance2.setInitiatorUserId(1L);
        instance2.setInstanceTitle("Test 2");

        Page<BizApprovalInstance> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(instance1, instance2));
        mockPage.setTotal(2);
        when(instanceMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            PageResult<BizApprovalInstance> result = approvalService.myApplications(null, null, 1, 10);
            assertEquals(2, result.getList().size());
            assertEquals(2, result.getTotal());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getPageSize());
        }
    }

    @Test
    void myApplications_withTitleFilter_appliesLikeCondition() {
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setId(1L);
        instance.setInitiatorUserId(1L);
        instance.setInstanceTitle("Leave Request");

        Page<BizApprovalInstance> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(instance));
        mockPage.setTotal(1);
        when(instanceMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            PageResult<BizApprovalInstance> result = approvalService.myApplications("Leave", null, 1, 10);
            assertEquals(1, result.getList().size());
            assertEquals("Leave Request", result.getList().get(0).getInstanceTitle());
        }
    }

    @Test
    void myApplications_withStatusFilter_appliesEqCondition() {
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setId(1L);
        instance.setInitiatorUserId(1L);
        instance.setInstanceTitle("Approved One");
        instance.setStatus(2);

        Page<BizApprovalInstance> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(instance));
        mockPage.setTotal(1);
        when(instanceMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            PageResult<BizApprovalInstance> result = approvalService.myApplications(null, 2, 1, 10);
            assertEquals(1, result.getList().size());
            assertEquals(2, result.getList().get(0).getStatus());
        }
    }

    @Test
    void myApplications_noFilters_returnsAllForUser() {
        BizApprovalInstance instance1 = new BizApprovalInstance();
        instance1.setId(1L);
        instance1.setInitiatorUserId(1L);
        instance1.setInstanceTitle("First");

        BizApprovalInstance instance2 = new BizApprovalInstance();
        instance2.setId(2L);
        instance2.setInitiatorUserId(1L);
        instance2.setInstanceTitle("Second");

        BizApprovalInstance instance3 = new BizApprovalInstance();
        instance3.setId(3L);
        instance3.setInitiatorUserId(1L);
        instance3.setInstanceTitle("Third");

        Page<BizApprovalInstance> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(instance1, instance2, instance3));
        mockPage.setTotal(3);
        when(instanceMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            PageResult<BizApprovalInstance> result = approvalService.myApplications(null, null, 1, 10);
            assertEquals(3, result.getList().size());
            assertEquals(3, result.getTotal());
        }
    }

    @Test
    void myApplications_emptyResult_returnsEmptyPage() {
        Page<BizApprovalInstance> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0);
        when(instanceMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            PageResult<BizApprovalInstance> result = approvalService.myApplications(null, null, 1, 10);
            assertTrue(result.getList().isEmpty());
            assertEquals(0, result.getTotal());
        }
    }

    // ========== getInstanceDetail tests ==========

    @Test
    void getInstanceDetail_existingId_returnsInstance() {
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setId(1L);
        instance.setInstanceTitle("Detail Test");
        instance.setInitiatorUserId(1L);
        when(instanceMapper.selectById(1L)).thenReturn(instance);

        BizApprovalInstance result = approvalService.getInstanceDetail(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Detail Test", result.getInstanceTitle());
    }

    @Test
    void getInstanceDetail_nonexistentId_throwsInstanceNotFound() {
        when(instanceMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> approvalService.getInstanceDetail(999L));
        assertEquals(ErrorCode.INSTANCE_NOT_FOUND.getCode(), ex.getCode());
    }

    // ========== getInstanceDiagram tests ==========

    @Test
    void getInstanceDiagram_returnsDiagramData() throws Exception {
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setId(1L);
        instance.setFlowableProcessInstanceId("proc-100");
        when(instanceMapper.selectById(1L)).thenReturn(instance);

        // Mock activity query
        HistoricActivityInstanceQuery activityQuery = mock(HistoricActivityInstanceQuery.class);
        when(historyService.createHistoricActivityInstanceQuery()).thenReturn(activityQuery);
        when(activityQuery.processInstanceId("proc-100")).thenReturn(activityQuery);
        when(activityQuery.finished()).thenReturn(activityQuery);

        HistoricActivityInstance activity1 = mock(HistoricActivityInstance.class);
        when(activity1.getActivityId()).thenReturn("node_1");
        HistoricActivityInstance activity2 = mock(HistoricActivityInstance.class);
        when(activity2.getActivityId()).thenReturn("node_2");
        when(activityQuery.list()).thenReturn(List.of(activity1, activity2));

        // Mock task query
        TaskQuery taskQuery = mock(TaskQuery.class);
        when(flowableTaskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("proc-100")).thenReturn(taskQuery);

        Task activeTask = mock(Task.class);
        when(activeTask.getTaskDefinitionKey()).thenReturn("node_3");
        when(taskQuery.list()).thenReturn(List.of(activeTask));

        // Mock historic process instance query
        HistoricProcessInstanceQuery historicQuery = mock(HistoricProcessInstanceQuery.class);
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historicQuery);
        when(historicQuery.processInstanceId("proc-100")).thenReturn(historicQuery);

        HistoricProcessInstance historicInstance = mock(HistoricProcessInstance.class);
        when(historicInstance.getProcessDefinitionId()).thenReturn("pd-1");
        when(historicQuery.singleResult()).thenReturn(historicInstance);

        // Mock process definition and resource
        ProcessDefinition pd = mock(ProcessDefinition.class);
        when(pd.getDeploymentId()).thenReturn("deploy-1");
        when(pd.getResourceName()).thenReturn("process.bpmn20.xml");
        when(repositoryService.getProcessDefinition("pd-1")).thenReturn(pd);

        String bpmnXmlContent = "<definitions>test-bpmn</definitions>";
        InputStream resourceStream = new ByteArrayInputStream(bpmnXmlContent.getBytes(StandardCharsets.UTF_8));
        when(repositoryService.getResourceAsStream("deploy-1", "process.bpmn20.xml")).thenReturn(resourceStream);

        InstanceDiagramVO result = approvalService.getInstanceDiagram(1L);
        assertNotNull(result);
        assertEquals("<definitions>test-bpmn</definitions>", result.getBpmnXml());
        assertEquals(List.of("node_1", "node_2"), result.getCompletedNodeIds());
        assertEquals(List.of("node_3"), result.getCurrentNodeIds());
    }

    @Test
    void getInstanceDiagram_nonexistentInstance_throwsInstanceNotFound() {
        when(instanceMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> approvalService.getInstanceDiagram(999L));
        assertEquals(ErrorCode.INSTANCE_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void getInstanceDiagram_flowableError_returnsEmptyBpmnXml() throws Exception {
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setId(1L);
        instance.setFlowableProcessInstanceId("proc-200");
        when(instanceMapper.selectById(1L)).thenReturn(instance);

        // Mock activity query
        HistoricActivityInstanceQuery activityQuery = mock(HistoricActivityInstanceQuery.class);
        when(historyService.createHistoricActivityInstanceQuery()).thenReturn(activityQuery);
        when(activityQuery.processInstanceId("proc-200")).thenReturn(activityQuery);
        when(activityQuery.finished()).thenReturn(activityQuery);
        when(activityQuery.list()).thenReturn(Collections.emptyList());

        // Mock task query
        TaskQuery taskQuery = mock(TaskQuery.class);
        when(flowableTaskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("proc-200")).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(Collections.emptyList());

        // Mock historic process instance query to throw
        HistoricProcessInstanceQuery historicQuery = mock(HistoricProcessInstanceQuery.class);
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historicQuery);
        when(historicQuery.processInstanceId("proc-200")).thenReturn(historicQuery);
        when(historicQuery.singleResult()).thenThrow(new RuntimeException("Flowable error"));

        InstanceDiagramVO result = approvalService.getInstanceDiagram(1L);
        assertNotNull(result);
        assertEquals("", result.getBpmnXml());
        assertTrue(result.getCompletedNodeIds().isEmpty());
        assertTrue(result.getCurrentNodeIds().isEmpty());
    }

    // ========== dashboardStats tests ==========

    @Test
    void dashboardStats_returnsAggregatedCounts() {
        when(taskMapper.selectCount(any())).thenReturn(5L, 10L);
        when(templateService.count(any())).thenReturn(3L);
        when(ccMapper.selectCount(any())).thenReturn(2L);

        BizApprovalTask recentTask = buildTask(1L, 1L, 1);
        when(taskMapper.selectList(any())).thenReturn(List.of(recentTask));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            DashboardStatsVO result = approvalService.dashboardStats();
            assertNotNull(result);
            assertEquals(5L, result.getTodoCount());
            assertEquals(10L, result.getDoneCount());
            assertEquals(3L, result.getTemplateCount());
            assertEquals(2L, result.getUnreadCcCount());
            assertEquals(1, result.getRecentActivities().size());
        }
    }

    @Test
    void dashboardStats_noData_returnsZeroCounts() {
        when(taskMapper.selectCount(any())).thenReturn(0L, 0L);
        when(templateService.count(any())).thenReturn(0L);
        when(ccMapper.selectCount(any())).thenReturn(0L);
        when(taskMapper.selectList(any())).thenReturn(Collections.emptyList());

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            DashboardStatsVO result = approvalService.dashboardStats();
            assertNotNull(result);
            assertEquals(0L, result.getTodoCount());
            assertEquals(0L, result.getDoneCount());
            assertEquals(0L, result.getTemplateCount());
            assertEquals(0L, result.getUnreadCcCount());
            assertTrue(result.getRecentActivities().isEmpty());
        }
    }

    @Test
    void dashboardStats_recentTasks_hasCorrectData() {
        when(taskMapper.selectCount(any())).thenReturn(1L, 2L);
        when(templateService.count(any())).thenReturn(5L);
        when(ccMapper.selectCount(any())).thenReturn(0L);

        BizApprovalTask task1 = buildTask(10L, 1L, 1);
        BizApprovalTask task2 = buildTask(20L, 1L, 2);
        when(taskMapper.selectList(any())).thenReturn(List.of(task1, task2));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            DashboardStatsVO result = approvalService.dashboardStats();
            List<BizApprovalTask> recent = result.getRecentActivities();
            assertEquals(2, recent.size());
            assertEquals(10L, recent.get(0).getId());
            assertEquals(20L, recent.get(1).getId());
        }
    }
}
