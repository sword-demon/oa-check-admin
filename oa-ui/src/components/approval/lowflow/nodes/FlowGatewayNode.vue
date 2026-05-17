<template>
  <div class="flow-gateway">
    <div class="flow-gateway__add-branch">
      <el-button :disabled="readonly" round size="small" type="primary" plain @click="emit('add-branch', node.id)">
        添加条件
      </el-button>
    </div>

    <div v-for="(branch, index) in node.branches || []" :key="branch.id" class="flow-gateway__column">
      <div v-if="index === 0" class="flow-gateway__mask flow-gateway__mask--left-top" />
      <div v-if="index === 0" class="flow-gateway__mask flow-gateway__mask--left-bottom" />
      <div v-if="index === (node.branches?.length || 0) - 1" class="flow-gateway__mask flow-gateway__mask--right-top" />
      <div v-if="index === (node.branches?.length || 0) - 1" class="flow-gateway__mask flow-gateway__mask--right-bottom" />

      <div class="flow-gateway__branch" :class="{ 'is-selected': selectedBranchId === branch.id, 'has-error': branchErrors(branch.id).length > 0 }">
        <button type="button" class="flow-gateway__branch-header" @click="emit('active-branch', node.id, branch.id)">
          <strong>{{ branch.name }}</strong>
          <small>{{ branchSummary(branch) }}</small>
          <el-tooltip v-if="branchErrors(branch.id).length > 0 && !readonly" placement="top">
            <template #content>
              <div v-for="error in branchErrors(branch.id)" :key="error.message">{{ error.message }}</div>
            </template>
            <el-icon><WarnTriangleFilled /></el-icon>
          </el-tooltip>
        </button>

        <div v-if="!readonly" class="flow-gateway__branch-actions">
          <el-button text size="small" :disabled="index === 0" @click="emit('move-branch', branch.id, -1)">左移</el-button>
          <el-button text size="small" :disabled="index === (node.branches?.length || 0) - 1" @click="emit('move-branch', branch.id, 1)">右移</el-button>
          <el-button text size="small" type="primary" @click="emit('set-default-branch', branch.id)">设默认</el-button>
          <el-button text size="small" type="danger" :disabled="(node.branches?.length || 0) <= 2" @click="emit('delete-branch', branch.id)">删除</el-button>
        </div>
      </div>

      <div class="flow-gateway__branch-body">
        <FlowTreeNode
          v-if="branch.children[0]"
          :node="branch.children[0]"
          :selected-node-id="selectedNodeId"
          :selected-branch-id="selectedBranchId"
          :readonly="readonly"
          :errors="errors"
          @active-node="(nodeId) => emit('active-node', nodeId)"
          @active-branch="(ownerId, branchId) => emit('active-branch', ownerId, branchId)"
          @add-node="(targetId, type) => emit('add-node', targetId, type)"
          @add-branch-child="(branchId, type) => emit('add-branch-child', branchId, type)"
          @delete-node="(nodeId) => emit('delete-node', nodeId)"
          @add-branch="(ownerId) => emit('add-branch', ownerId)"
          @delete-branch="(branchId) => emit('delete-branch', branchId)"
          @set-default-branch="(branchId) => emit('set-default-branch', branchId)"
          @move-branch="(branchId, offset) => emit('move-branch', branchId, offset)"
          @change="emit('change')"
        />
        <FlowAddButton v-else :readonly="readonly" @add-node="(type) => emit('add-branch-child', branch.id, type)" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { WarnTriangleFilled } from '@element-plus/icons-vue'
import type {
  ApprovalFlowBranch,
  ApprovalFlowNode,
  ApprovalFlowNodeType,
  ApprovalFlowValidationError,
} from '@/utils/approval-flow'
import FlowAddButton from './FlowAddButton.vue'
import FlowTreeNode from './FlowTreeNode.vue'

const props = withDefaults(defineProps<{
  node: ApprovalFlowNode
  selectedNodeId?: string
  selectedBranchId?: string
  readonly?: boolean
  errors?: Record<string, ApprovalFlowValidationError[]>
}>(), {
  selectedNodeId: '',
  selectedBranchId: '',
  readonly: false,
  errors: () => ({}),
})

const emit = defineEmits<{
  'active-node': [nodeId: string]
  'active-branch': [ownerId: string, branchId: string]
  'add-node': [targetId: string, type: ApprovalFlowNodeType]
  'add-branch-child': [branchId: string, type: ApprovalFlowNodeType]
  'delete-node': [nodeId: string]
  'add-branch': [ownerId: string]
  'delete-branch': [branchId: string]
  'set-default-branch': [branchId: string]
  'move-branch': [branchId: string, offset: number]
  change: []
}>()

function branchErrors(branchId: string) {
  return props.errors[branchId] || []
}

function branchSummary(branch: ApprovalFlowBranch) {
  if (branch.isDefault) return '默认出口'
  if (!branch.condition?.fieldKey) return '未配置条件'
  return `${branch.condition.fieldKey} ${branch.condition.operator} ${branch.condition.value}`
}
</script>

<style scoped lang="scss">
.flow-gateway {
  position: relative;
  display: flex;
  min-width: max-content;
  margin: 2px 0 0;
  border-top: 1px solid #c0c4cc;
  border-bottom: 1px solid #c0c4cc;
}

.flow-gateway__add-branch {
  position: absolute;
  top: -16px;
  left: 50%;
  z-index: 4;
  transform: translateX(-50%);
}

.flow-gateway__column {
  position: relative;
  display: flex;
  min-width: 260px;
  padding: 34px 24px 22px;
  flex-direction: column;
  align-items: center;
  background: #f6f8fb;

  &::before {
    position: absolute;
    inset: 0;
    width: 1px;
    height: 100%;
    margin: auto;
    background: #c0c4cc;
    content: '';
  }
}

.flow-gateway__mask {
  position: absolute;
  z-index: 1;
  width: 50%;
  height: 4px;
  background: #f6f8fb;
}

.flow-gateway__mask--left-top {
  top: -2px;
  left: 0;
}

.flow-gateway__mask--left-bottom {
  bottom: -2px;
  left: 0;
}

.flow-gateway__mask--right-top {
  top: -2px;
  right: 0;
}

.flow-gateway__mask--right-bottom {
  right: 0;
  bottom: -2px;
}

.flow-gateway__branch {
  position: relative;
  z-index: 2;
  width: 232px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 12px rgb(31 45 61 / 6%);

  &.is-selected {
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgb(64 158 255 / 16%);
  }

  &.has-error {
    border-color: #f56c6c;
  }
}

.flow-gateway__branch-header {
  display: flex;
  width: 100%;
  min-height: 56px;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  border: 0;
  background: transparent;
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;

  small {
    color: #909399;
  }

  .el-icon {
    position: absolute;
    top: 18px;
    right: 10px;
    color: #f56c6c;
  }
}

.flow-gateway__branch-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 2px;
  border-top: 1px solid #ebeef5;
  padding: 4px 8px;
}

.flow-gateway__branch-body {
  position: relative;
  z-index: 2;
  display: flex;
  min-height: 96px;
  flex-direction: column;
  align-items: center;
}
</style>
