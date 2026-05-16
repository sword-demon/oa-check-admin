package com.oa.admin.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizNotification;
import com.oa.admin.approval.mapper.BizNotificationMapper;
import com.oa.admin.approval.service.NotificationService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<BizNotificationMapper, BizNotification> implements NotificationService {

    @Override
    public void send(Long userId, String type, String title, String content, String link) {
        BizNotification n = new BizNotification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setLink(link);
        n.setIsRead(0);
        this.save(n);
    }

    @Override
    public void sendBatch(List<Long> userIds, String type, String title, String content, String link) {
        for (Long userId : userIds) {
            send(userId, type, title, content, link);
        }
    }

    @Override
    public PageResult<BizNotification> myNotifications(String type, Boolean unreadOnly, long page, long pageSize) {
        long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<BizNotification> wrapper = new LambdaQueryWrapper<BizNotification>()
            .eq(BizNotification::getUserId, userId);

        if (type != null && !type.isBlank()) {
            wrapper.eq(BizNotification::getType, type);
        }
        if (Boolean.TRUE.equals(unreadOnly)) {
            wrapper.eq(BizNotification::getIsRead, 0);
        }
        wrapper.orderByDesc(BizNotification::getCreatedAt);

        Page<BizNotification> result = this.page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public long unreadCount() {
        long userId = StpUtil.getLoginIdAsLong();
        return this.count(new LambdaQueryWrapper<BizNotification>()
            .eq(BizNotification::getUserId, userId)
            .eq(BizNotification::getIsRead, 0));
    }

    @Override
    public void markRead(Long notificationId) {
        BizNotification n = this.getById(notificationId);
        if (n == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        n.setIsRead(1);
        n.setReadAt(LocalDateTime.now());
        this.updateById(n);
    }

    @Override
    public void markAllRead() {
        long userId = StpUtil.getLoginIdAsLong();
        this.update(new LambdaUpdateWrapper<BizNotification>()
            .eq(BizNotification::getUserId, userId)
            .eq(BizNotification::getIsRead, 0)
            .set(BizNotification::getIsRead, 1)
            .set(BizNotification::getReadAt, LocalDateTime.now()));
    }
}
