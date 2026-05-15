package com.oa.admin.system.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum DataScope {
    ALL(1),
    DEPT(2),
    CUSTOM(3);

    private final int code;

    public static DataScope of(int code) {
        for (DataScope scope : values()) {
            if (scope.code == code) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Unknown DataScope code: " + code);
    }
}
