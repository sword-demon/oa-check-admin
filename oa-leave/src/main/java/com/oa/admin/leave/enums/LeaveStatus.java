package com.oa.admin.leave.enums;

/**
 * LeaveStatus enum
 * @author wxvirus
 */
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

    public int getCode() {
        return code;
    }
    public String getLabel() {
        return label;
    }
}
