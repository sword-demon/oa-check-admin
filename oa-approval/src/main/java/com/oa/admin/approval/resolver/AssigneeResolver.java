package com.oa.admin.approval.resolver;

import cn.dev33.satoken.stp.StpUtil;
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
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "用户不存在或未分配部门: " + userId);
        }
        SysDept dept = deptMapper.selectById(user.getDeptId());
        if (dept == null || dept.getLeaderUserId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "部门不存在或未设置负责人: " + user.getDeptId());
        }
        return dept.getLeaderUserId();
    }

    public Long resolveUpwardDeptLeader(Long userId, int level) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getDeptId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "用户不存在或未分配部门: " + userId);
        }
        Long currentDeptId = user.getDeptId();
        for (int i = 0; i < level; i++) {
            SysDept dept = deptMapper.selectById(currentDeptId);
            if (dept == null || dept.getParentId() == null || dept.getParentId() == 0L) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "已到达顶级部门, 无法继续向上: level=" + level);
            }
            currentDeptId = dept.getParentId();
        }
        SysDept targetDept = deptMapper.selectById(currentDeptId);
        if (targetDept == null || targetDept.getLeaderUserId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "目标部门不存在或未设置负责人: " + currentDeptId);
        }
        return targetDept.getLeaderUserId();
    }

    public Long resolveInitiator() {
        return StpUtil.getLoginIdAsLong();
    }
}
