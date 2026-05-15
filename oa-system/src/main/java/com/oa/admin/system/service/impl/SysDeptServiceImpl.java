package com.oa.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.system.entity.SysDept;
import com.oa.admin.common.constant.TreeConstants;
import com.oa.admin.system.mapper.SysDeptMapper;
import com.oa.admin.system.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<SysDept> tree(Integer status) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SysDept::getStatus, status)
               .orderByAsc(SysDept::getSort);
        List<SysDept> all = this.list(wrapper);
        return buildTree(all, TreeConstants.ROOT_PARENT_ID);
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
