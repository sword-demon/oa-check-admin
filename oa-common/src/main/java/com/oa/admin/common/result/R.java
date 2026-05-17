package com.oa.admin.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
/**
 * @author wxvirus
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> {
    private int code;
    private String msg;
    private T data;
    private long timestamp;

    private R() {
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public static <T> R<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMsg());
    }
}
