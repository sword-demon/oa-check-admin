package com.oa.admin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
/**
 * @author wxvirus
 */

@Data
@TableName("sys_role_dept")
public class SysRoleDept {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long deptId;
}
