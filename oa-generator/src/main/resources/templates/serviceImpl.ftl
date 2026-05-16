package ${ctx.packageName}.service.impl;

import ${ctx.packageName}.mapper.${ctx.entity.mapperName};
import ${ctx.packageName}.entity.${ctx.entity.name};
import ${ctx.packageName}.service.${ctx.entity.serviceName};
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
    public PageResult<${ctx.entity.name}> page(<#list ctx.entity.searchableFields as f>${TypeMapper.getSimpleJavaType(f.type)} ${f.name}<#if f_has_next>, </#if></#list><#if ctx.entity.searchableFields?size gt 0>, </#if>long page, long pageSize) {
        LambdaQueryWrapper<${ctx.entity.name}> wrapper = new LambdaQueryWrapper<>();
<#if ctx.entity.searchableFields?size gt 0>
        wrapper
<#list ctx.entity.searchableFields as f><#if f.isStringLike()>
                .like(${f.name} != null && !${f.name}.isEmpty(), ${ctx.entity.name}::get${f.capitalizedName}, ${f.name})<#else>
                .eq(${f.name} != null, ${ctx.entity.name}::get${f.capitalizedName}, ${f.name})</#if>
</#list>
                .orderByDesc(${ctx.entity.name}::getCreatedAt);<#else>
        wrapper.orderByDesc(${ctx.entity.name}::getCreatedAt);
</#if>
        Page<${ctx.entity.name}> result = this.page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }
}
