package com.oa.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    PageResult<SysRole> page(String roleName, Integer status, long page, long pageSize);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    void assignDataScope(Long roleId, Integer dataScope, List<Long> deptIds);
}
