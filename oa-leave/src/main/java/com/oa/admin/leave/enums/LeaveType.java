package com.oa.admin.leave.enums;

import lombok.Getter;

/**
 * LeaveType enum
 * @author wxvirus
 */
@Getter
public enum LeaveType {
    ANNUAL(1, "年假"),
    SICK(2, "病假"),
    PERSONAL(3, "事假");

    private final int code;
    private final String label;

    LeaveType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static LeaveType fromCode(int code) {
        for (LeaveType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown LeaveType code: " + code);
    }
}
