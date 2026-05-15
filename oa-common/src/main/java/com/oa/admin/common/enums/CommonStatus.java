package com.oa.admin.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum CommonStatus {
    DISABLED(0),
    ACTIVE(1);

    private final int code;

    public static CommonStatus of(int code) {
        for (CommonStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CommonStatus code: " + code);
    }
}
