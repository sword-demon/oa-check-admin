<template>
  <el-form label-width="80px" size="small">
    <el-form-item label="节点 ID">
      <el-input :model-value="element.businessObject.id" disabled />
    </el-form-item>
    <el-form-item label="节点名称">
      <el-input
        :model-value="element.businessObject.name"
        placeholder="请输入网关名称"
        :disabled="readOnly"
        @update:model-value="updateName($event)"
      />
    </el-form-item>

    <template v-if="outgoingFlows.length > 0">
      <el-divider content-position="left">条件分支配置</el-divider>

      <div v-for="flow in outgoingFlows" :key="flow.id" class="gateway-properties__flow">
        <ConditionEditor
          :flow="flow"
          :modeler="modeler"
          :read-only="readOnly"
        />
      </div>
    </template>

    <template v-else>
      <el-empty description="请连接出口线" :image-size="60" />
    </template>
  </el-form>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ConditionEditor from './ConditionEditor.vue'

const props = defineProps<{
  element: any
  modeler: any
  templateId: number
  readOnly: boolean
}>()

const outgoingFlows = computed(() => {
  const bo = props.element?.businessObject
  if (!bo?.outgoing) return []
  return Array.isArray(bo.outgoing) ? bo.outgoing : [bo.outgoing]
})

function updateName(name: string) {
  if (props.readOnly || !props.modeler) return
  const modeling = props.modeler.get('modeling')
  modeling.updateProperties(props.element, { name })
}
</script>

<style scoped lang="scss">
.gateway-properties__flow {
  margin-bottom: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
