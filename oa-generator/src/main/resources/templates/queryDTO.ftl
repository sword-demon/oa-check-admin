package ${ctx.packageName}.dto;

<#assign imports = []>
<#list ctx.entity.searchableFields as f>
    <#if TypeMapper.getImport(f.type)?? && !(imports?seq_contains(TypeMapper.getImport(f.type)))>
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
    private ${TypeMapper.getSimpleJavaType(f.type)} ${f.name};

</#list>
    /** 当前页 */
    private long page = 1;

    /** 每页条数 */
    private long pageSize = 20;
}
