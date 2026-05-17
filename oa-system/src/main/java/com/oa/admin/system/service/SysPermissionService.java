package com.oa.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.system.entity.SysPermission;

import java.util.List;
/**
 * @author wxvirus
 */

public interface SysPermissionService extends IService<SysPermission> {

    List<SysPermission> listByStatus(Integer status);

    List<SysPermission> tree(Integer status);
}
