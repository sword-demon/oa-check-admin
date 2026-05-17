<template>
  <el-dialog
    :model-value="visible"
    title="请假详情"
    width="600px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <template v-if="leave">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请标题" :span="2">{{ leave.title }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">
          {{ leaveTypeMap[leave.leaveType] || leave.leaveType }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType[leave.status]">
            {{ statusLabel[leave.status] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ leave.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ leave.endTime }}</el-descriptions-item>
        <el-descriptions-item label="请假原因" :span="2">
          {{ leave.reason || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ leave.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ leave.updatedAt }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="leave.approvalInstanceId" style="margin-top: 20px">
        <h4 style="margin-bottom: 10px">审批信息</h4>
        <el-button
          type="primary"
          link
          @click="goToApprovalInstance(leave.approvalInstanceId)"
        >
          查看审批详情 (实例 #{{ leave.approvalInstanceId }})
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps<{
  visible: boolean
  leaveData: any
}>()

defineEmits<{
  (e: 'update:visible', val: boolean): void
}>()

const router = useRouter()
const leave = computed(() => props.leaveData)

const leaveTypeMap: Record<number, string> = { 1: '年假', 2: '病假', 3: '事假' }
const statusLabel: Record<number, string> = { 0: '草稿', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已取消' }
const statusTagType: Record<number, 'info' | 'warning' | 'success' | 'danger'> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }

function goToApprovalInstance(instanceId: number) {
  router.push(`/approval/instance/${instanceId}`)
}
</script>
