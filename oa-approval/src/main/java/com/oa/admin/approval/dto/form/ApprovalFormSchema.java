package com.oa.admin.approval.dto.form;

import java.util.ArrayList;
import java.util.List;

import com.oa.admin.approval.constant.ApprovalFormConstants;
import lombok.Data;

/**
 * Dynamic approval form schema stored in biz_process_template.form_config.
 *
 * @author wxvirus
 */
@Data
public class ApprovalFormSchema {
    private Integer version = ApprovalFormConstants.CURRENT_SCHEMA_VERSION;
    private List<ApprovalFormField> fields = new ArrayList<>();
}
