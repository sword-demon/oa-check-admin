package com.oa.admin.common.exception;

import com.oa.admin.common.result.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructor_withCodeAndMessage_setsFields() {
        BusinessException ex = new BusinessException(1001, "系统异常");
        assertEquals(1001, ex.getCode());
        assertEquals("系统异常", ex.getMessage());
    }

    @Test
    void constructor_withErrorCode_extractsCodeAndMsg() {
        BusinessException ex = new BusinessException(ErrorCode.LOGIN_FAILED);
        assertEquals(2003, ex.getCode());
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void constructor_withErrorCode_notFound() {
        BusinessException ex = new BusinessException(ErrorCode.NOT_FOUND);
        assertEquals(1003, ex.getCode());
        assertEquals("资源不存在", ex.getMessage());
    }

    @Test
    void isRuntimeException() {
        BusinessException ex = new BusinessException(1001, "test");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void canBeThrownAndCaught() {
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        });
    }
}
