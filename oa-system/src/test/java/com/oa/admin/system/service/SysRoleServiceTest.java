package com.oa.admin.system.service;

import com.oa.admin.system.service.impl.SysRoleServiceImpl;
import com.oa.admin.system.entity.SysRole;
import com.oa.admin.system.entity.SysRoleDept;
import com.oa.admin.system.entity.SysRolePermission;
import com.oa.admin.system.mapper.SysRoleDeptMapper;
import com.oa.admin.system.mapper.SysRoleMapper;
import com.oa.admin.system.mapper.SysRolePermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
/**
 * @author wxvirus
 */

@ExtendWith(MockitoExtension.class)
class SysRoleServiceTest {

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysRolePermissionMapper rolePermissionMapper;

    @Mock
    private SysRoleDeptMapper roleDeptMapper;

    private SysRoleService roleService;

    @BeforeEach
    void setUp() throws Exception {
        roleService = new SysRoleServiceImpl(rolePermissionMapper, roleDeptMapper);
        injectBaseMapper(roleService, roleMapper);
    }

    static void injectBaseMapper(Object service, Object mapper) throws Exception {
        Class<?> c = service.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField("baseMapper");
                f.setAccessible(true);
                f.set(service, mapper);
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new RuntimeException("baseMapper field not found");
    }

    private SysRole buildRole(Long id, String name, Integer dataScope) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleName(name);
        role.setDataScope(dataScope);
        role.setStatus(1);
        role.setSort(1);
        return role;
    }

    @Test
    void assignPermissions_deletesOldAndInsertsNew() {
        when(rolePermissionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(2);
        when(rolePermissionMapper.insert(any(SysRolePermission.class))).thenReturn(1);

        roleService.assignPermissions(1L, List.of(10L, 20L));

        verify(rolePermissionMapper).delete(any(LambdaQueryWrapper.class));
        verify(rolePermissionMapper, times(2)).insert(any(SysRolePermission.class));
    }

    @Test
    void assignPermissions_withNullPermissionIds_onlyDeletes() {
        when(rolePermissionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);

        roleService.assignPermissions(1L, null);

        verify(rolePermissionMapper).delete(any(LambdaQueryWrapper.class));
        verify(rolePermissionMapper, never()).insert(any(SysRolePermission.class));
    }

    @Test
    void assignDataScope_withCustomScope_insertsDepts() {
        SysRole role = buildRole(1L, "Admin", 1);
        when(roleMapper.selectById(1L)).thenReturn(role);
        when(roleMapper.updateById(any(SysRole.class))).thenReturn(1);
        when(roleDeptMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(roleDeptMapper.insert(any(SysRoleDept.class))).thenReturn(1);

        roleService.assignDataScope(1L, 3, List.of(100L, 200L));

        assertEquals(3, role.getDataScope());
        verify(roleDeptMapper, times(2)).insert(any(SysRoleDept.class));
    }

    @Test
    void assignDataScope_withAllScope_doesNotInsertDepts() {
        SysRole role = buildRole(1L, "Admin", 1);
        when(roleMapper.selectById(1L)).thenReturn(role);
        when(roleMapper.updateById(any(SysRole.class))).thenReturn(1);
        when(roleDeptMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);

        roleService.assignDataScope(1L, 1, null);

        assertEquals(1, role.getDataScope());
        verify(roleDeptMapper, never()).insert(any(SysRoleDept.class));
    }
}
