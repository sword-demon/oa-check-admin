package com.oa.admin.system.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.admin.common.enums.CommonStatus;
import com.oa.admin.system.entity.SysPermission;
import com.oa.admin.system.entity.SysRole;
import com.oa.admin.system.entity.SysRolePermission;
import com.oa.admin.system.entity.SysUserRole;
import com.oa.admin.system.mapper.SysPermissionMapper;
import com.oa.admin.system.mapper.SysRoleMapper;
import com.oa.admin.system.mapper.SysRolePermissionMapper;
import com.oa.admin.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<Long> roleIds = getRoleIds(loginId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> permIds = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>()
                        .in(SysRolePermission::getRoleId, roleIds)
        ).stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());

        if (permIds.isEmpty()) {
            return Collections.emptyList();
        }
        return permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getId, permIds)
                        .eq(SysPermission::getStatus, CommonStatus.ACTIVE.getCode())
        ).stream().map(SysPermission::getPath).collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<Long> roleIds = getRoleIds(loginId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getStatus, CommonStatus.ACTIVE.getCode())
        ).stream().map(SysRole::getRoleKey).collect(Collectors.toList());
    }

    private List<Long> getRoleIds(Object loginId) {
        long userId = Long.parseLong(loginId.toString());
        return userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }
}
