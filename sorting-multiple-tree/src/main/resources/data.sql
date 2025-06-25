-- UI资源初始化数据 - 表1
INSERT INTO ui_resource_1 (id, parent_id, node_path, sort, title, en_title, show_status, active_status)
VALUES ('ui-1', null, '/ui-1', 1, '首页', 'Home', true, true),
       ('ui-2', null, '/ui-2', 2, '用户管理', 'User Management', true, true),
       ('ui-3', null, '/ui-3', 3, '系统设置', 'System Settings', true, true),
       ('ui-2-1', 'ui-2', '/ui-2/ui-2-1', 201, '用户列表', 'User List', true, true),
       ('ui-2-2', 'ui-2', '/ui-2/ui-2-2', 202, '角色管理', 'Role Management', true, true),
       ('ui-3-1', 'ui-3', '/ui-3/ui-3-1', 301, '菜单管理', 'Menu Management', true, true),
       ('ui-3-2', 'ui-3', '/ui-3/ui-3-2', 302, '权限配置', 'Permission Config', true, true),
       ('ui-5', null, '/ui-5', 5, '消息通知', 'Notifications', true, true),
       ('ui-5-1', 'ui-5', '/ui-5/ui-5-1', 501, '系统消息', 'System Messages', true, true),
       ('ui-5-2', 'ui-5', '/ui-5/ui-5-2', 502, '用户消息', 'User Messages', true, true);

-- UI资源初始化数据 - 表2
INSERT INTO ui_resource_2 (id, parent_id, node_path, sort, title, en_title, show_status, active_status)
VALUES ('ui-1', null, '/ui-1', 1, '首页V2', 'Home V2', true, true),
       ('ui-2', null, '/ui-2', 2, '用户管理', 'User Management', false, true),
       ('ui-4', null, '/ui-4', 4, '数据分析', 'Data Analysis', true, true),
       ('ui-2-1', 'ui-2', '/ui-2/ui-2-1', 201, '用户列表', 'User List', true, true),
       ('ui-2-3', 'ui-2', '/ui-2/ui-2-3', 203, '部门管理', 'Department Management', true, true),
       ('ui-4-1', 'ui-4', '/ui-4/ui-4-1', 401, '报表中心', 'Report Center', true, true),
       ('ui-4-2', 'ui-4', '/ui-4/ui-4-2', 402, '数据大屏', 'Data Dashboard', true, true),
       ('ui-6', null, '/ui-6', 6, '日志管理', 'Log Management', true, true),
       ('ui-6-1', 'ui-6', '/ui-6/ui-6-1', 601, '登录日志', 'Login Logs', true, true),
       ('ui-6-2', 'ui-6', '/ui-6/ui-6-2', 602, '操作日志', 'Operation Logs', true, true);

-- API资源初始化数据 - 表1
INSERT INTO api_resource_1 (id, parent_id, node_path, sort, title, en_title, show_status, active_status)
VALUES ('api-1', null, '/api-1', 1, '用户接口', 'User API', true, true),
       ('api-2', null, '/api-2', 2, '系统接口', 'System API', true, true),
       ('api-1-1', 'api-1', '/api-1/api-1-1', 101, '获取用户信息', 'Get User Info', true, true),
       ('api-1-2', 'api-1', '/api-1/api-1-2', 102, '更新用户信息', 'Update User Info', true, true),
       ('api-2-1', 'api-2', '/api-2/api-2-1', 201, '获取系统配置', 'Get System Config', true, true),
       ('api-2-2', 'api-2', '/api-2/api-2-2', 202, '更新系统配置', 'Update System Config', true, true),
       ('api-4', null, '/api-4', 4, '安全接口', 'Security API', true, true),
       ('api-4-1', 'api-4', '/api-4/api-4-1', 401, '获取权限列表', 'Get Permissions', true, true),
       ('api-4-2', 'api-4', '/api-4/api-4-2', 402, '验证用户权限', 'Validate User Permission', true, true);

-- API资源初始化数据 - 表2
INSERT INTO api_resource_2 (id, parent_id, node_path, sort, title, en_title, show_status, active_status)
VALUES ('api-1', null, '/api-1', 1, '用户接口V2', 'User API V2', true, true),
       ('api-3', null, '/api-3', 3, '分析接口', 'Analytics API', true, true),
       ('api-1-1', 'api-1', '/api-1/api-1-1', 101, '获取用户信息', 'Get User Info', true, false),
       ('api-1-3', 'api-1', '/api-1/api-1-3', 103, '删除用户', 'Delete User', true, true),
       ('api-3-1', 'api-3', '/api-3/api-3-1', 301, '获取分析数据', 'Get Analytics Data', true, true),
       ('api-3-2', 'api-3', '/api-3/api-3-2', 302, '生成报表', 'Generate Report', true, true),
       ('api-5', null, '/api-5', 5, '监控接口', 'Monitoring API', true, true),
       ('api-5-1', 'api-5', '/api-5/api-5-1', 501, '系统健康检查', 'System Health Check', true, true),
       ('api-5-2', 'api-5', '/api-5/api-5-2', 502, '服务状态监控', 'Service Status Monitor', true, true);

INSERT INTO api_resource_2 (id, parent_id, node_path, sort, title, en_title, show_status, active_status)
VALUES ('api-2-1', 'api-3', '/api-3/api-2-1', 303, '获取系统配置', 'Get System Config', true, true);
