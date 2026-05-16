package com.oa.admin.leave.service.impl;

import com.oa.admin.leave.dto.LeaveRequestCreateDTO;
import com.oa.admin.leave.dto.LeaveRequestQueryDTO;
import com.oa.admin.leave.dto.LeaveRequestUpdateDTO;
import com.oa.admin.leave.mapper.LeaveRequestMapper;
import com.oa.admin.leave.entity.LeaveRequest;
import com.oa.admin.leave.service.LeaveRequestService;
import com.oa.admin.leave.vo.LeaveRequestVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 请假申请 Service Implementation
 * @author wxvirus
 */
@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements LeaveRequestService {

    @Override
    public PageResult<LeaveRequestVO> page(LeaveRequestQueryDTO query) {
        LambdaQueryWrapper<LeaveRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .like(query.getTitle() != null && !query.getTitle().isEmpty(), LeaveRequest::getTitle, query.getTitle())

                .eq(query.getLeaveType() != null, LeaveRequest::getLeaveType, query.getLeaveType())

                .eq(query.getApplicantUserId() != null, LeaveRequest::getApplicantUserId, query.getApplicantUserId())

                .eq(query.getStatus() != null, LeaveRequest::getStatus, query.getStatus())
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
        LeaveRequest entity = toEntity(request);
        save(entity);
        return toVO(entity);
    }

    @Override
    public LeaveRequestVO update(Long id, LeaveRequestUpdateDTO request) {
        LeaveRequest entity = toEntity(request);
        entity.setId(id);
        updateById(entity);
        return toVO(getById(id));
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    private LeaveRequest toEntity(LeaveRequestCreateDTO request) {
        LeaveRequest entity = new LeaveRequest();
        entity.setTitle(request.getTitle());
        entity.setLeaveType(request.getLeaveType());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setReason(request.getReason());
        entity.setApplicantUserId(request.getApplicantUserId());
        entity.setStatus(request.getStatus());
        return entity;
    }

    private LeaveRequest toEntity(LeaveRequestUpdateDTO request) {
        LeaveRequest entity = new LeaveRequest();
        entity.setTitle(request.getTitle());
        entity.setLeaveType(request.getLeaveType());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setReason(request.getReason());
        entity.setApplicantUserId(request.getApplicantUserId());
        entity.setStatus(request.getStatus());
        return entity;
    }

    private LeaveRequestVO toVO(LeaveRequest entity) {
        if (entity == null) {
            return null;
        }
        LeaveRequestVO vo = new LeaveRequestVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setLeaveType(entity.getLeaveType());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setReason(entity.getReason());
        vo.setApplicantUserId(entity.getApplicantUserId());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
