package com.oa.admin.leave.dto;

import com.oa.admin.leave.enums.LeaveType;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 请假申请 update request
 * @author wxvirus
 */
@Data
public class LeaveRequestUpdateDTO {

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

}
