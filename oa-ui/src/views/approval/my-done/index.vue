<template>
  <div>
    <el-card>
      <template #header>我的已办</template>
      <el-table :data="tasks" stripe v-loading="loading">
        <el-table-column prop="taskName" label="审批节点" />
        <el-table-column prop="taskResult" label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.taskResult === 1 ? 'success' : 'danger'">{{ row.taskResult === 1 ? '通过' : '驳回' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taskComment" label="审批意见" />
        <el-table-column prop="completedAt" label="处理时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyDone } from '@/api/approval'

const loading = ref(false)
const tasks = ref<any[]>([])

async function loadData() {
  loading.value = true
  try {
    const data: any = await getMyDone()
    tasks.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
