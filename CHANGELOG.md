# Changelog

All notable changes to this project will be documented in this file.

## [0.3.0.0] - 2026-05-16

### Added

**可视化 BPMN 流程设计器 (Phase 2 Frontend)**
- bpmn-js 18.x 集成: 可视化拖拽设计审批流程图
- Flowable Moddle 扩展: TaskListener/ExtensionElements/InOutBinding 类型定义
- Flowable 命名空间属性透传 (assigneeType, assigneeConfig, multiInstanceType)
- BPMN 工具函数: generateDefaultXml, extractNodeConfigs, validateProcess, injectTaskListeners
- 模板 API 层: getTemplateXml, saveTemplateXml, getNodeConfigs, saveNodeConfigs, publishTemplate, createNewVersion
- 设计器页面三栏布局: 工具栏 + 画布 + 属性面板
- BpmnCanvas 组件: 支持 Modeler/Viewer 双模式, 容器自适应
- DesignerToolbar: 保存/发布/撤销/重做/缩放/XML预览/新建版本
- 属性面板: 开始/结束节点, 用户任务, 网关条件, 审批人, 多实例配置
- AssigneeConfig: 6 种审批人类型 (固定/部门主管/向上主管/角色/发起人/表达式) + UEL 自动生成
- MultiInstanceConfig: 普通/会签/或签 + 完成比例滑块 + 自动完成条件
- ConditionEditor: UEL 条件表达式编辑 + 表单字段下拉提示
- GatewayProperties: 出口线列表 + 每条线独立条件配置
- 钉钉风格 CSS 覆盖: 中文标签, 彩色元素 (绿开始/红结束/蓝任务/黄网关)
- 模板列表增强: 设计/查看按钮, 发布确认, 新建版本, 状态标签, 分页
- 已发布模板自动切换为只读 BpmnViewer 模式
- 30 秒自动保存 (防抖) + 页面离开未保存警告
- 发布前流程校验 (开始/结束/连接/网关出口)

**测试**
- bpmn-utils.test.ts: 19 tests (generateDefaultXml, extractNodeConfigs, validateProcess, injectTaskListeners)
- constants.test.ts: 7 tests (node types, assignee types, labels, status)
- template.test.ts: 9 tests (all API functions)
- bpmn-composables.test.ts: 13 tests (selection, commandStack, modeler initial state)
- Total: 67 tests (18 pre-existing + 49 new)

### Changed

- loadTemplateInfo 改用 getTemplate(id) 替代 getTemplates() 全量拉取
- handleSave 返回 boolean 标记保存是否成功, handlePublish 检查后再发布
- getTemplates API 新增分页参数 { page, size }

## [0.2.0.0] - 2026-05-15

### Added

**高级审批流模式 (Phase 2A/2B)**
- 条件分支: BPMN 排他网关 + UEL 表达式驱动的动态路由
- 并行网关: 多任务同时执行, 全部完成才继续
- 会签 (Countersign): 多人审批, 全部通过才算通过
- 或签 (Or-Sign): 多人审批, 一人通过即算通过
- 动态审批人: 支持固定人员/部门主管/角色/发起人自选
- 模板版本管理: 发布/取消发布/基于已发布版本创建新草稿
- Flowable 多实例任务自动检测 (TaskType: 普通/会签/或签)
- Flyway 迁移 V4 (节点配置表 + 模板版本字段)

**代码标准化 (Phase 3)**
- 业务常量提取: ApprovalConstants, FlowableConstants, TreeConstants
- 枚举体系: ApprovalInstanceStatus, ApprovalTaskResult, ApprovalTaskType, TemplateStatus, CommonStatus, PermissionType, DataScope
- Service 接口/实现分离: 所有 Service 提取接口 + ServiceImpl 实现类
- HikariCP 连接池配置

### Fixed

- 所有审批端点添加 `@SaCheckPermission` 权限注解
- 提交审批时校验模板必须为 PUBLISHED 状态
- 审批任务创建监听器增加 assignee null 检查
- Controller 参数校验 (templateId, result, bpmnXml)
- BPMN XML 保存委托到 Service 层, 禁止已发布模板直接修改 XML

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
