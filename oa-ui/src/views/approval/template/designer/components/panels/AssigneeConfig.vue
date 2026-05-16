<template>
  <div class="assignee-config">
    <el-form-item label="审批人类型">
      <el-select
        :model-value="assigneeType"
        placeholder="选择审批人类型"
        :disabled="readOnly"
        @update:model-value="handleTypeChange($event)"
      >
        <el-option
          v-for="opt in ASSIGNEE_TYPE_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="说明">
      <span class="assignee-config__desc">{{ currentOption?.description }}</span>
    </el-form-item>

    <!-- Fixed user config -->
    <el-form-item v-if="assigneeType === 'fixed'" label="用户 ID">
      <el-input
        :model-value="String(configData.userId ?? '')"
        placeholder="输入用户 ID"
        :disabled="readOnly"
        @update:model-value="updateConfig({ userId: Number($event) || 0 })"
      />
    </el-form-item>

    <!-- Upward dept leader config -->
    <el-form-item v-if="assigneeType === 'upwardDeptLeader'" label="上级层级">
      <el-input-number
        :model-value="Number(configData.level ?? 1)"
        :min="1"
        :max="10"
        :disabled="readOnly"
        @update:model-value="updateConfig({ level: $event })"
      />
    </el-form-item>

    <!-- Role config -->
    <el-form-item v-if="assigneeType === 'role'" label="角色 ID">
      <el-input
        :model-value="String(configData.roleId ?? '')"
        placeholder="输入角色 ID"
        :disabled="readOnly"
        @update:model-value="updateConfig({ roleId: Number($event) || 0 })"
      />
    </el-form-item>

    <!-- Expression config -->
    <el-form-item v-if="assigneeType === 'expression'" label="UEL 表达式">
      <el-input
        :model-value="String(configData.expression ?? '')"
        placeholder="${assigneeResolver.resolveDeptLeader(initiator)}"
        :disabled="readOnly"
        @update:model-value="updateConfig({ expression: $event })"
      />
    </el-form-item>

    <el-form-item v-if="uelExpression" label="生成表达式">
      <el-input :model-value="uelExpression" type="textarea" :rows="2" readonly />
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ASSIGNEE_TYPE_OPTIONS } from '@/bpmn/constants'

const props = defineProps<{
  element: any
  modeler: any
  readOnly: boolean
}>()

const assigneeType = ref('deptLeader')
const configData = ref<Record<string, unknown>>({})

const currentOption = computed(() =>
  ASSIGNEE_TYPE_OPTIONS.find((o) => o.value === assigneeType.value),
)

const uelExpression = computed(() => {
  if (!currentOption.value) return ''
  return currentOption.value.uelTemplate(configData.value)
})

// Read existing assignee config from element on mount
watch(
  () => props.element,
  (el) => {
    if (!el?.businessObject) return
    const bo = el.businessObject

    // Try to read from extension attributes
    const extAttrs = bo.$attrs || {}
    for (const [key, value] of Object.entries(extAttrs)) {
      if (key === 'flowable:assigneeType') {
        assigneeType.value = String(value)
      } else if (key === 'flowable:assigneeConfig') {
        try {
          configData.value = JSON.parse(String(value))
        } catch {
          configData.value = {}
        }
      }
    }

    // Fallback: infer from flowable:assignee
    if (!assigneeType.value && bo.assignee) {
      const assignee = String(bo.assignee)
      if (assignee.includes('resolveDeptLeader(initiator)') && !assignee.includes('Upward')) {
        assigneeType.value = 'deptLeader'
      } else if (assignee.includes('resolveUpwardDeptLeader')) {
        assigneeType.value = 'upwardDeptLeader'
      } else if (assignee.includes('resolveRoleUsers')) {
        assigneeType.value = 'role'
      } else if (assignee === '${initiator}') {
        assigneeType.value = 'initiator'
      } else if (assignee.startsWith('${')) {
        assigneeType.value = 'expression'
        configData.value = { expression: assignee }
      } else {
        assigneeType.value = 'fixed'
        configData.value = { userId: assignee }
      }
    }
  },
  { immediate: true },
)

function handleTypeChange(type: string) {
  assigneeType.value = type
  configData.value = {}
  updateAssignee()
}

function updateConfig(data: Record<string, unknown>) {
  configData.value = { ...configData.value, ...data }
  updateAssignee()
}

function updateAssignee() {
  if (props.readOnly || !props.modeler) return
  const modeling = props.modeler.get('modeling')
  const moddle = props.modeler.get('moddle')

  const expression = uelExpression.value

  // Update flowable:assignee on the element
  modeling.updateModdleProperties(props.element, props.element.businessObject, {
    'flowable:assignee': expression,
    'flowable:assigneeType': assigneeType.value,
    'flowable:assigneeConfig': JSON.stringify(configData.value),
  })
}
</script>

<style scoped lang="scss">
.assignee-config {
  &__desc {
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
  }
}
</style>
