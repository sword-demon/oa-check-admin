<template>
  <div class="flow-node-card-wrap">
    <div
      class="flow-node-card"
      :class="[
        `flow-node-card--${node.type}`,
        {
          'is-selected': selected,
          'is-readonly': readonly,
          'has-error': errors.length > 0,
        },
      ]"
      @click="activate"
    >
      <el-popconfirm
        v-if="showDelete"
        title="确定删除该节点吗？"
        width="190"
        placement="right-start"
        @confirm="emit('delete-node', node.id)"
      >
        <template #reference>
          <el-button class="flow-node-card__delete" circle plain size="small" type="danger" icon="CircleClose" @click.stop />
        </template>
      </el-popconfirm>

      <div class="flow-node-card__header" :style="{ borderColor: nodeColor }">
        <span class="flow-node-card__badge" :style="{ backgroundColor: nodeColor }">{{ nodeTypeLabel(node.type) }}</span>
        <el-input
          v-if="editing"
          ref="inputRef"
          v-model="node.name"
          size="small"
          maxlength="30"
          @click.stop
          @blur="finishEdit"
          @keyup.enter="finishEdit"
        />
        <button v-else type="button" class="flow-node-card__title" :disabled="readonly || node.type === 'end'" @click.stop="startEdit">
          <span>{{ node.name }}</span>
          <el-icon v-if="!readonly && node.type !== 'end'"><EditPen /></el-icon>
        </button>
      </div>

      <div class="flow-node-card__content">
        <slot>
          <span>{{ summary }}</span>
        </slot>
      </div>

      <el-tooltip v-if="errors.length > 0 && !readonly" placement="top">
        <template #content>
          <div v-for="error in errors" :key="error.message">{{ error.message }}</div>
        </template>
        <el-icon class="flow-node-card__warning"><WarnTriangleFilled /></el-icon>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import type { InputInstance } from 'element-plus'
import { EditPen, WarnTriangleFilled } from '@element-plus/icons-vue'
import { ASSIGNEE_TYPE_LABEL_MAP } from '@/bpmn/constants'
import type { ApprovalFlowNode, ApprovalFlowNodeType, ApprovalFlowValidationError } from '@/utils/approval-flow'

const props = withDefaults(defineProps<{
  node: ApprovalFlowNode
  selected?: boolean
  readonly?: boolean
  errors?: ApprovalFlowValidationError[]
}>(), {
  selected: false,
  readonly: false,
  errors: () => [],
})

const emit = defineEmits<{
  'active-node': [nodeId: string]
  'delete-node': [nodeId: string]
  change: []
}>()

const editing = ref(false)
const inputRef = ref<InputInstance>()

const showDelete = computed(() => !props.readonly && props.node.type !== 'start' && props.node.type !== 'end')
const nodeColor = computed(() => {
  const map: Record<ApprovalFlowNodeType, string> = {
    start: '#909399',
    approval: '#409eff',
    cc: '#67c23a',
    exclusive: '#e6a23c',
    parallel: '#67c23a',
    end: '#909399',
  }
  return map[props.node.type]
})
const summary = computed(() => {
  if (props.node.type === 'approval') {
    return ASSIGNEE_TYPE_LABEL_MAP[props.node.assigneeType || ''] || '未配置审批人'
  }
  if (props.node.type === 'cc') {
    return ASSIGNEE_TYPE_LABEL_MAP[props.node.assigneeType || ''] || '抄送配置'
  }
  if (props.node.type === 'exclusive') {
    return `${props.node.branches?.length || 0} 个条件出口`
  }
  if (props.node.type === 'start') return '流程发起'
  if (props.node.type === 'end') return '流程结束'
  return '流程节点'
})

function activate() {
  emit('active-node', props.node.id)
}

function startEdit() {
  if (props.readonly || props.node.type === 'end') return
  editing.value = true
  nextTick(() => inputRef.value?.focus())
}

function finishEdit() {
  if (!editing.value) return
  editing.value = false
  emit('change')
}

function nodeTypeLabel(type: ApprovalFlowNodeType) {
  const map: Record<ApprovalFlowNodeType, string> = {
    start: '开始',
    approval: '审批',
    cc: '抄送',
    exclusive: '条件',
    parallel: '并行',
    end: '结束',
  }
  return map[type]
}
</script>

<style scoped lang="scss">
.flow-node-card-wrap {
  position: relative;
  display: flex;
  justify-content: center;
  width: 100%;
}

.flow-node-card {
  position: relative;
  z-index: 2;
  width: 236px;
  min-height: 92px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 14px rgb(31 45 61 / 8%);
  cursor: pointer;

  &:hover {
    box-shadow: 0 0 0 2px rgb(64 158 255 / 12%);

    .flow-node-card__delete {
      display: inline-flex;
    }
  }

  &.is-selected {
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgb(64 158 255 / 18%);
  }

  &.has-error {
    border-color: #f56c6c;
  }
}

.flow-node-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  padding: 6px 10px;
  border-bottom: 2px solid;
}

.flow-node-card__badge {
  flex: 0 0 auto;
  border-radius: 999px;
  padding: 2px 8px;
  color: #fff;
  font-size: 12px;
}

.flow-node-card__title {
  min-width: 0;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  border: 0;
  background: transparent;
  color: #303133;
  font-weight: 700;
  text-align: left;
  cursor: pointer;

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &:disabled {
    cursor: default;
  }
}

.flow-node-card__content {
  min-height: 42px;
  padding: 14px 16px;
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

.flow-node-card__delete {
  position: absolute;
  top: -10px;
  right: -10px;
  z-index: 5;
  display: none;
}

.flow-node-card__warning {
  position: absolute;
  top: 50%;
  right: -30px;
  transform: translateY(-50%);
  color: #f56c6c;
  font-size: 20px;
}
</style>
