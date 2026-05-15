package com.oa.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.system.enums.DataScope;
import com.oa.admin.system.entity.SysRole;
import com.oa.admin.system.entity.SysRolePermission;
import com.oa.admin.system.entity.SysRoleDept;
import com.oa.admin.system.mapper.SysRoleMapper;
import com.oa.admin.system.mapper.SysRolePermissionMapper;
import com.oa.admin.system.mapper.SysRoleDeptMapper;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    @Override
    public PageResult<SysRole> page(String roleName, Integer status, long page, long pageSize) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(roleName != null && !roleName.isEmpty(), SysRole::getRoleName, roleName)
               .eq(status != null, SysRole::getStatus, status)
               .orderByAsc(SysRole::getSort);
        Page<SysRole> result = this.page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
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

    @Override
    @Transactional
    public void assignDataScope(Long roleId, Integer dataScope, List<Long> deptIds) {
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        role.setDataScope(dataScope);
        this.updateById(role);

        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getRoleId, roleId));
        if (dataScope.equals(DataScope.CUSTOM.getCode()) && deptIds != null) {
            deptIds.forEach(deptId -> {
                SysRoleDept rd = new SysRoleDept();
                rd.setRoleId(roleId);
                rd.setDeptId(deptId);
                roleDeptMapper.insert(rd);
            });
        }
    }
}
