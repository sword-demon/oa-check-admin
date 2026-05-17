package com.oa.admin.approval.constant;

/**
 * Constants for dynamic approval form schema parsing and validation.
 *
 * @author wxvirus
 */
public final class ApprovalFormConstants {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    public static final String FIELD_KEY_FIELDS = "fields";
    public static final String FIELD_KEY_VERSION = "version";
    public static final String LEGACY_FIELD_NAME = "name";
    public static final String LEGACY_FIELD_TYPE = "type";
    public static final String LEGACY_FIELD_LABEL = "label";
    public static final String DEFAULT_ATTACHMENT_MAX_FILES = "1";

    public static final String ERROR_FORM_SCHEMA_INVALID = "表单配置不完整: ";
    public static final String ERROR_FORM_DATA_INVALID = "表单数据校验失败: ";
    public static final String ERROR_FORM_FIELD_REQUIRED = "字段不能为空: ";
    public static final String ERROR_FORM_FIELD_DUPLICATED = "字段标识重复: ";
    public static final String ERROR_FORM_OPTION_REQUIRED = "选项字段必须至少配置一个选项: ";
    public static final String ERROR_FORM_FIELD_TYPE_INVALID = "字段类型不支持: ";
    public static final String ERROR_FORM_FIELD_VALUE_INVALID = "字段值格式错误: ";

    private ApprovalFormConstants() {
    }
}
