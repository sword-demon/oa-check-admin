<template>
  <el-form label-position="top" @submit.prevent>
    <el-form-item label="审批人策略">
      <el-select v-model="activeData.assigneeType" :disabled="readonly" @change="resetConfig">
        <el-option v-for="option in ASSIGNEE_TYPE_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
      </el-select>
    </el-form-item>

    <el-form-item v-if="activeData.assigneeType === 'fixed'" label="指定用户">
      <el-select
        v-model="config.userId"
        :disabled="readonly"
        :loading="userLoading"
        filterable
        clearable
        placeholder="选择审批用户"
        @change="syncConfig"
      >
        <el-option v-for="user in userOptions" :key="user.id" :label="userDisplayName(user)" :value="user.id" />
      </el-select>
    </el-form-item>

    <el-form-item v-if="activeData.assigneeType === 'role'" label="角色">
      <el-select
        v-model="config.roleId"
        :disabled="readonly"
        :loading="roleLoading"
        filterable
        clearable
        placeholder="选择审批角色"
        @change="syncConfig"
      >
        <el-option v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.id" />
      </el-select>
    </el-form-item>

    <el-form-item v-if="activeData.assigneeType === 'upwardDeptLeader'" label="上级层级">
      <el-input-number v-model="config.level" :disabled="readonly" :min="1" :max="10" @change="syncConfig" />
    </el-form-item>

    <el-form-item v-if="activeData.assigneeType === 'expression'" label="UEL 表达式">
      <el-input v-model="config.expression" :disabled="readonly" placeholder="${initiator}" @change="syncConfig" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import { ASSIGNEE_TYPE_OPTIONS } from '@/bpmn/constants'
import type { ApprovalFlowNode } from '@/utils/approval-flow'
import type { SysRole, SysUser } from '@/types'

const props = withDefaults(defineProps<{
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

const config = reactive<Record<string, any>>({})

watch(() => props.activeData, syncLocalConfig, { immediate: true })

function syncLocalConfig() {
  Object.keys(config).forEach((key) => delete config[key])
  Object.assign(config, props.activeData.assigneeConfig || {})
}

function resetConfig() {
  props.activeData.assigneeConfig = {}
  syncLocalConfig()
  emit('change')
}

function syncConfig() {
  props.activeData.assigneeConfig = { ...config }
  emit('change')
}

function userDisplayName(user: SysUser) {
  return user.nickname ? `${user.nickname} (${user.username})` : user.username
}
</script>
