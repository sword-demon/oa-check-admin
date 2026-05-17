<template>
  <ApprovalNodePanel
    :active-data="activeData"
    :user-options="userOptions"
    :role-options="roleOptions"
    :user-loading="userLoading"
    :role-loading="roleLoading"
    :readonly="readonly"
    @change="emit('change')"
  />
</template>

<script setup lang="ts">
import type { ApprovalFlowNode } from '@/utils/approval-flow'
import type { SysRole, SysUser } from '@/types'
import ApprovalNodePanel from './ApprovalNodePanel.vue'

withDefaults(defineProps<{
  activeData: ApprovalFlowNode
  userOptions?: SysUser[]
  roleOptions?: SysRole[]
  userLoading?: boolean
  roleLoading?: boolean
  readonly?: boolean
}>(), {
  userOptions: () => [],
  roleOptions: () => [],
  userLoading: false,
  roleLoading: false,
  readonly: false,
})

const emit = defineEmits<{
  change: []
}>()
</script>
