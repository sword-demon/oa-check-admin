<template>
  <div v-loading="loading" class="approval-form-detail">
    <LeaveReadonlyForm v-if="leaveDetail" :leave="leaveDetail" />

    <ApprovalDynamicForm
      v-else-if="showGenericForm"
      :model-value="rawData"
      :schema="template?.formConfig"
      readonly
      class="approval-form-detail__form"
    />

    <el-descriptions v-else-if="fallbackEntries.length" :column="2" border>
      <el-descriptions-item
        v-for="entry in fallbackEntries"
        :key="entry.key"
        :label="entry.label"
        :span="entry.span"
      >
        {{ entry.value }}
      </el-descriptions-item>
    </el-descriptions>

    <el-empty v-else description="暂无表单数据" :image-size="72" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getTemplate } from '@/api/template'
import { getLeaveDetail, type LeaveRequestDetail } from '@/api/leave'
import type { ProcessTemplate } from '@/types'
import {
  formatApprovalFormValue,
  parseApprovalFormConfig,
  parseApprovalFormData,
  type ApprovalFormField,
} from '@/utils/approval-form'
import LeaveReadonlyForm from '@/views/leave/components/LeaveReadonlyForm.vue'
import ApprovalDynamicForm from '@/components/approval/ApprovalDynamicForm.vue'

const LEAVE_TEMPLATE_KEY = 'leave_request'

const props = defineProps<{
  templateId: number
  formData: string | null
}>()

const loading = ref(false)
const template = ref<ProcessTemplate | null>(null)
const leaveDetail = ref<LeaveRequestDetail | null>(null)

const rawData = computed(() => parseApprovalFormData(props.formData))
const fields = computed<ApprovalFormField[]>(() => parseApprovalFormConfig(template.value?.formConfig))
const isLeaveTemplate = computed(() => template.value?.templateKey === LEAVE_TEMPLATE_KEY)
const showGenericForm = computed(() => !isLeaveTemplate.value && fields.value.length > 0)

const fallbackEntries = computed(() => {
  return Object.entries(rawData.value).map(([key, value]) => {
    const text = formatApprovalFormValue(value)
    return {
      key,
      label: fallbackLabel(key),
      value: text,
      span: text.length > 30 ? 2 : 1,
    }
  })
})

function fallbackLabel(key: string) {
  const map: Record<string, string> = {
    leaveRequestId: '关联请假单',
  }
  return map[key] || key
}

function resolveLeaveRequestId(): number | null {
  const rawId = rawData.value.leaveRequestId
  const id = Number(rawId)
  return Number.isInteger(id) && id > 0 ? id : null
}

async function loadTemplateContext() {
  template.value = null
  leaveDetail.value = null

  if (!props.templateId) return

  loading.value = true
  try {
    const data = await getTemplate(props.templateId) as ProcessTemplate
    template.value = data

    if (data?.templateKey === LEAVE_TEMPLATE_KEY) {
      const leaveRequestId = resolveLeaveRequestId()
      if (leaveRequestId) {
        try {
          leaveDetail.value = await getLeaveDetail(leaveRequestId)
        } catch {
          leaveDetail.value = null
        }
      }
    }
  } catch {
    template.value = null
  } finally {
    loading.value = false
  }
}

function isWideField(type: string) {
  return isTextareaField(type) || isCheckboxField(type) || isRadioField(type)
}

function isTextareaField(type: string) {
  return type === 'textarea'
}

function isSelectField(type: string) {
  return type === 'select'
}

function isRadioField(type: string) {
  return type === 'radio'
}

function isCheckboxField(type: string) {
  return type === 'checkbox' || type === 'checkbox-group'
}

function isNumberField(type: string) {
  return type === 'number' || type === 'input-number'
}

function isSwitchField(type: string) {
  return type === 'switch'
}

function isDateField(type: string) {
  return type === 'date' || type === 'datetime'
}

function datePickerType(type: string): 'date' | 'datetime' {
  return type === 'date' ? 'date' : 'datetime'
}

function stringValue(fieldName: string) {
  return formatApprovalFormValue(rawData.value[fieldName])
}

function scalarValue(fieldName: string) {
  const rawValue = rawData.value[fieldName]
  return isPrimitiveValue(rawValue) ? rawValue : undefined
}

function numberValue(fieldName: string) {
  const rawValue = rawData.value[fieldName]
  if (rawValue === null || rawValue === undefined || rawValue === '') return undefined
  const value = Number(rawValue)
  return Number.isNaN(value) ? undefined : value
}

function checkboxValue(fieldName: string): Array<string | number> {
  const rawValue = rawData.value[fieldName]
  if (Array.isArray(rawValue)) return rawValue.flatMap(normalizeCheckboxValue)
  if (typeof rawValue === 'string') {
    return rawValue.split(',').map((item) => item.trim()).filter(Boolean)
  }
  return normalizeCheckboxValue(rawValue)
}

function normalizeCheckboxValue(value: unknown): Array<string | number> {
  if (typeof value === 'string' || typeof value === 'number') return [value]
  if (typeof value === 'boolean') return [String(value)]
  return []
}

function switchValue(fieldName: string) {
  const rawValue = rawData.value[fieldName]
  return rawValue === true || rawValue === 1 || rawValue === '1' || rawValue === 'true'
}

function dateValue(fieldName: string) {
  const rawValue = rawData.value[fieldName]
  if (!rawValue) return undefined
  const value = new Date(String(rawValue))
  return Number.isNaN(value.getTime()) ? undefined : value
}

function hasDateValue(fieldName: string) {
  return Boolean(dateValue(fieldName))
}

function isPrimitiveValue(value: unknown): value is string | number | boolean {
  return typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean'
}

watch(
  [() => props.templateId, () => props.formData],
  () => {
    void loadTemplateContext()
  },
  { immediate: true },
)
</script>

<style scoped lang="scss">
.approval-form-detail {
  min-height: 120px;

  &__form :deep(.el-input-number .el-input__wrapper) {
    width: 100%;
  }
}
</style>
