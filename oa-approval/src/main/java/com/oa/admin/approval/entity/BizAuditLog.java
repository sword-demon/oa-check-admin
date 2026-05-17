package com.oa.admin.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.admin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wxvirus
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_audit_log")
public class BizAuditLog extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String module;
    private String action;
    private String targetType;
    private Long targetId;
    private String detail;
    private String ip;
}
