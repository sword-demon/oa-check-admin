package com.oa.admin.leave.enums;

/**
 * LeaveType enum
 * @author wxvirus
 */
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

    public int getCode() {
        return code;
    }
    public String getLabel() {
        return label;
    }
}
