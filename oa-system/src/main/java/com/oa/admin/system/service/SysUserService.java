package com.oa.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.system.entity.SysUser;

import java.util.List;
/**
 * @author wxvirus
 */

public interface SysUserService extends IService<SysUser> {

    PageResult<SysUser> page(String username, Integer status, long page, long pageSize);

    void create(SysUser user, List<Long> roleIds);

    void update(SysUser user, List<Long> roleIds);
}
