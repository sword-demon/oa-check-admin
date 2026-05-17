package com.oa.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.admin.common.result.R;
import com.oa.admin.system.entity.SysDept;
import com.oa.admin.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @author wxvirus
 */

@RestController
@RequestMapping("/api/v1/system/dept")
@RequiredArgsConstructor
public class SysDeptController {
    private final SysDeptService deptService;

    @GetMapping("/tree")
    @SaCheckPermission("system:dept:list")
    public R<List<SysDept>> tree(
            @RequestParam(required = false) Integer status) {
        return R.ok(deptService.tree(status));
    }

    @GetMapping
    @SaCheckPermission("system:dept:list")
    public R<List<SysDept>> list(
            @RequestParam(required = false) Integer status) {
        if (status != null) {
            LambdaQueryWrapper wrapper = new LambdaQueryWrapper<SysDept>()
                    .eq(SysDept::getStatus, status)
                    .orderByAsc(SysDept::getSort);
            return R.ok(deptService.list(wrapper));
        }
        return R.ok(deptService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:dept:query")
    public R<SysDept> getById(@PathVariable Long id) {
        return R.ok(deptService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("system:dept:add")
    public R<SysDept> create(@RequestBody SysDept dept) {
        deptService.save(dept);
        return R.ok(dept);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:dept:edit")
    public R<SysDept> update(@PathVariable Long id, @RequestBody SysDept dept) {
        dept.setId(id);
        deptService.updateById(dept);
        return R.ok(dept);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:dept:remove")
    public R<Void> delete(@PathVariable Long id) {
        deptService.removeById(id);
        return R.ok();
    }
}
