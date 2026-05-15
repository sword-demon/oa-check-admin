package com.oa.admin.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.admin.common.enums.CommonStatus;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.system.entity.SysUser;
import com.oa.admin.system.mapper.SysUserMapper;
import com.oa.admin.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper userMapper;

    @Override
    public String login(String username, String password) {
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getStatus, CommonStatus.ACTIVE.getCode())
        );
        if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public SysUser getCurrentUser() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        user.setPasswordHash(null);
        return user;
    }
}
