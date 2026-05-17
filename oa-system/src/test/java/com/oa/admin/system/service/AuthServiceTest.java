package com.oa.admin.system.service;

import com.oa.admin.system.service.impl.AuthServiceImpl;
import cn.dev33.satoken.stp.StpUtil;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.system.entity.SysUser;
import com.oa.admin.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
/**
 * @author wxvirus
 */

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SysUserMapper userMapper;

    private AuthService authService;

    private void setupService() throws Exception {
        authService = new AuthServiceImpl(userMapper);
    }

    private SysUser buildUser(Long id, String username, String passwordHash, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setStatus(status);
        return user;
    }

    @Test
    void login_withNonexistentUser_throwsLoginFailed() throws Exception {
        setupService();
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login("nobody", "password"));
        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), ex.getCode());
    }

    @Test
    void login_withDisabledUser_throwsLoginFailed() throws Exception {
        setupService();
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> authService.login("admin", "password123"));
    }

    @Test
    void logout_callsStpUtilLogout() throws Exception {
        setupService();
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            authService.logout();
            stpUtil.verify(StpUtil::logout);
        }
    }

    @Test
    void getCurrentUser_returnsUserWithoutPassword() throws Exception {
        setupService();
        SysUser user = buildUser(1L, "admin", "hash", 1);
        when(userMapper.selectById(1L)).thenReturn(user);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            SysUser result = authService.getCurrentUser();
            assertEquals("admin", result.getUsername());
            assertNull(result.getPasswordHash());
        }
    }

    @Test
    void getCurrentUser_whenUserNotFound_throwsUnauthorized() throws Exception {
        setupService();
        when(userMapper.selectById(999L)).thenReturn(null);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(999L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.getCurrentUser());
            assertEquals(ErrorCode.UNAUTHORIZED.getCode(), ex.getCode());
        }
    }
}
