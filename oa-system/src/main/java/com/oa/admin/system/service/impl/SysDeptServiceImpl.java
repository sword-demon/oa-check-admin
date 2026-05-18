package com.oa.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.system.entity.SysDept;
import com.oa.admin.common.constant.TreeConstants;
import com.oa.admin.system.mapper.SysDeptMapper;
import com.oa.admin.system.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * @author wxvirus
 */

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<SysDept> tree(String deptName, Integer status) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SysDept::getStatus, status)
               .orderByAsc(SysDept::getSort);
        List<SysDept> all = this.list(wrapper);
        List<SysDept> visible = filterTreeSource(all, deptName);
        return buildTree(visible, TreeConstants.ROOT_PARENT_ID);
    }

    @Override
    public List<SysDept> queryList(String deptName, Integer status, Long parentId) {
        String keyword = normalizeKeyword(deptName);
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(keyword != null, SysDept::getDeptName, keyword)
               .eq(status != null, SysDept::getStatus, status)
               .eq(parentId != null, SysDept::getParentId, parentId)
               .orderByAsc(SysDept::getSort);
        return this.list(wrapper);
    }

    private List<SysDept> filterTreeSource(List<SysDept> all, String deptName) {
        String keyword = normalizeKeyword(deptName);
        if (keyword == null) {
            return all;
        }

        Map<Long, SysDept> deptById = all.stream()
                .collect(Collectors.toMap(SysDept::getId, Function.identity()));
        Set<Long> visibleIds = new HashSet<>();
        all.stream()
                .filter(dept -> dept.getDeptName() != null && dept.getDeptName().contains(keyword))
                .forEach(dept -> collectAncestorIds(dept, deptById, visibleIds));
        return all.stream()
                .filter(dept -> visibleIds.contains(dept.getId()))
                .collect(Collectors.toList());
    }

    private void collectAncestorIds(SysDept dept, Map<Long, SysDept> deptById, Set<Long> visibleIds) {
        if (dept == null || !visibleIds.add(dept.getId())) {
            return;
        }
        collectAncestorIds(deptById.get(dept.getParentId()), deptById, visibleIds);
    }

    private String normalizeKeyword(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private List<SysDept> buildTree(List<SysDept> all, Long parentId) {
        return all.stream()
                .filter(d -> parentId.equals(d.getParentId()))
                .peek(d -> d.setChildren(buildTree(all, d.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<SysDept> listByParentId(Long parentId) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, parentId)
               .orderByAsc(SysDept::getSort);
        return this.list(wrapper);
    }
}
