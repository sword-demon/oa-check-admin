## ADDED Requirements

### Requirement: Dingtalk-style flow designer canvas
系统 SHALL 提供钉钉式审批流程设计器，用业务节点方式编排审批流程。

#### Scenario: Open flow designer
- **WHEN** 管理员进入审批模板流程设计步骤
- **THEN** 系统展示开始节点、流程节点画布、结束节点和节点属性面板

#### Scenario: Add approval node
- **WHEN** 管理员在流程中添加审批节点
- **THEN** 系统创建可命名的审批节点
- **AND** 节点属性面板允许配置审批人策略

#### Scenario: Add cc node
- **WHEN** 管理员在流程中添加抄送节点
- **THEN** 系统创建可命名的抄送节点
- **AND** 节点属性面板允许配置抄送人策略

### Requirement: Assignee configuration
系统 SHALL 支持审批节点配置常用审批人策略。

#### Scenario: Configure fixed users
- **WHEN** 管理员选择指定成员审批
- **THEN** 系统允许选择一个或多个用户
- **AND** 保存后的节点配置包含固定用户 ID 列表

#### Scenario: Configure department leader
- **WHEN** 管理员选择部门负责人审批
- **THEN** 系统将审批人解析策略保存为部门负责人
- **AND** 流程运行时根据发起人部门解析审批人

#### Scenario: Configure role approvers
- **WHEN** 管理员选择角色审批
- **THEN** 系统允许选择一个角色
- **AND** 流程运行时将该角色下用户作为候选审批人或多实例审批人

#### Scenario: Configure expression approver
- **WHEN** 管理员选择表达式审批
- **THEN** 系统允许输入受控表达式
- **AND** 发布校验必须验证表达式非空且格式合法

### Requirement: Branch and condition configuration
系统 SHALL 支持基于动态表单字段配置条件分支。

#### Scenario: Add exclusive branch
- **WHEN** 管理员添加条件分支节点
- **THEN** 系统创建至少两个条件分支出口
- **AND** 每个出口可配置分支名称和条件规则

#### Scenario: Configure condition from form field
- **WHEN** 管理员为分支选择表单字段、操作符和值
- **THEN** 系统保存结构化条件配置
- **AND** 发布时生成对应 Flowable 条件表达式

#### Scenario: Require default branch
- **WHEN** 条件分支存在多个出口
- **THEN** 系统要求至少一个出口标记为默认分支
- **AND** 未命中其他条件时流程进入默认分支

### Requirement: BPMN generation
系统 SHALL 将钉钉式业务节点模型转换为 Flowable 可部署的 BPMN XML。

#### Scenario: Generate BPMN from linear flow
- **WHEN** 流程包含开始、审批节点和结束节点
- **THEN** 系统生成包含 startEvent、userTask、sequenceFlow 和 endEvent 的 BPMN XML

#### Scenario: Generate BPMN from conditional flow
- **WHEN** 流程包含条件分支
- **THEN** 系统生成 exclusiveGateway 和带 conditionExpression 的 sequenceFlow

#### Scenario: Attach task listener
- **WHEN** BPMN XML 包含审批用户任务
- **THEN** 系统为用户任务注入审批任务创建监听器
- **AND** 运行时创建业务审批任务记录

### Requirement: Flow validation
系统 SHALL 在保存和发布前校验流程结构。

#### Scenario: Validate connected nodes
- **WHEN** 管理员点击发布
- **THEN** 系统校验开始节点有出口、结束节点有入口、审批节点有入口和出口

#### Scenario: Validate approval node assignee
- **WHEN** 审批节点未配置审批人策略
- **THEN** 系统阻止发布并提示具体节点名称

#### Scenario: Validate branch exits
- **WHEN** 条件分支少于两个出口或缺少默认分支
- **THEN** 系统阻止发布并提示条件分支配置不完整
