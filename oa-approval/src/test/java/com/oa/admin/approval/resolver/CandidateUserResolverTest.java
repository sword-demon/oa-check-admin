package com.oa.admin.approval.resolver;

import com.oa.admin.system.entity.SysUserRole;
import com.oa.admin.system.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateUserResolverTest {

    @Mock private SysUserRoleMapper userRoleMapper;
    @InjectMocks private CandidateUserResolver candidateUserResolver;

    @Test
    void resolveRoleUsers_returnsUserIds() {
        SysUserRole ur1 = new SysUserRole();
        ur1.setUserId(1L);
        ur1.setRoleId(2L);
        SysUserRole ur2 = new SysUserRole();
        ur2.setUserId(3L);
        ur2.setRoleId(2L);

        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ur1, ur2));

        List<Long> userIds = candidateUserResolver.resolveRoleUsers(2L);

        assertEquals(List.of(1L, 3L), userIds);
    }

    @Test
    void resolveRoleUsers_noUsers_returnsEmptyList() {
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<Long> userIds = candidateUserResolver.resolveRoleUsers(999L);

        assertTrue(userIds.isEmpty());
    }
}
