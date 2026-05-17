package com.oa.admin.approval.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;
/**
 * @author wxvirus
 */

@Getter
@AllArgsConstructor
public enum ApprovalTaskType {
    NORMAL(1),
    COUNTERSIGN(2),
    OR_SIGN(3);

    private final int code;

    public static ApprovalTaskType of(int code) {
        for (ApprovalTaskType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ApprovalTaskType code: " + code);
    }
}
