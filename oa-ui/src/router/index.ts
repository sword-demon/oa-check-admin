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
