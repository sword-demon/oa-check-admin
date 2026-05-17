package com.oa.admin.approval.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;
/**
 * @author wxvirus
 */

@Getter
@AllArgsConstructor
public enum ApprovalTaskResult {
    APPROVED(1),
    REJECTED(2),
    TRANSFERRED(3),
    CANCELLED(4);

    private final int code;

    public static ApprovalTaskResult of(int code) {
        for (ApprovalTaskResult result : values()) {
            if (result.code == code) {
                return result;
            }
        }
        throw new IllegalArgumentException("Unknown ApprovalTaskResult code: " + code);
    }
}
