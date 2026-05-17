<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-input v-model="filters.title" placeholder="搜索标题" clearable style="width: 160px" @clear="loadData" @keyup.enter="loadData" />
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px; margin-left: 8px" @change="loadData">
          <el-option label="审批中" :value="1" />
          <el-option label="通过" :value="2" />
          <el-option label="驳回" :value="3" />
          <el-option label="已撤回" :value="4" />
          <el-option label="已终止" :value="5" />
        </el-select>
        <el-button type="primary" style="margin-left: 8px" @click="loadData">查询</el-button>
      </div>
      <el-table :data="instances" stripe v-loading="loading">
        <el-table-column prop="instanceTitle" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="initiatorUserId" label="发起人ID" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="150" />
        <el-table-column prop="endAt" label="结束时间" min-width="150" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.id)">查看</el-button>
            <el-button v-if="row.status === 1" link type="danger" @click="handleTerminate(row.id)">终止</el-button>
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

    <el-dialog v-model="reassignVisible" title="转办任务" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="任务ID">{{ reassignTaskId }}</el-form-item>
        <el-form-item label="目标用户ID" required>
          <el-input-number v-model="reassignTarget" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reassignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReassign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminInstances, terminateInstance, reassignTask } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const instances = ref<any[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filters = reactive({ title: '', status: undefined as number | undefined })

const reassignVisible = ref(false)
const reassignTaskId = ref(0)
const reassignTarget = ref(0)

function statusType(status: number): 'primary' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<number, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    1: 'info',
    2: 'success',
    3: 'danger',
    4: 'warning',
    5: 'info',
  }
  return map[status] || 'info'
}

function statusLabel(status: number): string {
  const map: Record<number, string> = { 1: '审批中', 2: '通过', 3: '驳回', 4: '已撤回', 5: '已终止' }
  return map[status] || '未知'
}

async function loadData() {
  loading.value = true
  try {
    const result = await getAdminInstances({
      title: filters.title || undefined,
      status: filters.status,
      page: page.value,
      pageSize: pageSize.value,
    })
    instances.value = result?.list ?? []
    total.value = result?.total ?? 0
  } finally {
    loading.value = false
  }
}

function viewDetail(id: number) {
  router.push(`/approval/instance/${id}`)
}

async function handleTerminate(id: number) {
  try {
    await ElMessageBox.confirm('确认终止此审批实例? 终止后所有待办任务将被取消。', '终止确认', {
      confirmButtonText: '确认终止',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await terminateInstance(id)
    ElMessage.success('已终止')
    loadData()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('终止失败')
  }
}

function openReassign(taskId: number) {
  reassignTaskId.value = taskId
  reassignTarget.value = 0
  reassignVisible.value = true
}

async function handleReassign() {
  if (!reassignTarget.value) {
    ElMessage.warning('请输入目标用户ID')
    return
  }
  try {
    await reassignTask(reassignTaskId.value, reassignTarget.value)
    ElMessage.success('转办成功')
    reassignVisible.value = false
  } catch { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; margin-bottom: 15px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 15px; }
</style>
