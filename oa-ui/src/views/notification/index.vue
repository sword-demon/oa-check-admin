<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div>
          <el-radio-group v-model="filter" @change="loadData">
            <el-radio-button :value="'all'">全部</el-radio-button>
            <el-radio-button :value="'unread'">未读</el-radio-button>
          </el-radio-group>
        </div>
        <el-button type="primary" size="small" @click="handleMarkAllRead">全部已读</el-button>
      </div>
      <el-table :data="notifications" stripe v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRead" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'danger'" size="small">{{ row.isRead ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" min-width="150" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.link" link type="primary" @click="goLink(row)">查看</el-button>
            <el-button v-if="!row.isRead" link type="primary" @click="handleMarkRead(row.id)">已读</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyNotifications, markNotificationRead, markAllRead } from '@/api/notification'

const router = useRouter()
const loading = ref(false)
const notifications = ref<any[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filter = ref<'all' | 'unread'>('all')

function typeLabel(type: string): string {
  const map: Record<string, string> = {
    pending_task: '待办任务',
    approved: '审批通过',
    rejected: '审批驳回',
    cc_received: '抄送',
    task_transferred: '任务转办',
    instance_terminated: '实例终止',
  }
  return map[type] || type
}

async function loadData() {
  loading.value = true
  try {
    const result = await getMyNotifications({
      unreadOnly: filter.value === 'unread' ? true : undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    notifications.value = result?.list ?? []
    total.value = result?.total ?? 0
  } finally {
    loading.value = false
  }
}

async function handleMarkRead(id: number) {
  try {
    await markNotificationRead(id)
    loadData()
  } catch { /* handled */ }
}

async function handleMarkAllRead() {
  try {
    await markAllRead()
    ElMessage.success('已全部标记为已读')
    loadData()
  } catch { /* handled */ }
}

function goLink(row: any) {
  if (!row.isRead) handleMarkRead(row.id)
  if (row.link) router.push(row.link)
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 15px; }
</style>
