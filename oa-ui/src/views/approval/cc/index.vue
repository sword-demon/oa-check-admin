<template>
  <div>
    <el-card>
      <template #header>抄送给我的</template>
      <el-table :data="ccList" stripe v-loading="loading">
        <el-table-column prop="instanceTitle" label="申请标题" min-width="140" />
        <el-table-column prop="ccReason" label="抄送原因" min-width="120" />
        <el-table-column prop="readAt" label="阅读时间" width="180">
          <template #default="{ row }">
            <span v-if="row.readAt">{{ row.readAt }}</span>
            <el-tag v-else type="warning" size="small">未读</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="抄送时间" min-width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.approvalInstanceId)">查看</el-button>
            <el-button v-if="!row.readAt" link type="primary" @click="handleMarkRead(row.id)">标记已读</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyCc, markCcRead } from '@/api/approval'
import type { ApprovalCc } from '@/types'

const router = useRouter()
const loading = ref(false)
const ccList = ref<ApprovalCc[]>([])

async function loadData() {
  loading.value = true
  try {
    const data = await getMyCc()
    ccList.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

function viewDetail(instanceId: number) {
  router.push(`/approval/instance/${instanceId}`)
}

async function handleMarkRead(id: number) {
  try {
    await markCcRead(id)
    ElMessage.success('已标记已读')
    loadData()
  } catch { /* handled */ }
}

onMounted(loadData)
</script>
