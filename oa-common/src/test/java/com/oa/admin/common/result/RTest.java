package com.oa.admin.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author wxvirus
 */

class RTest {

    @Test
    void ok_withoutData_returnsSuccessResponse() {
        R<Void> r = R.ok();
        assertEquals(200, r.getCode());
        assertEquals("success", r.getMsg());
        assertNull(r.getData());
        assertTrue(r.getTimestamp() > 0);
    }

    @Test
    void ok_withData_returnsDataInResponse() {
        R<String> r = R.ok("hello");
        assertEquals(200, r.getCode());
        assertEquals("success", r.getMsg());
        assertEquals("hello", r.getData());
    }

    @Test
    void ok_withNullData_returnsSuccessWithNullData() {
        R<Object> r = R.ok(null);
        assertEquals(200, r.getCode());
        assertNull(r.getData());
    }

    @Test
    void ok_withComplexObject_returnsObjectAsData() {
        R<PageResult<String>> r = R.ok(new PageResult<>(java.util.List.of("a", "b"), 2, 1, 10));
        assertNotNull(r.getData());
        assertEquals(2, r.getData().getTotal());
    }

    @Test
    void fail_withCodeAndMsg_returnsFailureResponse() {
        R<Void> r = R.fail(1001, "系统异常");
        assertEquals(1001, r.getCode());
        assertEquals("系统异常", r.getMsg());
        assertNull(r.getData());
    }

    @Test
    void fail_withErrorCode_extractsCodeAndMsg() {
        R<Void> r = R.fail(ErrorCode.LOGIN_FAILED);
        assertEquals(2003, r.getCode());
        assertEquals("用户名或密码错误", r.getMsg());
        assertNull(r.getData());
    }

    @Test
    void fail_withErrorCode_forbidden() {
        R<Void> r = R.fail(ErrorCode.FORBIDDEN);
        assertEquals(2002, r.getCode());
        assertEquals("无权限访问", r.getMsg());
    }

    @Test
    void timestamp_isUnixSeconds() {
        long before = System.currentTimeMillis() / 1000;
        R<Void> r = R.ok();
        long after = System.currentTimeMillis() / 1000;
        assertTrue(r.getTimestamp() >= before && r.getTimestamp() <= after);
    }
}
