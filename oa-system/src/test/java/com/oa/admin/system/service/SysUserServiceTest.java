package com.oa.admin.system.service;

import com.oa.admin.system.entity.SysUser;
import com.oa.admin.system.entity.SysUserRole;
import com.oa.admin.system.mapper.SysUserMapper;
import com.oa.admin.system.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
class SysUserServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    private SysUserService userService;

    @BeforeEach
    void setUp() throws Exception {
        userService = new SysUserService(userRoleMapper);
        injectBaseMapper(userService, userMapper);
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

    private SysUser buildUser(Long id, String username) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash("plaintext");
        user.setStatus(1);
        return user;
    }

    @Test
    void create_hashesPasswordAndAssignsRoles() {
        SysUser user = buildUser(null, "newuser");
        when(userMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser u = invocation.getArgument(0);
            u.setId(1L);
            return 1;
        });
        when(userRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(userRoleMapper.insert(any(SysUserRole.class))).thenReturn(1);

        userService.create(user, List.of(1L, 2L));

        assertTrue(user.getPasswordHash().startsWith("$2a$"));
        verify(userRoleMapper, times(2)).insert(any(SysUserRole.class));
    }

    @Test
    void create_withNullRoles_deletesOldButNoInsert() {
        SysUser user = buildUser(null, "newuser");
        when(userMapper.insert(any(SysUser.class))).thenReturn(1);
        when(userRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);

        userService.create(user, null);

        verify(userRoleMapper).delete(any(LambdaQueryWrapper.class));
        verify(userRoleMapper, never()).insert(any(SysUserRole.class));
    }

    @Test
    void update_withNewPassword_hashesPassword() {
        SysUser user = buildUser(1L, "admin");
        user.setPasswordHash("newpassword");
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);
        when(userRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(userRoleMapper.insert(any(SysUserRole.class))).thenReturn(1);

        userService.update(user, List.of(1L));

        assertTrue(user.getPasswordHash().startsWith("$2a$"));
    }

    @Test
    void update_withoutPassword_keepsPasswordNull() {
        SysUser user = buildUser(1L, "admin");
        user.setPasswordHash("");
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        userService.update(user, (List<Long>) null);

        assertNull(user.getPasswordHash());
    }

    @Test
    void update_withNullRoleIds_doesNotReassignRoles() {
        SysUser user = buildUser(1L, "admin");
        user.setPasswordHash(null);
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        userService.update(user, (List<Long>) null);

        verify(userRoleMapper, never()).delete(any(LambdaQueryWrapper.class));
    }
}
