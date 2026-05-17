## Why

oa-leave 后端模块已完成 CRUD 全栈 (controller/service/mapper/entity/dto/vo/enums), 但前端缺少对应页面和路由, 用户无法通过 UI 操作请假. 同时, 请假申请仅停留在数据表层面, 未与 Flowable 审批引擎打通 — 创建请假后不会触发审批流, 无法走审批/驳回/撤回等流程. 代码生成器目前只生成 Java 后端和 Flyway 脚本, 新增业务模块时前端页面仍需手写.

## What Changes

- 新增请假管理前端页面: 列表/新建/编辑/详情, 注册路由和侧边栏菜单
- 请假申请提交时调用 `ApprovalService.submit()`, 将请假数据作为 formData 关联到审批实例
- 请假状态随审批流转自动更新 (草稿 → 审批中 → 已通过/已驳回)
- 审批驳回后支持重新编辑并重新提交
- 为 oa-generator 增加前端页面生成能力 (Vue 3 + Element Plus CRUD 页面模板)

## Capabilities

### New Capabilities

- `leave-frontend-pages`: 请假管理前端页面 — 列表、新建/编辑表单、详情查看, 包含路由注册和侧边栏菜单入口
- `leave-approval-workflow`: 请假申请与 Flowable 审批流的打通 — 提交时创建审批实例, 审批回调同步请假状态, 驳回后可重新提交
- `generator-frontend-extension`: 代码生成器扩展前端模板 — 根据 YAML spec 自动生成 Vue 3 + Element Plus 的 CRUD 页面、API 模块、路由配置

### Modified Capabilities

(无已有 capability 需修改)

## Impact

- **后端 oa-leave**: LeaveRequestService 新增 `submitForApproval()`、`onApprovalResult()` 方法; LeaveRequestController 新增提交审批端点; LeaveRequest 实体可能需增加 `approvalInstanceId` 字段
- **后端 oa-approval**: ApprovalService.submit() 已接受通用 formData, 无需修改核心逻辑; 可能需要审批回调/监听器机制通知业务模块
- **后端 oa-generator**: 新增 Velocity/Freemarker 前端模板 (列表页、表单页、API 模块、路由注册片段)
- **前端 oa-ui**: 新增 `views/leave/` 目录、`api/leave.ts`、路由条目、侧边栏菜单项
- **数据库**: V9 迁移脚本 — 给 `biz_leave_request` 表增加 `approval_instance_id` 列
