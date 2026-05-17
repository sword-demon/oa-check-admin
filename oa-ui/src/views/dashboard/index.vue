<template>
  <div class="page-shell dashboard-page">
    <section class="dashboard-hero">
      <div class="dashboard-hero__copy">
        <p class="page-subtitle page-subtitle--eyebrow">运营概览</p>
        <h1 class="page-title">流程与审批状态一览</h1>
        <p class="page-subtitle">
          聚合当前待办、已办和模板资产，帮助你快速判断处理负载与流程活跃度。
        </p>
      </div>
      <div class="dashboard-hero__metrics">
        <span>待处理 {{ stats.todoCount }}</span>
        <span>模板 {{ stats.templateCount }}</span>
      </div>
    </section>

    <el-row :gutter="20" class="dashboard-stats">
      <el-col :xs="24" :sm="12" :xl="6">
        <el-card class="dashboard-stat" shadow="hover">
          <div class="dashboard-stat__top">
            <span class="dashboard-stat__label">待办任务</span>
            <span class="dashboard-stat__badge dashboard-stat__badge--primary">进行中</span>
          </div>
          <div class="dashboard-stat__value">{{ stats.todoCount }}</div>
          <div class="dashboard-stat__hint">需要尽快处理的流程节点</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :xl="6">
        <el-card class="dashboard-stat" shadow="hover">
          <div class="dashboard-stat__top">
            <span class="dashboard-stat__label">已办任务</span>
            <span class="dashboard-stat__badge dashboard-stat__badge--success">已完成</span>
          </div>
          <div class="dashboard-stat__value">{{ stats.doneCount }}</div>
          <div class="dashboard-stat__hint">本账号完成的审批处理记录</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :xl="6">
        <el-card class="dashboard-stat" shadow="hover">
          <div class="dashboard-stat__top">
            <span class="dashboard-stat__label">审批模板</span>
            <span class="dashboard-stat__badge dashboard-stat__badge--accent">流程资产</span>
          </div>
          <div class="dashboard-stat__value">{{ stats.templateCount }}</div>
          <div class="dashboard-stat__hint">当前可维护的流程模板总量</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :xl="6">
        <el-card class="dashboard-stat" shadow="hover">
          <div class="dashboard-stat__top">
            <span class="dashboard-stat__label">抄送未读</span>
            <span class="dashboard-stat__badge dashboard-stat__badge--warning">待关注</span>
          </div>
          <div class="dashboard-stat__value">{{ stats.unreadCcCount }}</div>
          <div class="dashboard-stat__hint">需要补充查看的抄送消息</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="page-panel">
      <template #header>最近审批动态</template>
      <el-table :data="stats.recentActivities" stripe class="page-table">
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

<style scoped lang="scss">
.dashboard-page {
  gap: 22px;
}

.dashboard-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 28px;
  border: 1px solid rgba(47, 107, 98, 0.12);
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(183, 130, 76, 0.18), transparent 34%),
    linear-gradient(135deg, rgba(255, 253, 249, 0.82), rgba(237, 245, 243, 0.88));
  box-shadow: var(--app-shadow-sm);

  &__copy {
    max-width: 720px;
  }

  &__metrics {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: flex-end;

    span {
      display: inline-flex;
      align-items: center;
      min-height: 38px;
      padding: 0 14px;
      border: 1px solid rgba(63, 48, 28, 0.08);
      border-radius: 999px;
      background: rgba(255, 255, 255, 0.68);
      color: var(--app-text-secondary);
      font-size: 13px;
      font-weight: 500;
    }
  }
}

.dashboard-stats {
  margin-bottom: -20px;
}

.dashboard-stat {
  height: 100%;

  :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    gap: 16px;
    min-height: 168px;
  }

  &__top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  &__label {
    color: var(--app-text-secondary);
    font-size: 14px;
    font-weight: 600;
  }

  &__badge {
    display: inline-flex;
    align-items: center;
    min-height: 28px;
    padding: 0 10px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 700;

    &--primary {
      color: var(--el-color-primary);
      background: rgba(47, 107, 98, 0.1);
    }

    &--success {
      color: var(--el-color-success);
      background: rgba(68, 120, 84, 0.1);
    }

    &--accent {
      color: var(--app-accent);
      background: rgba(183, 130, 76, 0.12);
    }

    &--warning {
      color: var(--el-color-warning);
      background: rgba(183, 130, 76, 0.14);
    }
  }

  &__value {
    color: var(--app-text-primary);
    font-size: 40px;
    line-height: 1;
    font-weight: 700;
  }

  &__hint {
    color: var(--app-text-tertiary);
    font-size: 13px;
    line-height: 1.6;
  }
}

@media (max-width: 768px) {
  .dashboard-hero {
    padding: 20px;
  }
}

@media (max-width: 640px) {
  .dashboard-hero {
    flex-direction: column;
    align-items: flex-start;

    &__metrics {
      justify-content: flex-start;
    }
  }
}
</style>
