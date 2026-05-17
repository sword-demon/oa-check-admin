## ADDED Requirements

### Requirement: Leave request list page
系统 SHALL 提供请假申请列表页面, 展示当前用户的请假记录, 支持分页、按标题搜索、按类型和状态筛选.

#### Scenario: View leave request list
- **WHEN** 用户导航到 /leave 页面
- **THEN** 系统展示请假申请列表, 包含列: 标题, 请假类型, 开始时间, 结束时间, 状态, 操作
- **AND** 默认按创建时间倒序排列
- **AND** 每页显示 20 条记录

#### Scenario: Filter by leave type
- **WHEN** 用户在类型下拉框中选择 "年假"
- **THEN** 列表仅展示 leaveType = 1 的记录

#### Scenario: Filter by status
- **WHEN** 用户在状态下拉框中选择 "审批中"
- **THEN** 列表仅展示 status = 1 的记录

#### Scenario: Search by title
- **WHEN** 用户在搜索框输入关键字并搜索
- **THEN** 列表展示标题包含该关键字的记录 (模糊匹配)

### Requirement: Leave request create form
系统 SHALL 提供新建请假申请表单, 包含标题、请假类型、开始时间、结束时间、请假原因字段.

#### Scenario: Open create dialog
- **WHEN** 用户点击 "新建请假" 按钮
- **THEN** 系统弹出 el-dialog 表单, 包含必填字段: 标题, 请假类型, 开始时间, 结束时间
- **AND** 请假类型为下拉选择 (年假/病假/事假)
- **AND** 时间字段为日期时间选择器

#### Scenario: Submit with validation errors
- **WHEN** 用户未填写标题直接提交
- **THEN** 表单显示校验错误提示 "请输入申请标题"

#### Scenario: Successful creation
- **WHEN** 用户填写完整表单并点击 "提交审批"
- **THEN** 系统调用后端创建请假申请并提交审批
- **AND** 弹出成功提示
- **AND** 列表自动刷新, 新记录出现在顶部

### Requirement: Leave request detail view
系统 SHALL 提供请假申请详情查看功能, 展示完整的请假信息和关联的审批状态.

#### Scenario: View detail from list
- **WHEN** 用户点击列表中某条记录的 "查看" 按钮
- **THEN** 系统展示详情页面, 显示标题、类型、时间、原因、当前状态
- **AND** 如果有关联审批实例, 显示审批流程状态和审批历史

### Requirement: Leave request edit and resubmit
系统 SHALL 允许用户编辑被驳回的请假申请并重新提交.

#### Scenario: Edit rejected leave request
- **WHEN** 用户点击状态为 "已驳回" 的记录的 "编辑" 按钮
- **THEN** 系统打开编辑表单, 预填已有数据
- **AND** 用户修改后可点击 "重新提交"

#### Scenario: Resubmit edited request
- **WHEN** 用户修改被驳回的请假申请并点击 "重新提交"
- **THEN** 系统更新请假数据并重新提交审批
- **AND** 状态变为 "审批中"

### Requirement: Leave request sidebar menu entry
系统 SHALL 在侧边栏中添加 "请假管理" 菜单项, 位于审批管理菜单下方.

#### Scenario: Navigate via sidebar
- **WHEN** 用户点击侧边栏 "请假管理" 菜单
- **THEN** 路由导航到 /leave 页面
- **AND** 菜单项高亮显示

### Requirement: Leave request route registration
系统 SHALL 注册 /leave 路由到前端路由表.

#### Scenario: Direct URL access
- **WHEN** 用户在浏览器地址栏输入 /leave
- **THEN** 系统加载请假管理页面 (需已登录)
