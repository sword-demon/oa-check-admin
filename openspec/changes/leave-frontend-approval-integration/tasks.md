## 1. Database Migration

- [ ] 1.1 Create V9__leave_approval_integration.sql — 给 biz_leave_request 添加 approval_instance_id BIGINT NULL 列
- [ ] 1.2 Create V10__leave_template_seed.sql — 插入请假审批模板种子数据到 biz_process_template 表 (含 BPMN XML 引用 leave_request)

## 2. Backend: Leave-Approval Integration

- [ ] 2.1 LeaveRequest 实体增加 approvalInstanceId 字段
- [ ] 2.2 LeaveRequestVO 增加 approvalInstanceId 字段
- [ ] 2.3 LeaveRequestCreateDTO 移除 status 和 applicantUserId 字段 (由后端自动填充)
- [ ] 2.4 LeaveRequestService 接口新增 submitForApproval(Long id) 和 resubmit(Long id, LeaveRequestUpdateDTO dto) 方法
- [ ] 2.5 LeaveRequestServiceImpl 注入 ApprovalService, 实现 submitForApproval: 查找已发布的请假模板 → 调用 approvalService.submit() → 回写 approvalInstanceId → 状态改为审批中
- [ ] 2.6 LeaveRequestServiceImpl 实现 resubmit: 校验状态为已驳回 → 更新请假数据 → 调用 submitForApproval
- [ ] 2.7 LeaveRequestController 新增 POST /api/v1/leave/leave_request/{id}/submit 端点 (权限 leave:leave_request:submit)
- [ ] 2.8 LeaveRequestController 新增 POST /api/v1/leave/leave_request/{id}/resubmit 端点 (权限 leave:leave_request:edit)
- [ ] 2.9 LeaveRequestController 的 create 方法默认设置 status=0 (草稿), applicantUserId 从 Sa-Token 会话获取
- [ ] 2.10 新增 LeaveApprovalCallbackListener (Flowable ExecutionListener), 审批结束时根据 formData 中的 leaveRequestId 更新请假状态
- [ ] 2.11 LeaveRequestServiceImpl.update 中, 创建审批时自动填充 applicantUserId 为当前登录用户 (从 StpUtil 获取)

## 3. Frontend: Leave API Module

- [ ] 3.1 创建 oa-ui/src/api/leave.ts — 封装 leave CRUD + submitForApproval + resubmit 的 axios 调用

## 4. Frontend: Leave Pages

- [ ] 4.1 创建 oa-ui/src/views/leave/index.vue — 请假列表页 (el-table + 搜索/筛选 + 分页)
- [ ] 4.2 列表页包含状态标签渲染: 草稿(info), 审批中(warning), 已通过(success), 已驳回(danger), 已取消(info)
- [ ] 4.3 列表页操作列: 查看(所有状态), 编辑(仅已驳回), 提交审批(仅草稿), 重新提交(仅已驳回)
- [ ] 4.4 创建 oa-ui/src/views/leave/components/LeaveFormDialog.vue — 新建/编辑表单弹窗 (el-dialog + el-form)
- [ ] 4.5 表单字段: 标题(el-input), 请假类型(el-select 年假/病假/事假), 开始时间(el-date-picker datetime), 结束时间(el-date-picker datetime), 原因(el-input textarea)
- [ ] 4.6 表单校验: 标题必填, 类型必选, 开始/结束时间必选且结束 > 开始
- [ ] 4.7 创建 oa-ui/src/views/leave/components/LeaveDetailDialog.vue — 详情弹窗, 展示请假信息 + 关联审批状态/历史
- [ ] 4.8 注册路由: 在 router/index.ts 的 AdminLayout children 中添加 /leave 路由

## 5. Frontend: Sidebar Menu

- [ ] 5.1 在 AdminLayout.vue 侧边栏菜单中添加 "请假管理" 菜单项 (图标 Calendar, 路径 /leave)

## 6. Code Generator: Frontend Templates

- [ ] 6.1 在 oa-generator/src/main/resources/templates/ 下新增 vue/list.vue.vm — Vue 3 列表页模板
- [ ] 6.2 新增 vue/form-dialog.vue.vm — 表单弹窗模板
- [ ] 6.3 新增 vue/api.ts.vm — API 模块模板
- [ ] 6.4 新增 vue/route-snippet.ts.vm — 路由配置片段模板
- [ ] 6.5 更新 GeneratorEngine 或 GeneratorMain, 增加 --frontend 参数或自动检测并生成前端文件
- [ ] 6.6 验证: 使用 leave-request.yaml 运行生成器, 确认生成的前端文件结构正确且可直接使用

## 7. Integration Testing

- [ ] 7.1 验证请假提交审批端到端: 创建草稿 → 提交审批 → 查看审批实例 → 审批通过 → 请假状态同步
- [ ] 7.2 验证驳回重提: 提交审批 → 驳回 → 编辑 → 重新提交 → 状态正确
- [ ] 7.3 验证前端页面交互: 列表加载/筛选/分页/新建/编辑/查看详情
