package ${ctx.packageName}.dto;

<#assign imports = []>
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
 * ${ctx.entity.comment!''} update request
 * @author ${ctx.config.author!'generator'}
 */
@Data
public class ${ctx.entity.updateDtoName} {

<#list ctx.entity.fields as f>
    <#if f.comment??>
    /** ${f.comment} */
    </#if>
    private <#if f.enumRef??>${f.enumRef}<#else>${TypeMapper.getSimpleJavaType(f.type)}</#if> ${f.name};

</#list>
}
