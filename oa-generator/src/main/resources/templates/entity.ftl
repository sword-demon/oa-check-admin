package ${ctx.packageName}.entity;

<#assign hasLocalDateTime = false>
<#assign hasLocalDate = false>
<#assign hasBigDecimal = false>
<#assign hasJsonFormat = false>
<#list ctx.entity.fields as f>
    <#if f.type == "LocalDateTime">
        <#assign hasLocalDateTime = true>
    </#if>
    <#if f.type == "LocalDate">
        <#assign hasLocalDate = true>
    </#if>
    <#if f.type == "BigDecimal">
        <#assign hasBigDecimal = true>
    </#if>
    <#if f.jsonFormat>
        <#assign hasJsonFormat = true>
    </#if>
</#list>
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
<#if hasJsonFormat>
import com.fasterxml.jackson.annotation.JsonFormat;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>
<#if hasLocalDate>
import java.time.LocalDate;
</#if>
<#if hasLocalDateTime>
import java.time.LocalDateTime;
</#if>
import com.oa.admin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ${ctx.entity.comment!''}
 * @author ${ctx.config.author!'generator'}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("${ctx.entity.tableName}")
public class ${ctx.entity.name} extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

<#list ctx.entity.fields as f>
    <#if f.comment??>
    /** ${f.comment} */
    </#if>
    <#if f.jsonFormat>
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    </#if>
    private ${TypeMapper.getSimpleJavaType(f.type)} ${f.name};

</#list>
}
