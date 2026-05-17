<template>
  <div class="lowflow-approval-designer">
    <section class="lowflow-approval-designer__canvas">
      <div class="lowflow-approval-designer__toolbar">
        <el-button-group>
          <el-button size="small" icon="Minus" @click="zoomOut" />
          <el-button size="small">{{ zoom }}%</el-button>
          <el-button size="small" icon="Plus" @click="zoomIn" />
        </el-button-group>
      </div>

      <div class="lowflow-approval-designer__viewport">
        <div class="lowflow-approval-designer__tree" :style="{ transform: `scale(${zoom / 100})` }">
          <FlowTreeNode
            :node="flow"
            :selected-node-id="selectedNodeId"
            :selected-branch-id="selectedBranchId"
            :readonly="readonly"
            :errors="designerErrors"
            @active-node="selectNode"
            @active-branch="selectBranch"
            @add-node="addNodeAfter"
            @add-branch-child="addBranchChild"
            @delete-node="deleteNode"
            @add-branch="addBranch"
            @delete-branch="deleteBranch"
            @set-default-branch="setDefaultBranch"
            @move-branch="moveBranch"
            @change="emitChange"
          />
        </div>
      </div>
    </section>

    <FlowNodeDrawer
      v-model="drawerVisible"
      :active-node="selectedNode"
      :active-branch="selectedBranch"
      :selected-branch-id="selectedBranchId"
      :form-fields="formFields"
      :user-options="userOptions"
      :role-options="roleOptions"
      :user-loading="userLoading"
      :role-loading="roleLoading"
      :readonly="readonly"
      @select-branch="selectBranch"
      @add-branch="addBranch"
      @set-default-branch="setDefaultBranch"
      @change="emitChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { getRoleList } from '@/api/system'
import { getUserList } from '@/api/user'
import type { SysRole, SysUser } from '@/types'
import { parseApprovalFormConfig, type ApprovalFormField } from '@/utils/approval-form'
import {
  appendApprovalFlowBranchChild,
  createApprovalFlowNode,
  createDefaultApprovalFlow,
  findApprovalFlowBranch,
  findApprovalFlowBranchOwner,
  findApprovalFlowNode,
  getApprovalFlowLinearNodes,
  insertApprovalFlowNodeAfter,
  moveApprovalFlowBranch,
  normalizeApprovalFlowModel,
  removeApprovalFlowNode,
  validateApprovalFlow,
  validateApprovalFlowForDesigner,
  type ApprovalFlowBranch,
  type ApprovalFlowNode,
  type ApprovalFlowNodeType,
} from '@/utils/approval-flow'
import FlowTreeNode from './nodes/FlowTreeNode.vue'
import FlowNodeDrawer from './panels/FlowNodeDrawer.vue'

const props = withDefaults(defineProps<{
  modelValue?: ApprovalFlowNode | null
  formSchema?: string
  readonly?: boolean
}>(), {
  modelValue: null,
  formSchema: '',
  readonly: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: ApprovalFlowNode]
}>()

const flow = ref<ApprovalFlowNode>(normalizeApprovalFlowModel(cloneFlow(props.modelValue || createDefaultApprovalFlow())))
const selectedNodeId = ref(firstSelectableNodeId(flow.value))
const selectedBranchId = ref('')
const drawerVisible = ref(false)
const zoom = ref(100)
const userOptions = ref<SysUser[]>([])
const roleOptions = ref<SysRole[]>([])
const userLoading = ref(false)
const roleLoading = ref(false)
let skipNextModelSync = false

const formFields = computed<ApprovalFormField[]>(() => parseApprovalFormConfig(props.formSchema))
const selectedNode = computed(() => selectedNodeId.value ? findApprovalFlowNode(flow.value, selectedNodeId.value) : null)
const selectedBranch = computed(() => selectedBranchId.value ? findApprovalFlowBranch(flow.value, selectedBranchId.value) : null)
const designerErrors = computed(() => validateApprovalFlowForDesigner(flow.value))

watch(() => props.modelValue, (value) => {
  if (!value) return
  if (skipNextModelSync) {
    skipNextModelSync = false
    return
  }
  flow.value = normalizeApprovalFlowModel(cloneFlow(value))
  if (!findApprovalFlowNode(flow.value, selectedNodeId.value)) {
    selectedNodeId.value = firstSelectableNodeId(flow.value)
  }
  if (selectedBranchId.value && !findApprovalFlowBranch(flow.value, selectedBranchId.value)) {
    selectedBranchId.value = ''
  }
}, { deep: true })

function selectNode(nodeId: string) {
  selectedNodeId.value = nodeId
  selectedBranchId.value = ''
  drawerVisible.value = true
}

function selectBranch(ownerId: string, branchId: string) {
  selectedNodeId.value = ownerId
  selectedBranchId.value = branchId
  drawerVisible.value = true
}

function addNodeAfter(targetId: string, type: ApprovalFlowNodeType) {
  if (props.readonly || type === 'start' || type === 'end' || type === 'parallel') return
  const node = createApprovalFlowNode(type)
  const inserted = insertApprovalFlowNodeAfter(flow.value, targetId, node)
  if (!inserted && targetId === flow.value.id && flow.value.type !== 'end') {
    node.next = flow.value.next
    flow.value.next = node
  }
  selectedNodeId.value = node.id
  selectedBranchId.value = ''
  drawerVisible.value = true
  emitChange()
}

function addBranchChild(branchId: string, type: ApprovalFlowNodeType) {
  if (props.readonly || (type !== 'approval' && type !== 'cc')) return
  const branch = findApprovalFlowBranch(flow.value, branchId)
  if (!branch) return
  const node = createApprovalFlowNode(type)
  appendApprovalFlowBranchChild(branch, node)
  selectedNodeId.value = node.id
  selectedBranchId.value = ''
  drawerVisible.value = true
  emitChange()
}

function deleteNode(nodeId: string) {
  if (props.readonly) return
  removeApprovalFlowNode(flow.value, nodeId)
  selectedNodeId.value = firstSelectableNodeId(flow.value)
  selectedBranchId.value = ''
  emitChange()
}

function addBranch(ownerId: string) {
  if (props.readonly) return
  const node = findApprovalFlowNode(flow.value, ownerId)
  if (!node) return
  if (!node.branches) node.branches = []
  const branch: ApprovalFlowBranch = {
    id: newId('branch'),
    name: `条件${node.branches.length + 1}`,
    condition: { fieldKey: '', operator: '==', value: '' },
    children: [],
  }
  const defaultIndex = node.branches.findIndex((item) => item.isDefault)
  if (defaultIndex >= 0) {
    node.branches.splice(defaultIndex, 0, branch)
  } else {
    node.branches.push(branch)
  }
  selectedNodeId.value = node.id
  selectedBranchId.value = branch.id
  drawerVisible.value = true
  emitChange()
}

function deleteBranch(branchId: string) {
  if (props.readonly) return
  const owner = findApprovalFlowBranchOwner(flow.value, branchId)
  if (!owner?.branches || owner.branches.length <= 2) return
  const wasDefault = owner.branches.find((branch) => branch.id === branchId)?.isDefault
  owner.branches = owner.branches.filter((branch) => branch.id !== branchId)
  if (wasDefault && !owner.branches.some((branch) => branch.isDefault)) {
    const fallback = owner.branches[owner.branches.length - 1]
    if (fallback) {
      fallback.isDefault = true
      fallback.condition = undefined
    }
  }
  selectedBranchId.value = owner.branches[0]?.id || ''
  emitChange()
}

function setDefaultBranch(branchId: string) {
  const owner = findApprovalFlowBranchOwner(flow.value, branchId)
  owner?.branches?.forEach((branch) => {
    branch.isDefault = branch.id === branchId
    if (branch.isDefault) branch.condition = undefined
    if (!branch.isDefault && !branch.condition) {
      branch.condition = { fieldKey: '', operator: '==', value: '' }
    }
  })
  selectedBranchId.value = branchId
  emitChange()
}

function moveBranch(branchId: string, offset: number) {
  if (props.readonly) return
  const owner = findApprovalFlowBranchOwner(flow.value, branchId)
  if (!owner) return
  if (moveApprovalFlowBranch(owner, branchId, offset)) {
    emitChange()
  }
}

function emitChange() {
  skipNextModelSync = true
  emit('update:modelValue', cloneFlow(flow.value))
}

function zoomIn() {
  zoom.value = Math.min(140, zoom.value + 10)
}

function zoomOut() {
  zoom.value = Math.max(60, zoom.value - 10)
}

function firstSelectableNodeId(root: ApprovalFlowNode) {
  return getApprovalFlowLinearNodes(root)[0]?.id || root.id
}

function cloneFlow(value: ApprovalFlowNode) {
  return JSON.parse(JSON.stringify(value)) as ApprovalFlowNode
}

function newId(prefix: string) {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
}

async function loadUserOptions() {
  userLoading.value = true
  try {
    const data = await getUserList({ status: 1, page: 1, pageSize: 200 })
    userOptions.value = data.list || []
  } finally {
    userLoading.value = false
  }
}

async function loadRoleOptions() {
  roleLoading.value = true
  try {
    const data: any = await getRoleList({ status: 1, page: 1, pageSize: 200 })
    roleOptions.value = Array.isArray(data) ? data : data?.list || []
  } finally {
    roleLoading.value = false
  }
}

onMounted(() => {
  loadUserOptions()
  loadRoleOptions()
})

defineExpose({
  validationErrors: computed(() => validateApprovalFlow(flow.value)),
  flow,
})
</script>

<style scoped lang="scss">
.lowflow-approval-designer {
  min-height: 620px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  background: #f6f8fb;
}

.lowflow-approval-designer__canvas {
  position: relative;
  min-height: 620px;
  overflow: auto;
}

.lowflow-approval-designer__toolbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  justify-content: flex-end;
  padding: 10px;
  background: linear-gradient(180deg, rgb(246 248 251 / 96%), rgb(246 248 251 / 72%));
}

.lowflow-approval-designer__viewport {
  min-width: max-content;
  padding: 36px 56px 72px;
}

.lowflow-approval-designer__tree {
  width: max-content;
  min-width: 100%;
  transform-origin: top center;
  transition: transform 0.15s ease;
}

@media (max-width: 900px) {
  .lowflow-approval-designer,
  .lowflow-approval-designer__canvas {
    min-height: 560px;
  }

  .lowflow-approval-designer__viewport {
    padding: 28px 24px 56px;
  }
}
</style>
