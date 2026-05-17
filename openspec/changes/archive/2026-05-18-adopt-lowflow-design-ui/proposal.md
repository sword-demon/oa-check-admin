## Why

当前审批流程设计器虽然已经使用业务节点模型，但 UI 仍是本项目自定义的纵向卡片列表，节点插入、分支展示、错误提示和属性面板体验与 `lowflow-design` 差距较大。现在已经完成 `next` 链式流程模型和 Flowable XML 生成兼容，具备将界面层切换到 `lowflow-design` 风格的基础。

## What Changes

- 将审批模板流程设计步骤的主画布改为 `lowflow-design` 风格：递归节点树、节点卡片、加号插入器、分支泳道、右侧属性抽屉。
- 以当前 `ApprovalFlowNode.next` 链式模型作为数据源，保留旧 `children` 草稿兼容入口。
- 将节点新增、删除、分支新增、默认条件、节点错误提示等交互调整为 lowflow 的操作方式。
- 复用本项目现有审批人策略、动态表单字段、Flowable BPMN 生成、发布校验和节点配置保存链路。
- 不引入 lowflow 全量后端或替换 Flowable；只迁移前端 UI 结构、交互模式和必要的适配组件。

## Capabilities

### New Capabilities

- `lowflow-style-flow-designer-ui`: 审批模板流程设计器提供 lowflow 风格的可视化编排界面，并与现有动态表单、节点配置和 Flowable 发布链路保持兼容。

### Modified Capabilities

- 无。

## Impact

- 主要影响 `oa-ui/src/components/approval/ApprovalFlowDesigner.vue` 及可能新增的 `oa-ui/src/components/approval/lowflow/` 子组件。
- 影响 `oa-ui/src/utils/approval-flow.ts` 的 UI 辅助方法，但不改变 `generateApprovalFlowArtifacts()` 对外产物结构。
- 影响审批模板创建向导 `oa-ui/src/views/approval/template/index.vue` 中流程设计步骤的展示体验。
- 可能新增针对设计器节点树、分支 UI 和 BPMN DI 生成的前端测试。
- 后端发布、Flowable 部署、表单 schema、审批运行时不做架构替换。
