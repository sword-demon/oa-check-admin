<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ stats.todoCount }}</div>
            <div class="stat-label">待办任务</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ stats.doneCount }}</div>
            <div class="stat-label">已办任务</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ stats.templateCount }}</div>
            <div class="stat-label">审批模板</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ stats.unreadCcCount }}</div>
            <div class="stat-label">抄送未读</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="activity-card">
      <template #header>最近审批动态</template>
      <el-table :data="stats.recentActivities" stripe>
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="taskResult" label="状态">
          <template #default="{ row }">
            <el-tag v-if="row.taskResult === ApprovalTaskResult.APPROVED" type="success">通过</el-tag>
            <el-tag v-else-if="row.taskResult === ApprovalTaskResult.REJECTED" type="danger">驳回</el-tag>
            <el-tag v-else type="info">待处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="completedAt" label="时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { getDashboardStats } from '@/api/approval'
import type { DashboardStats } from '@/types'
import { ApprovalTaskResult } from '@/types'

const stats = reactive<DashboardStats>({
  todoCount: 0,
  doneCount: 0,
  templateCount: 0,
  unreadCcCount: 0,
  recentActivities: [],
})

onMounted(async () => {
  try {
    const data = await getDashboardStats()
    Object.assign(stats, data)
  } catch {
    // dashboard is non-critical
  }
})
</script>

<style scoped>
.dashboard { padding: 0; }
.stat-cards { margin-bottom: 20px; }
.stat-card { text-align: center; padding: 20px 0; }
.stat-value { font-size: 36px; font-weight: bold; color: #409EFF; }
.stat-label { font-size: 14px; color: #999; margin-top: 8px; }
.activity-card { margin-top: 20px; }
</style>
