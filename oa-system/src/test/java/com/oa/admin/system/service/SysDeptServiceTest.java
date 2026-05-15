package com.oa.admin.system.service;

import com.oa.admin.system.entity.SysDept;
import com.oa.admin.system.mapper.SysDeptMapper;
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
class SysDeptServiceTest {

    @Mock
    private SysDeptMapper deptMapper;

    private SysDeptService deptService;

    @BeforeEach
    void setUp() throws Exception {
        deptService = new SysDeptService();
        injectBaseMapper(deptService, deptMapper);
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

    private SysDept buildDept(Long id, Long parentId, String name, Integer sort) {
        SysDept dept = new SysDept();
        dept.setId(id);
        dept.setParentId(parentId);
        dept.setDeptName(name);
        dept.setSort(sort);
        dept.setStatus(1);
        return dept;
    }

    @Test
    void listByParentId_returnsOnlyChildren() {
        SysDept child1 = buildDept(2L, 1L, "Child1", 1);
        SysDept child2 = buildDept(3L, 1L, "Child2", 2);
        when(deptMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(child1, child2));

        List<SysDept> result = deptService.listByParentId(1L);
        assertEquals(2, result.size());
    }

    @Test
    void listByParentId_withNoChildren_returnsEmptyList() {
        when(deptMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<SysDept> result = deptService.listByParentId(999L);
        assertTrue(result.isEmpty());
    }
}
