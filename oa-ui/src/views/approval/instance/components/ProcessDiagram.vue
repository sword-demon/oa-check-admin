<template>
  <div class="process-diagram">
    <svg
      v-if="simpleDiagram"
      class="process-diagram__svg"
      :viewBox="simpleViewBox"
      role="img"
    >
      <defs>
        <marker
          id="process-diagram-arrow"
          markerWidth="10"
          markerHeight="10"
          refX="9"
          refY="3"
          orient="auto"
          markerUnits="strokeWidth"
        >
          <path d="M0,0 L0,6 L9,3 z" fill="#A8ABB2" />
        </marker>
      </defs>
      <g
        v-for="flow in simpleDiagram.flows"
        :key="flow.id"
        class="process-diagram__flow"
      >
        <line
          v-if="nodePosition(flow.sourceRef) && nodePosition(flow.targetRef)"
          :x1="nodeRight(flow.sourceRef)"
          :y1="nodeCenterY(flow.sourceRef)"
          :x2="nodeLeft(flow.targetRef)"
          :y2="nodeCenterY(flow.targetRef)"
          marker-end="url(#process-diagram-arrow)"
        />
      </g>
      <g
        v-for="node in simpleDiagram.nodes"
        :key="node.id"
        class="process-diagram__node"
        :class="nodeClass(node.id)"
      >
        <circle
          v-if="isEventNode(node.type)"
          :cx="nodeCenterX(node.id)"
          :cy="nodeCenterY(node.id)"
          :r="18"
        />
        <rect
          v-else
          :x="nodeLeft(node.id)"
          :y="nodeTop(node.id)"
          :width="nodeWidth(node.id)"
          :height="nodeHeight(node.id)"
          rx="4"
        />
        <text
          :x="nodeCenterX(node.id)"
          :y="nodeCenterY(node.id)"
          dominant-baseline="middle"
          text-anchor="middle"
        >
          {{ node.name }}
        </text>
      </g>
    </svg>
    <div
      v-else
      ref="containerRef"
      class="process-diagram__canvas"
      :class="{ 'process-diagram__canvas--hidden': !diagram?.bpmnXml || renderFailed }"
    />
    <el-empty v-if="!diagram?.bpmnXml" description="暂无流程图" />
    <el-empty v-else-if="renderFailed" description="流程图渲染失败" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onBeforeUnmount } from 'vue'
import BpmnViewer from 'bpmn-js/lib/Viewer'
import flowableModdle from '@/bpmn/moddle/flowable.moddle.json'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import { normalizeBpmnXmlForViewer, parseSimpleBpmnDiagram } from '@/bpmn/bpmn-utils'
import type { InstanceDiagram } from '@/types'

const props = defineProps<{
  diagram: InstanceDiagram | null
}>()

const containerRef = ref<HTMLElement | null>(null)
const renderFailed = ref(false)
let viewer: BpmnViewer | null = null

const simpleDiagram = computed(() => {
  if (!props.diagram?.bpmnXml) return null
  return parseSimpleBpmnDiagram(props.diagram.bpmnXml)
})

const nodePositions = computed(() => {
  const positions = new Map<string, { x: number; y: number; width: number; height: number }>()
  simpleDiagram.value?.nodes.forEach((node, index) => {
    const eventNode = isEventNode(node.type)
    const gatewayNode = node.type.endsWith('Gateway')
    const width = eventNode ? 36 : gatewayNode ? 50 : 120
    const height = eventNode ? 36 : gatewayNode ? 50 : 72
    positions.set(node.id, {
      x: 48 + index * 180,
      y: 160 - height / 2,
      width,
      height,
    })
  })
  return positions
})

const simpleViewBox = computed(() => {
  const count = simpleDiagram.value?.nodes.length || 1
  return `0 0 ${Math.max(320, 96 + (count - 1) * 180 + 140)} 320`
})

async function renderDiagram() {
  if (!containerRef.value) return

  if (simpleDiagram.value) {
    viewer?.destroy()
    viewer = null
    return
  }

  renderFailed.value = false
  if (!props.diagram?.bpmnXml) {
    viewer?.destroy()
    viewer = null
    return
  }

  if (!viewer) {
    viewer = new BpmnViewer({
      container: containerRef.value,
      moddleExtensions: { flowable: flowableModdle },
    })
  }

  try {
    await viewer.importXML(normalizeBpmnXmlForViewer(props.diagram.bpmnXml))

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
    renderFailed.value = true
  }
}

watch(() => props.diagram, renderDiagram, { immediate: true, flush: 'post' })

onBeforeUnmount(() => {
  viewer?.destroy()
  viewer = null
})

function nodePosition(nodeId: string) {
  return nodePositions.value.get(nodeId)
}

function nodeLeft(nodeId: string) {
  return nodePosition(nodeId)?.x ?? 0
}

function nodeTop(nodeId: string) {
  return nodePosition(nodeId)?.y ?? 0
}

function nodeWidth(nodeId: string) {
  return nodePosition(nodeId)?.width ?? 0
}

function nodeHeight(nodeId: string) {
  return nodePosition(nodeId)?.height ?? 0
}

function nodeRight(nodeId: string) {
  const position = nodePosition(nodeId)
  return position ? position.x + position.width : 0
}

function nodeCenterX(nodeId: string) {
  const position = nodePosition(nodeId)
  return position ? position.x + position.width / 2 : 0
}

function nodeCenterY(nodeId: string) {
  const position = nodePosition(nodeId)
  return position ? position.y + position.height / 2 : 0
}

function isEventNode(type: string) {
  return type === 'startEvent' || type === 'endEvent'
}

function nodeClass(nodeId: string) {
  return {
    'process-diagram__node--completed': props.diagram?.completedNodeIds.includes(nodeId),
    'process-diagram__node--current': props.diagram?.currentNodeIds.includes(nodeId),
  }
}
</script>

<style scoped lang="scss">
.process-diagram {
  width: 100%;
  height: 400px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  overflow: hidden;

  &__svg {
    width: 100%;
    height: 100%;
    display: block;
    background: #fff;
  }

  &__flow {
    line {
      stroke: #A8ABB2;
      stroke-width: 2;
    }
  }

  &__node {
    rect,
    circle {
      fill: #fff;
      stroke: #C0C4CC;
      stroke-width: 2;
    }

    text {
      fill: #303133;
      font-size: 14px;
      font-weight: 500;
      pointer-events: none;
    }

    &--completed {
      rect,
      circle {
        stroke: #67C23A;
        stroke-width: 4;
      }
    }

    &--current {
      rect,
      circle {
        stroke: #409EFF;
        stroke-width: 4;
      }
    }
  }

  &__canvas {
    width: 100%;
    height: 100%;

    &--hidden {
      display: none;
    }
  }
}
</style>
