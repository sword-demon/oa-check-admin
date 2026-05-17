package com.oa.admin.approval.dto.form;

import lombok.Data;

/**
 * Option item for select, radio and checkbox fields.
 *
 * @author wxvirus
 */
@Data
public class ApprovalFormOption {
    private String label;
    private Object value;
}
