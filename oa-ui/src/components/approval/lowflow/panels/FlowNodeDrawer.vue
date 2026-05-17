<template>
  <el-drawer v-model="visible" :lock-scroll="false" size="360px" class="flow-node-drawer">
    <template #header>
      <el-input
        v-if="editableNameTarget"
        v-model="editableNameTarget.name"
        maxlength="30"
        :disabled="readonly || activeNode?.type === 'end'"
        @change="emit('change')"
      />
      <span v-else>节点配置</span>
    </template>

    <BranchConditionPanel
      v-if="activeBranch"
      :active-branch="activeBranch"
      :form-fields="formFields"
      :readonly="readonly"
      @set-default-branch="(branchId) => emit('set-default-branch', branchId)"
      @change="emit('change')"
    />
    <ApprovalNodePanel
      v-else-if="activeNode?.type === 'approval'"
      :active-data="activeNode"
      :user-options="userOptions"
      :role-options="roleOptions"
      :user-loading="userLoading"
      :role-loading="roleLoading"
      :readonly="readonly"
      @change="emit('change')"
    />
    <CcNodePanel
      v-else-if="activeNode?.type === 'cc'"
      :active-data="activeNode"
      :user-options="userOptions"
      :role-options="roleOptions"
      :user-loading="userLoading"
      :role-loading="roleLoading"
      :readonly="readonly"
      @change="emit('change')"
    />
    <ExclusiveNodePanel
      v-else-if="activeNode?.type === 'exclusive'"
      :active-data="activeNode"
      :selected-branch-id="selectedBranchId"
      :readonly="readonly"
      @select-branch="(ownerId, branchId) => emit('select-branch', ownerId, branchId)"
      @add-branch="(ownerId) => emit('add-branch', ownerId)"
    />
    <el-empty v-else description="该节点暂无可配置属性" />
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ApprovalFlowBranch, ApprovalFlowNode } from '@/utils/approval-flow'
import type { ApprovalFormField } from '@/utils/approval-form'
import type { SysRole, SysUser } from '@/types'
import ApprovalNodePanel from './ApprovalNodePanel.vue'
import BranchConditionPanel from './BranchConditionPanel.vue'
import CcNodePanel from './CcNodePanel.vue'
import ExclusiveNodePanel from './ExclusiveNodePanel.vue'

const props = withDefaults(defineProps<{
  modelValue: boolean
  activeNode?: ApprovalFlowNode | null
  activeBranch?: ApprovalFlowBranch | null
  selectedBranchId?: string
  formFields?: ApprovalFormField[]
  userOptions?: SysUser[]
  roleOptions?: SysRole[]
  userLoading?: boolean
  roleLoading?: boolean
  readonly?: boolean
}>(), {
  activeNode: null,
  activeBranch: null,
  selectedBranchId: '',
  formFields: () => [],
  userOptions: () => [],
  roleOptions: () => [],
  userLoading: false,
  roleLoading: false,
  readonly: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'select-branch': [ownerId: string, branchId: string]
  'add-branch': [ownerId: string]
  'set-default-branch': [branchId: string]
  change: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})
const editableNameTarget = computed(() => props.activeBranch || props.activeNode)
</script>
