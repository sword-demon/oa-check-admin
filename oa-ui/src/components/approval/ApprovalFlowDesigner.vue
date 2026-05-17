<template>
  <LowflowApprovalDesigner
    :model-value="modelValue"
    :form-schema="formSchema"
    :readonly="readonly"
    @update:model-value="(value) => emit('update:modelValue', value)"
  />
</template>

<script setup lang="ts">
import type { ApprovalFlowNode } from '@/utils/approval-flow'
import LowflowApprovalDesigner from './lowflow/LowflowApprovalDesigner.vue'

withDefaults(defineProps<{
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
</script>
