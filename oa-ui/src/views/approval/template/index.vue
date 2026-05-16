<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div></div>
        <el-button type="primary" @click="openDialog()">创建模板</el-button>
      </div>
      <el-table :data="templates" stripe v-loading="loading">
        <el-table-column prop="templateName" label="模板名称" min-width="140" />
        <el-table-column prop="templateKey" label="模板标识" min-width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === TEMPLATE_STATUS.PUBLISHED ? 'success' : 'info'">
              {{ TEMPLATE_STATUS_LABEL_MAP[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDesigner(row)">
              {{ row.status === TEMPLATE_STATUS.PUBLISHED ? '查看流程' : '设计流程' }}
            </el-button>
            <el-button
              v-if="row.status !== TEMPLATE_STATUS.PUBLISHED"
              link
              type="primary"
              @click="openDialog(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status !== TEMPLATE_STATUS.PUBLISHED"
              link
              type="success"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === TEMPLATE_STATUS.PUBLISHED"
              link
              type="warning"
              @click="handleNewVersion(row)"
            >
              新建版本
            </el-button>
            <el-popconfirm
              v-if="row.status !== TEMPLATE_STATUS.PUBLISHED"
              title="确认删除此模板?"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
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
import type { reactive as _reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTemplates, createTemplate } from '@/api/approval'
import { publishTemplate, createNewVersion, deleteTemplate, updateTemplate } from '@/api/template'
import { TEMPLATE_STATUS, TEMPLATE_STATUS_LABEL_MAP } from '@/bpmn/constants'

const router = useRouter()

const loading = ref(false)
const templates = ref<any[]>([])
const dialogVisible = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })
const editing = ref<any>(null)
const form = reactive({ templateName: '', templateKey: '', formConfig: '' })

async function loadData() {
  loading.value = true
  try {
    const data: any = await getTemplates({ page: pagination.page, size: pagination.size })
    if (Array.isArray(data)) {
      templates.value = data
      pagination.total = data.length
    } else {
      templates.value = data?.records ?? data?.list ?? []
      pagination.total = data?.total ?? templates.value.length
    }
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
    if (editing.value) {
      await updateTemplate(editing.value.id, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createTemplate({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { /* handled by interceptor */ }
}

function openDesigner(row: any) {
  router.push(`/approval/template/designer/${row.id}`)
}

async function handlePublish(row: any) {
  try {
    await ElMessageBox.confirm('发布后流程将不可修改, 确认发布?', '发布确认', {
      confirmButtonText: '确认发布',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await publishTemplate(row.id)
    ElMessage.success('发布成功')
    loadData()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

async function handleNewVersion(row: any) {
  try {
    const data: any = await createNewVersion(row.id)
    const newId = data?.id ?? data
    ElMessage.success('新版本已创建')
    router.push(`/approval/template/designer/${newId}`)
  } catch {
    ElMessage.error('创建新版本失败')
  }
}

async function handleDelete(row: any) {
  try {
    await deleteTemplate(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 15px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 15px; }
</style>
