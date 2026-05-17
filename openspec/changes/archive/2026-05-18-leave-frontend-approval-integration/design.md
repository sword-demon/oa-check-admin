## Context

oa-leave 模块后端 CRUD 已由 oa-generator 从 `generators/leave-request.yaml` 生成完成, 包含完整的 controller/service/mapper/entity/dto/vo/enums. oa-approval 模块已有 `ApprovalService.submit(templateId, title, formData)` 通用审批提交接口, 以及 Flowable 任务监听器. 但两者之间没有集成 — 请假创建后停留在草稿状态, 不会触发审批流.

前端 oa-ui 已有 system (用户/角色/权限/部门)、approval (模板/待办/已办/抄送/实例) 等完整页面, 但没有 leave 相关页面.

## Goals / Non-Goals

**Goals:**

- 用户可通过前端 UI 完成请假申请的全生命周期: 填写 → 提交审批 → 审批人处理 → 状态同步
- 请假状态由审批引擎驱动, 非前端手动更新
- oa-generator 扩展后可从 YAML 自动生成前端 CRUD 页面, 未来新增业务模块时减少手写

**Non-Goals:**

- 不改造 oa-approval 核心引擎 (submit/approve/withdraw 等接口不变)
- 不实现请假日历视图或考勤统计
- 不实现会签/加签等高级审批模式 (已有 Flowable 示例但请假场景用不到)
- 代码生成器暂不生成 BPMN 流程定义, 仍需手写或使用设计器

## Decisions

### D1: 审批集成采用 service 层编排 (非事件驱动)

**选择**: 在 LeaveRequestService 中注入 ApprovalService, 提交时直接调用 `approvalService.submit()`.

**理由**: 请假和审批在同一事务内完成, 如果审批创建失败, 请假记录也应回滚. 事件驱动适合跨微服务场景, 本项目是单体多模块, 直接调用更简单可靠.

**备选**: Spring Event / Flowable ExecutionListener — 增加复杂度, 调试困难, 且 Flowable listener 在异步场景下可能脱离事务.

### D2: 业务表存储 approvalInstanceId 外键

**选择**: 给 `biz_leave_request` 表增加 `approval_instance_id BIGINT NULL` 列.

**理由**: 双向关联 — 请假记录可查到审批实例, 审批实例通过 formData 中的 leaveRequestId 可反查. status 同步不需要额外表.

**备选**: 独立关联表 `biz_leave_approval_relation` — 过度设计, 请假和审批是 1:1 关系, 外键足够.

### D3: 审批回调通过 Flowable TaskListener + Service 调用

**选择**: 在审批流程的结束事件 (End Event) 上注册 TaskListener, 当审批通过/驳回时回调 LeaveRequestService 更新状态.

**理由**: 复用现有 Flowable 监听器机制, 无需引入新的消息队列. 流程结束时通过 ExecutionListener 触发业务回调.

**备选**: 前端轮询审批状态 — 浪费资源, 延迟高; 定时任务扫描 — 复杂且不实时.

### D4: 前端页面结构对齐已有 system 模块模式

**选择**: 请假列表页使用 `el-table` + 分页, 表单用 `el-dialog` 弹窗, 状态用 `el-tag` 色彩区分, 与 system/user 页面保持一致.

**理由**: 降低用户学习成本, 维护统一的视觉语言. 与 Element Plus auto-import 配合, 无需额外引入.

### D5: 代码生成器前端模板使用 Velocity

**选择**: 在 oa-generator 的 templates/ 目录下新增 Vue 3 模板文件 (列表页、表单、API、路由片段).

**理由**: 后端代码生成已使用 Velocity 模板引擎, 前端模板复用同一引擎, 不引入新技术栈. 模板变量从 YAML spec 中已有的 entities/fields 定义直接读取.

**备选**: Handlebars / EJS — 引入额外依赖, 且后端已有 Velocity.

## Risks / Trade-offs

- **[审批提交事务一致性]** → LeaveRequest 创建和 ApprovalService.submit() 在同一事务中, 如果 Flowable 部署失败会回滚请假记录. 使用 `@Transactional` 保证.
- **[审批回调事务边界]** → Flowable ExecutionListener 在独立事务中执行, 如果更新请假状态失败, 审批已完成但请假状态未同步. 缓解: 回调方法加 `@Transactional` + 失败重试日志.
- **[前端生成器通用性有限]** → 生成的 CRUD 页面覆盖 80% 场景, 特殊业务逻辑 (如请假关联审批) 仍需手写. 这是可接受的 — 生成器目标是减少重复劳动, 不是消除所有手写.
- **[请假 BPMN 流程需预置]** → 需要预先通过 Flow Designer 创建请假审批模板并发布, 前端提交时引用 templateId. 如果模板不存在, 提交会失败. 缓解: 在 seed data 中预置请假模板.

## Migration Plan

1. **V9 迁移**: `ALTER TABLE biz_leave_request ADD COLUMN approval_instance_id BIGINT NULL`
2. **后端部署**: 新增方法向后兼容, 现有 CRUD 不受影响
3. **前端部署**: 新增页面和路由, 不影响现有页面
4. **BPMN 模板**: 通过 Flow Designer 创建或导入 `leave_request.bpmn20.xml` 并发布
5. **回滚**: 删除前端路由/页面, 后端新方法不影响旧接口, V9 迁移可通过 `ALTER TABLE ... DROP COLUMN` 回滚
