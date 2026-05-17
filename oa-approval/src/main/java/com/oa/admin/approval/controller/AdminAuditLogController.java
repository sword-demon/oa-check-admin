package com.oa.admin.approval.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.oa.admin.approval.entity.BizAuditLog;
import com.oa.admin.approval.service.AuditLogService;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/**
 * @author wxvirus
 */

@RestController
@RequestMapping("/api/v1/admin/audit-log")
@RequiredArgsConstructor
public class AdminAuditLogController {
    private final AuditLogService auditLogService;

    @SaCheckPermission("admin:audit:list")
    @GetMapping
    public R<PageResult<BizAuditLog>> queryLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(auditLogService.queryLogs(module, action, targetType, targetId, userId, startTime, endTime, page, pageSize));
    }
}
