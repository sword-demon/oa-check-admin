package ${ctx.packageName}.controller;

import ${ctx.packageName}.dto.${ctx.entity.createDtoName};
import ${ctx.packageName}.dto.${ctx.entity.queryDtoName};
import ${ctx.packageName}.dto.${ctx.entity.updateDtoName};
import ${ctx.packageName}.service.${ctx.entity.serviceName};
import ${ctx.packageName}.vo.${ctx.entity.voName};
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
    public R<PageResult<${ctx.entity.voName}>> list(${ctx.entity.queryDtoName} query) {
        return R.ok(${ctx.entity.beanName}Service.page(query));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("${ctx.config.module}:${ctx.entity.resourcePath}:query")
    public R<${ctx.entity.voName}> getById(@PathVariable Long id) {
        return R.ok(${ctx.entity.beanName}Service.getDetail(id));
    }

    @PostMapping
    @SaCheckPermission("${ctx.config.module}:${ctx.entity.resourcePath}:add")
    public R<${ctx.entity.voName}> create(@RequestBody ${ctx.entity.createDtoName} request) {
        return R.ok(${ctx.entity.beanName}Service.create(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("${ctx.config.module}:${ctx.entity.resourcePath}:edit")
    public R<${ctx.entity.voName}> update(@PathVariable Long id, @RequestBody ${ctx.entity.updateDtoName} request) {
        return R.ok(${ctx.entity.beanName}Service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("${ctx.config.module}:${ctx.entity.resourcePath}:remove")
    public R<Void> delete(@PathVariable Long id) {
        ${ctx.entity.beanName}Service.delete(id);
        return R.ok();
    }
}
