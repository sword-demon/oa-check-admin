<template>
  <div class="page-shell">
    <section class="page-header">
      <div class="page-header__titles">
        <p class="page-subtitle page-subtitle--eyebrow">Archive</p>
        <h1 class="page-title">我的已办</h1>
        <p class="page-subtitle">复盘已处理任务的结果、意见和完成时间，支持快速追溯审批详情。</p>
      </div>
    </section>

    <el-card class="page-panel">
      <div class="page-toolbar">
        <div class="page-toolbar__filters">
          <el-input v-model="searchTitle" class="field--lg" placeholder="搜索标题" clearable @clear="loadData" @keyup.enter="loadData" />
          <el-button type="primary" @click="loadData">搜索</el-button>
        </div>
        <div class="page-toolbar__actions">
          <span class="page-toolbar__meta">累计 {{ total }} 条已办</span>
        </div>
      </div>
      <el-table :data="tasks" stripe v-loading="loading" class="page-table">
        <el-table-column prop="instanceTitle" label="申请标题" />
        <el-table-column prop="taskName" label="审批节点" width="120" />
        <el-table-column prop="taskResult" label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="resultTagType(row.taskResult)">{{ resultLabel(row.taskResult) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taskComment" label="审批意见" show-overflow-tooltip />
        <el-table-column prop="completedAt" label="处理时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.approvalInstanceId)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="page-pagination">
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
import { getMyDonePaged } from '@/api/approval'
import type { TaskVO } from '@/types'
import { ApprovalTaskResult } from '@/types'

const router = useRouter()
const loading = ref(false)
const tasks = ref<TaskVO[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchTitle = ref('')

function resultTagType(result: number | null): 'success' | 'danger' | 'warning' | 'info' {
  if (result === ApprovalTaskResult.APPROVED) return 'success'
  if (result === ApprovalTaskResult.REJECTED) return 'danger'
  if (result === ApprovalTaskResult.TRANSFERRED) return 'warning'
  return 'info'
}

function resultLabel(result: number | null): string {
  if (result === ApprovalTaskResult.APPROVED) return '通过'
  if (result === ApprovalTaskResult.REJECTED) return '驳回'
  if (result === ApprovalTaskResult.TRANSFERRED) return '转办'
  if (result === ApprovalTaskResult.CANCELLED) return '已取消'
  return '未知'
}

async function loadData() {
  loading.value = true
  try {
    const result = await getMyDonePaged({
      title: searchTitle.value || undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    tasks.value = result?.list ?? []
    total.value = result?.total ?? 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function viewDetail(instanceId: number) {
  router.push(`/approval/instance/${instanceId}`)
}

onMounted(loadData)
</script>
