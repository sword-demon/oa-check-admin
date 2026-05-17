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
@TableName("biz_process_template")
public class BizProcessTemplate extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateName;
    private String templateKey;
    private String flowableProcessDefinitionId;
    private String formConfig;
    private String bpmnXml;
    private String publishedBpmnXml;
    private String flowableDeploymentId;
    private Integer version;
    private Integer status;
}
