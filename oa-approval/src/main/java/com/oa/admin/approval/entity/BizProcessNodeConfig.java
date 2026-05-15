package com.oa.admin.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.admin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_process_node_config")
public class BizProcessNodeConfig extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private String nodeId;
    private String nodeName;
    /** userTask, exclusiveGateway, parallelGateway, startEvent, endEvent */
    private String nodeType;
    /** fixed, deptLeader, role, initiator, expression */
    private String assigneeType;
    /** JSON config for assignee resolution */
    private String assigneeConfig;
    /** none, countersign, orSign */
    private String multiInstanceType;
    private BigDecimal completionRatio;
    private Integer sortOrder;
}
