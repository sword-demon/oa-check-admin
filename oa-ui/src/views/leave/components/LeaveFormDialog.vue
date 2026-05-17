<template>
  <el-dialog
    :model-value="visible"
    :title="isResubmit ? '重新编辑并提交' : (leaveData ? '编辑请假' : '新建请假')"
    width="550px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
    @close="resetForm"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="申请标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入申请标题" />
      </el-form-item>
      <el-form-item label="请假类型" prop="leaveType">
        <el-select v-model="form.leaveType" placeholder="请选择请假类型">
          <el-option label="年假" :value="1" />
          <el-option label="病假" :value="2" />
          <el-option label="事假" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker
          v-model="form.startTime"
          type="datetime"
          placeholder="选择开始时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DDTHH:mm:ss"
          :disabled-date="disabledDate"
        />
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker
          v-model="form.endTime"
          type="datetime"
          placeholder="选择结束时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DDTHH:mm:ss"
          :disabled-date="disabledDate"
        />
      </el-form-item>
      <el-form-item label="请假原因">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="3"
          placeholder="请输入请假原因"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="handleSubmit">
        {{ isResubmit ? '重新提交审批' : (leaveData ? '保存' : '创建') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createLeave, updateLeave, resubmitLeave, type LeaveRequestForm } from '@/api/leave'

const props = defineProps<{
  visible: boolean
  leaveData: any
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'saved'): void
}>()

const isResubmit = computed(() => props.leaveData?.status === 3)

const disabledDate = (time: Date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}

const formRef = ref<FormInstance>()
const form = reactive({
  title: '',
  leaveType: undefined as number | undefined,
  startTime: '',
  endTime: '',
  reason: '',
})

function getSubmitPayload(): LeaveRequestForm {
  return {
    title: form.title,
    leaveType: form.leaveType!,
    startTime: form.startTime,
    endTime: form.endTime,
    reason: form.reason,
  }
}

const rules: FormRules = {
  title: [{ required: true, message: '请输入申请标题', trigger: 'blur' }],
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (_rule, value, callback) => {
        if (value && form.startTime && new Date(value) <= new Date(form.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

watch(() => props.visible, (val) => {
  if (val && props.leaveData) {
    Object.assign(form, {
      title: props.leaveData.title || '',
      leaveType: props.leaveData.leaveType,
      startTime: props.leaveData.startTime || '',
      endTime: props.leaveData.endTime || '',
      reason: props.leaveData.reason || '',
    })
  }
})

function resetForm() {
  Object.assign(form, { title: '', leaveType: undefined, startTime: '', endTime: '', reason: '' })
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    if (isResubmit.value) {
      await resubmitLeave(props.leaveData.id, getSubmitPayload())
      ElMessage.success('已重新提交审批')
    } else if (props.leaveData) {
      await updateLeave(props.leaveData.id, getSubmitPayload())
      ElMessage.success('保存成功')
    } else {
      await createLeave(getSubmitPayload())
      ElMessage.success('创建成功')
    }
    emit('update:visible', false)
    emit('saved')
  } catch {
    // interceptor handles error
  }
}
</script>
