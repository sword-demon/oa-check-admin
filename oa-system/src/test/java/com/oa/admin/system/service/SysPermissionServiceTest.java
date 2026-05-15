package com.oa.admin.system.service;

import com.oa.admin.system.service.impl.SysPermissionServiceImpl;
import com.oa.admin.system.entity.SysPermission;
import com.oa.admin.system.mapper.SysPermissionMapper;
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

@ExtendWith(MockitoExtension.class)
class SysPermissionServiceTest {

    @Mock
    private SysPermissionMapper permissionMapper;

    private SysPermissionService permissionService;

    @BeforeEach
    void setUp() throws Exception {
        permissionService = new SysPermissionServiceImpl();
        injectBaseMapper(permissionService, permissionMapper);
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
        throw new RuntimeException("baseMapper field not found in class hierarchy");
    }

    private SysPermission buildPerm(Long id, Long parentId, String name, Integer type, Integer sort) {
        SysPermission perm = new SysPermission();
        perm.setId(id);
        perm.setParentId(parentId);
        perm.setPermissionName(name);
        perm.setPermissionType(type);
        perm.setSort(sort);
        perm.setStatus(1);
        return perm;
    }

    @Test
    void listByStatus_returnsFilteredList() {
        SysPermission p1 = buildPerm(1L, 0L, "System", 1, 1);
        SysPermission p2 = buildPerm(2L, 1L, "User", 1, 2);
        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(p1, p2));

        List<SysPermission> result = permissionService.listByStatus(1);
        assertEquals(2, result.size());
    }

    @Test
    void listByStatus_withNullStatus_returnsAll() {
        SysPermission p1 = buildPerm(1L, 0L, "System", 1, 1);
        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(p1));

        List<SysPermission> result = permissionService.listByStatus(null);
        assertEquals(1, result.size());
    }
}
