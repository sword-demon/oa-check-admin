package com.oa.admin.system.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.system.entity.SysUser;
import com.oa.admin.system.entity.SysUserRole;
import com.oa.admin.system.mapper.SysUserMapper;
import com.oa.admin.system.mapper.SysUserRoleMapper;
import com.oa.admin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public PageResult<SysUser> page(String username, Integer status, long page, long pageSize) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null && !username.isEmpty(), SysUser::getUsername, username)
               .eq(status != null, SysUser::getStatus, status)
               .orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> result = this.page(new Page<>(page, pageSize), wrapper);
        result.getRecords().forEach(u -> u.setPasswordHash(null));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    @Transactional
    public void create(SysUser user, List<Long> roleIds) {
        user.setPasswordHash(BCrypt.hashpw(user.getPasswordHash()));
        this.save(user);
        assignRoles(user.getId(), roleIds);
    }

    @Override
    @Transactional
    public void update(SysUser user, List<Long> roleIds) {
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            user.setPasswordHash(BCrypt.hashpw(user.getPasswordHash()));
        } else {
            user.setPasswordHash(null);
        }
        this.updateById(user);
        if (roleIds != null) {
            assignRoles(user.getId(), roleIds);
        }
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (roleIds != null) {
            roleIds.forEach(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            });
        }
    }
}
