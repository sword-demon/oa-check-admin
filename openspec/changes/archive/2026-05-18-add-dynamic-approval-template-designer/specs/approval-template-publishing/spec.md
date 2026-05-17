## ADDED Requirements

### Requirement: Template creation wizard
系统 SHALL 提供审批模板创建向导，将基础信息、动态表单、流程设计和发布确认组织为连续流程。

#### Scenario: Create template with wizard
- **WHEN** 管理员点击新建审批模板
- **THEN** 系统进入模板创建向导
- **AND** 管理员可以依次配置基础信息、动态表单和审批流程

#### Scenario: Save draft during wizard
- **WHEN** 管理员在向导任一步骤点击保存草稿
- **THEN** 系统保存当前模板基础信息、表单 schema 和流程配置
- **AND** 模板状态保持为草稿

#### Scenario: Resume draft editing
- **WHEN** 管理员打开未发布的模板草稿
- **THEN** 系统恢复上次保存的基础信息、表单 schema 和流程配置

### Requirement: Publish validation
系统 SHALL 在发布审批模板前同时校验表单配置和流程配置。

#### Scenario: Publish valid template
- **WHEN** 模板包含有效基础信息、至少一个有效表单字段和有效流程
- **THEN** 系统允许发布模板
- **AND** 发布后模板状态变为已发布

#### Scenario: Reject publish without form fields
- **WHEN** 模板没有任何可提交表单字段
- **THEN** 系统阻止发布并提示表单配置不完整

#### Scenario: Reject publish with invalid flow
- **WHEN** 模板流程存在断线节点、缺少审批人或条件分支不完整
- **THEN** 系统阻止发布并返回具体校验错误

### Requirement: Immutable published snapshot
系统 SHALL 将动态表单 schema、BPMN XML 和节点配置作为同一模板版本的发布快照。

#### Scenario: Publish snapshot
- **WHEN** 管理员发布模板
- **THEN** 系统固化当前表单 schema、BPMN XML 和节点配置
- **AND** 后续编辑必须通过新建版本进行

#### Scenario: Start instance with published version
- **WHEN** 用户基于已发布模板发起审批
- **THEN** 系统使用该模板版本绑定的 Flowable process definition 启动流程
- **AND** 审批实例记录该模板版本 ID

#### Scenario: View historical instance after new version
- **WHEN** 管理员发布同一模板 key 的新版本后用户查看旧审批实例
- **THEN** 系统仍按旧实例绑定的模板版本渲染表单和流程信息

### Requirement: Template version lifecycle
系统 SHALL 支持已发布模板创建新版本并保持历史版本可追溯。

#### Scenario: Create new version
- **WHEN** 管理员从已发布模板点击新建版本
- **THEN** 系统创建同一 templateKey 的草稿模板
- **AND** 新草稿版本号等于已发布版本号加一

#### Scenario: Edit new draft version
- **WHEN** 管理员编辑新建版本草稿
- **THEN** 系统允许修改基础信息、表单 schema 和流程配置
- **AND** 不改变原已发布版本

#### Scenario: Publish new version
- **WHEN** 管理员发布新版本草稿
- **THEN** 系统部署新的 Flowable process definition
- **AND** 新发起的审批使用新版本，历史审批不受影响

### Requirement: Submission API uses dynamic schema
系统 SHALL 支持通用审批发起接口根据模板动态表单 schema 接收和校验表单数据。

#### Scenario: Submit valid dynamic form
- **WHEN** 用户提交符合模板表单 schema 的审批数据
- **THEN** 系统创建审批实例并保存表单数据
- **AND** 流程变量包含可用于条件分支判断的字段值

#### Scenario: Submit missing required field
- **WHEN** 用户提交审批时缺少必填字段
- **THEN** 系统拒绝提交并返回字段级校验错误

#### Scenario: Submit unknown template
- **WHEN** 用户尝试基于不存在或未发布的模板发起审批
- **THEN** 系统拒绝提交并返回模板不可用错误
