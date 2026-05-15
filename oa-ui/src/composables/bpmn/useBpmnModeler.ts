import { ref, onUnmounted, type Ref } from 'vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import BpmnViewer from 'bpmn-js/lib/Viewer'
import flowableModdle from '@/bpmn/moddle/flowable.moddle.json'

export type BpmnInstance = BpmnModeler | BpmnViewer

export function useBpmnModeler(containerRef: Ref<HTMLElement | null>, readOnly = false) {
  const modeler = ref<BpmnInstance | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  function createModeler(): BpmnInstance {
    const options: Record<string, unknown> = {
      container: containerRef.value!,
      moddleExtensions: { flowable: flowableModdle },
    }

    const instance = readOnly ? new BpmnViewer(options) : new BpmnModeler(options)
    modeler.value = instance
    return instance
  }

  async function importXML(xml: string): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const instance = modeler.value || createModeler()
      const result = await (instance as BpmnModeler).importXML(xml)

      if (result.warnings.length > 0) {
        console.warn('BPMN import warnings:', result.warnings)
      }

      // Auto-fit canvas to center the diagram
      const canvas = (instance as BpmnModeler).get('canvas') as any
      canvas.zoom('fit-viewport', 'auto')
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to import BPMN XML'
      error.value = message
      console.error('BPMN import error:', err)
    } finally {
      loading.value = false
    }
  }

  async function saveXML(): Promise<string | null> {
    if (!modeler.value) return null

    try {
      const result = await (modeler.value as BpmnModeler).saveXML({ format: true })
      return result.xml ?? null
    } catch (err: unknown) {
      console.error('BPMN save error:', err)
      return null
    }
  }

  async function createDiagram(key: string, name: string): Promise<void> {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
             targetNamespace="http://oa.admin.com/approval">
    <process id="${key}" name="${name}" isExecutable="true">
        <startEvent id="startEvent" name="开始"/>
        <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="endEvent"/>
        <endEvent id="endEvent" name="结束"/>
    </process>
    <bpmndi:BPMNDiagram id="BPMNDiagram_1">
        <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${key}">
            <bpmndi:BPMNShape id="BPMNShape_startEvent" bpmnElement="startEvent">
                <omgdc:Bounds x="180" y="200" width="36" height="36"/>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNEdge id="BPMNEdge_flow1" bpmnElement="flow1">
                <omgdi:waypoint x="216" y="218"/>
                <omgdi:waypoint x="380" y="218"/>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNShape id="BPMNShape_endEvent" bpmnElement="endEvent">
                <omgdc:Bounds x="380" y="200" width="36" height="36"/>
            </bpmndi:BPMNShape>
        </bpmndi:BPMNPlane>
    </bpmndi:BPMNDiagram>
</definitions>`
    await importXML(xml)
  }

  function getModeler(): BpmnInstance | null {
    return modeler.value
  }

  onUnmounted(() => {
    if (modeler.value) {
      modeler.value.destroy()
      modeler.value = null
    }
  })

  return {
    modeler,
    loading,
    error,
    createModeler,
    importXML,
    saveXML,
    createDiagram,
    getModeler,
  }
}
