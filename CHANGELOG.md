# Changelog

All notable changes to this project will be documented in this file.

## [0.1.0.0] - 2026-05-14

### Added
- Spring Boot 3.5.x 多模块项目骨架 (oa-common, oa-system, oa-approval, oa-app)
- RBAC 五表模型 (sys_user, sys_dept, sys_role, sys_permission, 关联表)
- Sa-Token 1.44.0 集成 (登录认证, 权限校验, 会话管理)
- Flowable 7.2.0 审批引擎集成 (串行审批链)
- 审批业务模块 (模板管理, 实例, 任务, 抄送, 审计日志)
- Flyway 数据库迁移 (V1-V3: 建表 + 种子数据)
- Vue 3 + TypeScript + Vite 6 前端骨架
- Element Plus 管理后台布局 (侧边栏 + 顶部导航)
- 登录页, 仪表盘, 系统管理 (用户/角色/权限/部门), 审批管理页面
- Axios 请求封装 (Sa-Token header, 统一错误处理)
- Pinia 用户状态管理
- Vue Router 路由守卫 (token 校验)
- Docker Compose 一键部署 (MySQL + Redis + Backend + Frontend)
- `init.sh` 初始化脚本 (交互式替换包名/端口/数据库名)
- README + 项目文档
