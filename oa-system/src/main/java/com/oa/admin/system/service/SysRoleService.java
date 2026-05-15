package com.oa.admin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.system.entity.SysRole;
import com.oa.admin.system.entity.SysRolePermission;
import com.oa.admin.system.entity.SysRoleDept;
import com.oa.admin.system.mapper.SysRoleMapper;
import com.oa.admin.system.mapper.SysRolePermissionMapper;
import com.oa.admin.system.mapper.SysRoleDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    public PageResult<SysRole> page(String roleName, Integer status, long page, long pageSize) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(roleName != null && !roleName.isEmpty(), SysRole::getRoleName, roleName)
               .eq(status != null, SysRole::getStatus, status)
               .orderByAsc(SysRole::getSort);
        Page<SysRole> result = this.page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        if (permissionIds != null) {
            permissionIds.forEach(permissionId -> {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            });
        }
    }

    @Transactional
    public void assignDataScope(Long roleId, Integer dataScope, List<Long> deptIds) {
        SysRole role = this.getById(roleId);
        role.setDataScope(dataScope);
        this.updateById(role);

        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getRoleId, roleId));
        if (dataScope == 3 && deptIds != null) {
            deptIds.forEach(deptId -> {
                SysRoleDept rd = new SysRoleDept();
                rd.setRoleId(roleId);
                rd.setDeptId(deptId);
                roleDeptMapper.insert(rd);
            });
        }
    }
}
