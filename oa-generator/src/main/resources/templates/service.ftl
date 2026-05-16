package ${ctx.packageName}.service;

import ${ctx.packageName}.entity.${ctx.entity.name};
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.common.result.PageResult;

/**
 * ${ctx.entity.comment!''} Service
 * @author ${ctx.config.author!'generator'}
 */
public interface ${ctx.entity.serviceName} extends IService<${ctx.entity.name}> {

    /**
     * 分页查询
     */
    PageResult<${ctx.entity.name}> page(<#list ctx.entity.searchableFields as f>${TypeMapper.getSimpleJavaType(f.type)} ${f.name}<#if f_has_next>, </#if></#list><#if ctx.entity.searchableFields?size gt 0>, </#if>long page, long pageSize);
}
