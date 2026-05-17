<template>
  <el-form
    v-if="fields.length"
    ref="formRef"
    :model="localModel"
    :label-width="labelWidth"
    class="approval-dynamic-form"
    :class="{ 'approval-dynamic-form--readonly': readonly }"
  >
    <el-row :gutter="16">
      <el-col
        v-for="field in fields"
        :key="field.fieldKey"
        :xs="24"
        :md="isWideField(field) ? 24 : 12"
      >
        <el-form-item
          :label="field.label"
          :prop="field.fieldKey"
          :required="!readonly && field.required"
        >
          <el-input
            v-if="field.type === 'textarea'"
            :model-value="stringValue(field)"
            type="textarea"
            :rows="4"
            :placeholder="field.placeholder"
            :readonly="readonly"
            @update:model-value="updateField(field.fieldKey, $event)"
          />
          <el-input
            v-else-if="field.type === 'text'"
            :model-value="stringValue(field)"
            :placeholder="field.placeholder"
            :readonly="readonly"
            @update:model-value="updateField(field.fieldKey, $event)"
          />
          <el-input-number
            v-else-if="field.type === 'number'"
            :model-value="numberValue(field)"
            :min="field.min"
            :max="field.max"
            :controls="!readonly"
            :disabled="readonly"
            style="width: 100%"
            @update:model-value="updateField(field.fieldKey, $event)"
          />
          <el-date-picker
            v-else-if="field.type === 'date' || field.type === 'datetime'"
            :model-value="dateValue(field)"
            :type="field.type === 'date' ? 'date' : 'datetime'"
            :placeholder="field.placeholder"
            :disabled="readonly"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            @update:model-value="updateField(field.fieldKey, $event)"
          />
          <el-select
            v-else-if="field.type === 'select'"
            :model-value="scalarValue(field)"
            :placeholder="field.placeholder"
            :disabled="readonly"
            style="width: 100%"
            @update:model-value="updateField(field.fieldKey, $event)"
          >
            <el-option
              v-for="option in field.options"
              :key="`${field.fieldKey}-${String(option.value)}`"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          <el-radio-group
            v-else-if="field.type === 'radio'"
            :model-value="scalarValue(field)"
            :disabled="readonly"
            @update:model-value="updateField(field.fieldKey, $event)"
          >
            <el-radio
              v-for="option in field.options"
              :key="`${field.fieldKey}-${String(option.value)}`"
              :label="option.value"
            >
              {{ option.label }}
            </el-radio>
          </el-radio-group>
          <el-checkbox-group
            v-else-if="field.type === 'checkbox'"
            :model-value="arrayValue(field)"
            :disabled="readonly"
            @update:model-value="updateField(field.fieldKey, $event)"
          >
            <el-checkbox
              v-for="option in field.options"
              :key="`${field.fieldKey}-${String(option.value)}`"
              :label="option.value"
            >
              {{ option.label }}
            </el-checkbox>
          </el-checkbox-group>
          <el-input
            v-else-if="field.type === 'attachment'"
            :model-value="attachmentValue(field)"
            :placeholder="field.placeholder || '多个文件 URL 使用换行分隔'"
            type="textarea"
            :rows="3"
            :readonly="readonly"
            @update:model-value="updateAttachment(field.fieldKey, $event)"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>

  <el-empty v-else description="暂无表单字段" :image-size="72" />

  <el-descriptions v-if="readonly && extraEntries.length" class="approval-dynamic-form__extra" :column="2" border>
    <el-descriptions-item
      v-for="entry in extraEntries"
      :key="entry.key"
      :label="entry.key"
      :span="entry.value.length > 30 ? 2 : 1"
    >
      {{ entry.value }}
    </el-descriptions-item>
  </el-descriptions>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  formatApprovalFormValue,
  parseApprovalFormConfig,
  type ApprovalFormField,
} from '@/utils/approval-form'

const props = withDefaults(defineProps<{
  schema?: string | Record<string, unknown> | null
  modelValue: Record<string, unknown>
  readonly?: boolean
  labelWidth?: string
  showExtra?: boolean
}>(), {
  schema: null,
  readonly: false,
  labelWidth: '100px',
  showExtra: true,
})

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, unknown>]
}>()

const fields = computed(() => parseApprovalFormConfig(props.schema))
const localModel = computed(() => props.modelValue || {})
const fieldKeys = computed(() => new Set(fields.value.map((field) => field.fieldKey)))

const extraEntries = computed(() => {
  if (!props.showExtra) return []
  return Object.entries(localModel.value)
    .filter(([key]) => !fieldKeys.value.has(key))
    .map(([key, value]) => ({ key, value: formatApprovalFormValue(value) }))
})

function updateField(fieldKey: string, value: unknown) {
  emit('update:modelValue', { ...localModel.value, [fieldKey]: value })
}

function updateAttachment(fieldKey: string, value: string) {
  updateField(fieldKey, value.split('\n').map((item) => item.trim()).filter(Boolean))
}

function isWideField(field: ApprovalFormField) {
  return ['textarea', 'checkbox', 'radio', 'attachment'].includes(field.type)
}

function stringValue(field: ApprovalFormField) {
  const value = localModel.value[field.fieldKey]
  return value === undefined || value === null ? '' : String(value)
}

function scalarValue(field: ApprovalFormField) {
  const value = localModel.value[field.fieldKey]
  return typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean'
    ? value
    : undefined
}

function numberValue(field: ApprovalFormField) {
  const value = localModel.value[field.fieldKey]
  if (value === undefined || value === null || value === '') return undefined
  const number = Number(value)
  return Number.isNaN(number) ? undefined : number
}

function dateValue(field: ApprovalFormField) {
  const value = localModel.value[field.fieldKey]
  return value ? String(value) : undefined
}

function arrayValue(field: ApprovalFormField) {
  const value = localModel.value[field.fieldKey]
  if (Array.isArray(value)) return value
  if (value === undefined || value === null || value === '') return []
  return [value]
}

function attachmentValue(field: ApprovalFormField) {
  const value = localModel.value[field.fieldKey]
  if (Array.isArray(value)) return value.join('\n')
  return value === undefined || value === null ? '' : String(value)
}
</script>

<style scoped lang="scss">
.approval-dynamic-form {
  min-height: 96px;

  :deep(.el-input-number .el-input__wrapper) {
    width: 100%;
  }

  &--readonly :deep(.el-form-item) {
    margin-bottom: 14px;
  }

  &__extra {
    margin-top: 14px;
  }
}
</style>
