package com.oa.admin.leave.mapper;

import com.oa.admin.leave.entity.LeaveRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 请假申请 Mapper
 * @author wxvirus
 */
@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {
}
