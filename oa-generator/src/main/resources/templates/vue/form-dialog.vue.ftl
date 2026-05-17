<template>
  <el-dialog
    :model-value="visible"
    :title="${ctx.entity.beanName}Data ? '编辑${ctx.entity.comment}' : '新增${ctx.entity.comment}'"
    width="550px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
    @close="resetForm"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
  <#list ctx.entity.fields as field>
      <el-form-item label="${field.comment}" <#if !field.nullable>prop="${field.name}" <#else/></#if>required="${(!field.nullable)?c}">
        <#if field.enumRef?? && ctx.enums[field.enumRef]??>
        <el-select v-model="form.${field.name}" placeholder="请选择${field.comment}">
          <#list ctx.enums[field.enumRef].values as enumVal>
          <el-option label="${enumVal.label}" :value="${enumVal.code}" />
          </#list>
        </el-select>
        <#elseif field.type == "LocalDateTime">
        <el-date-picker
          v-model="form.${field.name}"
          type="datetime"
          placeholder="选择${field.comment}"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DDTHH:mm:ss"
        />
        <#elseif field.type == "String" && field.sqlType == "TEXT">
        <el-input v-model="form.${field.name}" type="textarea" :rows="3" placeholder="请输入${field.comment}" />
        <#else>
        <el-input v-model="form.${field.name}" placeholder="请输入${field.comment}" />
        </#if>
      </el-form-item>
  </#list>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { create${ctx.entity.name}, update${ctx.entity.name} } from '@/api/${ctx.config.module}'

const props = defineProps<{
  visible: boolean
  ${ctx.entity.beanName}Data: any
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'saved'): void
}>()

const formRef = ref<FormInstance>()
const form = reactive({
<#list ctx.entity.fields as field>
  ${field.name}: '' as <#if field.type == "Integer">number<#else/>string</#if>,
</#list>
})

const rules: FormRules = {
<#list ctx.entity.fields as field>
  <#if !field.nullable>
  ${field.name}: [{ required: true, message: '请${r"${field.type == \"Integer\" ? \"选择\" : \"输入\"}"}${field.comment}', trigger: '<#if field.enumRef??>change<#else/>blur</#if>' }],
  </#if>
</#list>
}

watch(() => props.visible, (val) => {
  if (val && props.${ctx.entity.beanName}Data) {
    Object.assign(form, {
  <#list ctx.entity.fields as field>
      ${field.name}: props.${ctx.entity.beanName}Data.${field.name} ?? '',
  </#list>
    })
  }
})

function resetForm() {
  Object.assign(form, {
  <#list ctx.entity.fields as field>
    ${field.name}: '',
  </#list>
  })
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    if (props.${ctx.entity.beanName}Data) {
      await update${ctx.entity.name}(props.${ctx.entity.beanName}Data.id, { ...form })
      ElMessage.success('更新成功')
    } else {
      await create${ctx.entity.name}({ ...form })
      ElMessage.success('创建成功')
    }
    emit('update:visible', false)
    emit('saved')
  } catch {
    // interceptor handles error
  }
}
</script>
