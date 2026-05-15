package com.oa.admin.system.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum PermissionType {
    MENU(1),
    BUTTON(2),
    API(3);

    private final int code;

    public static PermissionType of(int code) {
        for (PermissionType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PermissionType code: " + code);
    }
}
