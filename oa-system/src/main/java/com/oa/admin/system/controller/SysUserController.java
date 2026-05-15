package com.oa.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.common.result.R;
import com.oa.admin.system.entity.SysUser;
import com.oa.admin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system/user")
@RequiredArgsConstructor
public class SysUserController {
    private final SysUserService userService;

    @GetMapping
    @SaCheckPermission("system:user:list")
    public R<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(userService.page(username, status, page, pageSize));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:user:query")
    public R<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) user.setPasswordHash(null);
        return R.ok(user);
    }

    @PostMapping
    @SaCheckPermission("system:user:add")
    public R<SysUser> create(@RequestBody Map<String, Object> body) {
        SysUser user = new SysUser();
        user.setUsername((String) body.get("username"));
        user.setNickname((String) body.get("nickname"));
        user.setPasswordHash((String) body.get("password"));
        user.setEmail((String) body.get("email"));
        user.setPhone((String) body.get("phone"));
        user.setDeptId(body.get("deptId") != null ? Long.valueOf(body.get("deptId").toString()) : null);
        user.setStatus(body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : 1);
        @SuppressWarnings("unchecked")
        List<Long> roleIds = body.get("roleIds") != null ? (List<Long>) body.get("roleIds") : null;
        userService.create(user, roleIds);
        user.setPasswordHash(null);
        return R.ok(user);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:user:edit")
    public R<SysUser> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername((String) body.get("username"));
        user.setNickname((String) body.get("nickname"));
        user.setPasswordHash((String) body.get("password"));
        user.setEmail((String) body.get("email"));
        user.setPhone((String) body.get("phone"));
        user.setDeptId(body.get("deptId") != null ? Long.valueOf(body.get("deptId").toString()) : null);
        user.setStatus(body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : null);
        @SuppressWarnings("unchecked")
        List<Long> roleIds = body.get("roleIds") != null ? (List<Long>) body.get("roleIds") : null;
        userService.update(user, roleIds);
        user.setPasswordHash(null);
        return R.ok(user);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:remove")
    public R<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return R.ok();
    }
}
