## Skill routing

When the user's request matches an available skill, invoke it via the Skill tool. When in doubt, invoke the skill.

Key routing rules:
- Product ideas/brainstorming -> invoke /office-hours
- Strategy/scope -> invoke /plan-ceo-review
- Architecture -> invoke /plan-eng-review
- Design system/plan review -> invoke /design-consultation or /plan-design-review
- Full review pipeline -> invoke /autoplan
- Bugs/errors -> invoke /investigate
- QA/testing site behavior -> invoke /qa or /qa-only
- Code review/diff check -> invoke /review
- Visual polish -> invoke /design-review
- Ship/deploy/PR -> invoke /ship or /land-and-deploy
- Save progress -> invoke /context-save
- Resume context -> invoke /context-restore

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.5, MyBatis-Plus 3.5.9 |
| Auth | Sa-Token 1.44 (session in Redis) |
| BPMN | Flowable 7.2 |
| DB | MySQL 8.0, Flyway migrations |
| Frontend | Vue 3, TypeScript 5.8, Element Plus, Pinia, Vite 6 |
| BPMN Designer | bpmn-js |
| Testing (FE) | Vitest |
| Deploy | Docker Compose |

## Project Structure

Multi-module Maven monorepo with Vue 3 SPA frontend:

- `oa-app/` — Spring Boot entry point, Flyway migrations (V1-V8), BPMN processes
- `oa-common/` — BaseEntity, R<T> response wrapper, ErrorCode enum, GlobalExceptionHandler, enums, utils
- `oa-system/` — RBAC: User, Role, Permission, Department, Auth controllers/services
- `oa-approval/` — Approval workflow: Template, Instance, Task, CC, Audit, Notification, ProcessDeploy
- `oa-leave/` — Leave request module (business module example, generated from YAML)
- `oa-generator/` — YAML-driven code generator (entity/mapper/service/controller/dto/vo/enums)
- `oa-ui/` — Vue 3 SPA (views, api, stores, router, components, composables)
- `generators/` — YAML definitions for code generation

## Java Service 层规范

- 所有 Service 类必须先定义 interface, 再由对应的 `ServiceImpl` 实现类来实现
- 接口放在 `service/` 包下, 实现类放在 `service/impl/` 包下
- 命名规范: 接口 `XxxService`, 实现类 `XxxServiceImpl`
- Controller 和其他 Service 依赖注入时, 类型声明为接口而非实现类
- 对于继承 MyBatis-Plus `ServiceImpl<Mapper, Entity>` 的类, impl 类继续继承, interface 定义业务方法签名

## 业务字面量规范

- 业务代码中不得直接出现具有业务含义的字符串、数字、状态码、类型码、错误码等字面量
- 所有业务字面量必须通过以下方式统一定义和引用:
  - **常量类**: 用于错误码、魔术数字等固定值 (如 `ApprovalStatus.APPROVED`)
  - **枚举**: 用于状态码、类型码等有限集合 (如 `enum ApprovalStatus { PENDING, APPROVED, REJECTED }`)
  - **配置文件**: 用于可调整的业务参数 (如阈值、超时时间)
  - **数据字典**: 用于动态管理的业务分类 (通过字典表和字典服务)
- 判断条件中禁止硬编码: `if (status == 1)` 应改为 `if (status == ApprovalStatus.APPROVED.getCode())`
- 错误消息禁止内联拼接: `throw new RuntimeException("审批已通过")` 应改为 `throw new BusinessException(ErrorCode.APPROVAL_ALREADY_APPROVED)`

## Code Conventions

### Backend

- **API paths**: `/api/v1/{module}/{resource}` — `system`, `approval`, `leave`, `admin`
- **Response**: `R<T>` envelope with `{code, msg, data, timestamp}`; code 200 = success
- **Pagination**: `PageResult<T>` wrapper with `{records, total, page, pageSize}`
- **Auth annotations**: `@SaCheckPermission("module:resource:action")` on controller methods
- **Entity**: extends `BaseEntity` (auto-filled `createdAt`/`updatedAt`, logical `deleted`)
- **Errors**: `BusinessException(ErrorCode.xxx)` thrown from services; `GlobalExceptionHandler` catches all
- **DTO/VO**: request DTOs in `dto/` package, response VOs in `vo/` package
- **Dependency injection**: constructor injection via Lombok `@RequiredArgsConstructor`

### Frontend

- **Composition API**: `<script setup lang="ts">` style
- **Auto-import**: Element Plus components and Vue APIs auto-imported via unplugin
- **API calls**: `oa-ui/src/api/*.ts` modules using axios
- **State**: Pinia stores in `oa-ui/src/stores/`
- **Router guard**: checks `localStorage.getItem('token')`, redirects to `/login` if missing
- **Proxy**: Vite dev server proxies `/api` → `http://localhost:8080`

## Build & Run

| Command | What it does |
|---------|-------------|
| `mvn spring-boot:run -pl oa-app` | Start backend (requires MySQL + Redis) |
| `cd oa-ui && npm run dev` | Start frontend dev server on :5173 |
| `mvn clean package -DskipTests` | Build backend JAR |
| `cd oa-ui && npm run build` | Build frontend for production |
| `cd oa-ui && npm test` | Run Vitest tests |
| `docker-compose up -d` | Full stack (MySQL + Redis + backend + frontend) |

## Adding a New Business Module

1. Create YAML spec in `generators/` (see `generators/leave-request.yaml` for reference)
2. Run `oa-generator` to produce entity/mapper/service/controller/dto/vo/enums
3. Add module to root `pom.xml <modules>`
4. Add mapper package to `OaAdminApplication.@MapperScan`
5. Create Flyway migration `V{n+1}__*.sql` in `oa-app/src/main/resources/db/migration/`
6. Add frontend views in `oa-ui/src/views/{module}/` and register in `router/index.ts`
