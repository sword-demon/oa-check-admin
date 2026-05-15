package com.oa.admin.approval.resolver;

import cn.dev33.satoken.stp.StpUtil;
import com.oa.admin.common.constant.TreeConstants;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.system.entity.SysDept;
import com.oa.admin.system.entity.SysUser;
import com.oa.admin.system.mapper.SysDeptMapper;
import com.oa.admin.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves assignees dynamically from BPMN UEL expressions.
 * Referenced in BPMN as ${assigneeResolver.resolveXxx(...)}.
 */
@Component("assigneeResolver")
@RequiredArgsConstructor
public class AssigneeResolver {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    public Long resolveDeptLeader(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getDeptId() == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_NO_DEPT);
        }
        SysDept dept = deptMapper.selectById(user.getDeptId());
        if (dept == null || dept.getLeaderUserId() == null) {
            throw new BusinessException(ErrorCode.DEPT_NOT_FOUND_OR_NO_LEADER);
        }
        return dept.getLeaderUserId();
    }

    public Long resolveUpwardDeptLeader(Long userId, int level) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getDeptId() == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_NO_DEPT);
        }
        Long currentDeptId = user.getDeptId();
        for (int i = 0; i < level; i++) {
            SysDept dept = deptMapper.selectById(currentDeptId);
            if (dept == null || dept.getParentId() == null || TreeConstants.ROOT_PARENT_ID.equals(dept.getParentId())) {
                throw new BusinessException(ErrorCode.DEPT_TOP_REACHED);
            }
            currentDeptId = dept.getParentId();
        }
        SysDept targetDept = deptMapper.selectById(currentDeptId);
        if (targetDept == null || targetDept.getLeaderUserId() == null) {
            throw new BusinessException(ErrorCode.TARGET_DEPT_NOT_FOUND_OR_NO_LEADER);
        }
        return targetDept.getLeaderUserId();
    }

    public Long resolveInitiator() {
        return StpUtil.getLoginIdAsLong();
    }
}
