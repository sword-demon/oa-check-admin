<template>
  <div class="bpmn-canvas">
    <div v-if="loading" class="bpmn-canvas__loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    <div ref="containerRef" class="bpmn-canvas__container" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import BpmnViewer from 'bpmn-js/lib/Viewer'
import type { BpmnInstance } from '@/composables/bpmn/useBpmnModeler'
import flowableModdle from '@/bpmn/moddle/flowable.moddle.json'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import '@/styles/bpmn-overrides.scss'

const props = defineProps<{
  loading?: boolean
  readOnly?: boolean
}>()

const emit = defineEmits<{
  (e: 'modeler-ready', modeler: BpmnInstance): void
}>()

const containerRef = ref<HTMLElement | null>(null)
let modelerInstance: BpmnInstance | null = null
let resizeObserver: ResizeObserver | null = null

function getModeler() {
  return modelerInstance
}

onMounted(async () => {
  await nextTick()

  if (!containerRef.value) return

  const options: Record<string, unknown> = {
    container: containerRef.value,
    moddleExtensions: { flowable: flowableModdle },
  }

  modelerInstance = props.readOnly
    ? new BpmnViewer(options)
    : new BpmnModeler(options)

  resizeObserver = new ResizeObserver(() => {
    if (modelerInstance) {
      const canvas = (modelerInstance as any).get('canvas')
      if (canvas && canvas.resized) {
        canvas.resized()
      }
    }
  })

  resizeObserver.observe(containerRef.value)

  emit('modeler-ready', modelerInstance)
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }

  if (modelerInstance) {
    modelerInstance.destroy()
    modelerInstance = null
  }
})

defineExpose({ getModeler, containerRef })
</script>

<style scoped lang="scss">
.bpmn-canvas {
  width: 100%;
  height: 100%;
  position: relative;

  &__loading {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    color: #909399;
    z-index: 10;
  }

  &__container {
    width: 100%;
    height: 100%;
  }
}
</style>
