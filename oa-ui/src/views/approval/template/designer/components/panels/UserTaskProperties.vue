<template>
  <el-form label-width="80px" size="small">
    <el-form-item label="节点 ID">
      <el-input :model-value="element.businessObject.id" disabled />
    </el-form-item>
    <el-form-item label="节点名称">
      <el-input
        :model-value="element.businessObject.name"
        placeholder="请输入节点名称"
        :disabled="readOnly"
        @update:model-value="updateName($event)"
      />
    </el-form-item>

    <el-divider content-position="left">审批人配置</el-divider>

    <AssigneeConfig
      :element="element"
      :modeler="modeler"
      :read-only="readOnly"
    />

    <el-divider content-position="left">多实例配置</el-divider>

    <MultiInstanceConfig
      :element="element"
      :modeler="modeler"
      :read-only="readOnly"
    />
  </el-form>
</template>

<script setup lang="ts">
import AssigneeConfig from './AssigneeConfig.vue'
import MultiInstanceConfig from './MultiInstanceConfig.vue'

const props = defineProps<{
  element: any
  modeler: any
  templateId: number
  readOnly: boolean
}>()

function updateName(name: string) {
  if (props.readOnly || !props.modeler) return
  const modeling = props.modeler.get('modeling')
  modeling.updateProperties(props.element, { name })
}
</script>
