package com.oa.admin.approval.service;

import java.util.Map;

import com.oa.admin.approval.dto.form.ApprovalFormSchema;

/**
 * Parses and validates dynamic approval form schemas and submitted values.
 *
 * @author wxvirus
 */
public interface ApprovalFormSchemaService {
    ApprovalFormSchema parse(String formConfig);

    void validateForSave(String formConfig);

    void validateForPublish(String formConfig);

    void validateSubmission(String formConfig, Map<String, Object> formData);
}
