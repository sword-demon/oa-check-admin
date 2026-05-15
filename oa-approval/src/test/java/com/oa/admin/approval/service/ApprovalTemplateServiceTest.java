package com.oa.admin.approval.service;

import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizProcessTemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalTemplateServiceTest {

    @Mock
    private BizProcessTemplateMapper templateMapper;

    private ApprovalTemplateService templateService;

    @BeforeEach
    void setUp() throws Exception {
        templateService = new ApprovalTemplateService();
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

    private BizProcessTemplate buildTemplate(Long id, String name, Integer status) {
        BizProcessTemplate t = new BizProcessTemplate();
        t.setId(id);
        t.setTemplateName(name);
        t.setStatus(status);
        t.setTemplateKey("test_key");
        return t;
    }

    @Test
    void publish_existingTemplate_setsStatusToOne() {
        BizProcessTemplate template = buildTemplate(1L, "Leave", 0);
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(templateMapper.updateById(any(BizProcessTemplate.class))).thenReturn(1);

        BizProcessTemplate result = templateService.publish(1L);

        assertEquals(1, result.getStatus());
        verify(templateMapper).updateById(any(BizProcessTemplate.class));
    }

    @Test
    void publish_nonexistentTemplate_returnsNull() {
        when(templateMapper.selectById(999L)).thenReturn(null);

        BizProcessTemplate result = templateService.publish(999L);

        assertNull(result);
        verify(templateMapper, never()).updateById(any(BizProcessTemplate.class));
    }

    @Test
    void unpublish_existingTemplate_setsStatusToZero() {
        BizProcessTemplate template = buildTemplate(1L, "Leave", 1);
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(templateMapper.updateById(any(BizProcessTemplate.class))).thenReturn(1);

        BizProcessTemplate result = templateService.unpublish(1L);

        assertEquals(0, result.getStatus());
        verify(templateMapper).updateById(any(BizProcessTemplate.class));
    }

    @Test
    void unpublish_nonexistentTemplate_returnsNull() {
        when(templateMapper.selectById(999L)).thenReturn(null);

        BizProcessTemplate result = templateService.unpublish(999L);

        assertNull(result);
        verify(templateMapper, never()).updateById(any(BizProcessTemplate.class));
    }
}
