package com.oa.admin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.system.entity.SysDept;
import com.oa.admin.system.mapper.SysDeptMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> {

    public List<SysDept> tree(Integer status) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SysDept::getStatus, status)
               .orderByAsc(SysDept::getSort);
        List<SysDept> all = this.list(wrapper);
        return buildTree(all, 0L);
    }

    private List<SysDept> buildTree(List<SysDept> all, Long parentId) {
        Map<Long, List<SysDept>> grouped = all.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId));

        List<SysDept> roots = grouped.getOrDefault(parentId, new ArrayList<>());
        // The tree is flat here; callers can build nested VO structures as needed.
        return roots;
    }

    public List<SysDept> listByParentId(Long parentId) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, parentId)
               .orderByAsc(SysDept::getSort);
        return this.list(wrapper);
    }
}
