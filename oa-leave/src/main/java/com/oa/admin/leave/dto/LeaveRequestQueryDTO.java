package com.oa.admin.leave.dto;

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
    private Integer leaveType;

    /** 申请人ID */
    private Long applicantUserId;

    /** 状态 */
    private Integer status;

    /** 当前页 */
    private long page = 1;

    /** 每页条数 */
    private long pageSize = 20;
}
