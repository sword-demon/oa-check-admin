import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/',
      component: () => import('@/layouts/AdminLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '仪表盘' },
        },
        {
          path: 'system/user',
          name: 'UserManage',
          component: () => import('@/views/system/user/index.vue'),
          meta: { title: '用户管理' },
        },
        {
          path: 'system/role',
          name: 'RoleManage',
          component: () => import('@/views/system/role/index.vue'),
          meta: { title: '角色管理' },
        },
        {
          path: 'system/permission',
          name: 'PermissionManage',
          component: () => import('@/views/system/permission/index.vue'),
          meta: { title: '权限管理' },
        },
        {
          path: 'system/dept',
          name: 'DeptManage',
          component: () => import('@/views/system/dept/index.vue'),
          meta: { title: '部门管理' },
        },
        {
          path: 'approval/template',
          name: 'ApprovalTemplate',
          component: () => import('@/views/approval/template/index.vue'),
          meta: { title: '审批模板' },
        },
        {
          path: 'approval/template/designer/:id',
          name: 'FlowDesigner',
          component: () => import('@/views/approval/template/designer/index.vue'),
          meta: { title: '流程设计器' },
        },
        {
          path: 'approval/my-apply',
          name: 'MyApply',
          component: () => import('@/views/approval/my-apply/index.vue'),
          meta: { title: '我的申请' },
        },
        {
          path: 'approval/my-todo',
          name: 'MyTodo',
          component: () => import('@/views/approval/my-todo/index.vue'),
          meta: { title: '我的待办' },
        },
        {
          path: 'approval/my-done',
          name: 'MyDone',
          component: () => import('@/views/approval/my-done/index.vue'),
          meta: { title: '我的已办' },
        },
        {
          path: 'approval/cc',
          name: 'ApprovalCc',
          component: () => import('@/views/approval/cc/index.vue'),
          meta: { title: '抄送给我的' },
        },
        {
          path: 'approval/instance/:id',
          name: 'InstanceDetail',
          component: () => import('@/views/approval/instance/detail.vue'),
          meta: { title: '审批详情' },
        },
        {
          path: 'admin/audit-log',
          name: 'AdminAuditLog',
          component: () => import('@/views/admin/audit-log/index.vue'),
          meta: { title: '审计日志' },
        },
        {
          path: 'admin/instances',
          name: 'AdminInstances',
          component: () => import('@/views/admin/instances/index.vue'),
          meta: { title: '实例管理' },
        },
        {
          path: 'admin/metrics',
          name: 'AdminMetrics',
          component: () => import('@/views/admin/metrics/index.vue'),
          meta: { title: '流程指标' },
        },
        {
          path: 'leave',
          name: 'LeaveManage',
          component: () => import('@/views/leave/index.vue'),
          meta: { title: '请假管理' },
        },
        {
          path: 'notification',
          name: 'Notification',
          component: () => import('@/views/notification/index.vue'),
          meta: { title: '站内通知' },
        },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
