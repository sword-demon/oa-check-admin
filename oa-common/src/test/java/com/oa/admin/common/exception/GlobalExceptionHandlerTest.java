package com.oa.admin.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.common.result.R;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * @author wxvirus
 */

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotLogin_returnsUnauthorizedWithTokenExpired() {
        NotLoginException ex = mock(NotLoginException.class);
        R<Void> result = handler.handleNotLogin(ex);
        assertEquals(ErrorCode.TOKEN_EXPIRED.getCode(), result.getCode());
    }

    @Test
    void handleNotPermission_returnsForbidden() {
        NotPermissionException ex = mock(NotPermissionException.class);
        R<Void> result = handler.handleNotPermission(ex);
        assertEquals(ErrorCode.FORBIDDEN.getCode(), result.getCode());
    }

    @Test
    void handleNotRole_returnsForbidden() {
        NotRoleException ex = mock(NotRoleException.class);
        R<Void> result = handler.handleNotRole(ex);
        assertEquals(ErrorCode.FORBIDDEN.getCode(), result.getCode());
    }

    @Test
    void handleBusiness_returnsOkWithBusinessCode() {
        BusinessException ex = new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        R<Void> result = handler.handleBusiness(ex);
        assertEquals(ErrorCode.TEMPLATE_NOT_FOUND.getCode(), result.getCode());
        assertEquals("审批模板不存在", result.getMsg());
    }

    @Test
    void handleBusiness_withCustomCodeAndMsg() {
        BusinessException ex = new BusinessException(9999, "自定义错误");
        R<Void> result = handler.handleBusiness(ex);
        assertEquals(9999, result.getCode());
        assertEquals("自定义错误", result.getMsg());
    }

    @Test
    void handleException_returnsSystemError() {
        Exception ex = new RuntimeException("something broke");
        R<Void> result = handler.handleException(ex);
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), result.getCode());
    }
}
