package com.oa.admin.leave.dto;

import com.oa.admin.leave.enums.LeaveStatus;
import com.oa.admin.leave.enums.LeaveType;
import lombok.Data;

/**
 * 请假申请 query request
 * @author wxvirus
 */
@Data
public class LeaveRequestQueryDTO {

    /** 申请标题 */
    private String title;

    /** 请假类型 */
    private LeaveType leaveType;

    /** 申请人ID */
    private Long applicantUserId;

    /** 状态 */
    private LeaveStatus status;

    /** 当前页 */
    private long page = 1;

    /** 每页条数 */
    private long pageSize = 20;
}
