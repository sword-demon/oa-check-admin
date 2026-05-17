package com.oa.admin.generator.parser;

import java.util.Map;
import java.util.Set;
/**
 * @author wxvirus
 */

public final class TypeMapper {

    private static final Map<String, String> JAVA_TYPE_MAP = Map.of(
            "String", "String",
            "Integer", "Integer",
            "Long", "Long",
            "BigDecimal", "java.math.BigDecimal",
            "Boolean", "Boolean",
            "LocalDate", "java.time.LocalDate",
            "LocalDateTime", "java.time.LocalDateTime",
            "Text", "String"
    );

    private static final Set<String> VALID_TYPES = JAVA_TYPE_MAP.keySet();

    private TypeMapper() {
    }

    public static boolean isValidType(String type) {
        return VALID_TYPES.contains(type);
    }

    public static Set<String> getValidTypes() {
        return VALID_TYPES;
    }

    public static String getJavaType(String yamlType) {
        String fullType = JAVA_TYPE_MAP.get(yamlType);
        if (fullType == null) {
            throw new IllegalArgumentException("Unknown type: " + yamlType);
        }
        return fullType;
    }

    public static String getSimpleJavaType(String yamlType) {
        String fullType = getJavaType(yamlType);
        int dot = fullType.lastIndexOf('.');
        return dot >= 0 ? fullType.substring(dot + 1) : fullType;
    }

    public static boolean needsImport(String yamlType) {
        String fullType = JAVA_TYPE_MAP.get(yamlType);
        return fullType != null && fullType.contains(".");
    }

    public static String getImport(String yamlType) {
        String fullType = JAVA_TYPE_MAP.get(yamlType);
        if (fullType != null && fullType.contains(".")) {
            return fullType;
        }
        return null;
    }

    public static boolean isStringLike(String yamlType) {
        return "String".equals(yamlType) || "Text".equals(yamlType);
    }
}
