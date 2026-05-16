package ${ctx.packageName}.vo;

<#assign imports = ["java.time.LocalDateTime"]>
<#list ctx.entity.fields as f>
    <#if TypeMapper.getImport(f.type)?? && !(imports?seq_contains(TypeMapper.getImport(f.type)))>
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
    private ${TypeMapper.getSimpleJavaType(f.type)} ${f.name};

</#list>
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
