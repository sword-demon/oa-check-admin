<template>
  <div class="page-shell">
    <section class="page-header">
      <div class="page-header__titles">
        <p class="page-subtitle page-subtitle--eyebrow">Approval Workspace</p>
        <h1 class="page-title">我的申请</h1>
        <p class="page-subtitle">查看当前申请状态、发起新流程，并在审批中时执行撤回。</p>
      </div>
    </section>

    <el-card class="page-panel">
      <div class="page-toolbar">
        <div class="page-toolbar__filters">
          <el-input v-model="searchTitle" class="field--lg" placeholder="搜索标题" clearable @clear="loadData" @keyup.enter="loadData" />
          <el-select v-model="searchStatus" class="field--sm" placeholder="状态" clearable @change="loadData">
            <el-option label="审批中" :value="ApprovalInstanceStatus.PENDING" />
            <el-option label="通过" :value="ApprovalInstanceStatus.APPROVED" />
            <el-option label="驳回" :value="ApprovalInstanceStatus.REJECTED" />
            <el-option label="已撤回" :value="ApprovalInstanceStatus.WITHDRAWN" />
          </el-select>
        </div>
        <div class="page-toolbar__actions">
          <span class="page-toolbar__meta">共 {{ total }} 条申请</span>
          <el-button type="primary" @click="submitDialogVisible = true">发起申请</el-button>
        </div>
      </div>
      <el-table :data="instances" stripe v-loading="loading" class="page-table">
        <el-table-column prop="instanceTitle" label="申请标题" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" />
        <el-table-column prop="endAt" label="结束时间" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.id)">查看</el-button>
            <el-button v-if="row.status === ApprovalInstanceStatus.PENDING" link type="warning" @click="handleWithdraw(row.id)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="page-pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="submitDialogVisible" title="发起审批" width="550px" destroy-on-close>
      <el-form :model="submitForm" label-width="80px">
        <el-form-item label="审批模板" required>
          <el-select v-model="submitForm.templateId" placeholder="选择模板" style="width: 100%" @change="resetDynamicForm">
            <el-option v-for="t in templateList" :key="t.id" :label="t.templateName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请标题" required>
          <el-input v-model="submitForm.title" />
        </el-form-item>
        <ApprovalDynamicForm
          v-if="selectedTemplateFields.length"
          v-model="dynamicFormData"
          :schema="selectedTemplate?.formConfig"
          label-width="90px"
        />
        <el-form-item v-else label="表单数据">
          <el-input v-model="submitForm.formData" type="textarea" :rows="4" placeholder="JSON 格式表单数据" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitApproval, withdrawInstance, getTemplates, getMyApplications } from '@/api/approval'
import type { ApprovalInstance, ProcessTemplate } from '@/types'
import { ApprovalInstanceStatus, TemplateStatus } from '@/types'
import ApprovalDynamicForm from '@/components/approval/ApprovalDynamicForm.vue'
import { parseApprovalFormConfig } from '@/utils/approval-form'

const router = useRouter()
const loading = ref(false)
const instances = ref<ApprovalInstance[]>([])
const templateList = ref<ProcessTemplate[]>([])
const submitDialogVisible = ref(false)
const submitForm = reactive({ templateId: undefined as number | undefined, title: '', formData: '{}' })
const dynamicFormData = ref<Record<string, unknown>>({})

const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchTitle = ref('')
const searchStatus = ref<number | undefined>(undefined)
const selectedTemplate = computed(() => templateList.value.find((item) => item.id === submitForm.templateId) || null)
const selectedTemplateFields = computed(() => parseApprovalFormConfig(selectedTemplate.value?.formConfig))

function statusType(status: ApprovalInstanceStatus): 'primary' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<number, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    [ApprovalInstanceStatus.PENDING]: 'info',
    [ApprovalInstanceStatus.APPROVED]: 'success',
    [ApprovalInstanceStatus.REJECTED]: 'danger',
    [ApprovalInstanceStatus.WITHDRAWN]: 'warning',
    [ApprovalInstanceStatus.CANCELLED]: 'info',
  }
  return map[status] || 'info'
}

function statusLabel(status: ApprovalInstanceStatus) {
  const map: Record<number, string> = {
    [ApprovalInstanceStatus.PENDING]: '审批中',
    [ApprovalInstanceStatus.APPROVED]: '通过',
    [ApprovalInstanceStatus.REJECTED]: '驳回',
    [ApprovalInstanceStatus.WITHDRAWN]: '已撤回',
    [ApprovalInstanceStatus.CANCELLED]: '已终止',
  }
  return map[status] || '未知'
}

async function loadData() {
  loading.value = true
  try {
    const result = await getMyApplications({
      title: searchTitle.value || undefined,
      status: searchStatus.value,
      page: page.value,
      pageSize: pageSize.value,
    })
    instances.value = result.list || []
    total.value = result.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function loadTemplates() {
  const data = await getTemplates({
    status: TemplateStatus.PUBLISHED,
    page: 1,
    pageSize: 100,
  })
  templateList.value = data.list || []
}

async function handleSubmit() {
  if (!submitForm.templateId || !submitForm.title) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    const formData = selectedTemplateFields.value.length
      ? JSON.stringify(dynamicFormData.value)
      : submitForm.formData
    await submitApproval({ templateId: submitForm.templateId, title: submitForm.title, formData })
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    Object.assign(submitForm, { templateId: undefined, title: '', formData: '{}' })
    dynamicFormData.value = {}
    loadData()
  } catch {
    // handled by interceptor
  }
}

function resetDynamicForm() {
  dynamicFormData.value = {}
}

async function handleWithdraw(id: number) {
  try {
    await withdrawInstance(id)
    ElMessage.success('已撤回')
    loadData()
  } catch {
    // handled by interceptor
  }
}

function viewDetail(id: number) {
  router.push(`/approval/instance/${id}`)
}

onMounted(() => {
  loadData()
  loadTemplates()
})
</script>
