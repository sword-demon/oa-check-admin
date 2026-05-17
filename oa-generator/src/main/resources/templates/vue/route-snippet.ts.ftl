// Route snippet for ${ctx.config.module} module — add to router/index.ts children array
<#list ctx.entity.allEntities as entity>
        {
          path: '${ctx.config.module}',
          name: '${entity.name}Manage',
          component: () => import('@/views/${ctx.config.module}/index.vue'),
          meta: { title: '${entity.comment}管理' },
        },
</#list>
