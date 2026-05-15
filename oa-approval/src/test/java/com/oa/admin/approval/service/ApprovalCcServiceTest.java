package com.oa.admin.approval.service;

import cn.dev33.satoken.stp.StpUtil;
import com.oa.admin.approval.entity.BizApprovalCc;
import com.oa.admin.approval.mapper.BizApprovalCcMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalCcServiceTest {

    @Mock
    private BizApprovalCcMapper ccMapper;

    private ApprovalCcService ccService;

    @BeforeEach
    void setUp() throws Exception {
        ccService = new ApprovalCcService();
        injectBaseMapper(ccService, ccMapper);
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
    void myCc_returnsCcForCurrentUser() {
        BizApprovalCc cc = new BizApprovalCc();
        cc.setId(1L);
        cc.setCcUserId(1L);
        when(ccMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cc));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            List<BizApprovalCc> result = ccService.myCc();
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getCcUserId());
        }
    }

    @Test
    void markRead_unreadCc_setsReadAt() {
        BizApprovalCc cc = new BizApprovalCc();
        cc.setId(1L);
        cc.setReadAt(null);
        when(ccMapper.selectById(1L)).thenReturn(cc);
        when(ccMapper.updateById(any(BizApprovalCc.class))).thenReturn(1);

        ccService.markRead(1L);

        assertNotNull(cc.getReadAt());
        verify(ccMapper).updateById(any(BizApprovalCc.class));
    }

    @Test
    void markRead_alreadyReadCc_doesNothing() {
        BizApprovalCc cc = new BizApprovalCc();
        cc.setId(1L);
        cc.setReadAt(LocalDateTime.now());
        when(ccMapper.selectById(1L)).thenReturn(cc);

        ccService.markRead(1L);

        verify(ccMapper, never()).updateById(any(BizApprovalCc.class));
    }

    @Test
    void markRead_nonexistentCc_doesNothing() {
        when(ccMapper.selectById(999L)).thenReturn(null);

        ccService.markRead(999L);

        verify(ccMapper, never()).updateById(any(BizApprovalCc.class));
    }

    @Test
    void createCc_createsEntriesForAllUsers() {
        when(ccMapper.insert(any(BizApprovalCc.class))).thenReturn(1);

        ccService.createCc(100L, List.of(1L, 2L, 3L), "test reason");

        verify(ccMapper, times(3)).insert(any(BizApprovalCc.class));
    }
}
