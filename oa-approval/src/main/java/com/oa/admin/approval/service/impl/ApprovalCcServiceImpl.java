package com.oa.admin.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.dto.CcVO;
import com.oa.admin.approval.entity.BizApprovalCc;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.mapper.BizApprovalCcMapper;
import com.oa.admin.approval.mapper.BizApprovalInstanceMapper;
import com.oa.admin.approval.service.ApprovalCcService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * @author wxvirus
 */

@Service
@RequiredArgsConstructor
public class ApprovalCcServiceImpl extends ServiceImpl<BizApprovalCcMapper, BizApprovalCc> implements ApprovalCcService {

    private final BizApprovalInstanceMapper instanceMapper;

    @Override
    public List<BizApprovalCc> myCc() {
        long userId = StpUtil.getLoginIdAsLong();
        return this.list(new LambdaQueryWrapper<BizApprovalCc>()
            .eq(BizApprovalCc::getCcUserId, userId)
            .orderByDesc(BizApprovalCc::getCreatedAt));
    }

    @Override
    public List<CcVO> myCcWithDetails() {
        List<BizApprovalCc> ccList = myCc();
        if (ccList.isEmpty()) {
            return List.of();
        }

        Set<Long> instanceIds = ccList.stream()
            .map(BizApprovalCc::getApprovalInstanceId)
            .collect(Collectors.toSet());

        Map<Long, BizApprovalInstance> instanceMap = new HashMap<>();
        if (!instanceIds.isEmpty()) {
            instanceMapper.selectBatchIds(instanceIds).forEach(inst ->
                instanceMap.put(inst.getId(), inst));
        }

        return ccList.stream().map(cc -> {
            BizApprovalInstance instance = instanceMap.get(cc.getApprovalInstanceId());
            return CcVO.builder()
                .id(cc.getId())
                .approvalInstanceId(cc.getApprovalInstanceId())
                .instanceTitle(instance != null ? instance.getInstanceTitle() : null)
                .instanceStatus(instance != null ? instance.getStatus() : null)
                .ccUserId(cc.getCcUserId())
                .ccReason(cc.getCcReason())
                .readAt(cc.getReadAt())
                .createdAt(cc.getCreatedAt())
                .build();
        }).toList();
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
