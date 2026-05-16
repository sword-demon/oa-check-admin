<template>
  <div>
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="4">
        <el-card shadow="hover">
          <el-statistic title="总实例数" :value="metrics?.totalInstances ?? 0" />
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <el-statistic title="审批中" :value="metrics?.pendingInstances ?? 0" />
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <el-statistic title="已通过" :value="metrics?.approvedInstances ?? 0" />
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <el-statistic title="已驳回" :value="metrics?.rejectedInstances ?? 0" />
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <el-statistic title="已撤回" :value="metrics?.withdrawnInstances ?? 0" />
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <el-statistic title="平均耗时(h)" :value="metrics?.avgDurationHours ?? 0" :precision="2" />
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>按模板统计</template>
      <el-table :data="metrics?.templateMetrics ?? []" stripe>
        <el-table-column prop="templateName" label="模板名称" min-width="140" />
        <el-table-column prop="total" label="总数" width="80" />
        <el-table-column prop="pending" label="审批中" width="80" />
        <el-table-column prop="approved" label="已通过" width="80" />
        <el-table-column prop="rejected" label="已驳回" width="80" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminMetrics, type AdminMetrics } from '@/api/admin'

const metrics = ref<AdminMetrics | null>(null)

async function loadData() {
  metrics.value = await getAdminMetrics()
}

onMounted(loadData)
</script>
