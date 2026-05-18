package com.oa.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.system.entity.SysDept;

import java.util.List;
/**
 * @author wxvirus
 */

public interface SysDeptService extends IService<SysDept> {

    List<SysDept> tree(String deptName, Integer status);

    List<SysDept> queryList(String deptName, Integer status, Long parentId);

    List<SysDept> listByParentId(Long parentId);
}
