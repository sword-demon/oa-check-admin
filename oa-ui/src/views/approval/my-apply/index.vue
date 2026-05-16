<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div></div>
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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="warning" @click="handleWithdraw(row.id)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
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
          <el-input v-model="submitForm.formData" type="textarea" :rows="4" placeholder='JSON格式表单数据' />
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
import { ElMessage } from 'element-plus'
import { submitApproval, withdrawInstance, getTemplates } from '@/api/approval'

const loading = ref(false)
const instances = ref<any[]>([])
const templateList = ref<any[]>([])
const submitDialogVisible = ref(false)
const submitForm = reactive({ templateId: undefined as number | undefined, title: '', formData: '{}' })

function statusType(status: number): 'primary' | 'success' | 'warning' | 'info' | 'danger' | undefined {
  const map: Record<number, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = { 1: 'info', 2: 'success', 3: 'danger', 4: 'info', 5: 'warning' }
  return map[status] || 'info'
}

function statusLabel(status: number) {
  const map: Record<number, string> = { 1: '审批中', 2: '通过', 3: '驳回', 4: '已撤回', 5: '已终止' }
  return map[status] || '未知'
}

async function loadTemplates() {
  const data: any = await getTemplates()
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
  } catch { /* handled */ }
}

async function handleWithdraw(id: number) {
  try {
    await withdrawInstance(id)
    ElMessage.success('已撤回')
  } catch { /* handled */ }
}

onMounted(loadTemplates)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 15px; }
</style>
