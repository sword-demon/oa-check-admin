## Why

当前审批模板只能维护基础信息和 BPMN XML，业务人员新建模板时无法在同一入口完成表单字段设计、审批节点编排、审批人配置和发布校验。需要提供一套接近钉钉审批的低代码模板设计体验，让管理员可以直接创建可提交、可流转、可展示详情的审批模板。

## What Changes

- 新增审批模板创建向导，将“基础信息、动态表单、流程设计、预览发布”串成完整流程。
- 新增动态表单构建能力，支持文本、数字、日期、日期时间、下拉、单选、多选、文本域、附件等常用字段类型。
- 新增表单字段属性配置，支持字段标题、占位提示、必填、默认值、选项、校验规则和展示顺序。
- 增强钉钉式流程设计器，支持开始节点、审批节点、抄送节点、条件分支、并行分支和结束节点的可视化编排。
- 增强审批人配置，支持指定成员、部门负责人、上级部门负责人、角色、发起人本人和表达式等现有后端可解析策略。
- 发布模板时同时校验表单配置与流程配置，并将表单 schema、BPMN XML、节点配置作为同一模板版本的不可变发布快照。
- 审批实例详情根据模板表单 schema 动态渲染提交数据，避免每个业务审批都手写详情页。
- 不移除现有 BPMN XML 编辑和预览能力；新设计器生成的配置继续落到现有 `biz_process_template`、`biz_process_node_config` 和 Flowable 部署链路。

## Capabilities

### New Capabilities

- `dynamic-approval-form-builder`: 审批模板可视化表单构建、字段 schema 保存、表单预览与运行时渲染。
- `dingtalk-approval-flow-designer`: 钉钉式审批流程设计器、节点配置、条件分支、流程校验与发布。
- `approval-template-publishing`: 审批模板版本化发布，将动态表单与流程定义作为一致的发布快照。

### Modified Capabilities

- None

## Impact

- 后端: `oa-approval` 模板服务、模板发布逻辑、节点配置保存、审批实例详情查询。
- 数据库: `biz_process_template.form_config` 结构规范化，必要时新增表单 schema 相关字段或迁移；继续兼容现有模板数据。
- 前端: `oa-ui/src/views/approval/template`、流程设计器组件、动态表单构建器组件、审批详情动态表单渲染组件。
- 流程引擎: 继续使用 Flowable BPMN XML 部署，发布后按模板绑定的 process definition 启动流程。
- 权限: 复用现有审批模板新增、编辑、发布权限，必要时细分表单设计和流程设计操作权限。
