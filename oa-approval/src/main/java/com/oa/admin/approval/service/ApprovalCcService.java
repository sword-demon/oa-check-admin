package com.oa.admin.approval.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizApprovalCc;
import com.oa.admin.approval.mapper.BizApprovalCcMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalCcService extends ServiceImpl<BizApprovalCcMapper, BizApprovalCc> {

    public List<BizApprovalCc> myCc() {
        long userId = StpUtil.getLoginIdAsLong();
        return this.list(new LambdaQueryWrapper<BizApprovalCc>()
            .eq(BizApprovalCc::getCcUserId, userId)
            .orderByDesc(BizApprovalCc::getCreatedAt));
    }

    public void markRead(Long ccId) {
        BizApprovalCc cc = this.getById(ccId);
        if (cc != null && cc.getReadAt() == null) {
            cc.setReadAt(LocalDateTime.now());
            this.updateById(cc);
        }
    }

    public void createCc(Long instanceId, List<Long> ccUserIds, String reason) {
        ccUserIds.forEach(userId -> {
            BizApprovalCc cc = new BizApprovalCc();
            cc.setApprovalInstanceId(instanceId);
            cc.setCcUserId(userId);
            cc.setCcReason(reason);
            this.save(cc);
        });
    }
}
