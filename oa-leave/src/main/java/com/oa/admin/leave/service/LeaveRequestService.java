package com.oa.admin.leave.service;

import com.oa.admin.leave.dto.LeaveRequestCreateDTO;
import com.oa.admin.leave.dto.LeaveRequestQueryDTO;
import com.oa.admin.leave.dto.LeaveRequestUpdateDTO;
import com.oa.admin.leave.entity.LeaveRequest;
import com.oa.admin.leave.vo.LeaveRequestVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.common.result.PageResult;

/**
 * 请假申请 Service
 * @author wxvirus
 */
public interface LeaveRequestService extends IService<LeaveRequest> {

    PageResult<LeaveRequestVO> page(LeaveRequestQueryDTO query);

    LeaveRequestVO getDetail(Long id);

    LeaveRequestVO create(LeaveRequestCreateDTO request);

    LeaveRequestVO update(Long id, LeaveRequestUpdateDTO request);

    void delete(Long id);

    /**
     * 提交请假审批
     */
    LeaveRequestVO submitForApproval(Long id);

    /**
     * 驳回后重新编辑并提交
     */
    LeaveRequestVO resubmit(Long id, LeaveRequestUpdateDTO dto);

    /**
     * 审批回调: 更新请假状态
     */
    void onApprovalResult(Long leaveRequestId, int approvalResult);
}
