<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div class="toolbar__search">
          <el-input v-model="searchTitle" placeholder="搜索标题" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData" />
          <el-button type="primary" style="margin-left: 10px" @click="loadData">搜索</el-button>
        </div>
      </div>
      <el-table :data="tasks" stripe v-loading="loading">
        <el-table-column prop="instanceTitle" label="申请标题" />
        <el-table-column prop="taskName" label="审批节点" width="120" />
        <el-table-column prop="formDataSummary" label="表单摘要" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="接收时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.approvalInstanceId)">查看</el-button>
            <el-button link type="success" @click="openAction(row, 1)">通过</el-button>
            <el-button link type="danger" @click="openAction(row, 2)">驳回</el-button>
            <el-button link type="warning" @click="openTransfer(row.id)">转办</el-button>
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

    <el-dialog v-model="actionDialogVisible" :title="actionResult === 1 ? '审批通过' : '审批驳回'" width="400px" destroy-on-close>
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

    <el-dialog v-model="transferVisible" title="转办任务" width="450px" destroy-on-close>
      <el-form :model="transferForm" label-width="80px">
        <el-form-item label="目标用户ID" required>
          <el-input v-model.number="transferForm.targetUserId" placeholder="请输入目标用户ID" />
        </el-form-item>
        <el-form-item label="转办原因">
          <el-input v-model="transferForm.reason" type="textarea" :rows="3" placeholder="请输入转办原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="warning" @click="handleTransfer">确定转办</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyTodoPaged, approveTask, transferTask } from '@/api/approval'
import type { TaskVO } from '@/types'

const router = useRouter()
const loading = ref(false)
const tasks = ref<TaskVO[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchTitle = ref('')

// Approve/reject dialog state
const actionDialogVisible = ref(false)
const actionResult = ref(1)
const actionComment = ref('')
const currentTaskId = ref(0)

// Transfer dialog state
const transferVisible = ref(false)
const transferForm = reactive({ taskId: 0, targetUserId: undefined as number | undefined, reason: '' })

async function loadData() {
  loading.value = true
  try {
    const result = await getMyTodoPaged({
      title: searchTitle.value || undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    tasks.value = result?.list ?? []
    total.value = result?.total ?? 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function viewDetail(instanceId: number) {
  router.push(`/approval/instance/${instanceId}`)
}

function openAction(task: TaskVO, result: number) {
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
  } catch {
    // handled by interceptor
  }
}

function openTransfer(taskId: number) {
  transferForm.taskId = taskId
  transferForm.targetUserId = undefined
  transferForm.reason = ''
  transferVisible.value = true
}

async function handleTransfer() {
  if (!transferForm.targetUserId) return
  try {
    await transferTask(transferForm.taskId, {
      targetUserId: transferForm.targetUserId,
      reason: transferForm.reason,
    })
    ElMessage.success('转办成功')
    transferVisible.value = false
    loadData()
  } catch {
    // handled by interceptor
  }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.toolbar__search { display: flex; align-items: center; }
.pagination { display: flex; justify-content: flex-end; margin-top: 15px; }
</style>
