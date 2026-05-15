package com.oa.admin.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizApprovalCc;
import com.oa.admin.approval.mapper.BizApprovalCcMapper;
import com.oa.admin.approval.service.ApprovalCcService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalCcServiceImpl extends ServiceImpl<BizApprovalCcMapper, BizApprovalCc> implements ApprovalCcService {

    @Override
    public List<BizApprovalCc> myCc() {
        long userId = StpUtil.getLoginIdAsLong();
        return this.list(new LambdaQueryWrapper<BizApprovalCc>()
            .eq(BizApprovalCc::getCcUserId, userId)
            .orderByDesc(BizApprovalCc::getCreatedAt));
    }

    @Override
    public void markRead(Long ccId) {
        BizApprovalCc cc = this.getById(ccId);
        if (cc == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        long userId = StpUtil.getLoginIdAsLong();
        if (!cc.getCcUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (cc.getReadAt() == null) {
            cc.setReadAt(LocalDateTime.now());
            this.updateById(cc);
        }
    }

    @Override
    public void createCc(Long instanceId, List<Long> ccUserIds, String reason) {
        if (ccUserIds == null || ccUserIds.isEmpty()) {
            return;
        }
        ccUserIds.forEach(userId -> {
            BizApprovalCc cc = new BizApprovalCc();
            cc.setApprovalInstanceId(instanceId);
            cc.setCcUserId(userId);
            cc.setCcReason(reason);
            this.save(cc);
        });
    }
}
