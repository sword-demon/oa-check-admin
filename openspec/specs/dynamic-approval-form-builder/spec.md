## ADDED Requirements

### Requirement: Dynamic approval form schema
系统 SHALL 为审批模板提供结构化动态表单 schema，用于保存字段定义、校验规则和展示顺序。

#### Scenario: Save form schema
- **WHEN** 管理员在审批模板设计页保存动态表单
- **THEN** 系统保存包含 schema 版本、字段列表、字段属性和排序信息的 `form_config`
- **AND** 每个字段包含稳定唯一的 fieldKey

#### Scenario: Load existing form schema
- **WHEN** 管理员打开已有审批模板设计页
- **THEN** 系统根据 `form_config` 还原字段列表、字段属性和展示顺序

#### Scenario: Load legacy form config
- **WHEN** 模板的 `form_config` 为旧版 fields 结构
- **THEN** 系统以兼容模式加载字段
- **AND** 不阻塞管理员继续编辑并保存为新版 schema

### Requirement: Form field types
系统 SHALL 支持审批表单常用字段类型。

#### Scenario: Add basic field types
- **WHEN** 管理员添加文本、数字、日期、日期时间或文本域字段
- **THEN** 系统在表单画布中创建对应字段
- **AND** 属性面板展示该字段类型支持的配置项

#### Scenario: Add option field types
- **WHEN** 管理员添加下拉、单选或多选字段
- **THEN** 系统要求配置至少一个可选项
- **AND** 保存后的 schema 包含选项值和选项标签

#### Scenario: Add attachment field
- **WHEN** 管理员添加附件字段
- **THEN** 系统允许配置是否必填和最大上传数量
- **AND** 运行时表单按文件 URL 列表提交字段值

### Requirement: Field property configuration
系统 SHALL 允许管理员配置字段标题、提示、默认值、必填、校验规则和可见顺序。

#### Scenario: Configure required field
- **WHEN** 管理员将字段设置为必填
- **THEN** 运行时表单提交时必须校验该字段非空

#### Scenario: Configure numeric validation
- **WHEN** 管理员为数字字段配置最小值或最大值
- **THEN** 运行时表单提交时必须校验输入值在允许范围内

#### Scenario: Reorder fields
- **WHEN** 管理员拖拽调整字段顺序并保存
- **THEN** 审批发起表单和审批详情 SHALL 按保存后的顺序渲染字段

### Requirement: Form preview and runtime rendering
系统 SHALL 使用同一份表单 schema 渲染模板预览、审批发起表单和审批详情。

#### Scenario: Preview form during design
- **WHEN** 管理员点击表单预览
- **THEN** 系统展示与运行时一致的表单控件、默认值和必填标识

#### Scenario: Submit approval form data
- **WHEN** 用户基于动态表单发起审批
- **THEN** 系统按 schema 收集字段值并写入审批实例 `formData`
- **AND** 后端校验必填字段和基础字段类型

#### Scenario: Render approval detail
- **WHEN** 用户查看审批实例详情
- **THEN** 系统根据模板表单 schema 渲染字段标签和值
- **AND** 未定义在 schema 中的额外字段 SHALL 作为附加数据展示或折叠显示
