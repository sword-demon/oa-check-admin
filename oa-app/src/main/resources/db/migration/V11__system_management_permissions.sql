-- V11: Complete system management API permissions and grant them to admin role

INSERT INTO sys_permission (id, parent_id, permission_name, permission_type, path, sort, status) VALUES
(120, 12, '角色新增', 2, 'system:role:add',    1, 1),
(121, 12, '角色编辑', 2, 'system:role:edit',   2, 1),
(122, 12, '角色删除', 2, 'system:role:remove', 3, 1),
(123, 12, '角色查询', 3, 'system:role:query',  4, 1),
(124, 12, '角色列表', 3, 'system:role:list',   5, 1),
(130, 13, '权限新增', 2, 'system:permission:add',    1, 1),
(131, 13, '权限编辑', 2, 'system:permission:edit',   2, 1),
(132, 13, '权限删除', 2, 'system:permission:remove', 3, 1),
(133, 13, '权限查询', 3, 'system:permission:query',  4, 1),
(134, 13, '权限列表', 3, 'system:permission:list',   5, 1),
(140, 14, '部门新增', 2, 'system:dept:add',    1, 1),
(141, 14, '部门编辑', 2, 'system:dept:edit',   2, 1),
(142, 14, '部门删除', 2, 'system:dept:remove', 3, 1),
(143, 14, '部门查询', 3, 'system:dept:query',  4, 1),
(144, 14, '部门列表', 3, 'system:dept:list',   5, 1)
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    permission_name = VALUES(permission_name),
    permission_type = VALUES(permission_type),
    path = VALUES(path),
    sort = VALUES(sort),
    status = VALUES(status);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id
FROM sys_permission
WHERE path IN (
    'system:role:add',
    'system:role:edit',
    'system:role:remove',
    'system:role:query',
    'system:role:list',
    'system:permission:add',
    'system:permission:edit',
    'system:permission:remove',
    'system:permission:query',
    'system:permission:list',
    'system:dept:add',
    'system:dept:edit',
    'system:dept:remove',
    'system:dept:query',
    'system:dept:list'
);
