<template>
  <div class="properties-panel">
    <div class="properties-panel__header">
      <span class="properties-panel__title">属性配置</span>
    </div>

    <div v-if="!selectedElement" class="properties-panel__empty">
      <el-empty description="请在画布中选择节点" :image-size="80" />
    </div>

    <div v-else class="properties-panel__content">
      <component
        :is="currentPanel"
        :element="selectedElement"
        :modeler="modeler"
        :template-id="templateId"
        :read-only="readOnly"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import EmptyState from './panels/EmptyState.vue'
import StartEndProperties from './panels/StartEndProperties.vue'
import UserTaskProperties from './panels/UserTaskProperties.vue'
import GatewayProperties from './panels/GatewayProperties.vue'

const props = defineProps<{
  selectedElement: any
  modeler: any
  templateId: number
  readOnly: boolean
}>()

const panelMap: Record<string, any> = {
  'bpmn:StartEvent': StartEndProperties,
  'bpmn:EndEvent': StartEndProperties,
  'bpmn:UserTask': UserTaskProperties,
  'bpmn:ExclusiveGateway': GatewayProperties,
  'bpmn:ParallelGateway': GatewayProperties,
}

const currentPanel = computed(() => {
  if (!props.selectedElement?.businessObject) return EmptyState
  const type = props.selectedElement.businessObject.$type
  return panelMap[type] || EmptyState
})
</script>

<style scoped lang="scss">
.properties-panel {
  display: flex;
  flex-direction: column;

  &__header {
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
    flex-shrink: 0;
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }

  &__empty {
    padding: 40px 16px;
  }

  &__content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
  }
}
</style>
