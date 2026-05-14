-- ============================================================
-- V3: Seed data - admin user, basic roles, permissions
-- ============================================================

-- Default admin user (BCrypt hashed password, change after first login)
INSERT INTO sys_user (username, password_hash, nickname, status)
VALUES ('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', 1);

INSERT INTO sys_dept (id, parent_id, dept_name, sort, status)
VALUES (1, 0, '总公司', 0, 1);

INSERT INTO sys_dept (id, parent_id, dept_name, sort, leader_user_id, status)
VALUES (2, 1, '研发部', 1, 1, 1);

INSERT INTO sys_dept (id, parent_id, dept_name, sort, status)
VALUES (3, 1, '市场部', 2, 1);

-- Roles
INSERT INTO sys_role (id, role_name, role_key, sort, data_scope, status)
VALUES (1, '超级管理员', 'admin', 0, 1, 1);

INSERT INTO sys_role (id, role_name, role_key, sort, data_scope, status)
VALUES (2, '普通用户', 'user', 1, 2, 1);

-- Admin gets admin role
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- Menu permissions
INSERT INTO sys_permission (id, parent_id, permission_name, permission_type, path, component, icon, sort, status) VALUES
(1,  0, '仪表盘',     1, '/dashboard',            'dashboard/index',     'Odometer',   0, 1),
(10, 0, '系统管理',   1, '/system',                '',                    'Setting',    1, 1),
(11, 10, '用户管理',  1, '/system/user',           'system/user/index',   'User',       1, 1),
(12, 10, '角色管理',  1, '/system/role',           'system/role/index',   'Key',        2, 1),
(13, 10, '权限管理',  1, '/system/permission',     'system/permission/index', 'Lock',   3, 1),
(14, 10, '部门管理',  1, '/system/dept',           'system/dept/index',   'OfficeBuilding', 4, 1),
(20, 0, '审批管理',   1, '/approval',              '',                    'Document',   2, 1),
(21, 20, '审批模板',  1, '/approval/template',     'approval/template/index', 'Files', 1, 1),
(22, 20, '我的申请',  1, '/approval/my-apply',     'approval/my-apply/index', 'EditPen', 2, 1),
(23, 20, '我的待办',  1, '/approval/my-todo',      'approval/my-todo/index',  'List',   3, 1),
(24, 20, '我的已办',  1, '/approval/my-done',      'approval/my-done/index',  'Finished', 4, 1),
(25, 20, '抄送给我的', 1, '/approval/cc',          'approval/cc/index',        'Message', 5, 1);

-- Button/API permissions for user management
INSERT INTO sys_permission (id, parent_id, permission_name, permission_type, path, sort, status) VALUES
(110, 11, '用户新增', 2, 'system:user:add',    1, 1),
(111, 11, '用户编辑', 2, 'system:user:edit',   2, 1),
(112, 11, '用户删除', 2, 'system:user:remove', 3, 1),
(113, 11, '用户查询', 2, 'system:user:query',  4, 1),
(114, 11, '用户列表', 2, 'system:user:list',   5, 1);

-- Admin role gets all permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- Sample approval template: leave request
INSERT INTO biz_process_template (template_name, template_key, form_config, version, status)
VALUES (
    '请假审批',
    'leave_request',
    '{"fields":[{"name":"leave_days","type":"number","label":"请假天数","required":true},{"name":"reason","type":"textarea","label":"请假原因","required":true},{"name":"leave_type","type":"select","label":"请假类型","options":["年假","事假","病假"],"required":true}]}',
    1,
    2
);
