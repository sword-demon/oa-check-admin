# Changelog

All notable changes to this project will be documented in this file.

## [0.1.0.0] - 2026-05-15

### Added

**后端核心**
- Spring Boot 3.5.x 多模块项目骨架 (oa-common, oa-system, oa-approval, oa-app)
- RBAC 五表模型 + 完整 CRUD 服务层 (用户, 角色, 权限, 部门)
- Sa-Token 1.44.0 集成 (登录认证, 权限校验 `@SaCheckPermission`, 会话管理, StpInterface 权限加载)
- Flowable 7.2.0 审批引擎集成 (BPMN 串行审批链, TaskListener 任务同步, ProcessEndEventListener 流程完成监听)
- 审批业务模块 (模板管理, 实例提交/审批/撤回, 任务同步, 抄送, 审计日志)
- Flyway 数据库迁移 V1-V3 (建表 + 种子数据 + 示例请假审批模板)
- 统一响应体 `R<T>`, `PageResult<T>`, `ErrorCode` 错误码体系
- 全局异常处理 (Sa-Token 异常, 业务异常, 参数校验)
- BaseEntity 自动填充时间戳 + 逻辑删除

**前端完整功能**
- Vue 3 + TypeScript + Vite 6 + Element Plus 管理后台
- 登录页 (表单验证, Sa-Token token 存储)
- 仪表盘 (待办/已办/模板/抄送统计 + 最近审批动态)
- 系统管理: 用户管理 (搜索/分页/CRUD), 角色管理 (CRUD + 权限分配树), 权限管理 (树形表格 + CRUD), 部门管理 (树形表格 + CRUD)
- 审批管理: 模板管理 (CRUD), 我的申请 (发起/撤回), 我的待办 (通过/驳回), 我的已办 (历史), 抄送给我的 (标记已读)
- 侧边栏布局 (可折叠, 路由导航)
- Axios 请求拦截器 (Sa-Token header 注入, 401 重定向, 统一错误提示)

**基础设施**
- Docker Compose 一键部署 (MySQL 8.x + Redis + Backend + Frontend + Nginx)
- `init.sh` 初始化脚本 (交互式替换包名/端口/数据库名)
- 请假审批示例 BPMN 流程定义 (leave_request.bpmn20.xml)
- README + VERSION + 项目文档
