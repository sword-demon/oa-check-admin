<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div class="toolbar__search">
          <el-input v-model="searchTitle" placeholder="搜索标题" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData" />
          <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 120px; margin-left: 10px" @change="loadData">
            <el-option label="审批中" :value="ApprovalInstanceStatus.PENDING" />
            <el-option label="通过" :value="ApprovalInstanceStatus.APPROVED" />
            <el-option label="驳回" :value="ApprovalInstanceStatus.REJECTED" />
            <el-option label="已撤回" :value="ApprovalInstanceStatus.WITHDRAWN" />
          </el-select>
        </div>
        <el-button type="primary" @click="submitDialogVisible = true">发起申请</el-button>
      </div>
      <el-table :data="instances" stripe v-loading="loading">
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
      <div class="pagination">
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
          <el-select v-model="submitForm.templateId" placeholder="选择模板" style="width: 100%">
            <el-option v-for="t in templateList" :key="t.id" :label="t.templateName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请标题" required>
          <el-input v-model="submitForm.title" />
        </el-form-item>
        <el-form-item label="表单数据">
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitApproval, withdrawInstance, getTemplates, getMyApplications } from '@/api/approval'
import type { ApprovalInstance } from '@/types'
import { ApprovalInstanceStatus } from '@/types'

const router = useRouter()
const loading = ref(false)
const instances = ref<ApprovalInstance[]>([])
const templateList = ref<{ id: number; templateName: string }[]>([])
const submitDialogVisible = ref(false)
const submitForm = reactive({ templateId: undefined as number | undefined, title: '', formData: '{}' })

const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchTitle = ref('')
const searchStatus = ref<number | undefined>(undefined)

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
  const data = await getTemplates()
  templateList.value = Array.isArray(data) ? data : []
}

async function handleSubmit() {
  if (!submitForm.templateId || !submitForm.title) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    await submitApproval({ templateId: submitForm.templateId, title: submitForm.title, formData: submitForm.formData })
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    Object.assign(submitForm, { templateId: undefined, title: '', formData: '{}' })
    loadData()
  } catch {
    // handled by interceptor
  }
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

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.toolbar__search { display: flex; align-items: center; }
.pagination { display: flex; justify-content: flex-end; margin-top: 15px; }
</style>
