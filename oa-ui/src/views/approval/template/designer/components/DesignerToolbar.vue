<template>
  <div class="designer-toolbar">
    <div class="designer-toolbar__left">
      <el-button :icon="ArrowLeft" text @click="$emit('back')">
        返回列表
      </el-button>
      <el-divider direction="vertical" />
      <span class="designer-toolbar__title">{{ templateName || '流程设计器' }}</span>
      <el-tag v-if="isPublished" type="success" size="small">已发布</el-tag>
      <el-tag v-else type="info" size="small">草稿</el-tag>
    </div>

    <div class="designer-toolbar__center">
      <el-button-group>
        <el-button
          :icon="RefreshLeft"
          :disabled="!canUndo || isPublished"
          size="small"
          @click="$emit('undo')"
        >
          撤销
        </el-button>
        <el-button
          :icon="RefreshRight"
          :disabled="!canRedo || isPublished"
          size="small"
          @click="$emit('redo')"
        >
          重做
        </el-button>
      </el-button-group>

      <el-button-group>
        <el-button :icon="ZoomIn" size="small" @click="$emit('zoomIn')">放大</el-button>
        <el-button :icon="ZoomOut" size="small" @click="$emit('zoomOut')">缩小</el-button>
        <el-button :icon="FullScreen" size="small" @click="$emit('zoomFit')">适应</el-button>
      </el-button-group>
    </div>

    <div class="designer-toolbar__right">
      <el-button size="small" @click="$emit('previewXml')">
        XML 预览
      </el-button>

      <template v-if="isPublished">
        <el-button type="primary" size="small" @click="$emit('newVersion')">
          新建版本
        </el-button>
      </template>
      <template v-else>
        <el-button
          type="primary"
          size="small"
          :loading="saving"
          @click="$emit('save')"
        >
          保存
        </el-button>
        <el-button type="success" size="small" @click="$emit('publish')">
          发布
        </el-button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowLeft, RefreshLeft, RefreshRight, ZoomIn, ZoomOut, FullScreen } from '@element-plus/icons-vue'
import { TEMPLATE_STATUS } from '@/bpmn/constants'

const props = defineProps<{
  templateStatus: number
  templateName: string
  canUndo: boolean
  canRedo: boolean
  saving: boolean
}>()

defineEmits<{
  (e: 'save'): void
  (e: 'publish'): void
  (e: 'undo'): void
  (e: 'redo'): void
  (e: 'zoomIn'): void
  (e: 'zoomOut'): void
  (e: 'zoomFit'): void
  (e: 'previewXml'): void
  (e: 'newVersion'): void
  (e: 'back'): void
}>()

const isPublished = computed(() => props.templateStatus === TEMPLATE_STATUS.PUBLISHED)
</script>

<style scoped lang="scss">
.designer-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;

  &__left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  &__center {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}
</style>
