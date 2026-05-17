package com.oa.admin.approval.enums;

import java.util.Arrays;
import java.util.Locale;

/**
 * Supported dynamic approval form field types.
 *
 * @author wxvirus
 */
public enum ApprovalFormFieldType {
    TEXT("text"),
    NUMBER("number"),
    DATE("date"),
    DATETIME("datetime"),
    TEXTAREA("textarea"),
    SELECT("select"),
    RADIO("radio"),
    CHECKBOX("checkbox"),
    ATTACHMENT("attachment");

    private final String code;

    ApprovalFormFieldType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean isOptionType() {
        return this == SELECT || this == RADIO || this == CHECKBOX;
    }

    public static ApprovalFormFieldType of(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw new IllegalArgumentException("blank field type");
        }
        String normalized = rawType.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "string", "varchar" -> TEXT;
            case "integer", "int", "long", "bigdecimal", "decimal" -> NUMBER;
            case "localdate" -> DATE;
            case "localdatetime", "datetime-local" -> DATETIME;
            case "text" -> TEXT;
            default -> Arrays.stream(values())
                .filter(type -> type.code.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(rawType));
        };
    }
}
