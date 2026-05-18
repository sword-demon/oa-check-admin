<template>
  <div class="page-shell">
    <section class="page-header">
      <div class="page-header__titles">
        <p class="page-subtitle page-subtitle--eyebrow">Pending Queue</p>
        <h1 class="page-title">我的待办</h1>
        <p class="page-subtitle">集中处理等待审批的任务节点，支持通过、驳回和转办。</p>
      </div>
    </section>

    <el-card class="page-panel">
      <div class="toolbar">
        <div class="search">
          <el-input v-model="searchTitle" placeholder="搜索标题" clearable style="width: 200px" @clear="handleSearch" @keyup.enter="handleSearch" />
          <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
        </div>
      </div>
      <el-table :data="tasks" stripe v-loading="loading" class="page-table">
        <el-table-column prop="instanceTitle" label="申请标题" />
        <el-table-column prop="taskName" label="审批节点" width="120" />
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
        <el-form-item label="目标用户" required>
          <el-select
            v-model="transferForm.targetUserId"
            placeholder="请选择转办目标"
            clearable
            filterable
            :loading="transferUserLoading"
            no-data-text="暂无可转办用户"
            style="width: 100%"
          >
            <el-option
              v-for="user in transferUserOptions"
              :key="user.id"
              :label="userOptionLabel(user)"
              :value="user.id"
              :disabled="user.id === currentUserId"
            />
          </el-select>
          <div class="transfer-hint">当前登录用户不可转办给自己。</div>
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
import { computed, ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyTodoPaged, approveTask, transferTask } from '@/api/approval'
import { getUserList } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { CommonStatus, type SysUser, type TaskVO } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tasks = ref<TaskVO[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchTitle = ref('')
const transferUserLoading = ref(false)
const transferUserOptions = ref<SysUser[]>([])
const transferUsersLoaded = ref(false)
const currentUserId = computed(() => Number(userStore.userInfo?.id || 0) || null)
const TRANSFER_USER_PAGE_SIZE = 500

// Approve/reject dialog state
const actionDialogVisible = ref(false)
const actionResult = ref(1)
const actionComment = ref('')
const currentTaskId = ref(0)

// Transfer dialog state
const transferVisible = ref(false)
const transferForm = reactive({ taskId: 0, targetUserId: undefined as number | undefined, reason: '' })

function userLabel(user: SysUser) {
  return user.nickname ? `${user.nickname}（${user.username}）` : user.username
}

function userOptionLabel(user: SysUser) {
  const label = userLabel(user)
  return user.id === currentUserId.value ? `${label}（当前登录用户，不可转办）` : label
}

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

function handleSearch() {
  page.value = 1
  loadData()
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

async function loadTransferUsers(force = false) {
  if (transferUserLoading.value || (transferUsersLoaded.value && !force)) return

  transferUserLoading.value = true
  try {
    const data = await getUserList({
      status: CommonStatus.ACTIVE,
      page: 1,
      pageSize: TRANSFER_USER_PAGE_SIZE,
    })
    transferUserOptions.value = data.list || []
    transferUsersLoaded.value = true
  } catch {
    transferUserOptions.value = []
  } finally {
    transferUserLoading.value = false
  }
}

async function openTransfer(taskId: number) {
  transferForm.taskId = taskId
  transferForm.targetUserId = undefined
  transferForm.reason = ''
  await loadTransferUsers()
  transferVisible.value = true
}

async function handleTransfer() {
  if (!transferForm.targetUserId) {
    ElMessage.warning('请选择目标用户')
    return
  }
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
.transfer-hint {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.4;
  color: #909399;
}
</style>
