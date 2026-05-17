package com.oa.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.oa.admin.common.result.R;
import com.oa.admin.system.entity.SysPermission;
import com.oa.admin.system.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @author wxvirus
 */

@RestController
@RequestMapping("/api/v1/system/permission")
@RequiredArgsConstructor
public class SysPermissionController {
    private final SysPermissionService permissionService;

    @GetMapping("/tree")
    @SaCheckPermission("system:permission:list")
    public R<List<SysPermission>> tree(
            @RequestParam(required = false) Integer status) {
        return R.ok(permissionService.tree(status));
    }

    @GetMapping
    @SaCheckPermission("system:permission:list")
    public R<List<SysPermission>> list(
            @RequestParam(required = false) Integer status) {
        return R.ok(permissionService.listByStatus(status));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:permission:query")
    public R<SysPermission> getById(@PathVariable Long id) {
        return R.ok(permissionService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("system:permission:add")
    public R<SysPermission> create(@RequestBody SysPermission permission) {
        permissionService.save(permission);
        return R.ok(permission);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:permission:edit")
    public R<SysPermission> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        permission.setId(id);
        permissionService.updateById(permission);
        return R.ok(permission);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:permission:remove")
    public R<Void> delete(@PathVariable Long id) {
        permissionService.removeById(id);
        return R.ok();
    }
}
