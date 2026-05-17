## Context

当前模板创建向导的流程设计步骤已经具备这些基础能力：

```text
ApprovalFlowDesigner.vue
  -> ApprovalFlowNode(next/branches/children legacy)
  -> generateApprovalFlowArtifacts()
  -> bpmn_xml + biz_process_node_config
  -> 后端发布校验
  -> Flowable deploy
```

`lowflow-design` 的设计器核心结构是：

```text
flowDesign/index.vue
  -> TreeNode.vue 递归渲染 node.next
  -> Node.vue 统一节点卡片和删除/激活/加号入口
  -> Add.vue 节点类型弹出选择器
  -> GatewayNode.vue 横向分支泳道
  -> panels/index.vue 右侧 Drawer 动态属性面板
```

本项目已经完成 `next` 链式模型兼容，因此本次重点是替换 UI 层和交互结构，不改变保存、发布和 Flowable 运行链路。

## Goals / Non-Goals

**Goals:**

- 将当前纵向线性卡片 UI 改为 lowflow 风格的递归节点树 UI。
- 主链节点通过 `next` 递归展示，条件分支通过横向泳道展示。
- 节点插入统一通过加号弹出选择器完成，新增节点插入到目标节点和原 `next` 之间。
- 节点属性从固定右侧 aside 调整为 Drawer 或等价抽屉面板，并按节点类型切换配置组件。
- 节点错误以节点卡片上的警告标记呈现，鼠标悬浮展示具体校验错误。
- 保持现有审批人策略、用户/角色选择、动态表单条件字段、BPMN XML 生成、节点配置保存和发布校验不变。

**Non-Goals:**

- 不引入 lowflow 的完整表单渲染器、AdvancedFilter、UnoCSS、全局 `svg-icon` 体系或 mock API。
- 不新增 lowflow 当前未被本项目支持的节点类型，例如 timer、notify、service。
- 不重写后端 converter 为 Java 版 lowflow converter；本项目继续由前端业务模型生成 Flowable BPMN XML。
- 不改变审批模板 API、数据库结构和 Flowable 运行时监听器。

## Decisions

### D1: 采用“适配 lowflow UI 模式”，不直接复制 lowflow 源码

选择：在 `oa-ui/src/components/approval/lowflow/` 下新增本项目专用组件，参考 lowflow 的组件边界和视觉结构，使用 Element Plus 与现有类型。

原因：
- lowflow 依赖 `AdvancedFilter`、`Render` 字段模型、`svg-icon`、`Recordable`、自定义 hook 等，不适合整包复制。
- 本项目已有动态表单字段结构和审批人策略，直接复制会产生大量桥接层。
- 组件化迁移可以控制风险，符合 KISS/YAGNI。

备选：直接把 `/tmp/lowflow-design/src/views/flowDesign` 复制进项目。该方案短期看快，但依赖补齐和类型适配成本高，且容易引入当前业务不支持的节点。

### D2: 以 `ApprovalFlowNode` 作为唯一业务模型

选择：继续使用现有 `ApprovalFlowNode`：

```text
start
  next -> approval | cc | exclusive | parallel | end
exclusive
  branches[] -> branch.children[0] -> next...
```

原因：
- 已经能生成 Flowable 可部署 XML 和 `NodeConfig[]`。
- 已经支持旧 `children` 草稿规范化。
- 避免再引入 lowflow 的 `ConditionNode`/`AssigneeNode` 类型体系导致双模型转换。

备选：完全切换到 lowflow 的 `FlowNode`/`ConditionNode` 类型。该方案会要求重写条件、审批人和 BPMN 生成映射，当前收益不足。

### D3: UI 组件按 lowflow 边界拆分

目标组件结构：

```text
oa-ui/src/components/approval/lowflow/
  LowflowApprovalDesigner.vue    # 容器、缩放、拖动滚动、错误映射、Drawer 状态
  nodes/
    FlowTreeNode.vue             # 递归渲染 node + node.next
    FlowNodeCard.vue             # 统一节点卡片、标题编辑、删除、错误提示
    FlowAddButton.vue            # 加号和节点类型选择
    FlowGatewayNode.vue          # 条件分支横向泳道
    FlowBranchNode.vue           # 分支头、默认出口、分支内 next 链入口
  panels/
    FlowNodeDrawer.vue           # 动态选择节点属性面板
    ApprovalNodePanel.vue
    CcNodePanel.vue
    ExclusiveNodePanel.vue
    BranchConditionPanel.vue
```

`ApprovalFlowDesigner.vue` 可以作为兼容入口保留，内部委托给新组件，减少上层模板向导改动。

### D4: 节点错误由校验结果映射到节点 ID

选择：保留 `validateApprovalFlow(root): string[]` 对外 API，同时新增 UI 内部错误映射方法，将可定位错误挂到节点或分支：

```text
validateApprovalFlow(root)
  -> string[] 给发布/下一步使用
validateApprovalFlowForDesigner(root)
  -> Record<nodeId, ErrorInfo[]> 给节点卡片显示
```

原因：
- 上层发布逻辑已经使用 `string[]`，不需要破坏。
- lowflow UI 需要节点上显示红色警告图标和 tooltip。

### D5: 条件分支 UI 先适配现有简单条件模型

选择：Drawer 内继续使用当前 `{ fieldKey, operator, value }` 条件编辑器，而不是引入 lowflow 的 `AdvancedFilter`。

原因：
- 本项目首版条件生成只支持简单字段表达式。
- 引入 `AdvancedFilter` 会同时引入新的字段模型和条件组语义，超出本次 UI 替换范围。

## Risks / Trade-offs

| 风险 | 缓解 |
| --- | --- |
| lowflow 视觉迁移后与现有保存逻辑不一致 | 保持 `v-model<ApprovalFlowNode>` 契约不变，所有 mutation 只改 `next/branches` |
| 分支横向布局在小屏溢出 | 画布提供横向滚动和缩放控制，移动端仍以可滚动为主 |
| 节点错误无法准确定位到分支 | 分支使用独立 branch id，并在 UI 中把分支作为可激活配置对象 |
| 直接删除节点造成链断裂 | 删除逻辑必须旁路当前节点：`parent.next = node.next`，分支入口删除时更新 `branch.children[0]` |
| 高级 BPMN 预览和业务设计器预览不一致 | 业务设计器保存的 XML 继续由 `generateApprovalFlowArtifacts()` 生成并补 BPMN DI |

## Migration Plan

1. 新增 lowflow 风格组件目录，先实现只读渲染和当前模型递归展示。
2. 接入节点新增、删除、标题编辑和 Drawer 激活。
3. 接入审批、抄送、条件分支属性面板，复用现有用户/角色 API 和表单字段解析。
4. 替换 `ApprovalFlowDesigner.vue` 内部实现，保持对外 props/emits 不变。
5. 增加前端单测覆盖链式插入、删除、分支渲染、错误映射和 BPMN 生成。
6. 运行模板向导手工验证：设计表单 -> 设计流程 -> 预览 -> 发布 -> 查看流程图。

回滚策略：保留旧 `ApprovalFlowDesigner.vue` 的上层入口契约；如新 UI 存在阻塞，可临时切回旧组件实现，数据模型和 BPMN 产物不需要回滚。

## Open Questions

- 是否需要完全还原 lowflow 的 timer、notify、service 节点入口，还是继续隐藏未支持节点。
- 条件分支是否在本次只保留简单条件，后续再单独引入条件组/AND/OR 高级过滤器。
