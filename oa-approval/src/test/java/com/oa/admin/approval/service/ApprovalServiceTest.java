package com.oa.admin.approval.service;

import com.oa.admin.approval.service.impl.ApprovalServiceImpl;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
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

    private ApprovalService approvalService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        approvalService = new ApprovalServiceImpl(templateService, taskMapper, runtimeService, flowableTaskService, objectMapper);
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
        t.setStatus(1);
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
}
