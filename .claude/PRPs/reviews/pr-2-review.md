# PR Review: #2 — v0.2.0.0 feat: advanced approval flow patterns + code standards + security hardening

**Reviewed**: 2026-05-16
**Author**: sword-demon
**Branch**: feat/phase2-flow-designer → feat/phase1-project-scaffold
**Decision**: REQUEST CHANGES

## Summary
Phase 2A/2B 高级审批流 + Phase 3 代码标准化 + 安全修复. 94 tests 全绿, 编译通过. 但存在 2 个 CRITICAL 和 8 个 HIGH 问题需要修复后才能合并.

## Findings

### CRITICAL (2)
- ApprovalController:35 — Long.valueOf() / Integer.parseInt() 无 try-catch, 非法输入返回 500
- ApprovalTaskCreateListener:45 — Long.parseLong(assignee) 无异常处理, 可中断流程

### HIGH (8)
- ApprovalController — 6 个端点缺少 @SaCheckPermission (myTodo/myDone/instanceTasks/getNodeConfigs/myCc/markCcRead)
- ApprovalController — Map<String,Object> 接收参数, 无 DTO + @Valid 声明式校验
- ApprovalServiceImpl:90 — approve() 不校验 result 是否在 ApprovalTaskResult 枚举范围
- ApprovalServiceImpl:60-68 — formData putAll 到 Flowable 变量无白名单过滤, 变量注入风险
- SysRoleServiceImpl:55 — getById 返回 null 无检查, NPE
- ApprovalCcServiceImpl:26 — markRead() 不验证当前用户是否是抄送接收者, 可标记他人已读
- ApprovalCcServiceImpl:35 — ccUserIds 无 null 检查
- ApprovalServiceImpl:115-175 — myTodo/myDone/instanceTasks 无分页限制

### MEDIUM (7)
- 多处直接突变 MyBatis-Plus 实体对象, 违反不可变规则
- saveNodeConfigs 先删后插, 并发场景可读到空配置
- saveDraft 使用 saveOrUpdate, 语义不清晰
- createNewVersion 共享 templateKey, 版本管理风险
- withdraw Integer.valueOf().equals() 自动装箱写法不清晰
- 测试文件中仍使用字面量数字而非枚举常量
- createTemplate 接收实体对象暴露受保护字段

### LOW (4)
- buildTree 递归无深度限制, 循环引用风险
- createCc 逐条 insert 效率低
- injectBaseMapper 在 4 个测试类中重复定义
- AuditLogServiceImpl StpUtil 未登录异常未捕获

## Validation Results
| Check | Result |
|---|---|
| Compile | Pass |
| Tests (94) | Pass |
| Build | Pass |

## Files Reviewed
70 files: 20 Added, 46 Modified, 4 Config/Meta
