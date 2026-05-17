package com.oa.admin.leave.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.enums.TemplateStatus;
import com.oa.admin.approval.service.ApprovalService;
import com.oa.admin.approval.service.ApprovalTemplateService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.leave.dto.LeaveRequestCreateDTO;
import com.oa.admin.leave.dto.LeaveRequestQueryDTO;
import com.oa.admin.leave.dto.LeaveRequestUpdateDTO;
import com.oa.admin.leave.entity.LeaveRequest;
import com.oa.admin.leave.enums.LeaveStatus;
import com.oa.admin.leave.enums.LeaveType;
import com.oa.admin.leave.mapper.LeaveRequestMapper;
import com.oa.admin.leave.service.LeaveRequestService;
import com.oa.admin.leave.vo.LeaveRequestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 请假申请 Service Implementation
 * @author wxvirus
 */
@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements LeaveRequestService {

    private final ApprovalService approvalService;
    private final ApprovalTemplateService templateService;

    @Override
    public PageResult<LeaveRequestVO> page(LeaveRequestQueryDTO query) {
        LambdaQueryWrapper<LeaveRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .like(query.getTitle() != null && !query.getTitle().isEmpty(), LeaveRequest::getTitle, query.getTitle())
                .eq(query.getLeaveType() != null, LeaveRequest::getLeaveType, query.getLeaveType().getCode())
                .eq(query.getApplicantUserId() != null, LeaveRequest::getApplicantUserId, query.getApplicantUserId())
                .eq(query.getStatus() != null, LeaveRequest::getStatus, query.getStatus().getCode())
                .orderByDesc(LeaveRequest::getCreatedAt);
        Page<LeaveRequest> result = this.page(new Page<>(query.getPage(), query.getPageSize()), wrapper);
        return new PageResult<>(result.getRecords().stream().map(this::toVO).toList(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    public LeaveRequestVO getDetail(Long id) {
        return toVO(getById(id));
    }

    @Override
    public LeaveRequestVO create(LeaveRequestCreateDTO request) {
        LeaveRequest entity = new LeaveRequest();
        entity.setTitle(request.getTitle());
        entity.setLeaveType(request.getLeaveType() == null ? null : request.getLeaveType().getCode());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setReason(request.getReason());
        entity.setApplicantUserId(StpUtil.getLoginIdAsLong());
        entity.setStatus(LeaveStatus.DRAFT.getCode());
        save(entity);
        return toVO(entity);
    }

    @Override
    public LeaveRequestVO update(Long id, LeaveRequestUpdateDTO request) {
        LeaveRequest entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        entity.setTitle(request.getTitle());
        entity.setLeaveType(request.getLeaveType() == null ? null : request.getLeaveType().getCode());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setReason(request.getReason());
        updateById(entity);
        return toVO(getById(id));
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveRequestVO submitForApproval(Long id) {
        LeaveRequest entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (entity.getStatus() != LeaveStatus.DRAFT.getCode()) {
            throw new BusinessException(ErrorCode.CANNOT_WITHDRAW);
        }

        BizProcessTemplate template = findLeaveTemplate();
        String formData = "{\"leaveRequestId\":" + entity.getId() + "}";
        var instance = approvalService.submit(template.getId(), entity.getTitle(), formData);

        entity.setApprovalInstanceId(instance.getId());
        entity.setStatus(LeaveStatus.PENDING.getCode());
        updateById(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveRequestVO resubmit(Long id, LeaveRequestUpdateDTO dto) {
        LeaveRequest entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (entity.getStatus() != LeaveStatus.REJECTED.getCode()) {
            throw new BusinessException(ErrorCode.CANNOT_WITHDRAW);
        }

        entity.setTitle(dto.getTitle());
        entity.setLeaveType(dto.getLeaveType() == null ? null : dto.getLeaveType().getCode());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setReason(dto.getReason());
        updateById(entity);

        return submitForApproval(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onApprovalResult(Long leaveRequestId, int approvalResult) {
        LeaveRequest entity = getById(leaveRequestId);
        if (entity == null) {
            return;
        }
        if (approvalResult == 1) {
            entity.setStatus(LeaveStatus.APPROVED.getCode());
        } else if (approvalResult == 2) {
            entity.setStatus(LeaveStatus.REJECTED.getCode());
        }
        updateById(entity);
    }

    private BizProcessTemplate findLeaveTemplate() {
        LambdaQueryWrapper<BizProcessTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizProcessTemplate::getTemplateKey, "leave_request")
               .eq(BizProcessTemplate::getStatus, TemplateStatus.PUBLISHED.getCode())
               .orderByDesc(BizProcessTemplate::getVersion)
               .last("LIMIT 1");
        BizProcessTemplate template = templateService.getOne(wrapper);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_PUBLISHED);
        }
        return template;
    }

    private LeaveRequestVO toVO(LeaveRequest entity) {
        if (entity == null) {
            return null;
        }
        LeaveRequestVO vo = new LeaveRequestVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setLeaveType(entity.getLeaveType() == null ? null : LeaveType.fromCode(entity.getLeaveType()));
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setReason(entity.getReason());
        vo.setApplicantUserId(entity.getApplicantUserId());
        vo.setStatus(entity.getStatus() == null ? null : LeaveStatus.fromCode(entity.getStatus()));
        vo.setApprovalInstanceId(entity.getApprovalInstanceId());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
