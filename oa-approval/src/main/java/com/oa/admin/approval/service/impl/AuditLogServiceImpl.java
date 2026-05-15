package com.oa.admin.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizAuditLog;
import com.oa.admin.approval.mapper.BizAuditLogMapper;
import com.oa.admin.approval.service.AuditLogService;
import org.springframework.stereotype.Service;

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
}
