<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ todoCount }}</div>
            <div class="stat-label">待办任务</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ doneCount }}</div>
            <div class="stat-label">已办任务</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ templateCount }}</div>
            <div class="stat-label">审批模板</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ unreadCcCount }}</div>
            <div class="stat-label">抄送未读</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="activity-card">
      <template #header>最近审批动态</template>
      <el-table :data="recentActivities" stripe>
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="instanceTitle" label="审批标题" />
        <el-table-column prop="taskResult" label="状态">
          <template #default="{ row }">
            <el-tag v-if="row.taskResult === 1" type="success">通过</el-tag>
            <el-tag v-else-if="row.taskResult === 2" type="danger">驳回</el-tag>
            <el-tag v-else type="info">待处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyTodo, getMyDone, getTemplates } from '@/api/approval'

const todoCount = ref(0)
const doneCount = ref(0)
const templateCount = ref(0)
const unreadCcCount = ref(0)
const recentActivities = ref<any[]>([])

onMounted(async () => {
  try {
    const [todos, dones, templates] = await Promise.all([
      getMyTodo() as any,
      getMyDone() as any,
      getTemplates() as any,
    ])
    todoCount.value = Array.isArray(todos) ? todos.length : 0
    doneCount.value = Array.isArray(dones) ? dones.length : 0
    templateCount.value = Array.isArray(templates) ? templates.length : 0
    const combined = [
      ...(Array.isArray(dones) ? dones : []),
      ...(Array.isArray(todos) ? todos : []),
    ]
    combined.sort(
      (a: any, b: any) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
    recentActivities.value = combined.slice(0, 5)
  } catch {
    // silently fail - dashboard is non-critical
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
