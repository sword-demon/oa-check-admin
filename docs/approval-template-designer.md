# 动态审批模板设计器

## 配置链路

```text
模板创建向导
  -> 基础信息: templateName / templateKey
  -> 动态表单: form_config
  -> 流程设计: 业务流程模型
  -> 生成产物: bpmn_xml + biz_process_node_config
  -> 发布快照: published_bpmn_xml + form_config + node_config
```

发布后的审批实例绑定具体 `process_template_id`，历史实例按当时的模板版本读取表单 schema 和流程定义。

## 表单 Schema

`form_config` 使用 JSON 保存，当前版本为 `1`。

```json
{
  "version": 1,
  "fields": [
    {
      "fieldKey": "leave_days",
      "label": "请假天数",
      "type": "number",
      "required": true,
      "min": 0.5,
      "max": 30,
      "sortOrder": 0
    }
  ]
}
```

| 字段 | 说明 |
| --- | --- |
| `fieldKey` | 稳定唯一字段标识，提交数据和条件分支都使用该值 |
| `label` | 表单展示名称 |
| `type` | 字段类型 |
| `required` | 是否必填 |
| `options` | 下拉、单选、多选的可选项 |
| `min` / `max` | 数字字段范围 |
| `maxFiles` | 附件字段最大 URL 数 |
| `sortOrder` | 展示顺序 |

支持字段类型：

| 类型 | 用途 |
| --- | --- |
| `text` | 单行文本 |
| `textarea` | 多行文本 |
| `number` | 数字、金额、天数 |
| `date` | 日期 |
| `datetime` | 日期时间 |
| `select` | 下拉单选 |
| `radio` | 单选 |
| `checkbox` | 多选 |
| `attachment` | 附件 URL 列表 |

## 流程节点

钉钉式设计器维护业务节点模型，保存草稿时生成 Flowable BPMN XML 和节点配置。流程模型参考 `lowflow-design` 的链式结构：普通节点通过 `next` 串联，条件节点通过 `branches` 持有出口，分支出口内的节点也以入口节点加 `next` 继续串联。

```json
{
  "id": "start",
  "type": "start",
  "name": "发起申请",
  "next": {
    "id": "approval_1",
    "type": "approval",
    "name": "审批",
    "assigneeType": "deptLeader",
    "next": {
      "id": "end",
      "type": "end",
      "name": "流程结束"
    }
  }
}
```

旧草稿中使用的 `children` 线性数组仍可读取，进入新设计器时会规范化为 `next` 链，BPMN 生成器也同时兼容两种结构。

## Lowflow 风格 UI

流程设计器 UI 采用 `lowflow-design` 的组件边界和交互模式，但不直接引入 lowflow 的完整源码依赖：

```text
ApprovalFlowDesigner.vue
  -> lowflow/LowflowApprovalDesigner.vue
    -> nodes/FlowTreeNode.vue       # 递归渲染 node 和 node.next
    -> nodes/FlowNodeCard.vue       # 节点卡片、标题编辑、删除、错误提示
    -> nodes/FlowAddButton.vue      # 加号菜单
    -> nodes/FlowGatewayNode.vue    # 条件分支横向泳道
    -> panels/FlowNodeDrawer.vue    # 属性抽屉
```

| UI 能力 | 当前行为 |
| --- | --- |
| 主链渲染 | 使用 `FlowTreeNode` 按 `next` 递归展示 |
| 节点插入 | 节点后的加号菜单支持审批人、抄送人、条件分支 |
| 条件分支 | 使用横向泳道展示分支出口和分支内节点链 |
| 属性配置 | 点击节点或分支后打开抽屉，按类型展示配置表单 |
| 校验反馈 | 发布前校验错误会映射到节点或分支，并在卡片上显示警告 |
| 大画布 | 画布支持滚动和缩放，分支较多时横向浏览 |

当前支持的 lowflow 风格节点入口：

| 入口 | 本项目节点类型 | 说明 |
| --- | --- | --- |
| 审批人 | `approval` | 使用现有审批人策略和任务监听器 |
| 抄送人 | `cc` | 保存为服务任务并生成可部署的 no-op implementation |
| 条件分支 | `exclusive` | 使用现有简单字段条件模型 |

暂不开放的 lowflow 节点入口：

| lowflow 节点 | 当前状态 |
| --- | --- |
| `timer` | 未接入运行时语义，暂不展示入口 |
| `notify` | 未接入消息通知服务，暂不展示入口 |
| `service` | 未开放任意服务调用配置，暂不展示入口 |

如需回滚 UI，`ApprovalFlowDesigner.vue` 是兼容入口；上层模板向导仍只依赖 `modelValue`、`formSchema`、`readonly` 和 `update:modelValue`，可在不迁移数据结构的情况下切回旧设计器壳。

| 节点类型 | BPMN 产物 | 说明 |
| --- | --- | --- |
| `start` | `startEvent` | 发起申请 |
| `approval` | `userTask` | 审批任务，注入 `approvalTaskCreateListener` |
| `cc` | `serviceTask` | 抄送节点 |
| `exclusive` | `exclusiveGateway` | 条件分支，至少两个出口且必须有默认出口 |
| `parallel` | `parallelGateway` | 并行网关首版保留基础节点 |
| `end` | `endEvent` | 流程结束 |

审批人策略：

| 策略 | 配置 |
| --- | --- |
| `fixed` | 指定用户 ID |
| `deptLeader` | 发起人部门负责人 |
| `upwardDeptLeader` | 指定层级的上级部门负责人 |
| `role` | 角色 ID |
| `initiator` | 发起人本人 |
| `expression` | 受控 UEL 表达式，例如 `${initiator}` |

条件分支使用 `{ fieldKey, operator, value }` 生成 Flowable 条件表达式。非默认分支必须配置条件，默认分支作为未命中兜底出口。

## 发布校验

发布前同时校验：

| 范围 | 校验 |
| --- | --- |
| 表单 | 至少一个字段、字段标识唯一、选项有效、数字范围有效 |
| 流程 | 开始和结束连通、审批节点有审批人、条件分支出口完整 |
| 版本 | 已发布版本不可直接修改，需新建版本 |
| 运行 | 发起审批使用模板版本绑定的 Flowable process definition ID |

## 已知限制

| 限制 | 当前行为 |
| --- | --- |
| 附件存储 | 首版只提交文件 URL 列表，未接入独立文件服务 |
| 高级表达式 | 仅校验表达式非空且形如 `${...}`，复杂安全白名单后续完善 |
| 条件组合 | 首版以简单字段条件为主，不支持嵌套 OR 条件组 |
| 并行分支 | 保留并行网关节点能力，复杂并行分支 UI 后续扩展 |
| 历史 BPMN 反向解析 | 只有新向导生成的 XML 内包含业务模型，可恢复钉钉式流程；手写 BPMN 仍通过高级设计器维护 |
