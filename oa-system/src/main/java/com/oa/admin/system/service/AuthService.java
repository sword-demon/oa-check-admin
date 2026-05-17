package com.oa.admin.system.service;

import com.oa.admin.system.entity.SysUser;

/**
 * @author wxvirus
 */
public interface AuthService {

    String login(String username, String password);

    void logout();

    SysUser getCurrentUser();
}
