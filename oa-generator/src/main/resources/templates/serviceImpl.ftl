package ${ctx.packageName}.service.impl;

import ${ctx.packageName}.dto.${ctx.entity.createDtoName};
import ${ctx.packageName}.dto.${ctx.entity.queryDtoName};
import ${ctx.packageName}.dto.${ctx.entity.updateDtoName};
import ${ctx.packageName}.mapper.${ctx.entity.mapperName};
import ${ctx.packageName}.entity.${ctx.entity.name};
<#assign enumImports = []>
<#list ctx.entity.fields as f>
    <#if f.enumRef?? && !(enumImports?seq_contains(ctx.packageName + ".enums." + f.enumRef))>
        <#assign enumImports = enumImports + [ctx.packageName + ".enums." + f.enumRef]>
    </#if>
</#list>
<#list enumImports?sort as importName>
import ${importName};
</#list>
import ${ctx.packageName}.service.${ctx.entity.serviceName};
import ${ctx.packageName}.vo.${ctx.entity.voName};
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ${ctx.entity.comment!''} Service Implementation
 * @author ${ctx.config.author!'generator'}
 */
@Service
@RequiredArgsConstructor
public class ${ctx.entity.serviceImplName} extends ServiceImpl<${ctx.entity.mapperName}, ${ctx.entity.name}> implements ${ctx.entity.serviceName} {

    @Override
    public PageResult<${ctx.entity.voName}> page(${ctx.entity.queryDtoName} query) {
        LambdaQueryWrapper<${ctx.entity.name}> wrapper = new LambdaQueryWrapper<>();
<#if ctx.entity.searchableFields?size gt 0>
        wrapper
<#list ctx.entity.searchableFields as f><#if f.isStringLike()>
                .like(query.get${f.capitalizedName}() != null && !query.get${f.capitalizedName}().isEmpty(), ${ctx.entity.name}::get${f.capitalizedName}, query.get${f.capitalizedName}())<#else>
                .eq(query.get${f.capitalizedName}() != null, ${ctx.entity.name}::get${f.capitalizedName}, <#if f.enumRef??>query.get${f.capitalizedName}().getCode()<#else>query.get${f.capitalizedName}()</#if>)</#if>
</#list>
                .orderByDesc(${ctx.entity.name}::getCreatedAt);
<#else>
        wrapper.orderByDesc(${ctx.entity.name}::getCreatedAt);
</#if>
        Page<${ctx.entity.name}> result = this.page(new Page<>(query.getPage(), query.getPageSize()), wrapper);
        return new PageResult<>(result.getRecords().stream().map(this::toVO).toList(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    public ${ctx.entity.voName} getDetail(Long id) {
        return toVO(getById(id));
    }

    @Override
    public ${ctx.entity.voName} create(${ctx.entity.createDtoName} request) {
        ${ctx.entity.name} entity = toEntity(request);
        save(entity);
        return toVO(entity);
    }

    @Override
    public ${ctx.entity.voName} update(Long id, ${ctx.entity.updateDtoName} request) {
        ${ctx.entity.name} entity = toEntity(request);
        entity.setId(id);
        updateById(entity);
        return toVO(getById(id));
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    private ${ctx.entity.name} toEntity(${ctx.entity.createDtoName} request) {
        ${ctx.entity.name} entity = new ${ctx.entity.name}();
<#list ctx.entity.fields as f>
        entity.set${f.capitalizedName}(<#if f.enumRef??>request.get${f.capitalizedName}() == null ? null : request.get${f.capitalizedName}().getCode()<#else>request.get${f.capitalizedName}()</#if>);
</#list>
        return entity;
    }

    private ${ctx.entity.name} toEntity(${ctx.entity.updateDtoName} request) {
        ${ctx.entity.name} entity = new ${ctx.entity.name}();
<#list ctx.entity.fields as f>
        entity.set${f.capitalizedName}(<#if f.enumRef??>request.get${f.capitalizedName}() == null ? null : request.get${f.capitalizedName}().getCode()<#else>request.get${f.capitalizedName}()</#if>);
</#list>
        return entity;
    }

    private ${ctx.entity.voName} toVO(${ctx.entity.name} entity) {
        if (entity == null) {
            return null;
        }
        ${ctx.entity.voName} vo = new ${ctx.entity.voName}();
        vo.setId(entity.getId());
<#list ctx.entity.fields as f>
        vo.set${f.capitalizedName}(<#if f.enumRef??>entity.get${f.capitalizedName}() == null ? null : ${f.enumRef}.fromCode(entity.get${f.capitalizedName}())<#else>entity.get${f.capitalizedName}()</#if>);
</#list>
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
