# OA Check Admin

RBAC 权限管理 + 审批流引擎后台管理平台模板. Fork 后运行一条 `init.sh` 脚本即可得到完整的权限 + 审批底座.

## 技术栈

| 层 | 技术 | 版本 |
|---|------|------|
| 后端 | Java + Spring Boot | 21 + 3.5.x |
| ORM | MyBatis-Plus | 3.5.x |
| 权限 | Sa-Token | 1.44.0 |
| 审批引擎 | Flowable | 7.2.0 OSS |
| 数据库迁移 | Flyway | latest |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis | 7.x |
| 前端 | Vue 3 + TypeScript + Vite | 6.x |
| UI | Element Plus | latest |
| 状态管理 | Pinia | 3.x |

## 快速开始

### 前置依赖

- macOS / Linux (Windows 通过 WSL2)
- Java 21+ (`JAVA_HOME` 配置正确)
- Node.js 18+
- Docker + Docker Compose

### 一键启动

```bash
# 1. Fork 并 clone 本仓库
git clone https://github.com/YOUR_USERNAME/oa-check-admin.git
cd oa-check-admin

# 2. 运行初始化脚本 (交互式配置包名/端口/数据库名)
./init.sh

# 3. 启动所有服务
docker compose up -d

# 4. 访问系统
# 前端: http://localhost:80
# 后端: http://localhost:8080
# 默认账号: admin / admin123 (首次登录建议修改密码)
```

## 项目结构

```
oa-check-admin/
├── pom.xml                          # Maven 父 POM (依赖版本锁定)
├── oa-common/                       # 公共模块 (BaseEntity, 统一响应, 异常处理)
├── oa-system/                       # 系统管理模块 (RBAC: 用户/角色/权限/部门)
├── oa-approval/                     # 审批模块 (Flowable 引擎集成)
├── oa-app/                          # 启动模块 (配置, Flyway 迁移, 入口)
├── oa-ui/                           # 前端 (Vue 3 + Element Plus)
├── docker-compose.yml               # 一键部署 (MySQL + Redis + Backend + Frontend)
├── init.sh                          # 项目初始化脚本
└── README.md
```

## 核心功能 (Phase 1)

### RBAC 权限管理
- 用户管理 (CRUD + 角色分配)
- 角色管理 (CRUD + 权限分配)
- 权限管理 (菜单/按钮/API 三级权限树)
- 部门管理 (树形组织架构)
- 数据权限 (全部/本部门/自定义)

### 审批流
- 审批模板管理 (表单字段 JSON 配置)
- 发起审批 / 我的申请
- 我的待办 (通过/驳回 + 审批意见)
- 我的已办 (历史记录)
- 抄送记录
- 撤回 (仅限第一个审批人未处理时)

### 安全
- Sa-Token 会话管理 (单设备登录)
- 接口级权限校验 (`@SaCheckPermission`)
- 角色级权限校验 (`@SaCheckRole`)
- BCrypt 密码加密
- 审计日志

## API 风格

- RESTful, 统一前缀 `/api/v1/`
- 认证: Header `satoken: {token_value}`
- 响应体: `{ "code": 200, "msg": "success", "data": {...}, "timestamp": 1234567890 }`
- 分页: `{ "code": 200, "data": { "list": [...], "total": 100, "page": 1, "pageSize": 20 } }`

## 页面路由

```
/login                          登录页
/dashboard                      仪表盘 (待办统计, 审批动态)
/system/user                    用户管理
/system/role                    角色管理
/system/permission              权限管理
/system/dept                    部门管理
/approval/template              审批模板管理
/approval/my-apply              我的申请
/approval/my-todo               我的待办
/approval/my-done               我的已办
/approval/cc                    抄送给我的
```

## 数据模型

### RBAC 五表
`sys_user` / `sys_dept` / `sys_role` / `sys_permission` + 关联表 (`sys_user_role`, `sys_role_permission`, `sys_role_dept`)

### 审批流
`biz_process_template` / `biz_approval_instance` / `biz_approval_task` / `biz_approval_cc` / `biz_audit_log`

## 环境变量

后端支持通过环境变量覆盖配置 (适合 Docker 部署):

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SPRING_DATASOURCE_URL` | 数据库连接 | `jdbc:mysql://localhost:3306/oa_admin` |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户 | `root` |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | `root123` |
| `SPRING_DATA_REDIS_HOST` | Redis 地址 | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis 端口 | `6379` |

## Phase 2 计划

- 可视化流程设计器 (bpmn-js 定制, 钉钉风格)
- 条件分支 / 并行网关 / 会签 / 或签
- 动态候选人 (从组织架构选择)
- 低代码表单设计器

## Phase 3 计划

- CLI 代码生成器 (YAML/DSL → 全套代码)
- 参考 JHipster JDL 思路, 专注审批 + 权限领域

## License

MIT
