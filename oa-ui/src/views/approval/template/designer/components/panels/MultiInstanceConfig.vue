<template>
  <div class="multi-instance-config">
    <el-form-item label="审批方式">
      <el-radio-group
        :model-value="miType"
        :disabled="readOnly"
        @update:model-value="handleTypeChange($event as string)"
      >
        <el-radio
          v-for="opt in MULTI_INSTANCE_TYPE_OPTIONS"
          :key="opt.value"
          :value="opt.value"
        >
          {{ opt.label }}
        </el-radio>
      </el-radio-group>
    </el-form-item>

    <template v-if="miType === 'countersign' || miType === 'orSign'">
      <el-form-item label="集合表达式">
        <el-input
          :model-value="collection"
          placeholder="assigneeList"
          :disabled="readOnly"
          @update:model-value="collection = $event"
        />
        <div class="multi-instance-config__hint">
          Flowable 运行时解析为用户列表的表达式
        </div>
      </el-form-item>

      <el-form-item label="元素变量">
        <el-input
          :model-value="elementVariable"
          placeholder="assignee"
          :disabled="readOnly"
          @update:model-value="elementVariable = $event"
        />
      </el-form-item>

      <el-form-item v-if="miType === 'countersign'" label="完成比例">
        <el-input-number
          :model-value="completionRatio"
          :min="1"
          :max="100"
          :step="10"
          :disabled="readOnly"
          @update:model-value="completionRatio = $event as number"
        />
        <span class="multi-instance-config__unit">%</span>
      </el-form-item>

      <el-form-item label="完成条件">
        <el-input :model-value="completionCondition" readonly type="textarea" :rows="2" />
      </el-form-item>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { MULTI_INSTANCE_TYPE_OPTIONS } from '@/bpmn/constants'

const props = defineProps<{
  element: any
  modeler: any
  readOnly: boolean
}>()

const miType = ref('none')
const collection = ref('assigneeList')
const elementVariable = ref('assignee')
const completionRatio = ref(100)

const completionCondition = computed(() => {
  if (miType.value === 'countersign') {
    if (completionRatio.value === 100) {
      return '${nrOfCompletedInstances == nrOfInstances}'
    }
    const ratio = (completionRatio.value / 100).toFixed(2)
    return `\${nrOfCompletedInstances / nrOfInstances >= ${ratio}}`
  }
  if (miType.value === 'orSign') {
    return '${nrOfCompletedInstances == 1}'
  }
  return ''
})

// Read existing multi-instance config from element on mount
watch(
  () => props.element,
  (el) => {
    if (!el?.businessObject) return
    const bo = el.businessObject

    // Check if element has multiInstanceLoopCharacteristics
    const miChar = bo.loopCharacteristics
    if (miChar) {
      const isSequential = miChar.isSequential
      const completionCond = miChar.completionCondition?.body || ''

      if (!isSequential) {
        if (completionCond.includes('nrOfCompletedInstances == 1')) {
          miType.value = 'orSign'
        } else {
          miType.value = 'countersign'
        }
      }

      // Read collection and element variable from flowable extensions
      const attrs = miChar.$attrs || {}
      collection.value = String(attrs['flowable:collection'] || miChar.collection || 'assigneeList')
      elementVariable.value = String(attrs['flowable:elementVariable'] || miChar.elementVariable || 'assignee')
    } else {
      miType.value = 'none'
    }
  },
  { immediate: true },
)

function handleTypeChange(type: string) {
  miType.value = type
  updateMultiInstance()
}

function updateMultiInstance() {
  if (props.readOnly || !props.modeler) return
  const modeling = props.modeler.get('modeling')
  const moddle = props.modeler.get('moddle')

  if (miType.value === 'none') {
    // Remove multi-instance characteristics
    modeling.updateProperties(props.element, {
      loopCharacteristics: undefined,
    })
    return
  }

  // Create or update multi-instance loop characteristics
  const bo = props.element.businessObject

  const condBody = completionCondition.value

  const completionConditionEl = condBody
    ? moddle.create('bpmn:FormalExpression', { body: condBody })
    : undefined

  const miChar = moddle.create('bpmn:MultiInstanceLoopCharacteristics', {
    isSequential: false,
    collection: collection.value,
    elementVariable: elementVariable.value,
    completionCondition: completionConditionEl,
  })

  modeling.updateProperties(props.element, {
    loopCharacteristics: miChar,
  })
}
</script>

<style scoped lang="scss">
.multi-instance-config {
  &__hint {
    font-size: 11px;
    color: #909399;
    margin-top: 4px;
  }

  &__unit {
    margin-left: 8px;
    color: #606266;
  }
}
</style>
