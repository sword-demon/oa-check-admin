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
  </el-form>
</template>

<script setup lang="ts">
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
