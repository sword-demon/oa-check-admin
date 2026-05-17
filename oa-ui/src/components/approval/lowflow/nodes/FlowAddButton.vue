<template>
  <div class="flow-add-button">
    <el-popover
      v-if="!readonly"
      ref="popoverRef"
      placement="bottom"
      trigger="click"
      title="添加节点"
      :width="280"
    >
      <div class="flow-add-button__grid">
        <button type="button" class="flow-add-button__option" @click="selectNode('approval')">
          <el-icon><Stamp /></el-icon>
          <span>审批人</span>
        </button>
        <button type="button" class="flow-add-button__option" @click="selectNode('cc')">
          <el-icon><Promotion /></el-icon>
          <span>抄送人</span>
        </button>
        <button type="button" class="flow-add-button__option" @click="selectNode('exclusive')">
          <el-icon><Share /></el-icon>
          <span>条件分支</span>
        </button>
      </div>
      <template #reference>
        <el-button class="flow-add-button__trigger" type="primary" icon="Plus" circle />
      </template>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { PopoverInstance } from 'element-plus'
import { Promotion, Share, Stamp } from '@element-plus/icons-vue'
import type { ApprovalFlowNodeType } from '@/utils/approval-flow'

withDefaults(defineProps<{
  readonly?: boolean
}>(), {
  readonly: false,
})

const emit = defineEmits<{
  'add-node': [type: ApprovalFlowNodeType]
}>()

const popoverRef = ref<PopoverInstance>()

function selectNode(type: ApprovalFlowNodeType) {
  emit('add-node', type)
  popoverRef.value?.hide()
}
</script>

<style scoped lang="scss">
.flow-add-button {
  position: relative;
  display: flex;
  justify-content: center;
  width: 100%;
  padding: 18px 0 26px;

  &::before {
    position: absolute;
    inset: 0;
    width: 1px;
    height: 100%;
    margin: auto;
    background: #c0c4cc;
    content: '';
  }
}

.flow-add-button__trigger {
  position: relative;
  z-index: 1;
}

.flow-add-button__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.flow-add-button__option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-height: 72px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #f7f9fc;
  color: #303133;
  cursor: pointer;

  .el-icon {
    margin-top: 8px;
    font-size: 24px;
    color: #409eff;
  }

  &:hover {
    border-color: #409eff;
    background: #ecf5ff;
    color: #409eff;
  }
}
</style>
