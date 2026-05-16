<template>
  <div ref="containerRef" class="process-diagram" />
</template>

<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from 'vue'
import BpmnViewer from 'bpmn-js/lib/Viewer'
import type { InstanceDiagram } from '@/types'

const props = defineProps<{
  diagram: InstanceDiagram | null
}>()

const containerRef = ref<HTMLElement | null>(null)
let viewer: BpmnViewer | null = null

async function renderDiagram() {
  if (!props.diagram || !props.diagram.bpmnXml || !containerRef.value) return

  if (!viewer) {
    viewer = new BpmnViewer({ container: containerRef.value })
  }

  try {
    await viewer.importXML(props.diagram.bpmnXml)

    const canvas = viewer.get('canvas') as any
    canvas.zoom('fit-viewport', 'auto')

    const overlays = viewer.get('overlays') as any
    overlays.remove({ type: 'highlight' })

    for (const nodeId of props.diagram.completedNodeIds) {
      try {
        overlays.add(nodeId, 'highlight', {
          position: { top: -3, left: -3 },
          style: { border: '3px solid #67C23A', borderRadius: '4px' },
        })
      } catch {
        // node may not exist in diagram
      }
    }

    for (const nodeId of props.diagram.currentNodeIds) {
      try {
        overlays.add(nodeId, 'highlight', {
          position: { top: -3, left: -3 },
          style: { border: '3px solid #409EFF', borderRadius: '4px' },
        })
      } catch {
        // node may not exist in diagram
      }
    }
  } catch {
    // BPMN import/render failure is non-critical
  }
}

watch(() => props.diagram, renderDiagram, { immediate: true })

onBeforeUnmount(() => {
  viewer?.destroy()
  viewer = null
})
</script>

<style scoped lang="scss">
.process-diagram {
  width: 100%;
  height: 400px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  overflow: hidden;
}
</style>
