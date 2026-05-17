package com.oa.admin.leave.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * LeaveStatus enum
 * @author wxvirus
 */
@Getter
public enum LeaveStatus {
    DRAFT(0, "草稿"),
    PENDING(1, "审批中"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已驳回"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String label;

    LeaveStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator
    public static LeaveStatus fromCode(int code) {
        for (LeaveStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown LeaveStatus code: " + code);
    }
}
