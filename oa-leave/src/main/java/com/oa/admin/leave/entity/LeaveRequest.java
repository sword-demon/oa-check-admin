package com.oa.admin.leave.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import com.oa.admin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请假申请
 * @author wxvirus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_leave_request")
public class LeaveRequest extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请标题 */
    private String title;

    /** 请假类型 */
    private Integer leaveType;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 请假原因 */
    private String reason;

    /** 申请人ID */
    private Long applicantUserId;

    /** 状态 */
    private Integer status;

}
