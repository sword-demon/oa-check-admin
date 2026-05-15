package com.oa.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.system.entity.SysPermission;
import com.oa.admin.common.constant.TreeConstants;
import com.oa.admin.system.mapper.SysPermissionMapper;
import com.oa.admin.system.service.SysPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Override
    public List<SysPermission> listByStatus(Integer status) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SysPermission::getStatus, status)
               .orderByAsc(SysPermission::getSort);
        return this.list(wrapper);
    }

    @Override
    public List<SysPermission> tree(Integer status) {
        List<SysPermission> all = listByStatus(status);
        return buildTree(all, TreeConstants.ROOT_PARENT_ID);
    }

    private List<SysPermission> buildTree(List<SysPermission> all, Long parentId) {
        return all.stream()
                .filter(p -> parentId.equals(p.getParentId()))
                .peek(p -> p.setChildren(buildTree(all, p.getId())))
                .collect(Collectors.toList());
    }
}
