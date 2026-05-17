<template>
  <el-form label-position="top" @submit.prevent>
    <el-form-item label="分支名称">
      <el-input v-model="activeBranch.name" :disabled="readonly" @change="emit('change')" />
    </el-form-item>

    <el-form-item label="默认出口">
      <el-switch v-model="activeBranch.isDefault" :disabled="readonly" @change="emit('set-default-branch', activeBranch.id)" />
    </el-form-item>

    <template v-if="!activeBranch.isDefault">
      <el-alert
        v-if="formFields.length === 0"
        title="请先在动态表单步骤添加字段，条件分支会从表单字段中选择变量。"
        type="warning"
        :closable="false"
        class="branch-condition-panel__alert"
      />

      <el-form-item label="表单变量">
        <el-select
          v-model="condition.fieldKey"
          :disabled="readonly || formFields.length === 0"
          filterable
          placeholder="选择表单字段"
          @change="handleConditionFieldChange"
        >
          <el-option
            v-for="field in formFields"
            :key="field.fieldKey"
            :label="`${field.label} (${field.fieldKey})`"
            :value="field.fieldKey"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="操作符">
        <el-select v-model="condition.operator" :disabled="readonly" @change="syncCondition">
          <el-option v-for="option in conditionOperatorOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="比较值">
        <el-input-number
          v-if="selectedConditionField?.type === 'number'"
          v-model="numericConditionValue"
          :disabled="readonly"
          controls-position="right"
          @change="syncCondition"
        />
        <el-select
          v-else-if="selectedConditionField && selectedConditionField.options.length > 0"
          v-model="condition.value"
          :disabled="readonly"
          filterable
          placeholder="选择选项值"
          @change="syncCondition"
        >
          <el-option
            v-for="option in selectedConditionField.options"
            :key="String(option.value)"
            :label="option.label"
            :value="option.value as string | number"
          />
        </el-select>
        <el-date-picker
          v-else-if="selectedConditionField?.type === 'date'"
          v-model="condition.value"
          :disabled="readonly"
          type="date"
          value-format="YYYY-MM-DD"
          @change="syncCondition"
        />
        <el-date-picker
          v-else-if="selectedConditionField?.type === 'datetime'"
          v-model="condition.value"
          :disabled="readonly"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          @change="syncCondition"
        />
        <el-input v-else v-model="condition.value" :disabled="readonly" placeholder="输入比较值" @change="syncCondition" />
      </el-form-item>
    </template>
  </el-form>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type { ApprovalFlowBranch, ApprovalFlowCondition } from '@/utils/approval-flow'
import type { ApprovalFormField } from '@/utils/approval-form'

const props = withDefaults(defineProps<{
  activeBranch: ApprovalFlowBranch
  formFields?: ApprovalFormField[]
  readonly?: boolean
}>(), {
  formFields: () => [],
  readonly: false,
})

const emit = defineEmits<{
  change: []
  'set-default-branch': [branchId: string]
}>()

const condition = reactive<{
  fieldKey: string
  operator: ApprovalFlowCondition['operator']
  value: string | number
}>({ fieldKey: '', operator: '==', value: '' })

const selectedConditionField = computed(() => {
  return props.formFields.find((field) => field.fieldKey === condition.fieldKey) || null
})
const numericConditionValue = computed<number | undefined>({
  get() {
    if (condition.value === '') return undefined
    const value = Number(condition.value)
    return Number.isNaN(value) ? undefined : value
  },
  set(value) {
    condition.value = value ?? ''
  },
})
const conditionOperatorOptions = computed(() => {
  if (selectedConditionField.value?.type === 'number') {
    return [
      { label: '等于', value: '==' },
      { label: '不等于', value: '!=' },
      { label: '大于', value: '>' },
      { label: '大于等于', value: '>=' },
      { label: '小于', value: '<' },
      { label: '小于等于', value: '<=' },
    ] as Array<{ label: string; value: ApprovalFlowCondition['operator'] }>
  }
  if (selectedConditionField.value?.type === 'checkbox') {
    return [
      { label: '包含', value: 'contains' },
      { label: '等于', value: '==' },
      { label: '不等于', value: '!=' },
    ] as Array<{ label: string; value: ApprovalFlowCondition['operator'] }>
  }
  return [
    { label: '等于', value: '==' },
    { label: '不等于', value: '!=' },
    { label: '包含', value: 'contains' },
  ] as Array<{ label: string; value: ApprovalFlowCondition['operator'] }>
})

watch(() => props.activeBranch, syncLocalCondition, { immediate: true })

function syncLocalCondition() {
  Object.assign(condition, {
    fieldKey: props.activeBranch.condition?.fieldKey || '',
    operator: props.activeBranch.condition?.operator || '==',
    value: typeof props.activeBranch.condition?.value === 'boolean'
      ? String(props.activeBranch.condition.value)
      : props.activeBranch.condition?.value ?? '',
  })
}

function syncCondition() {
  props.activeBranch.condition = {
    fieldKey: condition.fieldKey,
    operator: condition.operator,
    value: normalizeConditionValue(condition.value),
  }
  emit('change')
}

function handleConditionFieldChange() {
  const field = selectedConditionField.value
  if (!field) {
    condition.value = ''
    syncCondition()
    return
  }
  if (!conditionOperatorOptions.value.some((option) => option.value === condition.operator)) {
    condition.operator = conditionOperatorOptions.value[0]?.value || '=='
  }
  if (field.type === 'number') {
    condition.value = typeof condition.value === 'number' ? condition.value : 0
  } else if (field.options.length > 0) {
    const firstOption = field.options[0]
    condition.value = typeof firstOption.value === 'boolean' ? String(firstOption.value) : firstOption.value
  } else {
    condition.value = ''
  }
  syncCondition()
}

function normalizeConditionValue(value: unknown): string | number | boolean {
  if (typeof value === 'number' || typeof value === 'boolean') return value
  if (typeof value !== 'string') return String(value ?? '')
  const trimmed = value.trim()
  if (trimmed === 'true') return true
  if (trimmed === 'false') return false
  if (trimmed !== '' && !Number.isNaN(Number(trimmed))) return Number(trimmed)
  return trimmed
}
</script>

<style scoped lang="scss">
.branch-condition-panel__alert {
  margin-bottom: 12px;
}
</style>
