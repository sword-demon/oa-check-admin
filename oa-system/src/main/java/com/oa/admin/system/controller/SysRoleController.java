package com.oa.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.common.result.R;
import com.oa.admin.system.entity.SysRole;
import com.oa.admin.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system/role")
@RequiredArgsConstructor
public class SysRoleController {
    private final SysRoleService roleService;

    @GetMapping
    @SaCheckPermission("system:role:list")
    public R<PageResult<SysRole>> list(
            @RequestParam(defaultValue = "") String roleName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(roleService.page(roleName, status, page, pageSize));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:role:query")
    public R<SysRole> getById(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("system:role:add")
    public R<SysRole> create(@RequestBody SysRole role) {
        roleService.save(role);
        return R.ok(role);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:role:edit")
    public R<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleService.updateById(role);
        return R.ok(role);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:remove")
    public R<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return R.ok();
    }

    @PostMapping("/{id}/permissions")
    @SaCheckPermission("system:role:edit")
    public R<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        roleService.assignPermissions(id, body.get("permissionIds"));
        return R.ok();
    }

    @PostMapping("/{id}/data-scope")
    @SaCheckPermission("system:role:edit")
    public R<Void> assignDataScope(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer dataScope = Integer.parseInt(body.get("dataScope").toString());
        @SuppressWarnings("unchecked")
        List<Long> deptIds = (List<Long>) body.get("deptIds");
        roleService.assignDataScope(id, dataScope, deptIds);
        return R.ok();
    }
}
