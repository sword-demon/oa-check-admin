package com.oa.admin.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.admin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_approval_instance")
public class BizApprovalInstance extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long processTemplateId;
    private String instanceTitle;
    private String flowableProcessInstanceId;
    private Long initiatorUserId;
    /** 1=pending 2=approved 3=rejected 4=withdrawn 5=cancelled */
    private Integer status;
    private String formData;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
}
