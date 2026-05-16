<template>
  <div v-loading="loading" class="instance-detail">
    <div class="instance-detail__header">
      <el-page-header @back="router.back()">
        <template #content>
          <span>{{ instance?.instanceTitle || '审批详情' }}</span>
          <el-tag v-if="instance" :type="statusType(instance.status)" style="margin-left: 8px">
            {{ statusLabel(instance.status) }}
          </el-tag>
        </template>
      </el-page-header>
    </div>

    <el-row :gutter="20" class="instance-detail__body" v-if="instance">
      <el-col :span="12">
        <el-card header="流程图">
          <ProcessDiagram :diagram="diagram" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="审批进度">
          <ApprovalTimeline :tasks="tasks" />
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="instance?.formData" header="表单数据" class="instance-detail__form">
      <pre class="instance-detail__form-data">{{ formatFormData(instance.formData) }}</pre>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getInstanceDetail, getInstanceTasks, getInstanceDiagram } from '@/api/approval'
import type { ApprovalInstance, ApprovalTask, InstanceDiagram } from '@/types'
import { ApprovalInstanceStatus } from '@/types'
import ApprovalTimeline from './components/ApprovalTimeline.vue'
import ProcessDiagram from './components/ProcessDiagram.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const instance = ref<ApprovalInstance | null>(null)
const tasks = ref<ApprovalTask[]>([])
const diagram = ref<InstanceDiagram | null>(null)

function statusType(status: ApprovalInstanceStatus): 'info' | 'success' | 'danger' | 'warning' {
  const map: Record<number, 'info' | 'success' | 'danger' | 'warning'> = {
    [ApprovalInstanceStatus.PENDING]: 'info',
    [ApprovalInstanceStatus.APPROVED]: 'success',
    [ApprovalInstanceStatus.REJECTED]: 'danger',
    [ApprovalInstanceStatus.WITHDRAWN]: 'warning',
    [ApprovalInstanceStatus.CANCELLED]: 'info',
  }
  return map[status] || 'info'
}

function statusLabel(status: ApprovalInstanceStatus) {
  const map: Record<number, string> = {
    [ApprovalInstanceStatus.PENDING]: '审批中',
    [ApprovalInstanceStatus.APPROVED]: '通过',
    [ApprovalInstanceStatus.REJECTED]: '驳回',
    [ApprovalInstanceStatus.WITHDRAWN]: '已撤回',
    [ApprovalInstanceStatus.CANCELLED]: '已终止',
  }
  return map[status] || '未知'
}

function formatFormData(json: string) {
  try {
    return JSON.stringify(JSON.parse(json), null, 2)
  } catch {
    return json
  }
}

onMounted(async () => {
  const id = Number(route.params.id)
  loading.value = true
  try {
    const [inst, taskList, diagramData] = await Promise.all([
      getInstanceDetail(id),
      getInstanceTasks(id),
      getInstanceDiagram(id).catch(() => null),
    ])
    instance.value = inst
    tasks.value = Array.isArray(taskList) ? taskList : []
    diagram.value = diagramData
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.instance-detail {
  &__header {
    margin-bottom: 20px;
  }

  &__body {
    margin-bottom: 20px;
  }

  &__form {
    &-data {
      background: #f5f7fa;
      padding: 12px 16px;
      border-radius: 4px;
      font-size: 13px;
      line-height: 1.6;
      margin: 0;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
}
</style>
