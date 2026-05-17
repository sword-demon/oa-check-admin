<template>
  <div v-if="leave" class="leave-readonly-form">
    <el-form label-width="100px" class="leave-readonly-form__form">
      <el-row :gutter="16">
        <el-col :xs="24" :md="12">
          <el-form-item label="申请标题">
            <el-input :model-value="leave.title" readonly />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-form-item label="请假类型">
            <el-select :model-value="leave.leaveType" disabled style="width: 100%">
              <el-option
                v-for="option in leaveTypeOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-form-item label="开始时间">
            <el-input :model-value="leave.startTime" readonly />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-form-item label="结束时间">
            <el-input :model-value="leave.endTime" readonly />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="请假原因">
            <el-input :model-value="leave.reason || '-'" type="textarea" :rows="4" readonly />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
  <el-empty v-else description="暂无表单数据" />
</template>

<script setup lang="ts">
import type { LeaveRequestDetail } from '@/api/leave'

defineProps<{
  leave: LeaveRequestDetail | null
}>()

const leaveTypeOptions = [
  { label: '年假', value: 1 },
  { label: '病假', value: 2 },
  { label: '事假', value: 3 },
]
</script>

