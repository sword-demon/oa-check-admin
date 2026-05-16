package com.oa.admin.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.approval.entity.BizNotification;
import com.oa.admin.common.result.PageResult;

public interface NotificationService extends IService<BizNotification> {

    void send(Long userId, String type, String title, String content, String link);

    void sendBatch(java.util.List<Long> userIds, String type, String title, String content, String link);

    PageResult<BizNotification> myNotifications(String type, Boolean unreadOnly, long page, long pageSize);

    long unreadCount();

    void markRead(Long notificationId);

    void markAllRead();
}
