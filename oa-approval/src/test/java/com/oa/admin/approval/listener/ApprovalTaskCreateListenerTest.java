package com.oa.admin.approval.listener;

import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import org.flowable.task.service.delegate.DelegateTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalTaskCreateListenerTest {

    @Mock private BizApprovalInstanceMapper instanceMapper;
    @Mock private BizApprovalTaskMapper taskMapper;
    @Mock private DelegateTask delegateTask;

    private ApprovalTaskCreateListener listener;

    @BeforeEach
    void setUp() {
        listener = new ApprovalTaskCreateListener(instanceMapper, taskMapper);
    }

    private void setupMocks(String processInstanceId, String assignee, Long instanceId) {
        when(delegateTask.getProcessInstanceId()).thenReturn(processInstanceId);
        when(delegateTask.getAssignee()).thenReturn(assignee);
        when(delegateTask.getId()).thenReturn("task-1");
        when(delegateTask.getName()).thenReturn("Approve");

        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setId(instanceId);
        when(instanceMapper.selectOne(any())).thenReturn(instance);
        when(taskMapper.insert(any(BizApprovalTask.class))).thenReturn(1);
    }

    @Test
    void notify_normalTask_noMultiInstanceVariables_setsTaskType1() {
        setupMocks("proc-1", "42", 100L);
        when(delegateTask.getVariableLocal("nrOfInstances")).thenReturn(null);
        when(delegateTask.getVariable("nrOfInstances")).thenReturn(null);

        listener.notify(delegateTask);

        ArgumentCaptor<BizApprovalTask> captor = ArgumentCaptor.forClass(BizApprovalTask.class);
        verify(taskMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getTaskType());
        assertEquals(42L, captor.getValue().getAssigneeUserId());
    }

    @Test
    void notify_multiInstanceDefault_setsTaskType2_countersign() {
        setupMocks("proc-1", "10", 100L);
        when(delegateTask.getVariableLocal("nrOfInstances")).thenReturn(3);
        when(delegateTask.getVariable("completionCondition")).thenReturn(null);

        listener.notify(delegateTask);

        ArgumentCaptor<BizApprovalTask> captor = ArgumentCaptor.forClass(BizApprovalTask.class);
        verify(taskMapper).insert(captor.capture());
        assertEquals(2, captor.getValue().getTaskType());
    }

    @Test
    void notify_multiInstanceOrSign_setsTaskType3() {
        setupMocks("proc-1", "10", 100L);
        when(delegateTask.getVariableLocal("nrOfInstances")).thenReturn(3);
        when(delegateTask.getVariable("completionCondition")).thenReturn("${nrOfCompletedInstances == 1}");

        listener.notify(delegateTask);

        ArgumentCaptor<BizApprovalTask> captor = ArgumentCaptor.forClass(BizApprovalTask.class);
        verify(taskMapper).insert(captor.capture());
        assertEquals(3, captor.getValue().getTaskType());
    }

    @Test
    void notify_multiInstanceVariableOnScope_setsTaskType2() {
        setupMocks("proc-1", "10", 100L);
        when(delegateTask.getVariableLocal("nrOfInstances")).thenReturn(null);
        when(delegateTask.getVariable("nrOfInstances")).thenReturn(5);
        when(delegateTask.getVariable("completionCondition")).thenReturn(null);

        listener.notify(delegateTask);

        ArgumentCaptor<BizApprovalTask> captor = ArgumentCaptor.forClass(BizApprovalTask.class);
        verify(taskMapper).insert(captor.capture());
        assertEquals(2, captor.getValue().getTaskType());
    }

    @Test
    void notify_noBusinessInstance_skipsInsert() {
        when(delegateTask.getProcessInstanceId()).thenReturn("proc-unknown");
        when(instanceMapper.selectOne(any())).thenReturn(null);

        listener.notify(delegateTask);

        verify(taskMapper, never()).insert(any(BizApprovalTask.class));
    }
}
