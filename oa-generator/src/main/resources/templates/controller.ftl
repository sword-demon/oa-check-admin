package ${ctx.packageName}.controller;

import ${ctx.packageName}.entity.${ctx.entity.name};
import ${ctx.packageName}.service.${ctx.entity.serviceName};
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ${ctx.entity.comment!''} Controller
 * @author ${ctx.config.author!'generator'}
 */
@RestController
@RequestMapping("/api/v1/${ctx.config.module}/${ctx.entity.resourcePath}")
@RequiredArgsConstructor
public class ${ctx.entity.controllerName} {

    private final ${ctx.entity.serviceName} ${ctx.entity.beanName}Service;

    @GetMapping
    @SaCheckPermission("${ctx.config.module}:${ctx.entity.resourcePath}:list")
    public R<PageResult<${ctx.entity.name}>> list(
<#list ctx.entity.searchableFields as f>
            @RequestParam(required = false) ${TypeMapper.getSimpleJavaType(f.type)} ${f.name},
</#list>
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(${ctx.entity.beanName}Service.page(<#list ctx.entity.searchableFields as f>${f.name}, </#list>page, pageSize));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("${ctx.config.module}:${ctx.entity.resourcePath}:query")
    public R<${ctx.entity.name}> getById(@PathVariable Long id) {
        return R.ok(${ctx.entity.beanName}Service.getById(id));
    }

    @PostMapping
    @SaCheckPermission("${ctx.config.module}:${ctx.entity.resourcePath}:add")
    public R<${ctx.entity.name}> create(@RequestBody ${ctx.entity.name} ${ctx.entity.beanName}) {
        ${ctx.entity.beanName}Service.save(${ctx.entity.beanName});
        return R.ok(${ctx.entity.beanName});
    }

    @PutMapping("/{id}")
    @SaCheckPermission("${ctx.config.module}:${ctx.entity.resourcePath}:edit")
    public R<${ctx.entity.name}> update(@PathVariable Long id, @RequestBody ${ctx.entity.name} ${ctx.entity.beanName}) {
        ${ctx.entity.beanName}.setId(id);
        ${ctx.entity.beanName}Service.updateById(${ctx.entity.beanName});
        return R.ok(${ctx.entity.beanName});
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("${ctx.config.module}:${ctx.entity.resourcePath}:remove")
    public R<Void> delete(@PathVariable Long id) {
        ${ctx.entity.beanName}Service.removeById(id);
        return R.ok();
    }
}
