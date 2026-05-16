<template>
  <div class="approval-timeline">
    <div v-if="tasks.length === 0" class="approval-timeline__empty">暂无审批记录</div>
    <el-timeline v-else>
      <el-timeline-item
        v-for="task in tasks"
        :key="task.id"
        :type="timelineType(task)"
        :timestamp="task.completedAt || ''"
        placement="top"
      >
        <div class="approval-timeline__card">
          <div class="approval-timeline__header">
            <span class="approval-timeline__name">{{ task.taskName }}</span>
            <el-tag v-if="task.taskResult" :type="resultTagType(task.taskResult)" size="small">
              {{ resultLabel(task.taskResult) }}
            </el-tag>
            <el-tag v-else type="primary" size="small">待处理</el-tag>
          </div>
          <div v-if="task.taskComment" class="approval-timeline__comment">{{ task.taskComment }}</div>
          <div class="approval-timeline__meta">
            审批人: {{ task.assigneeUserId }} &middot; {{ formatTime(task) }}
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup lang="ts">
import type { ApprovalTask } from '@/types'
import { ApprovalTaskResult } from '@/types'

defineProps<{
  tasks: ApprovalTask[]
}>()

function timelineType(task: ApprovalTask): 'primary' | 'success' | 'danger' | 'warning' {
  if (task.taskResult === null) return 'primary'
  if (task.taskResult === ApprovalTaskResult.APPROVED) return 'success'
  if (task.taskResult === ApprovalTaskResult.REJECTED) return 'danger'
  return 'warning'
}

function resultTagType(result: number): 'success' | 'danger' | 'warning' {
  if (result === ApprovalTaskResult.APPROVED) return 'success'
  if (result === ApprovalTaskResult.REJECTED) return 'danger'
  return 'warning'
}

function resultLabel(result: number) {
  const map: Record<number, string> = {
    [ApprovalTaskResult.APPROVED]: '通过',
    [ApprovalTaskResult.REJECTED]: '驳回',
    [ApprovalTaskResult.TRANSFERRED]: '转办',
  }
  return map[result] || '未知'
}

function formatTime(task: ApprovalTask) {
  return task.completedAt || '等待处理中'
}
</script>

<style scoped lang="scss">
.approval-timeline {
  &__empty {
    text-align: center;
    color: #909399;
    padding: 40px 0;
  }

  &__card {
    padding: 4px 0;
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__name {
    font-weight: 500;
  }

  &__comment {
    margin-top: 4px;
    color: #606266;
    font-size: 13px;
  }

  &__meta {
    margin-top: 4px;
    color: #909399;
    font-size: 12px;
  }
}
</style>
