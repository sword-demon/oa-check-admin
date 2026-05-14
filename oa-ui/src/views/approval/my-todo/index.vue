<template>
  <div>
    <el-card>
      <template #header>我的待办</template>
      <el-table :data="tasks" stripe v-loading="loading">
        <el-table-column prop="taskName" label="审批节点" />
        <el-table-column prop="approvalInstanceId" label="审批实例ID" width="120" />
        <el-table-column prop="createdAt" label="接收时间" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="openAction(row, 1)">通过</el-button>
            <el-button link type="danger" @click="openAction(row, 2)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="actionDialogVisible" :title="actionResult === 1 ? '审批通过' : '审批驳回'" width="400px">
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="actionComment" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionDialogVisible = false">取消</el-button>
        <el-button :type="actionResult === 1 ? 'success' : 'danger'" @click="handleAction">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyTodo, approveTask } from '@/api/approval'

const loading = ref(false)
const tasks = ref<any[]>([])
const actionDialogVisible = ref(false)
const actionResult = ref(1)
const actionComment = ref('')
const currentTaskId = ref(0)

async function loadData() {
  loading.value = true
  try {
    const data: any = await getMyTodo()
    tasks.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

function openAction(task: any, result: number) {
  currentTaskId.value = task.id
  actionResult.value = result
  actionComment.value = ''
  actionDialogVisible.value = true
}

async function handleAction() {
  try {
    await approveTask(currentTaskId.value, { result: actionResult.value, comment: actionComment.value })
    ElMessage.success(actionResult.value === 1 ? '已通过' : '已驳回')
    actionDialogVisible.value = false
    loadData()
  } catch { /* handled */ }
}

onMounted(loadData)
</script>
