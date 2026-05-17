package com.oa.admin.approval.service;

import com.oa.admin.approval.service.impl.ApprovalTemplateServiceImpl;
import com.oa.admin.approval.entity.BizProcessNodeConfig;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizProcessNodeConfigMapper;
import com.oa.admin.approval.mapper.BizProcessTemplateMapper;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
/**
 * @author wxvirus
 */

@ExtendWith(MockitoExtension.class)
class ApprovalTemplateServiceTest {

    @Mock private ProcessDeployService deployService;
    @Mock private BizProcessNodeConfigMapper nodeConfigMapper;
    @Mock private BizProcessTemplateMapper templateMapper;
    @Mock private ApprovalFormSchemaService formSchemaService;

    private ApprovalTemplateService templateService;

    @BeforeEach
    void setUp() throws Exception {
        templateService = new ApprovalTemplateServiceImpl(deployService, nodeConfigMapper, formSchemaService);
        injectBaseMapper(templateService, templateMapper);
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

    @Test
    void saveDraft_newTemplate_savesWithDraftStatus() {
        when(templateMapper.insertOrUpdate(any(BizProcessTemplate.class))).thenReturn(true);

        BizProcessTemplate template = new BizProcessTemplate();
        template.setTemplateName("Test");
        template.setTemplateKey("test_key");

        BizProcessTemplate result = templateService.saveDraft(template, null);

        assertEquals(1, result.getStatus());
        verify(templateMapper).insertOrUpdate(any(BizProcessTemplate.class));
        verify(formSchemaService).validateForSave(template.getFormConfig());
    }

    @Test
    void publish_validDraft_deploysAndSetsPublished() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setBpmnXml("""
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:flowable="http://flowable.org/bpmn">
              <process id="test" isExecutable="true">
                <startEvent id="start"/>
                <sequenceFlow id="flow1" sourceRef="start" targetRef="end"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """);
        template.setVersion(1);
        template.setFormConfig("{\"fields\":[{\"fieldKey\":\"reason\",\"type\":\"text\",\"label\":\"原因\"}]}");

        when(templateMapper.selectById(1L)).thenReturn(template);
        when(deployService.deployTemplate(any())).thenReturn("proc-def-1");
        when(templateMapper.updateById(any(BizProcessTemplate.class))).thenReturn(1);

        BizProcessTemplate result = templateService.publish(1L);

        assertEquals(2, result.getStatus());
        assertEquals(template.getBpmnXml(), result.getPublishedBpmnXml());
        assertEquals("proc-def-1", result.getFlowableProcessDefinitionId());
        assertEquals(1, result.getVersion());
        verify(formSchemaService).validateForPublish(template.getFormConfig());
        verify(deployService).deployTemplate(any());
    }

    @Test
    void publish_alreadyPublished_throwsAlreadyPublished() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(2);

        when(templateMapper.selectById(1L)).thenReturn(template);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> templateService.publish(1L));
        assertEquals(ErrorCode.TEMPLATE_ALREADY_PUBLISHED.getCode(), ex.getCode());
    }

    @Test
    void publish_noBpmnXml_throwsBpmnInvalid() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setBpmnXml(null);

        when(templateMapper.selectById(1L)).thenReturn(template);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> templateService.publish(1L));
        assertEquals(ErrorCode.BPMN_XML_INVALID.getCode(), ex.getCode());
    }

    @Test
    void publish_invalidFlow_throwsAggregatedParamError() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setBpmnXml("""
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:flowable="http://flowable.org/bpmn">
              <process id="test" isExecutable="true">
                <startEvent id="start"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """);
        template.setFormConfig("{\"fields\":[{\"fieldKey\":\"reason\",\"type\":\"text\",\"label\":\"原因\"}]}");
        when(templateMapper.selectById(1L)).thenReturn(template);

        BusinessException ex = assertThrows(BusinessException.class, () -> templateService.publish(1L));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("开始事件没有出口连线"));
        verify(deployService, never()).deployTemplate(any());
    }

    @Test
    void publish_duplicateSequenceFlowId_throwsParamErrorBeforeDeploy() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setBpmnXml("""
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:flowable="http://flowable.org/bpmn">
              <process id="test" isExecutable="true">
                <startEvent id="start"/>
                <userTask id="approval" name="审批" flowable:assignee="${initiator}"/>
                <endEvent id="end"/>
                <sequenceFlow id="flow_approval_end" sourceRef="start" targetRef="approval"/>
                <sequenceFlow id="flow_approval_end" sourceRef="approval" targetRef="end"/>
              </process>
            </definitions>
            """);
        template.setFormConfig("{\"fields\":[{\"fieldKey\":\"reason\",\"type\":\"text\",\"label\":\"原因\"}]}");
        when(templateMapper.selectById(1L)).thenReturn(template);

        BusinessException ex = assertThrows(BusinessException.class, () -> templateService.publish(1L));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("流程定义存在重复元素ID: flow_approval_end"));
        verify(deployService, never()).deployTemplate(any());
    }

    @Test
    void publish_serviceTaskWithoutImplementation_throwsParamErrorBeforeDeploy() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setBpmnXml("""
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:flowable="http://flowable.org/bpmn">
              <process id="test" isExecutable="true">
                <startEvent id="start"/>
                <serviceTask id="cc1" name="抄送节点"/>
                <endEvent id="end"/>
                <sequenceFlow id="flow_start_cc1" sourceRef="start" targetRef="cc1"/>
                <sequenceFlow id="flow_cc1_end" sourceRef="cc1" targetRef="end"/>
              </process>
            </definitions>
            """);
        template.setFormConfig("{\"fields\":[{\"fieldKey\":\"reason\",\"type\":\"text\",\"label\":\"原因\"}]}");
        when(templateMapper.selectById(1L)).thenReturn(template);

        BusinessException ex = assertThrows(BusinessException.class, () -> templateService.publish(1L));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("服务任务缺少执行实现: 抄送节点"));
        verify(deployService, never()).deployTemplate(any());
    }

    @Test
    void publish_serviceTaskWithFlowableExpression_passesValidation() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setBpmnXml("""
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:flowable="http://flowable.org/bpmn">
              <process id="test" isExecutable="true">
                <startEvent id="start"/>
                <serviceTask id="cc1" name="抄送节点" flowable:expression="${true}"/>
                <endEvent id="end"/>
                <sequenceFlow id="flow_start_cc1" sourceRef="start" targetRef="cc1"/>
                <sequenceFlow id="flow_cc1_end" sourceRef="cc1" targetRef="end"/>
              </process>
            </definitions>
            """);
        template.setVersion(1);
        template.setFormConfig("{\"fields\":[{\"fieldKey\":\"reason\",\"type\":\"text\",\"label\":\"原因\"}]}");

        when(templateMapper.selectById(1L)).thenReturn(template);
        when(deployService.deployTemplate(any())).thenReturn("proc-def-1");
        when(templateMapper.updateById(any(BizProcessTemplate.class))).thenReturn(1);

        BizProcessTemplate result = templateService.publish(1L);

        assertEquals(2, result.getStatus());
        verify(deployService).deployTemplate(any());
    }

    @Test
    void publish_deployFailure_throwsBusinessExceptionInsteadOfRawException() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setBpmnXml("""
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:flowable="http://flowable.org/bpmn">
              <process id="test" isExecutable="true">
                <startEvent id="start"/>
                <sequenceFlow id="flow1" sourceRef="start" targetRef="end"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """);
        template.setFormConfig("{\"fields\":[{\"fieldKey\":\"reason\",\"type\":\"text\",\"label\":\"原因\"}]}");

        when(templateMapper.selectById(1L)).thenReturn(template);
        when(deployService.deployTemplate(any())).thenThrow(new RuntimeException("Flowable parse error"));

        BusinessException ex = assertThrows(BusinessException.class, () -> templateService.publish(1L));

        assertEquals(ErrorCode.PROCESS_DEPLOY_FAILED.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("Flowable parse error"));
        verify(templateMapper, never()).updateById(any(BizProcessTemplate.class));
    }


    @Test
    void createNewVersion_clonesAsDraft() {
        BizProcessTemplate published = new BizProcessTemplate();
        published.setId(1L);
        published.setTemplateName("Leave");
        published.setTemplateKey("leave_request");
        published.setFormConfig("{\"fields\":[]}");
        published.setPublishedBpmnXml("<xml>published</xml>");
        published.setVersion(3);
        published.setStatus(2);

        when(templateMapper.selectById(1L)).thenReturn(published);
        when(templateMapper.insert(any(BizProcessTemplate.class))).thenReturn(1);

        BizProcessTemplate newDraft = templateService.createNewVersion(1L);

        assertEquals(1, newDraft.getStatus());
        assertEquals("<xml>published</xml>", newDraft.getBpmnXml());
        assertEquals(4, newDraft.getVersion());
        assertNull(newDraft.getId());
    }

    @Test
    void createNewVersion_clonesNodeConfigsWhenNewIdAvailable() {
        BizProcessTemplate published = new BizProcessTemplate();
        published.setId(1L);
        published.setTemplateName("Leave");
        published.setTemplateKey("leave_request");
        published.setFormConfig("{\"fields\":[]}");
        published.setPublishedBpmnXml("<xml>published</xml>");
        published.setVersion(1);
        published.setStatus(2);

        BizProcessNodeConfig config = new BizProcessNodeConfig();
        config.setNodeId("task1");
        config.setNodeName("审批");
        config.setNodeType("userTask");
        config.setAssigneeType("fixed");
        config.setAssigneeConfig("{\"userIds\":[1]}");
        config.setSortOrder(1);

        when(templateMapper.selectById(1L)).thenReturn(published);
        when(templateMapper.insert(any(BizProcessTemplate.class))).thenAnswer(invocation -> {
            BizProcessTemplate draft = invocation.getArgument(0);
            draft.setId(2L);
            return 1;
        });
        when(nodeConfigMapper.selectList(any())).thenReturn(List.of(config));

        BizProcessTemplate newDraft = templateService.createNewVersion(1L);

        assertEquals(2L, newDraft.getId());
        verify(nodeConfigMapper).insert(argThat((BizProcessNodeConfig cloned) ->
            cloned.getTemplateId().equals(2L)
                && cloned.getNodeId().equals("task1")
                && cloned.getAssigneeType().equals("fixed")
        ));
    }

    @Test
    void getNodeConfigs_returnsOrderedConfigs() {
        BizProcessNodeConfig config = new BizProcessNodeConfig();
        config.setNodeId("task1");

        when(nodeConfigMapper.selectList(any())).thenReturn(List.of(config));

        List<BizProcessNodeConfig> configs = templateService.getNodeConfigs(1L);

        assertEquals(1, configs.size());
        assertEquals("task1", configs.get(0).getNodeId());
    }

    @Test
    void unpublish_existingTemplate_setsStatusToDraft() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(2);
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(templateMapper.updateById(any(BizProcessTemplate.class))).thenReturn(1);

        BizProcessTemplate result = templateService.unpublish(1L);

        assertEquals(1, result.getStatus());
        verify(templateMapper).updateById(any(BizProcessTemplate.class));
    }

    @Test
    void saveNodeConfigs_publishedTemplate_throwsAlreadyPublished() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(2);
        when(templateMapper.selectById(1L)).thenReturn(template);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> templateService.saveNodeConfigs(1L, List.of(new BizProcessNodeConfig())));

        assertEquals(ErrorCode.TEMPLATE_ALREADY_PUBLISHED.getCode(), ex.getCode());
        verify(nodeConfigMapper, never()).delete(any());
    }
}
