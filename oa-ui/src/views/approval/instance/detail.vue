<template>
  <div v-loading="loading" class="page-shell instance-detail">
    <section class="page-header instance-detail__header">
      <div class="page-header__titles">
        <el-page-header @back="router.back()">
          <template #content>
            <span>{{ instance?.instanceTitle || '审批详情' }}</span>
            <el-tag v-if="instance" :type="statusType(instance.status)" class="instance-detail__tag">
              {{ statusLabel(instance.status) }}
            </el-tag>
          </template>
        </el-page-header>
        <p class="page-subtitle">查看流程节点高亮、审批进度和表单提交内容。</p>
      </div>
    </section>

    <el-row v-if="instance" :gutter="20" class="instance-detail__body">
      <el-col :xs="24" :lg="12">
        <el-card header="流程图" class="page-panel">
          <ProcessDiagram :diagram="diagram" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card header="审批进度" class="page-panel">
          <ApprovalTimeline :tasks="tasks" />
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="instance?.formData" header="表单详情" class="page-panel">
      <ApprovalFormDetail :template-id="instance.processTemplateId" :form-data="instance.formData" />
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
import ApprovalFormDetail from './components/ApprovalFormDetail.vue'

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
    gap: 8px;
  }

  &__body {
    margin-bottom: 0;
  }

  &__tag {
    margin-left: 8px;
  }
}
</style>
