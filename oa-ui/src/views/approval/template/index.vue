<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div></div>
        <el-button type="primary" @click="openDialog()">创建模板</el-button>
      </div>
      <el-table :data="templates" stripe v-loading="loading">
        <el-table-column prop="templateName" label="模板名称" />
        <el-table-column prop="templateKey" label="模板标识" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : 'info'">{{ row.status === 2 ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑模板' : '创建模板'" width="650px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="模板名称" required>
          <el-input v-model="form.templateName" />
        </el-form-item>
        <el-form-item label="模板标识" required>
          <el-input v-model="form.templateKey" :disabled="!!editing" placeholder="如 leave_request" />
        </el-form-item>
        <el-form-item label="表单配置">
          <el-input v-model="form.formConfig" type="textarea" :rows="6" placeholder='JSON格式, 如: {"fields":[{"name":"reason","type":"textarea","label":"原因","required":true}]}' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTemplates, createTemplate } from '@/api/approval'

const loading = ref(false)
const templates = ref<any[]>([])
const dialogVisible = ref(false)
const editing = ref<any>(null)
const form = reactive({ templateName: '', templateKey: '', formConfig: '' })

async function loadData() {
  loading.value = true
  try {
    const data: any = await getTemplates()
    templates.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

function openDialog(tpl?: any) {
  editing.value = tpl || null
  if (tpl) {
    Object.assign(form, { templateName: tpl.templateName, templateKey: tpl.templateKey, formConfig: tpl.formConfig || '' })
  } else {
    Object.assign(form, { templateName: '', templateKey: '', formConfig: '' })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    await createTemplate({ ...form })
    ElMessage.success(editing.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch { /* handled by interceptor */ }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 15px; }
</style>
