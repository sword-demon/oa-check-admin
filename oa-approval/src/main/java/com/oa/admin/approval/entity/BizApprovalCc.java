package com.oa.admin.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.admin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
/**
 * @author wxvirus
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_approval_cc")
public class BizApprovalCc extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long approvalInstanceId;
    private Long ccUserId;
    private String ccReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;
}
