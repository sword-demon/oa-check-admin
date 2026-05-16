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

    /**
     * 分页查询
     */
    PageResult<LeaveRequestVO> page(LeaveRequestQueryDTO query);

    /**
     * 详情查询
     */
    LeaveRequestVO getDetail(Long id);

    /**
     * 新增
     */
    LeaveRequestVO create(LeaveRequestCreateDTO request);

    /**
     * 更新
     */
    LeaveRequestVO update(Long id, LeaveRequestUpdateDTO request);

    /**
     * 删除
     */
    void delete(Long id);
}
