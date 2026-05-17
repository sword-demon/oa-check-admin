package com.oa.admin.approval.dto.form;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Field definition in approval form schema.
 *
 * @author wxvirus
 */
@Data
public class ApprovalFormField {
    private String fieldKey;
    private String label;
    private String type;
    private String placeholder;
    private Boolean required;
    private Object defaultValue;
    private List<ApprovalFormOption> options = new ArrayList<>();
    private BigDecimal min;
    private BigDecimal max;
    private Integer maxFiles;
    private Integer sortOrder;
}
