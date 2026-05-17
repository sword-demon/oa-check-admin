package com.oa.admin.approval.service;

import com.oa.admin.approval.service.impl.AuditLogServiceImpl;
import cn.dev33.satoken.stp.StpUtil;
import com.oa.admin.approval.entity.BizAuditLog;
import com.oa.admin.approval.mapper.BizAuditLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
/**
 * @author wxvirus
 */

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private BizAuditLogMapper auditLogMapper;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() throws Exception {
        auditLogService = new AuditLogServiceImpl();
        injectBaseMapper(auditLogService, auditLogMapper);
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
    void log_createsAuditEntry() {
        when(auditLogMapper.insert(any(BizAuditLog.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            auditLogService.log("approval", "approve", "task", 100L, "Approved by admin");

            verify(auditLogMapper).insert(any(BizAuditLog.class));
        }
    }

    @Test
    void log_withNullDetail_stillPersists() {
        when(auditLogMapper.insert(any(BizAuditLog.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            auditLogService.log("system", "login", "user", 1L, null);

            verify(auditLogMapper).insert(any(BizAuditLog.class));
        }
    }
}
