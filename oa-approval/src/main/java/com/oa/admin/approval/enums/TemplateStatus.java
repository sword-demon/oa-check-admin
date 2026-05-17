package com.oa.admin.approval.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;
/**
 * @author wxvirus
 */

@Getter
@AllArgsConstructor
public enum TemplateStatus {
    DRAFT(1),
    PUBLISHED(2);

    private final int code;

    public static TemplateStatus of(int code) {
        for (TemplateStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TemplateStatus code: " + code);
    }
}
