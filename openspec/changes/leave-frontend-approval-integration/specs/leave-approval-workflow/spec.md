## ADDED Requirements

### Requirement: Submit leave request for approval
系统 SHALL 在创建请假申请时自动提交到 Flowable 审批引擎, 创建关联的审批实例.

#### Scenario: Submit new leave request
- **WHEN** 用户提交一个新请假申请
- **THEN** 系统在同一事务中创建 `biz_leave_request` 记录 (status=1 审批中) 和审批实例
- **AND** `biz_leave_request.approval_instance_id` 存储关联的审批实例 ID
- **AND** 审批实例的 formData 包含 leaveRequestId

#### Scenario: Submit without published leave template
- **WHEN** 系统中不存在已发布的请假审批模板
- **THEN** 提交失败, 返回 TEMPLATE_NOT_PUBLISHED 错误码

### Requirement: Approval callback syncs leave status
系统 SHALL 在审批流结束时自动更新关联请假申请的状态.

#### Scenario: Approval approved
- **WHEN** 审批实例的所有任务全部通过 (result=1)
- **THEN** 关联的 `biz_leave_request.status` 更新为 2 (已通过)

#### Scenario: Approval rejected
- **WHEN** 任一审批任务驳回 (result=2)
- **THEN** 关联的 `biz_leave_request.status` 更新为 3 (已驳回)

#### Scenario: Approval withdrawn
- **WHEN** 申请人撤回审批实例
- **THEN** 关联的 `biz_leave_request.status` 更新为 4 (已取消)

### Requirement: Leave request approval instance binding
系统 SHALL 在 `biz_leave_request` 表中存储审批实例关联.

#### Scenario: Database migration
- **WHEN** 应用启动并执行 Flyway 迁移
- **THEN** `biz_leave_request` 表新增 `approval_instance_id BIGINT NULL` 列

### Requirement: Resubmit rejected leave request
系统 SHALL 允许已驳回的请假申请重新编辑后再次提交审批.

#### Scenario: Resubmit after edit
- **WHEN** 用户编辑已驳回 (status=3) 的请假申请并重新提交
- **THEN** 系统更新请假数据, status 变为 1 (审批中)
- **AND** 创建新的审批实例 (旧的 approval_instance_id 被替换)

#### Scenario: Resubmit non-rejected request
- **WHEN** 用户尝试重新提交非驳回状态的请假申请
- **THEN** 系统拒绝操作并返回 CANNOT_WITHDRAW 错误

### Requirement: Leave status display labels
系统 SHALL 使用枚举值渲染请假状态的中文标签和颜色.

#### Scenario: Status label rendering
- **WHEN** 前端渲染请假列表或详情
- **THEN** status=0 显示 "草稿" (info), status=1 显示 "审批中" (warning), status=2 显示 "已通过" (success), status=3 显示 "已驳回" (danger), status=4 显示 "已取消" (info)

### Requirement: Leave approval template seed data
系统 SHALL 在数据库中预置请假审批模板, 使请假提交能找到对应模板.

#### Scenario: Template exists after migration
- **WHEN** Flyway 迁移执行完成
- **THEN** `biz_process_template` 表中存在一条名称为 "请假审批" 的已发布模板
- **AND** 关联的 BPMN XML 为 leave_request 流程定义
