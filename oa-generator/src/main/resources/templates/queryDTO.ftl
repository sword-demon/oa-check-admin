package ${ctx.packageName}.dto;

<#assign imports = []>
<#list ctx.entity.searchableFields as f>
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
 * ${ctx.entity.comment!''} query request
 * @author ${ctx.config.author!'generator'}
 */
@Data
public class ${ctx.entity.queryDtoName} {

<#list ctx.entity.searchableFields as f>
    <#if f.comment??>
    /** ${f.comment} */
    </#if>
    private <#if f.enumRef??>${f.enumRef}<#else>${TypeMapper.getSimpleJavaType(f.type)}</#if> ${f.name};

</#list>
    /** 当前页 */
    private long page = 1;

    /** 每页条数 */
    private long pageSize = 20;
}
