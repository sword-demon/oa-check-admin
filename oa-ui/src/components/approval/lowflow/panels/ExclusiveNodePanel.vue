<template>
  <div class="exclusive-node-panel">
    <div class="exclusive-node-panel__branches">
      <button
        v-for="branch in activeData.branches || []"
        :key="branch.id"
        type="button"
        class="exclusive-node-panel__branch"
        :class="{ 'is-active': selectedBranchId === branch.id }"
        @click="emit('select-branch', activeData.id, branch.id)"
      >
        <span>{{ branch.name }}</span>
        <small>{{ branch.isDefault ? '默认出口' : '条件出口' }}</small>
      </button>
    </div>
    <el-button v-if="!readonly" size="small" @click="emit('add-branch', activeData.id)">添加出口</el-button>
  </div>
</template>

<script setup lang="ts">
import type { ApprovalFlowNode } from '@/utils/approval-flow'

withDefaults(defineProps<{
  activeData: ApprovalFlowNode
  selectedBranchId?: string
  readonly?: boolean
}>(), {
  selectedBranchId: '',
  readonly: false,
})

const emit = defineEmits<{
  'select-branch': [ownerId: string, branchId: string]
  'add-branch': [ownerId: string]
}>()
</script>

<style scoped lang="scss">
.exclusive-node-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.exclusive-node-panel__branches {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.exclusive-node-panel__branch {
  display: flex;
  min-width: 112px;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  padding: 8px 10px;
  cursor: pointer;

  small {
    color: #909399;
  }

  &.is-active {
    border-color: #409eff;
    color: #409eff;
  }
}
</style>
