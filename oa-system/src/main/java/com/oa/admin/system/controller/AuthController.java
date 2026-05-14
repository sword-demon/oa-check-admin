package com.oa.admin.system.controller;

import com.oa.admin.common.result.R;
import com.oa.admin.system.entity.SysUser;
import com.oa.admin.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String token = authService.login(body.get("username"), body.get("password"));
        SysUser user = authService.getCurrentUser();
        return R.ok(Map.of("token", token, "user", user));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @GetMapping("/me")
    public R<SysUser> me() {
        return R.ok(authService.getCurrentUser());
    }
}
