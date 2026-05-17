<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div class="search">
          <el-input
            v-model="search.title"
            placeholder="申请标题"
            clearable
            style="width: 200px"
            @clear="loadData"
          />
          <el-select
            v-model="search.leaveType"
            placeholder="请假类型"
            clearable
            style="width: 130px; margin-left: 10px"
            @change="loadData"
          >
            <el-option label="年假" :value="1" />
            <el-option label="病假" :value="2" />
            <el-option label="事假" :value="3" />
          </el-select>
          <el-select
            v-model="search.status"
            placeholder="状态"
            clearable
            style="width: 120px; margin-left: 10px"
            @change="loadData"
          >
            <el-option label="草稿" :value="0" />
            <el-option label="审批中" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已驳回" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="loadData">
            搜索
          </el-button>
        </div>
        <el-button type="primary" @click="openFormDialog()">新建请假</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="leaveType" label="类型" width="90">
          <template #default="{ row }">
            {{ leaveTypeMap[row.leaveType] || row.leaveType }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status]">
              {{ statusLabel[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
            <el-button
              v-if="row.status === 3"
              link type="warning"
              @click="openFormDialog(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 0"
              link type="success"
              @click="handleSubmitApproval(row.id)"
            >
              提交审批
            </el-button>
            <el-button
              v-if="row.status === 3"
              link type="primary"
              @click="handleResubmit(row.id)"
            >
              重新提交
            </el-button>
            <el-popconfirm
              v-if="row.status === 0"
              title="确认删除?"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 15px; justify-content: flex-end"
        @current-change="loadData"
      />
    </el-card>

    <LeaveFormDialog
      v-model:visible="formVisible"
      :leave-data="editingLeave"
      @saved="loadData"
    />

    <LeaveDetailDialog
      v-model:visible="detailVisible"
      :leave-data="viewingLeave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getLeaveList,
  deleteLeave,
  submitLeaveForApproval,
} from '@/api/leave'
import LeaveFormDialog from './components/LeaveFormDialog.vue'
import LeaveDetailDialog from './components/LeaveDetailDialog.vue'

const leaveTypeMap: Record<number, string> = { 1: '年假', 2: '病假', 3: '事假' }
const statusLabel: Record<number, string> = { 0: '草稿', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已取消' }
const statusTagType: Record<number, 'info' | 'warning' | 'success' | 'danger'> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const search = reactive({
  title: '',
  leaveType: undefined as number | undefined,
  status: undefined as number | undefined,
})

const formVisible = ref(false)
const editingLeave = ref<any>(null)
const detailVisible = ref(false)
const viewingLeave = ref<any>(null)

async function loadData() {
  loading.value = true
  try {
    const data: any = await getLeaveList({
      title: search.title || undefined,
      leaveType: search.leaveType,
      status: search.status,
      page: page.value,
      pageSize: pageSize.value,
    })
    tableData.value = data.list || data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openFormDialog(leave?: any) {
  editingLeave.value = leave || null
  formVisible.value = true
}

function openDetail(leave: any) {
  viewingLeave.value = leave
  detailVisible.value = true
}

async function handleSubmitApproval(id: number) {
  try {
    await submitLeaveForApproval(id)
    ElMessage.success('已提交审批')
    loadData()
  } catch {
    // interceptor handles error
  }
}

async function handleResubmit(id: number) {
  try {
    await ElMessageBox.confirm('确认重新编辑并提交审批?', '重新提交')
    openFormDialog(tableData.value.find(r => r.id === id))
  } catch {
    // cancelled
  }
}

async function handleDelete(id: number) {
  try {
    await deleteLeave(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // interceptor handles error
  }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 15px; }
.search { display: flex; align-items: center; }
</style>
