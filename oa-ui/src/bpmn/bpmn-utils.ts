import type BpmnModeler from 'bpmn-js/lib/Modeler'
import type { NodeConfig } from '@/api/template'
import { NODE_TYPE_LABEL_MAP } from './constants'

interface BpmnElement {
  businessObject: BpmnBusinessObject
}

interface BpmnBusinessObject {
  $type: string
  id: string
  name?: string
  outgoing?: BpmnBusinessObject[]
  incoming?: BpmnBusinessObject[]
  extensionElements?: any
  $attrs?: Record<string, unknown>
}

interface BpmnElementRegistry {
  filter(fn: (element: BpmnElement) => boolean): BpmnElement[]
}

export function generateDefaultXml(key: string, name: string): string {
  return `<?xml version="1.0" encoding="UTF-8"?>
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
                <omgdi:waypoint x="350" y="218"/>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNShape id="BPMNShape_endEvent" bpmnElement="endEvent">
                <omgdc:Bounds x="350" y="200" width="36" height="36"/>
            </bpmndi:BPMNShape>
        </bpmndi:BPMNPlane>
    </bpmndi:BPMNDiagram>

</definitions>`
}

export function extractNodeConfigs(modeler: BpmnModeler): NodeConfig[] {
  const elementRegistry = modeler.get('elementRegistry') as unknown as BpmnElementRegistry
  const elements = elementRegistry.filter((element) => {
    const bo = element.businessObject
    if (!bo) return false
    const type = bo.$type
    return (
      type === 'bpmn:UserTask' ||
      type === 'bpmn:ExclusiveGateway' ||
      type === 'bpmn:ParallelGateway' ||
      type === 'bpmn:StartEvent' ||
      type === 'bpmn:EndEvent'
    )
  })

  return elements.map((element, index) => {
    const bo = element.businessObject
    const nodeType = mapBpmnTypeToNodeType(bo.$type)

    const config: NodeConfig = {
      nodeId: bo.id,
      nodeName: bo.name || '',
      nodeType,
      sortOrder: index,
    }

    if (nodeType === 'userTask') {
      const extensionAttrs = getExtensionAttrs(bo)
      config.assigneeType = extensionAttrs.assigneeType || 'fixed'
      config.assigneeConfig = extensionAttrs.assigneeConfig || '{}'
      config.multiInstanceType = extensionAttrs.multiInstanceType || 'none'
      config.completionRatio = extensionAttrs.completionRatio
        ? Number(extensionAttrs.completionRatio)
        : null
    }

    return config
  })
}

function mapBpmnTypeToNodeType(bpmnType: string): string {
  const map: Record<string, string> = {
    'bpmn:UserTask': 'userTask',
    'bpmn:ExclusiveGateway': 'exclusiveGateway',
    'bpmn:ParallelGateway': 'parallelGateway',
    'bpmn:StartEvent': 'startEvent',
    'bpmn:EndEvent': 'endEvent',
  }
  return map[bpmnType] || 'userTask'
}

function getExtensionAttrs(bo: any): Record<string, string> {
  const attrs: Record<string, string> = {}
  const extensionElements = bo.extensionElements
  if (!extensionElements) return attrs

  // Look for custom flowable attributes stored in documentation or extension values
  // bpmn-js stores custom attributes on the businessObject directly when using moddle extensions
  if (bo.$attrs) {
    for (const [key, value] of Object.entries(bo.$attrs)) {
      if (key.startsWith('flowable:')) {
        const attrName = key.replace('flowable:', '')
        attrs[attrName] = String(value)
      }
    }
  }

  return attrs
}

export interface ValidationError {
  nodeId: string
  nodeName: string
  message: string
}

export function validateProcess(modeler: BpmnModeler): ValidationError[] {
  const errors: ValidationError[] = []
  const elementRegistry = modeler.get('elementRegistry') as unknown as BpmnElementRegistry

  const startEvents = elementRegistry.filter(
    (e) => e.businessObject?.$type === 'bpmn:StartEvent',
  )
  const endEvents = elementRegistry.filter(
    (e) => e.businessObject?.$type === 'bpmn:EndEvent',
  )
  const userTasks = elementRegistry.filter(
    (e) => e.businessObject?.$type === 'bpmn:UserTask',
  )
  const gateways = elementRegistry.filter(
    (e) =>
      e.businessObject?.$type === 'bpmn:ExclusiveGateway' ||
      e.businessObject?.$type === 'bpmn:ParallelGateway',
  )

  if (startEvents.length === 0) {
    errors.push({ nodeId: '', nodeName: '', message: '流程缺少开始事件' })
  }

  for (const start of startEvents) {
    const bo = start.businessObject
    if (!bo.outgoing || bo.outgoing.length === 0) {
      errors.push({
        nodeId: bo.id,
        nodeName: bo.name || '开始事件',
        message: '开始事件没有出口连线',
      })
    }
  }

  if (endEvents.length === 0) {
    errors.push({ nodeId: '', nodeName: '', message: '流程缺少结束事件' })
  }

  for (const end of endEvents) {
    const bo = end.businessObject
    if (!bo.incoming || bo.incoming.length === 0) {
      errors.push({
        nodeId: bo.id,
        nodeName: bo.name || '结束事件',
        message: '结束事件没有入口连线',
      })
    }
  }

  for (const task of userTasks) {
    const bo = task.businessObject
    if (!bo.incoming || bo.incoming.length === 0) {
      errors.push({
        nodeId: bo.id,
        nodeName: bo.name || '用户任务',
        message: `用户任务"${bo.name || bo.id}"没有入口连线`,
      })
    }
    if (!bo.outgoing || bo.outgoing.length === 0) {
      errors.push({
        nodeId: bo.id,
        nodeName: bo.name || '用户任务',
        message: `用户任务"${bo.name || bo.id}"没有出口连线`,
      })
    }
  }

  for (const gw of gateways) {
    const bo = gw.businessObject
    const typeName = NODE_TYPE_LABEL_MAP[
      bo.$type === 'bpmn:ExclusiveGateway' ? 'exclusiveGateway' : 'parallelGateway'
    ] || '网关'

    if (!bo.outgoing || bo.outgoing.length < 2) {
      errors.push({
        nodeId: bo.id,
        nodeName: bo.name || typeName,
        message: `${typeName}"${bo.name || bo.id}"至少需要 2 个出口`,
      })
    }
  }

  return errors
}

export function injectTaskListeners(xml: string): string {
  // Inject approvalTaskCreateListener on all userTask elements that don't have it
  const listenerSnippet = `<extensionElements>
                <flowable:taskListener event="create" delegateExpression="\${approvalTaskCreateListener}"/>
            </extensionElements>`

  return xml.replace(/<userTask([^>]*)>/g, (match, attrs: string) => {
    if (match.includes('approvalTaskCreateListener')) return match
    if (match.includes('</userTask>')) {
      return match.replace('</userTask>', `${listenerSnippet}</userTask>`)
    }
    return `<userTask${attrs}>\n            ${listenerSnippet}\n        </userTask>`
  })
}
