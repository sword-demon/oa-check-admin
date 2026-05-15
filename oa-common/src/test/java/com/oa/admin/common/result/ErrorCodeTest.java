package com.oa.admin.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeTest {

    @Test
    void allErrorCodes_haveNonNullCodeAndMsg() {
        for (ErrorCode ec : ErrorCode.values()) {
            assertNotNull(ec.getMsg(), ec.name() + " should have a non-null msg");
            assertTrue(ec.getCode() > 0, ec.name() + " should have a positive code");
        }
    }

    @Test
    void systemErrorCodes_areIn10xxRange() {
        assertTrue(ErrorCode.SYSTEM_ERROR.getCode() >= 1000 && ErrorCode.SYSTEM_ERROR.getCode() < 1100);
        assertTrue(ErrorCode.PARAM_ERROR.getCode() >= 1000 && ErrorCode.PARAM_ERROR.getCode() < 1100);
        assertTrue(ErrorCode.NOT_FOUND.getCode() >= 1000 && ErrorCode.NOT_FOUND.getCode() < 1100);
    }

    @Test
    void authErrorCodes_areIn20xxRange() {
        assertTrue(ErrorCode.UNAUTHORIZED.getCode() >= 2000 && ErrorCode.UNAUTHORIZED.getCode() < 2100);
        assertTrue(ErrorCode.FORBIDDEN.getCode() >= 2000 && ErrorCode.FORBIDDEN.getCode() < 2100);
        assertTrue(ErrorCode.LOGIN_FAILED.getCode() >= 2000 && ErrorCode.LOGIN_FAILED.getCode() < 2100);
        assertTrue(ErrorCode.TOKEN_EXPIRED.getCode() >= 2000 && ErrorCode.TOKEN_EXPIRED.getCode() < 2100);
    }

    @Test
    void approvalErrorCodes_areIn30xxRange() {
        assertTrue(ErrorCode.TEMPLATE_NOT_FOUND.getCode() >= 3000 && ErrorCode.TEMPLATE_NOT_FOUND.getCode() < 3100);
        assertTrue(ErrorCode.INSTANCE_NOT_FOUND.getCode() >= 3000 && ErrorCode.INSTANCE_NOT_FOUND.getCode() < 3100);
        assertTrue(ErrorCode.TASK_NOT_FOUND.getCode() >= 3000 && ErrorCode.TASK_NOT_FOUND.getCode() < 3100);
        assertTrue(ErrorCode.ALREADY_APPROVED.getCode() >= 3000 && ErrorCode.ALREADY_APPROVED.getCode() < 3100);
        assertTrue(ErrorCode.CANNOT_WITHDRAW.getCode() >= 3000 && ErrorCode.CANNOT_WITHDRAW.getCode() < 3100);
    }

    @Test
    void codesAreUnique() {
        long distinctCount = java.util.Arrays.stream(ErrorCode.values())
                .map(ErrorCode::getCode)
                .distinct()
                .count();
        assertEquals(ErrorCode.values().length, distinctCount, "All error codes should be unique");
    }
}
