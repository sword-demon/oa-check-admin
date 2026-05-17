package com.oa.admin.approval.resolver;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.admin.system.entity.SysUserRole;
import com.oa.admin.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves user ID lists for multi-instance (countersign/or-sign) collections.
 * Referenced in BPMN as ${candidateUserResolver.resolveRoleUsers(roleId)}.
 * @author wxvirus
 */
@Component("candidateUserResolver")
@RequiredArgsConstructor
public class CandidateUserResolver {

    private final SysUserRoleMapper userRoleMapper;

    public List<Long> resolveRoleUsers(Long roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, roleId);
        return userRoleMapper.selectList(wrapper).stream()
                .map(SysUserRole::getUserId)
                .collect(Collectors.toList());
    }
}
