<template>
  <div class="condition-editor">
    <div class="condition-editor__header">
      <span class="condition-editor__target">
        目标: {{ targetName || flow.targetRef?.id || '未知' }}
      </span>
    </div>

    <el-form-item label="条件表达式" label-width="90px" size="small">
      <div class="condition-editor__input-row">
        <el-input
          :model-value="conditionExpression"
          placeholder="${approved == true}"
          :disabled="readOnly"
          @update:model-value="updateCondition($event)"
        />
        <el-dropdown v-if="formFields.length > 0" trigger="click" @command="insertField">
          <el-button size="small" :disabled="readOnly">
            <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="field in formFields"
                :key="field.name"
                :command="field.name"
              >
                {{ field.label || field.name }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <div class="condition-editor__hint">
        支持 UEL 表达式, 如: ${leave_days > 3}, ${approved == true}
      </div>
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import type { ApprovalFormField } from '@/utils/approval-form'

const props = defineProps<{
  flow: any
  modeler: any
  readOnly: boolean
  formFields: ApprovalFormField[]
}>()

const conditionExpression = ref('')

const targetName = computed(() => {
  return props.flow.targetRef?.name || ''
})

watch(
  () => props.flow,
  (flow) => {
    if (!flow) {
      conditionExpression.value = ''
      return
    }

    const condExpr = flow.conditionExpression
    if (condExpr) {
      conditionExpression.value = condExpr.body || ''
    } else {
      conditionExpression.value = ''
    }
  },
  { immediate: true },
)

function updateCondition(expression: string) {
  if (props.readOnly || !props.modeler) return
  conditionExpression.value = expression

  const modeling = props.modeler.get('modeling')
  const moddle = props.modeler.get('moddle')

  // Find the flow element in the element registry
  const elementRegistry = props.modeler.get('elementRegistry')
  const flowElement = elementRegistry.filter(
    (el: any) => el.businessObject === props.flow,
  )[0]

  if (!flowElement) return

  if (!expression) {
    // Remove condition expression
    modeling.updateModdleProperties(flowElement, props.flow, {
      conditionExpression: undefined,
    })
    return
  }

  const formalExpression = moddle.create('bpmn:FormalExpression', {
    body: expression,
  })

  modeling.updateModdleProperties(flowElement, props.flow, {
    conditionExpression: formalExpression,
  })
}

function insertField(fieldName: string) {
  const current = conditionExpression.value || ''
  const expr = current
    ? `${current} \${${fieldName}}`
    : `\${${fieldName}}`
  updateCondition(expr)
}
</script>

<style scoped lang="scss">
.condition-editor {
  &__header {
    margin-bottom: 8px;
  }

  &__target {
    font-size: 12px;
    color: #606266;
    font-weight: 500;
  }

  &__hint {
    font-size: 11px;
    color: #909399;
    margin-top: 4px;
  }

  &__input-row {
    display: flex;
    gap: 4px;
    width: 100%;
  }
}
</style>
