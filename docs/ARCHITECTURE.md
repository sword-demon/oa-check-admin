# OA Check Admin — 架构文档

> 版本: **0.3.0.0** · 最后更新: 2026-05-16

## 1. 架构概览

系统采用 **前后端分离 + Maven 多模块** 的单体架构，后端通过模块分层实现领域隔离，前端为 Vue 3 SPA 应用。审批引擎使用 Flowable 7.2.0，权限认证使用 Sa-Token 1.44.0。

```
┌─────────────────────────────────────────────────────┐
│                    Nginx (反向代理)                    │
│              oa-ui (Vue 3 SPA 静态资源)                │
└──────────────────────┬──────────────────────────────┘
                       │ /api/*
┌──────────────────────▼──────────────────────────────┐
│              Spring Boot 3.5.x 应用                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐ │
│  │ oa-system │ │oa-approval│ │ oa-leave │ │oa-gen  │ │
│  │  (RBAC)   │ │ (审批引擎) │ │ (请假示例)│ │(代码生成)│ │
│  └─────┬─────┘ └────┬─────┘ └────┬─────┘ └───┬────┘ │
│        └──────┬──────┘───────────┘            │      │
│         oa-common (公共基础设施)               │      │
└──────────────────────┬──────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   ┌─────────┐   ┌─────────┐   ┌──────────┐
   │ MySQL 8 │   │ Redis 7 │   │ Flowable │
   │ (主存储) │   │ (缓存)  │   │  (流程)   │
   └─────────┘   └─────────┘   └──────────┘
```

## 2. 技术栈

### 2.1 后端

| 层面       | 技术         | 版本      | 用途                |
| ---------- | ------------ | --------- | ------------------- |
| 运行时     | Java         | 17        | 编译目标            |
| 框架       | Spring Boot  | 3.5.x     | 应用框架            |
| ORM        | MyBatis-Plus | 3.5.9     | 数据访问 + 分页     |
| 认证       | Sa-Token     | 1.44.0    | 会话管理 + 权限校验 |
| 审批引擎   | Flowable     | 7.2.0 OSS | BPMN 流程执行       |
| 数据库迁移 | Flyway       | -         | 版本化 DDL/DML      |
| 工具库     | Hutool       | 5.8.34    | 通用工具            |
| 关系数据库 | MySQL        | 8.x       | 主数据存储          |
| 缓存       | Redis        | 7.x       | Sa-Token 会话存储   |

### 2.2 前端

| 层面      | 技术         | 版本  |
| --------- | ------------ | ----- |
| 框架      | Vue 3        | 3.5.x |
| 语言      | TypeScript   | 5.8   |
| 构建      | Vite         | 6.3.x |
| UI 组件库 | Element Plus | 2.9.x |
| 状态管理  | Pinia        | 3.x   |
| 路由      | Vue Router   | 4.5.x |
| HTTP      | Axios        | 1.9.x |
| BPMN      | bpmn-js      | 18.x  |
| 测试      | Vitest       | 4.x   |

## 3. Maven 模块架构

### 3.1 模块清单

```
oa-check-admin (父 POM，版本锁定)
├── oa-common        公共基础设施
├── oa-system        系统管理 (RBAC)
├── oa-approval      审批工作流
├── oa-leave         请假业务 (示例模块)
├── oa-generator     YAML 代码生成器
└── oa-app           启动入口 (装配 + 迁移)
```

### 3.2 模块依赖关系

```
oa-common ← oa-system ← oa-approval ← oa-app
    ↑            ↑            ↑
    └────────────┴──── oa-leave ← oa-app
    ↑
    └── oa-generator (独立工具模块)
```

依赖规则：

- `oa-common` 无内部依赖，被所有模块引用
- `oa-system` 仅依赖 `oa-common`
- `oa-approval` 依赖 `oa-common` + `oa-system`（需要用户/角色信息解析审批人）
- `oa-leave` 依赖 `oa-common` + `oa-approval`（业务模块接入审批流）
- `oa-generator` 仅依赖 `oa-common`，作为独立 CLI 工具运行
- `oa-app` 聚合所有模块，包含 Spring Boot 启动类和 Flyway 迁移脚本

## 4. 模块详细设计

### 4.1 oa-common — 公共基础设施

提供所有模块共享的基础能力：

```
com.oa.admin.common
├── config/          全局配置 (MyBatis-Plus, Jackson, Web)
├── constant/        公共常量
├── entity/          BaseEntity (id, createTime, updateTime, deleted)
├── enums/           CommonStatus 等通用枚举
├── event/           Spring Event 基础事件
├── exception/       BusinessException + GlobalExceptionHandler
├── result/          R<T> 统一响应, PageResult<T>, ErrorCode
└── util/            通用工具类
```

**关键设计：**

- `BaseEntity`: 统一 id / createTime / updateTime / deleted 字段，MyBatis-Plus 自动填充
- `R<T>`: 统一 API 响应包装器，`code` + `msg` + `data` + `timestamp`
- `ErrorCode`: 枚举化错误码，业务异常通过 `BusinessException(ErrorCode)` 抛出
- `GlobalExceptionHandler`: 统一捕获 Sa-Token 异常、业务异常、参数校验异常

### 4.2 oa-system — 系统管理 (RBAC)

实现基于角色的访问控制（RBAC）五表模型：

```
com.oa.admin.system
├── config/          SaTokenConfig, WebMvcConfig
├── controller/      AuthController, SysUserController, SysRoleController,
│                    SysPermissionController, SysDeptController
├── entity/          SysUser, SysRole, SysPermission, SysDept,
│                    SysUserRole, SysRolePermission, SysRoleDept
├── enums/           PermissionType, DataScope
├── mapper/          MyBatis-Plus Mapper 接口
└── service/         接口 + impl 实现类
```

**RBAC 数据模型：**

```
SysUser ──(N:N)── SysRole ──(N:N)── SysPermission
                      │
                      └──(N:N)── SysDept (数据权限)
```

- `SysPermission`: 支持菜单/按钮/API 三种类型，树形结构
- `SysRole`: 通过 `dataScope` 字段控制数据权限范围
- `SysDept`: 树形组织架构，`parentId` 自关联
- 认证流程: 登录 → Sa-Token 生成 token → Redis 存储 → 请求拦截校验

### 4.3 oa-approval — 审批工作流

集成 Flowable 引擎，实现完整的审批生命周期管理：

```
com.oa.admin.approval
├── config/          FlowableConfig (引擎配置)
├── constant/        ApprovalConstants, FlowableConstants
├── controller/      ApprovalController, AdminApprovalController,
│                    AdminAuditLogController, NotificationController
├── dto/             模板/实例/任务相关 DTO + 表单校验
│    └── form/       表单字段定义 (FormField, FormSchema)
├── entity/          BizProcessTemplate, BizApprovalInstance,
│                    BizApprovalTask, BizApprovalCc, BizAuditLog,
│                    BizNotification, BizProcessNodeConfig
├── enums/           ApprovalInstanceStatus, ApprovalTaskResult,
│                    ApprovalTaskType, TemplateStatus,
│                    ApprovalFormFieldType, NotificationType
├── listener/        ApprovalTaskCreateListener (任务创建回调),
│                    ProcessEndEventListener (流程完成回调)
├── mapper/          MyBatis-Plus Mapper 接口
├── resolver/        候选人解析器 (审批人策略 → Flowable assignee)
└── service/         接口 + impl 实现类
```

**核心流程：**

```
模板定义 → 发布部署 → 发起审批 → 流程实例启动
                                     │
                          ┌──────────▼──────────┐
                          │  Flowable 引擎执行    │
                          │  (TaskListener 同步)  │
                          └──────────┬──────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              ▼                      ▼                      ▼
         审批任务创建            抄送通知触发           流程完成回调
     (ApprovalTaskCreate    (ServiceTask)        (ProcessEndEvent
      Listener)                                   Listener)
              │                                            │
              ▼                                            ▼
     写入 biz_approval_task                        写入 biz_audit_log
     + biz_notification                           + 更新业务状态
```

**审批人解析策略：**

| 策略               | 配置         | 解析逻辑             |
| ------------------ | ------------ | -------------------- |
| `fixed`            | 用户 ID 列表 | 直接分配             |
| `deptLeader`       | 无           | 查询发起人部门负责人 |
| `upwardDeptLeader` | 层级数       | 向上 N 级部门负责人  |
| `role`             | 角色 ID      | 查询角色下的用户     |
| `initiator`        | 无           | 发起人本人           |
| `expression`       | UEL 表达式   | Flowable 表达式解析  |

### 4.4 oa-leave — 请假业务模块

作为业务模块的参考实现，展示如何接入审批框架：

```
com.oa.admin.leave
├── controller/      LeaveRequestController
├── dto/             LeaveRequestCreateDTO, LeaveRequestUpdateDTO, LeaveRequestQueryDTO
├── entity/          LeaveRequest
├── enums/           LeaveStatus, LeaveType
├── listener/        LeaveApprovalCallbackListener (审批结果回调)
├── mapper/          LeaveRequestMapper
├── service/         LeaveRequestService + impl
└── vo/              LeaveRequestVO
```

**与审批模块的集成：**

1. 发起请假时，调用 `ApprovalService` 提交审批
2. `LeaveApprovalCallbackListener` 监听审批结果事件
3. 审批通过/驳回后，自动更新请假单状态

### 4.5 oa-generator — 代码生成器

YAML 驱动的离线代码生成工具：

```
com.oa.admin.generator
├── parser/          YAML DSL 解析器
├── engine/          CodeGenerator (FreeMarker 模板引擎)
├── model/           YAML 数据模型
├── util/            文件操作工具
└── cli/             命令行入口
```

**生成模板：**

- `entity.java.ftl` → MyBatis-Plus 实体
- `mapper.java.ftl` → Mapper 接口
- `service.java.ftl` → Service 接口
- `serviceImpl.java.ftl` → Service 实现
- `controller.java.ftl` → REST Controller
- `migration.sql.ftl` → Flyway DDL

### 4.6 oa-app — 应用启动模块

Spring Boot 入口，负责装配和基础设施：

```
com.oa.admin
└── OaAdminApplication.java    @SpringBootApplication + @MapperScan

resources/
├── application.yml             应用配置
├── db/migration/               Flyway 迁移脚本 (V1 ~ V16)
└── processes/                  BPMN 流程定义文件
```

## 5. 数据库设计

### 5.1 数据库迁移

使用 Flyway 管理，共 16 个版本迁移脚本：

| 版本范围  | 内容                                                                                                                      |
| --------- | ------------------------------------------------------------------------------------------------------------------------- |
| V1        | 系统表建表 (`sys_user`, `sys_role`, `sys_permission`, `sys_dept` + 关联表)                                                |
| V2        | 审批业务表建表 (`biz_process_template`, `biz_approval_instance`, `biz_approval_task`, `biz_approval_cc`, `biz_audit_log`) |
| V3        | 种子数据 (管理员账号、基础角色权限)                                                                                       |
| V4        | Phase 2: 节点配置表 + 模板版本字段                                                                                        |
| V5 ~ V6   | 实例增强 + Bug 修复                                                                                                       |
| V7        | 通知表 (`biz_notification`)                                                                                               |
| V8 ~ V9   | 请假模块表 + 审批集成                                                                                                     |
| V10 ~ V12 | 请假模板种子数据 + BPMN 图                                                                                                |
| V13 ~ V16 | 实体基类字段 + 权限修复 + 唯一约束                                                                                        |

### 5.2 核心数据表

**RBAC 五表：**

| 表名                  | 说明                                                    |
| --------------------- | ------------------------------------------------------- |
| `sys_user`            | 用户表 (username, password, dept_id, status)            |
| `sys_role`            | 角色表 (roleCode, roleName, dataScope)                  |
| `sys_permission`      | 权限表 (树形: id, parentId, type, permissionCode, path) |
| `sys_dept`            | 部门表 (树形: id, parentId, deptName, leaderId)         |
| `sys_user_role`       | 用户-角色关联                                           |
| `sys_role_permission` | 角色-权限关联                                           |
| `sys_role_dept`       | 角色-部门关联 (自定义数据权限)                          |

**审批流程表：**

| 表名                      | 说明                                                                    |
| ------------------------- | ----------------------------------------------------------------------- |
| `biz_process_template`    | 审批模板 (name, templateKey, formConfig, bpmnXml, status, version)      |
| `biz_process_node_config` | 节点配置 (templateId, nodeId, nodeType, assigneeType, assigneeConfig)   |
| `biz_approval_instance`   | 审批实例 (templateId, initiatorId, formData, status, processInstanceId) |
| `biz_approval_task`       | 审批任务 (instanceId, assigneeId, result, comment, taskType)            |
| `biz_approval_cc`         | 抄送记录 (instanceId, ccUserId, read)                                   |
| `biz_audit_log`           | 审计日志 (instanceId, operatorId, action, detail)                       |
| `biz_notification`        | 站内通知 (userId, type, title, content, read, relatedId)                |

## 6. 前端架构

### 6.1 目录结构

```
oa-ui/src/
├── api/                API 请求层 (按模块拆分)
│   ├── auth.ts         认证 API
│   ├── system.ts       系统管理 API
│   ├── approval.ts     审批 API
│   ├── template.ts     模板 API
│   ├── leave.ts        请假 API
│   ├── notification.ts 通知 API
│   └── admin.ts        管理后台 API
├── assets/styles/      全局样式
├── bpmn/               BPMN 工具函数和常量
├── components/
│   └── approval/       审批相关组件
│       ├── ApprovalDynamicForm.vue     动态表单渲染
│       ├── ApprovalFlowDesigner.vue    流程设计器入口
│       ├── ApprovalFormDesigner.vue    表单设计器
│       └── lowflow/                    Lowflow 风格设计器
│           ├── LowflowApprovalDesigner.vue
│           ├── nodes/                  节点组件 (TreeNode, NodeCard, AddButton, Gateway)
│           └── panels/                 属性面板 (NodeDrawer)
├── composables/        组合式函数
│   ├── bpmn/           BPMN Modeler/Viewer 组合式封装
│   └── useNotification.ts  通知轮询
├── layouts/            布局组件 (AdminLayout 侧边栏)
├── router/             Vue Router 路由配置
├── stores/             Pinia 状态管理 (auth, notification)
├── styles/             SCSS 样式变量和混入
├── types/              TypeScript 类型定义
├── utils/              工具函数 (request, token, format)
└── views/              页面视图
    ├── login/          登录
    ├── dashboard/      仪表盘
    ├── system/         系统管理 (user, role, permission, dept)
    ├── approval/       审批管理 (template, designer, my-apply, my-todo, my-done, cc, instance)
    ├── admin/          管理后台 (audit-log, instances, metrics)
    ├── leave/          请假管理
    └── notification/   站内通知
```

### 6.2 关键设计

**请求层：**

- Axios 实例统一封装，自动注入 `satoken` Header
- 401 自动重定向到登录页
- 统一错误提示（ElMessage）

**认证流程：**

- 登录 → token 存入 `localStorage` → 路由守卫检查 token → 请求拦截器注入 Header

**流程设计器组件树：**

```
ApprovalFlowDesigner.vue (兼容入口)
  └── LowflowApprovalDesigner.vue
      ├── FlowTreeNode.vue          # 递归渲染 node + node.next
      │   ├── FlowNodeCard.vue      # 节点卡片 (标题/删除/错误提示)
      │   └── FlowAddButton.vue     # 加号菜单 (审批人/抄送/条件分支)
      ├── FlowGatewayNode.vue       # 条件分支横向泳道
      └── FlowNodeDrawer.vue        # 属性配置抽屉
```

**BPMN 双模式：**

- 设计态: `BpmnModeler` → 可拖拽编辑
- 查看态: `BpmnViewer` → 只读渲染

## 7. 编码规范

### 7.1 后端规范

| 规范       | 约定                                                                           |
| ---------- | ------------------------------------------------------------------------------ |
| Service 层 | 必须先定义接口 (`XxxService`)，再由实现类 (`XxxServiceImpl`) 实现              |
| 依赖注入   | 声明类型为接口，非实现类                                                       |
| ORM        | 继承 `ServiceImpl<Mapper, Entity>`，使用 `LambdaQueryWrapper`                  |
| 字面量     | 禁止硬编码，使用常量类 (`ApprovalConstants`) 或枚举 (`ApprovalInstanceStatus`) |
| 异常       | 使用 `BusinessException(ErrorCode)` 抛出，禁止 `new RuntimeException("...")`   |
| Controller | `@SaCheckPermission` 注解控制接口权限                                          |
| 数据库     | Flyway 版本化迁移，禁止手动修改表结构                                          |

### 7.2 前端规范

| 规范 | 约定                                                       |
| ---- | ---------------------------------------------------------- |
| 组件 | Vue 3 `<script setup>` + TypeScript                        |
| 状态 | Pinia stores，按领域拆分                                   |
| 请求 | 统一通过 `api/` 层调用，不直接使用 Axios                   |
| 类型 | 所有 API 数据结构定义 `types/` 目录                        |
| 命名 | 页面 `PascalCase`，组件 `PascalCase`，工具函数 `camelCase` |

## 8. 部署架构

### 8.1 Docker Compose 部署

```yaml
services:
  mysql:8.0       # 主数据库 (端口 13306)
  redis:7-alpine  # 缓存 + 会话存储 (端口 16379)
  backend         # Spring Boot JAR (端口 18080)
  frontend        # Nginx + 静态资源 (端口 180)
```

**启动依赖链：** `mysql (healthy)` → `redis` → `backend` → `frontend`

### 8.2 环境配置

| 变量                         | 默认值                                 | 说明       |
| ---------------------------- | -------------------------------------- | ---------- |
| `SPRING_DATASOURCE_URL`      | `jdbc:mysql://localhost:3306/oa_admin` | 数据库连接 |
| `SPRING_DATASOURCE_USERNAME` | `root`                                 | 数据库用户 |
| `SPRING_DATASOURCE_PASSWORD` | `root123`                              | 数据库密码 |
| `SPRING_DATA_REDIS_HOST`     | `localhost`                            | Redis 地址 |
| `SPRING_DATA_REDIS_PORT`     | `6379`                                 | Redis 端口 |

## 9. 测试策略

| 层级         | 工具                    | 范围                   |
| ------------ | ----------------------- | ---------------------- |
| 后端单元测试 | JUnit 5 + Mockito       | Service 层业务逻辑     |
| 前端单元测试 | Vitest + Vue Test Utils | API 层、工具函数、组件 |
| BPMN 测试    | Flowable Test           | 流程定义验证           |
| 集成测试     | Spring Boot Test        | 完整审批流程端到端     |

当前测试覆盖：前端 67 个测试用例（bpmn-utils, constants, template API, composables）。

## 10. 已知限制与技术债务

| 限制             | 当前状态                                 | 规划                 |
| ---------------- | ---------------------------------------- | -------------------- |
| 附件存储         | 仅保存文件 URL 列表，无独立文件服务      | 后续接入 OSS         |
| 条件组合         | 仅支持简单字段条件，不支持嵌套 OR        | 后续扩展条件模型     |
| 消息通知         | 仅站内通知，未接入邮件/IM                | 后续集成消息通道     |
| 代码生成器       | 仅生成后端代码                           | 后续支持前端页面生成 |
| 历史模板反向解析 | 仅新向导生成的 XML 可恢复为 Lowflow 模型 | 保留高级设计器兼容   |
| 并行分支         | 保留基础并行网关，复杂 UI 待扩展         | 后续完善             |
