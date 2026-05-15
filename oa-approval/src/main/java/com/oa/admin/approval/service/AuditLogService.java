package com.oa.admin.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.approval.entity.BizAuditLog;

public interface AuditLogService extends IService<BizAuditLog> {

    void log(String module, String action, String targetType, Long targetId, String detail);
}
