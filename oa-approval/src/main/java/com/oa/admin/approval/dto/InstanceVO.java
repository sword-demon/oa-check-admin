package com.oa.admin.approval.dto;

import lombok.Builder;
import lombok.Data;
/**
 * @author wxvirus
 */

@Data
@Builder
public class InstanceVO {
    private Long id;
    private Long processTemplateId;
    private String instanceTitle;
    private Long initiatorUserId;
    private Integer status;
    private String formData;
    private String createdAt;
    private String endAt;
}
