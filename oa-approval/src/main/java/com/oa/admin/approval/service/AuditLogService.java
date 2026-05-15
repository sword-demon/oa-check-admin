package com.oa.admin.approval.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizAuditLog;
import com.oa.admin.approval.mapper.BizAuditLogMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService extends ServiceImpl<BizAuditLogMapper, BizAuditLog> {

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
}
