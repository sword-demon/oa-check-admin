## ADDED Requirements

### Requirement: Lowflow-style recursive flow canvas
系统 SHALL 在审批模板流程设计步骤中提供 lowflow 风格的递归流程画布，用业务节点方式展示 `start.next` 主链和条件分支链路。

#### Scenario: Render main next chain
- **WHEN** 管理员打开流程设计步骤
- **THEN** 系统 SHALL 按 `start -> next -> next` 顺序展示开始节点、业务节点和结束节点
- **AND** 节点之间 SHALL 显示纵向连接线和加号插入入口

#### Scenario: Render exclusive branches
- **WHEN** 流程包含条件分支节点
- **THEN** 系统 SHALL 以横向分支泳道展示每个分支出口
- **AND** 每个分支出口 SHALL 展示分支名称、默认出口状态和分支内节点链路

#### Scenario: Support scrollable large canvas
- **WHEN** 流程节点或分支数量超出可视区域
- **THEN** 系统 SHALL 允许管理员通过画布滚动查看完整流程

### Requirement: Lowflow-style node insertion and deletion
系统 SHALL 通过 lowflow 风格加号菜单完成节点插入，并保持链式流程结构连续。

#### Scenario: Insert node after current node
- **WHEN** 管理员在节点后的加号菜单选择审批节点、抄送节点或条件分支
- **THEN** 系统 SHALL 将新节点插入到当前节点和原下一个节点之间
- **AND** 原下一个节点 SHALL 成为新节点的 `next`

#### Scenario: Delete middle node
- **WHEN** 管理员删除非结束节点
- **THEN** 系统 SHALL 从链路中移除该节点
- **AND** 该节点的前置节点 SHALL 直接连接到该节点原 `next`

#### Scenario: Insert branch child node
- **WHEN** 管理员在条件分支出口内添加审批节点或抄送节点
- **THEN** 系统 SHALL 将该节点追加到该分支出口的节点链路中

### Requirement: Lowflow-style node card states
系统 SHALL 使用 lowflow 风格节点卡片展示节点状态、节点名称、节点摘要和校验错误。

#### Scenario: Edit node title inline
- **WHEN** 管理员点击节点标题编辑入口
- **THEN** 系统 SHALL 允许直接编辑节点名称
- **AND** 编辑结果 SHALL 同步到流程模型

#### Scenario: Show approval summary
- **WHEN** 节点类型为审批节点
- **THEN** 系统 SHALL 在节点卡片中展示当前审批人策略摘要

#### Scenario: Show validation warning
- **WHEN** 节点存在发布前校验错误
- **THEN** 系统 SHALL 在节点卡片上展示警告标记
- **AND** 管理员悬浮警告标记时 SHALL 看到该节点关联的错误信息

### Requirement: Lowflow-style property drawer
系统 SHALL 以抽屉面板方式编辑当前选中节点或分支的属性。

#### Scenario: Open node drawer
- **WHEN** 管理员点击审批节点、抄送节点或条件分支节点
- **THEN** 系统 SHALL 打开属性抽屉
- **AND** 抽屉内容 SHALL 按节点类型展示对应配置表单

#### Scenario: Configure approval assignee
- **WHEN** 管理员在审批节点抽屉中配置审批人策略
- **THEN** 系统 SHALL 支持现有指定成员、部门负责人、上级部门负责人、角色、发起人和表达式策略
- **AND** 保存后的模型 SHALL 保持现有 `assigneeType` 和 `assigneeConfig` 结构

#### Scenario: Configure branch condition
- **WHEN** 管理员在分支条件抽屉中配置非默认分支
- **THEN** 系统 SHALL 允许从动态表单字段中选择变量、操作符和值
- **AND** 保存后的模型 SHALL 保持现有 `{ fieldKey, operator, value }` 条件结构

### Requirement: Compatibility with publishing artifacts
系统 SHALL 在 UI 替换后继续生成现有发布产物，不改变审批模板发布 API 和运行时。

#### Scenario: Save draft after UI replacement
- **WHEN** 管理员保存使用 lowflow 风格设计器编辑的模板草稿
- **THEN** 系统 SHALL 保存可恢复的业务流程模型
- **AND** 系统 SHALL 生成 Flowable BPMN XML 和节点配置快照

#### Scenario: Publish flow after UI replacement
- **WHEN** 管理员发布使用 lowflow 风格设计器编辑的模板
- **THEN** 系统 SHALL 通过现有表单校验、流程校验和 Flowable 部署流程
- **AND** 发布成功后的流程定义 SHALL 可用于发起审批实例

#### Scenario: Load legacy draft
- **WHEN** 管理员打开旧版 `children` 结构草稿
- **THEN** 系统 SHALL 将旧结构规范化为 `next` 链式结构后展示
- **AND** 管理员保存后 SHALL 不丢失旧草稿中的节点、分支和审批人配置
