package com.oa.admin.approval.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;
/**
 * @author wxvirus
 */

@Getter
@AllArgsConstructor
public enum ApprovalInstanceStatus {
    PENDING(1),
    APPROVED(2),
    REJECTED(3),
    WITHDRAWN(4),
    CANCELLED(5);

    private final int code;

    public static ApprovalInstanceStatus of(int code) {
        for (ApprovalInstanceStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ApprovalInstanceStatus code: " + code);
    }
}
