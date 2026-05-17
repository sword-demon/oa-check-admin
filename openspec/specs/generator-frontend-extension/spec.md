## ADDED Requirements

### Requirement: Generate Vue 3 list page template
oa-generator SHALL 根据 YAML spec 中的 entity 定义自动生成 Vue 3 + Element Plus 列表页面.

#### Scenario: Generate list page for entity with searchable fields
- **WHEN** YAML spec 中定义了 entity 且部分 fields 标记为 `searchable: true`
- **THEN** 生成的列表页包含 el-table 展示所有字段列
- **AND** 搜索区域包含 searchable 字段对应的筛选控件 (文本用 el-input, 枚举用 el-select)
- **AND** 包含分页组件 el-pagination

#### Scenario: Generate list page for entity without searchable fields
- **WHEN** YAML spec 中定义了 entity 但没有 searchable 字段
- **THEN** 生成的列表页仅包含 el-table 和分页, 无搜索区域

### Requirement: Generate Vue 3 form dialog template
oa-generator SHALL 根据 YAML spec 中的 entity 字段类型生成对应的表单控件.

#### Scenario: Generate form for various field types
- **WHEN** YAML spec 定义了包含 String, Integer, LocalDateTime, TEXT 类型字段的 entity
- **THEN** String 字段生成 el-input, Integer (枚举) 字段生成 el-select, LocalDateTime 字段生成 el-date-picker, TEXT 字段生成 el-input type="textarea"

### Requirement: Generate API module
oa-generator SHALL 生成前端 API 模块文件, 封装对应后端 CRUD 接口的 axios 调用.

#### Scenario: Generate API module for entity
- **WHEN** YAML spec 定义了 module 名和 entity
- **THEN** 生成 `api/{module}.ts` 文件, 导出 list, getById, create, update, delete 函数
- **AND** 每个函数正确调用 `/api/v1/{module}/{entity_table_name}` 端点

### Requirement: Generate route configuration snippet
oa-generator SHALL 生成路由配置片段, 可直接插入到 router/index.ts 中.

#### Scenario: Generate route entry
- **WHEN** YAML spec 定义了 module 名
- **THEN** 生成路由对象片段, path 为 `/{module}`, component 指向生成的列表页面
- **AND** 包含 meta.title 使用 entity 的中文 comment

### Requirement: Generator CLI integration
oa-generator SHALL 通过 Maven profile 或 main 方法执行前端代码生成.

#### Scenario: Run generator for frontend artifacts
- **WHEN** 开发者运行 oa-generator 并传入 YAML spec 路径
- **THEN** 生成器读取 YAML spec, 在 oa-ui/src/ 下生成对应的 view 文件, api 文件, 并输出路由片段到控制台
- **AND** 生成文件路径遵循 oa-ui 现有目录约定 (views/{module}/, api/{module}.ts)
