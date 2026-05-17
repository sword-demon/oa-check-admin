-- V14: Add missing leave:leave_request:submit permission and bind to admin role

INSERT INTO sys_permission (id, parent_id, permission_name, permission_type, path, sort, status) VALUES
(315, 31, '请假申请提交审批', 2, 'leave:leave_request:submit', 6, 1)
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
WHERE path = 'leave:leave_request:submit';
