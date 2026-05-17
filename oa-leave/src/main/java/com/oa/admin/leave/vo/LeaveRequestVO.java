package com.oa.admin.leave.vo;

import com.oa.admin.leave.enums.LeaveStatus;
import com.oa.admin.leave.enums.LeaveType;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 请假申请 response
 * @author wxvirus
 */
@Data
public class LeaveRequestVO {

    private Long id;

    /** 申请标题 */
    private String title;

    /** 请假类型 */
    private LeaveType leaveType;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 请假原因 */
    private String reason;

    /** 申请人ID */
    private Long applicantUserId;

    /** 状态 */
    private LeaveStatus status;

    /** 关联审批实例ID */
    private Long approvalInstanceId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
