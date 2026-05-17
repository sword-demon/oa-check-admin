package com.oa.admin.approval.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.enums.ApprovalInstanceStatus;
import com.oa.admin.approval.enums.ApprovalTaskResult;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.mapper.BizApprovalTaskMapper;
import com.oa.admin.common.event.ApprovalCompletedEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessEndEventListenerTest {

    @Mock
    private BizApprovalInstanceMapper instanceMapper;

    @Mock
    private BizApprovalTaskMapper taskMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ProcessEndEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new ProcessEndEventListener(instanceMapper, taskMapper, eventPublisher);
    }

    @Test
    void onEvent_processCompletedWithPendingInstance_marksApproved() {
        BizApprovalInstance instance = buildInstance(1L, ApprovalInstanceStatus.PENDING.getCode());
        when(instanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(instance);
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        listener.onEvent(processEvent(FlowableEngineEventType.PROCESS_COMPLETED, "proc-1"));

        assertEquals(ApprovalInstanceStatus.APPROVED.getCode(), instance.getStatus());
        verify(instanceMapper).updateById(instance);
        verify(eventPublisher).publishEvent(any(ApprovalCompletedEvent.class));
    }

    @Test
    void onEvent_processCompletedWithRejectedTask_marksRejected() {
        BizApprovalInstance instance = buildInstance(1L, ApprovalInstanceStatus.PENDING.getCode());
        BizApprovalTask rejectedTask = new BizApprovalTask();
        rejectedTask.setTaskResult(ApprovalTaskResult.REJECTED.getCode());
        when(instanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(instance);
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rejectedTask));

        listener.onEvent(processEvent(FlowableEngineEventType.PROCESS_COMPLETED, "proc-1"));

        assertEquals(ApprovalInstanceStatus.REJECTED.getCode(), instance.getStatus());
        verify(instanceMapper).updateById(instance);
    }

    @Test
    void onEvent_processCancelledForWithdrawnInstance_doesNotOverwriteStatus() {
        BizApprovalInstance instance = buildInstance(1L, ApprovalInstanceStatus.WITHDRAWN.getCode());

        listener.onEvent(flowableEvent(FlowableEngineEventType.PROCESS_CANCELLED));

        assertEquals(ApprovalInstanceStatus.WITHDRAWN.getCode(), instance.getStatus());
        verify(instanceMapper, never()).selectOne(any(LambdaQueryWrapper.class));
        verify(instanceMapper, never()).updateById(any(BizApprovalInstance.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void onEvent_processCompletedForWithdrawnInstance_doesNotOverwriteStatus() {
        BizApprovalInstance instance = buildInstance(1L, ApprovalInstanceStatus.WITHDRAWN.getCode());
        when(instanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(instance);

        listener.onEvent(processEvent(FlowableEngineEventType.PROCESS_COMPLETED, "proc-1"));

        assertEquals(ApprovalInstanceStatus.WITHDRAWN.getCode(), instance.getStatus());
        verify(taskMapper, never()).selectList(any(LambdaQueryWrapper.class));
        verify(instanceMapper, never()).updateById(any(BizApprovalInstance.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void getTypes_onlySubscribesProcessCompleted() {
        assertEquals(List.of(FlowableEngineEventType.PROCESS_COMPLETED), listener.getTypes());
        assertTrue(listener.getTypes().contains(FlowableEngineEventType.PROCESS_COMPLETED));
    }

    private FlowableEngineEntityEvent processEvent(FlowableEngineEventType eventType, String processInstanceId) {
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        ExecutionEntity execution = mock(ExecutionEntity.class);
        when(event.getType()).thenReturn(eventType);
        when(event.getEntity()).thenReturn(execution);
        when(execution.getProcessInstanceId()).thenReturn(processInstanceId);
        return event;
    }

    private FlowableEvent flowableEvent(FlowableEngineEventType eventType) {
        FlowableEvent event = mock(FlowableEvent.class);
        when(event.getType()).thenReturn(eventType);
        return event;
    }

    private BizApprovalInstance buildInstance(Long id, int status) {
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setId(id);
        instance.setFlowableProcessInstanceId("proc-1");
        instance.setFormData("{}");
        instance.setStatus(status);
        return instance;
    }
}
