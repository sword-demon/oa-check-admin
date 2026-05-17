<template>
  <div class="flow-tree-node">
    <FlowNodeCard
      :node="node"
      :selected="selectedNodeId === node.id"
      :readonly="readonly"
      :errors="errors[node.id] || []"
      @active-node="(nodeId) => emit('active-node', nodeId)"
      @delete-node="(nodeId) => emit('delete-node', nodeId)"
      @change="emit('change')"
    />

    <FlowGatewayNode
      v-if="node.type === 'exclusive'"
      :node="node"
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

    <FlowAddButton
      v-if="node.type !== 'end'"
      :readonly="readonly"
      @add-node="(type) => emit('add-node', node.id, type)"
    />

    <FlowTreeNode
      v-if="node.next"
      :node="node.next"
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
  </div>
</template>

<script setup lang="ts">
import FlowAddButton from './FlowAddButton.vue'
import FlowGatewayNode from './FlowGatewayNode.vue'
import FlowNodeCard from './FlowNodeCard.vue'
import type {
  ApprovalFlowNode,
  ApprovalFlowNodeType,
  ApprovalFlowValidationError,
} from '@/utils/approval-flow'

withDefaults(defineProps<{
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
</script>

<style scoped lang="scss">
.flow-tree-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: max-content;
}
</style>
