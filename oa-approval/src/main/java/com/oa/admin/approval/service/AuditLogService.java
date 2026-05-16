package com.oa.admin.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.approval.entity.BizAuditLog;
import com.oa.admin.common.result.PageResult;

public interface AuditLogService extends IService<BizAuditLog> {

    void log(String module, String action, String targetType, Long targetId, String detail);

    PageResult<BizAuditLog> queryLogs(String module, String action, String targetType,
            Long targetId, Long userId, String startTime, String endTime, long page, long pageSize);
}
