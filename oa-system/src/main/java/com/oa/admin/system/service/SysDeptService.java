package com.oa.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.system.entity.SysDept;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {

    List<SysDept> tree(Integer status);

    List<SysDept> listByParentId(Long parentId);
}
