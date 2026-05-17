package com.oa.admin.approval.service.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.admin.approval.constant.ApprovalFormConstants;
import com.oa.admin.approval.dto.form.ApprovalFormField;
import com.oa.admin.approval.dto.form.ApprovalFormOption;
import com.oa.admin.approval.dto.form.ApprovalFormSchema;
import com.oa.admin.approval.enums.ApprovalFormFieldType;
import com.oa.admin.approval.service.ApprovalFormSchemaService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Default implementation for dynamic approval form schema handling.
 *
 * @author wxvirus
 */
@Service
@RequiredArgsConstructor
public class ApprovalFormSchemaServiceImpl implements ApprovalFormSchemaService {
    private final ObjectMapper objectMapper;

    @Override
    public ApprovalFormSchema parse(String formConfig) {
        if (formConfig == null || formConfig.isBlank()) {
            return new ApprovalFormSchema();
        }
        try {
            Map<String, Object> root = objectMapper.readValue(formConfig, new TypeReference<>() {});
            ApprovalFormSchema schema = new ApprovalFormSchema();
            Object version = root.get(ApprovalFormConstants.FIELD_KEY_VERSION);
            if (version instanceof Number number) {
                schema.setVersion(number.intValue());
            }
            normalizeLegacyFields(root, schema);
            normalizeFields(schema);
            return schema;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw formSchemaError(ApprovalFormConstants.ERROR_FORM_FIELD_VALUE_INVALID + e.getMessage());
        }
    }

    @Override
    public void validateForSave(String formConfig) {
        ApprovalFormSchema schema = parse(formConfig);
        validateFields(schema, false);
    }

    @Override
    public void validateForPublish(String formConfig) {
        ApprovalFormSchema schema = parse(formConfig);
        validateFields(schema, true);
    }

    @Override
    public void validateSubmission(String formConfig, Map<String, Object> formData) {
        ApprovalFormSchema schema = parse(formConfig);
        if (schema.getFields() == null || schema.getFields().isEmpty()) {
            return;
        }
        Map<String, Object> data = formData == null ? Map.of() : formData;
        validateFields(schema, true);

        for (ApprovalFormField field : schema.getFields()) {
            Object value = data.get(field.getFieldKey());
            if (Boolean.TRUE.equals(field.getRequired()) && isEmptyValue(value)) {
                throw formDataError(ApprovalFormConstants.ERROR_FORM_FIELD_REQUIRED + fieldLabel(field));
            }
            if (isEmptyValue(value)) {
                continue;
            }
            validateValue(field, value);
        }
    }

    @SuppressWarnings("unchecked")
    private void normalizeLegacyFields(Map<String, Object> root, ApprovalFormSchema schema) {
        Object fields = root.get(ApprovalFormConstants.FIELD_KEY_FIELDS);
        if (!(fields instanceof List<?> rawFields)) {
            return;
        }
        List<ApprovalFormField> normalized = rawFields.stream()
            .filter(Map.class::isInstance)
            .map(raw -> normalizeLegacyField((Map<String, Object>) raw))
            .toList();
        schema.setFields(normalized);
    }

    private ApprovalFormField normalizeLegacyField(Map<String, Object> raw) {
        ApprovalFormField field = new ApprovalFormField();
        if (isBlank(field.getFieldKey())) {
            Object legacyName = raw.get(ApprovalFormConstants.LEGACY_FIELD_NAME);
            field.setFieldKey(legacyName == null ? null : legacyName.toString());
        }
        Object fieldKey = raw.get("fieldKey");
        if (fieldKey != null) {
            field.setFieldKey(fieldKey.toString());
        }

        Object legacyType = raw.get(ApprovalFormConstants.LEGACY_FIELD_TYPE);
        field.setType(legacyType == null ? null : legacyType.toString());

        Object legacyLabel = raw.get(ApprovalFormConstants.LEGACY_FIELD_LABEL);
        field.setLabel(legacyLabel == null ? field.getFieldKey() : legacyLabel.toString());

        Object placeholder = raw.get("placeholder");
        field.setPlaceholder(placeholder == null ? null : placeholder.toString());
        Object required = raw.get("required");
        if (required instanceof Boolean bool) {
            field.setRequired(bool);
        }
        field.setDefaultValue(raw.get("defaultValue"));
        field.setOptions(normalizeOptions(raw.get("options")));
        field.setMin(toBigDecimal(raw.get("min")));
        field.setMax(toBigDecimal(raw.get("max")));
        Object maxFiles = raw.get("maxFiles");
        if (maxFiles instanceof Number number) {
            field.setMaxFiles(number.intValue());
        }
        Object sortOrder = raw.get("sortOrder");
        if (sortOrder instanceof Number number) {
            field.setSortOrder(number.intValue());
        }
        return field;
    }

    private List<ApprovalFormOption> normalizeOptions(Object rawOptions) {
        if (!(rawOptions instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
            .map(this::normalizeOption)
            .filter(Objects::nonNull)
            .toList();
    }

    @SuppressWarnings("unchecked")
    private ApprovalFormOption normalizeOption(Object rawOption) {
        if (rawOption instanceof Map<?, ?> map) {
            Map<String, Object> optionMap = (Map<String, Object>) map;
            ApprovalFormOption option = new ApprovalFormOption();
            Object label = optionMap.get("label");
            Object value = optionMap.get("value");
            option.setLabel(label == null ? String.valueOf(value) : label.toString());
            option.setValue(value);
            return option;
        }
        if (rawOption == null) {
            return null;
        }
        ApprovalFormOption option = new ApprovalFormOption();
        option.setLabel(rawOption.toString());
        option.setValue(rawOption);
        return option;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw formSchemaError(ApprovalFormConstants.ERROR_FORM_FIELD_VALUE_INVALID + value);
        }
    }

    private void normalizeFields(ApprovalFormSchema schema) {
        if (schema.getFields() == null) {
            schema.setFields(List.of());
            return;
        }
        for (ApprovalFormField field : schema.getFields()) {
            if (!isBlank(field.getType())) {
                field.setType(ApprovalFormFieldType.of(field.getType()).getCode());
            }
            if (field.getOptions() == null) {
                field.setOptions(List.of());
            }
        }
    }

    private void validateFields(ApprovalFormSchema schema, boolean requireFields) {
        List<ApprovalFormField> fields = schema.getFields();
        if (fields == null || fields.isEmpty()) {
            if (requireFields) {
                throw formSchemaError("至少需要一个表单字段");
            }
            return;
        }

        Set<String> keys = new HashSet<>();
        for (ApprovalFormField field : fields) {
            if (isBlank(field.getFieldKey())) {
                throw formSchemaError(ApprovalFormConstants.ERROR_FORM_FIELD_REQUIRED + "字段标识");
            }
            if (!keys.add(field.getFieldKey())) {
                throw formSchemaError(ApprovalFormConstants.ERROR_FORM_FIELD_DUPLICATED + field.getFieldKey());
            }
            if (isBlank(field.getLabel())) {
                throw formSchemaError(ApprovalFormConstants.ERROR_FORM_FIELD_REQUIRED + field.getFieldKey() + ".label");
            }

            ApprovalFormFieldType type = parseFieldType(field.getType());
            if (type.isOptionType() && (field.getOptions() == null || field.getOptions().isEmpty())) {
                throw formSchemaError(ApprovalFormConstants.ERROR_FORM_OPTION_REQUIRED + fieldLabel(field));
            }
            if (type.isOptionType()) {
                validateOptions(field);
            }
            if (type == ApprovalFormFieldType.NUMBER
                && field.getMin() != null
                && field.getMax() != null
                && field.getMin().compareTo(field.getMax()) > 0) {
                throw formSchemaError("数字字段最小值不能大于最大值: " + fieldLabel(field));
            }
            if (type == ApprovalFormFieldType.ATTACHMENT
                && field.getMaxFiles() != null
                && field.getMaxFiles() <= 0) {
                throw formSchemaError("附件最大数量必须大于 0: " + fieldLabel(field));
            }
        }
    }

    private void validateOptions(ApprovalFormField field) {
        Set<String> optionValues = new HashSet<>();
        for (ApprovalFormOption option : field.getOptions()) {
            if (option == null || isBlank(option.getLabel()) || option.getValue() == null) {
                throw formSchemaError("选项值和标签不能为空: " + fieldLabel(field));
            }
            String normalizedValue = String.valueOf(option.getValue());
            if (!optionValues.add(normalizedValue)) {
                throw formSchemaError("选项值重复: " + fieldLabel(field));
            }
        }
    }

    private void validateValue(ApprovalFormField field, Object value) {
        ApprovalFormFieldType type = parseFieldType(field.getType());
        switch (type) {
            case NUMBER -> validateNumberValue(field, value);
            case SELECT, RADIO -> validateSingleOptionValue(field, value);
            case CHECKBOX -> validateMultipleOptionValue(field, value);
            case ATTACHMENT -> validateAttachmentValue(field, value);
            default -> {
                if (!(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean)) {
                    throw formDataError(ApprovalFormConstants.ERROR_FORM_FIELD_VALUE_INVALID + fieldLabel(field));
                }
            }
        }
    }

    private void validateNumberValue(ApprovalFormField field, Object value) {
        BigDecimal number;
        try {
            number = new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw formDataError(ApprovalFormConstants.ERROR_FORM_FIELD_VALUE_INVALID + fieldLabel(field));
        }
        if (field.getMin() != null && number.compareTo(field.getMin()) < 0) {
            throw formDataError(fieldLabel(field) + "不能小于" + field.getMin());
        }
        if (field.getMax() != null && number.compareTo(field.getMax()) > 0) {
            throw formDataError(fieldLabel(field) + "不能大于" + field.getMax());
        }
    }

    private void validateSingleOptionValue(ApprovalFormField field, Object value) {
        if (!optionValues(field).contains(String.valueOf(value))) {
            throw formDataError(ApprovalFormConstants.ERROR_FORM_FIELD_VALUE_INVALID + fieldLabel(field));
        }
    }

    private void validateMultipleOptionValue(ApprovalFormField field, Object value) {
        if (!(value instanceof Collection<?> values)) {
            throw formDataError(ApprovalFormConstants.ERROR_FORM_FIELD_VALUE_INVALID + fieldLabel(field));
        }
        Set<String> allowed = optionValues(field);
        for (Object item : values) {
            if (!allowed.contains(String.valueOf(item))) {
                throw formDataError(ApprovalFormConstants.ERROR_FORM_FIELD_VALUE_INVALID + fieldLabel(field));
            }
        }
    }

    private void validateAttachmentValue(ApprovalFormField field, Object value) {
        if (!(value instanceof Collection<?> values)) {
            throw formDataError(ApprovalFormConstants.ERROR_FORM_FIELD_VALUE_INVALID + fieldLabel(field));
        }
        int maxFiles = field.getMaxFiles() == null
            ? Integer.parseInt(ApprovalFormConstants.DEFAULT_ATTACHMENT_MAX_FILES)
            : field.getMaxFiles();
        if (values.size() > maxFiles) {
            throw formDataError(fieldLabel(field) + "附件数量不能超过" + maxFiles);
        }
        if (values.stream().anyMatch(item -> !(item instanceof String) || isBlank((String) item))) {
            throw formDataError(ApprovalFormConstants.ERROR_FORM_FIELD_VALUE_INVALID + fieldLabel(field));
        }
    }

    private Set<String> optionValues(ApprovalFormField field) {
        Set<String> values = new HashSet<>();
        if (field.getOptions() != null) {
            field.getOptions().stream()
                .map(ApprovalFormOption::getValue)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .forEach(values::add);
        }
        return values;
    }

    private ApprovalFormFieldType parseFieldType(String rawType) {
        try {
            return ApprovalFormFieldType.of(rawType);
        } catch (IllegalArgumentException e) {
            throw formSchemaError(ApprovalFormConstants.ERROR_FORM_FIELD_TYPE_INVALID + rawType);
        }
    }

    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String text) {
            return text.isBlank();
        }
        if (value instanceof Collection<?> collection) {
            return collection.isEmpty();
        }
        return false;
    }

    private String fieldLabel(ApprovalFormField field) {
        return isBlank(field.getLabel()) ? field.getFieldKey() : field.getLabel();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BusinessException formSchemaError(String detail) {
        return new BusinessException(
            ErrorCode.PARAM_ERROR.getCode(),
            ApprovalFormConstants.ERROR_FORM_SCHEMA_INVALID + detail
        );
    }

    private BusinessException formDataError(String detail) {
        return new BusinessException(
            ErrorCode.PARAM_ERROR.getCode(),
            ApprovalFormConstants.ERROR_FORM_DATA_INVALID + detail
        );
    }
}
