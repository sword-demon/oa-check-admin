package com.oa.admin.approval.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.oa.admin.approval.entity.BizNotification;
import com.oa.admin.approval.service.NotificationService;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/**
 * @author wxvirus
 */

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
@SaCheckLogin
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/my")
    public R<PageResult<BizNotification>> myNotifications(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(notificationService.myNotifications(type, unreadOnly, page, pageSize));
    }

    @GetMapping("/unread-count")
    public R<Long> unreadCount() {
        return R.ok(notificationService.unreadCount());
    }

    @PostMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return R.ok();
    }

    @PostMapping("/read-all")
    public R<Void> markAllRead() {
        notificationService.markAllRead();
        return R.ok();
    }
}
