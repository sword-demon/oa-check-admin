<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-select v-model="filters.module" placeholder="模块" clearable style="width: 120px">
          <el-option label="审批" value="approval" />
        </el-select>
        <el-select v-model="filters.action" placeholder="操作" clearable style="width: 120px; margin-left: 10px">
          <el-option label="提交" value="submit" />
          <el-option label="通过" value="approve" />
          <el-option label="驳回" value="reject" />
          <el-option label="撤回" value="withdraw" />
          <el-option label="转办" value="transfer" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          style="margin-left: 10px"
          @change="loadData"
        />
        <el-button type="primary" style="margin-left: 10px" @click="loadData">查询</el-button>
      </div>
      <el-table :data="logs" stripe v-loading="loading">
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="module" label="模块" width="80" />
        <el-table-column prop="action" label="操作" width="80" />
        <el-table-column prop="targetType" label="目标类型" width="80" />
        <el-table-column prop="targetId" label="目标ID" width="80" />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="createdAt" label="时间" min-width="160" />
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
import { ref, reactive, onMounted } from 'vue'
import { queryAuditLogs } from '@/api/admin'

const loading = ref(false)
const logs = ref<any[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filters = reactive({ module: '', action: '' })
const dateRange = ref<[string, string] | null>(null)

async function loadData() {
  loading.value = true
  try {
    const result = await queryAuditLogs({
      module: filters.module || undefined,
      action: filters.action || undefined,
      startTime: dateRange.value?.[0] ? new Date(dateRange.value[0]).toISOString() : undefined,
      endTime: dateRange.value?.[1] ? new Date(dateRange.value[1]).toISOString() : undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    logs.value = result?.list ?? []
    total.value = result?.total ?? 0
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; margin-bottom: 15px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 15px; }
</style>
