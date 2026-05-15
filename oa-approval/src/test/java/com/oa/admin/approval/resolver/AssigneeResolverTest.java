package com.oa.admin.approval.resolver;

import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.system.entity.SysDept;
import com.oa.admin.system.entity.SysUser;
import com.oa.admin.system.entity.SysUserRole;
import com.oa.admin.system.mapper.SysDeptMapper;
import com.oa.admin.system.mapper.SysUserMapper;
import com.oa.admin.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssigneeResolverTest {

    @Mock private SysUserMapper userMapper;
    @Mock private SysDeptMapper deptMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @InjectMocks private AssigneeResolver assigneeResolver;

    private SysUser testUser;
    private SysDept testDept;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setId(1L);
        testUser.setDeptId(10L);

        testDept = new SysDept();
        testDept.setId(10L);
        testDept.setParentId(0L);
        testDept.setLeaderUserId(99L);
    }

    @Test
    void resolveDeptLeader_returnsLeaderForUsersDept() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(deptMapper.selectById(10L)).thenReturn(testDept);

        Long leaderId = assigneeResolver.resolveDeptLeader(1L);

        assertEquals(99L, leaderId);
    }

    @Test
    void resolveDeptLeader_userNotFound_throws() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> assigneeResolver.resolveDeptLeader(999L));
    }

    @Test
    void resolveDeptLeader_noDept_throws() {
        testUser.setDeptId(null);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> assigneeResolver.resolveDeptLeader(1L));
    }

    @Test
    void resolveUpwardDeptLeader_oneLevel_returnsParentDeptLeader() {
        SysDept parentDept = new SysDept();
        parentDept.setId(20L);
        parentDept.setParentId(0L);
        parentDept.setLeaderUserId(88L);

        testDept.setParentId(20L);

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(deptMapper.selectById(10L)).thenReturn(testDept);
        when(deptMapper.selectById(20L)).thenReturn(parentDept);

        Long leaderId = assigneeResolver.resolveUpwardDeptLeader(1L, 1);

        assertEquals(88L, leaderId);
    }

    @Test
    void resolveUpwardDeptLeader_alreadyAtTop_throws() {
        testDept.setParentId(0L);

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(deptMapper.selectById(10L)).thenReturn(testDept);

        assertThrows(BusinessException.class, () -> assigneeResolver.resolveUpwardDeptLeader(1L, 1));
    }
}
