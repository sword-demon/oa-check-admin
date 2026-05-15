package com.oa.admin.approval.service;

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

@ExtendWith(MockitoExtension.class)
class ApprovalTemplateServiceTest {

    @Mock private ProcessDeployService deployService;
    @Mock private BizProcessNodeConfigMapper nodeConfigMapper;
    @Mock private BizProcessTemplateMapper templateMapper;

    private ApprovalTemplateService templateService;

    @BeforeEach
    void setUp() throws Exception {
        templateService = new ApprovalTemplateService(deployService, nodeConfigMapper);
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
    }

    @Test
    void publish_validDraft_deploysAndSetsPublished() {
        BizProcessTemplate template = new BizProcessTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setBpmnXml("<xml>test</xml>");
        template.setVersion(1);

        when(templateMapper.selectById(1L)).thenReturn(template);
        when(deployService.deployTemplate(any())).thenReturn("proc-def-1");
        when(templateMapper.updateById(any(BizProcessTemplate.class))).thenReturn(1);

        BizProcessTemplate result = templateService.publish(1L);

        assertEquals(2, result.getStatus());
        assertEquals("<xml>test</xml>", result.getPublishedBpmnXml());
        assertEquals("proc-def-1", result.getFlowableProcessDefinitionId());
        assertEquals(2, result.getVersion());
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
        assertEquals(3, newDraft.getVersion());
        assertNull(newDraft.getId());
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
}
