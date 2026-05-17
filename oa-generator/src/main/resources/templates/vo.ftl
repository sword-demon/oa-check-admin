package ${ctx.packageName}.vo;

<#assign imports = ["java.time.LocalDateTime"]>
<#list ctx.entity.fields as f>
    <#if f.enumRef?? && !(imports?seq_contains(ctx.packageName + ".enums." + f.enumRef))>
        <#assign imports = imports + [ctx.packageName + ".enums." + f.enumRef]>
    <#elseif TypeMapper.getImport(f.type)?? && !(imports?seq_contains(TypeMapper.getImport(f.type)))>
        <#assign imports = imports + [TypeMapper.getImport(f.type)]>
    </#if>
</#list>
<#list imports?sort as importName>
import ${importName};
</#list>
import lombok.Data;

/**
 * ${ctx.entity.comment!''} response
 * @author ${ctx.config.author!'generator'}
 */
@Data
public class ${ctx.entity.voName} {

    private Long id;

<#list ctx.entity.fields as f>
    <#if f.comment??>
    /** ${f.comment} */
    </#if>
    private <#if f.enumRef??>${f.enumRef}<#else>${TypeMapper.getSimpleJavaType(f.type)}</#if> ${f.name};

</#list>
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
