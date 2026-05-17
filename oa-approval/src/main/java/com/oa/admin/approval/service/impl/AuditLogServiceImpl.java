package com.oa.admin.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizAuditLog;
import com.oa.admin.approval.mapper.BizAuditLogMapper;
import com.oa.admin.approval.service.AuditLogService;
import com.oa.admin.common.result.PageResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * @author wxvirus
 */

@Service
public class AuditLogServiceImpl extends ServiceImpl<BizAuditLogMapper, BizAuditLog> implements AuditLogService {

    @Override
    public void log(String module, String action, String targetType, Long targetId, String detail) {
        BizAuditLog auditLog = new BizAuditLog();
        auditLog.setUserId(StpUtil.getLoginIdAsLong());
        auditLog.setModule(module);
        auditLog.setAction(action);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setDetail(detail);
        this.save(auditLog);
    }

    @Override
    public PageResult<BizAuditLog> queryLogs(String module, String action, String targetType,
            Long targetId, Long userId, String startTime, String endTime, long page, long pageSize) {
        LambdaQueryWrapper<BizAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null) {
            wrapper.eq(BizAuditLog::getModule, module);
        }
        if (action != null) {
            wrapper.eq(BizAuditLog::getAction, action);
        }
        if (targetType != null) {
            wrapper.eq(BizAuditLog::getTargetType, targetType);
        }
        if (targetId != null) {
            wrapper.eq(BizAuditLog::getTargetId, targetId);
        }
        if (userId != null) {
            wrapper.eq(BizAuditLog::getUserId, userId);
        }
        if (startTime != null && !startTime.isBlank()) {
            wrapper.ge(BizAuditLog::getCreatedAt,
                LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (endTime != null && !endTime.isBlank()) {
            wrapper.le(BizAuditLog::getCreatedAt,
                LocalDateTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        wrapper.orderByDesc(BizAuditLog::getCreatedAt);

        Page<BizAuditLog> result = this.page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }
}
