import type BpmnModeler from 'bpmn-js/lib/Modeler'
import type { NodeConfig } from '@/api/template'
import { NODE_TYPE_LABEL_MAP } from './constants'

interface BpmnElement {
  type?: string
  businessObject: BpmnBusinessObject
  incoming?: unknown[]
  outgoing?: unknown[]
  labelTarget?: unknown
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

const BPMNDI_NS = 'http://www.omg.org/spec/BPMN/20100524/DI'
const OMGDC_NS = 'http://www.omg.org/spec/DD/20100524/DC'
const OMGDI_NS = 'http://www.omg.org/spec/DD/20100524/DI'

interface DiagramShape {
  id: string
  x: number
  y: number
  width: number
  height: number
}

export interface SimpleBpmnNode {
  id: string
  name: string
  type: string
}

export interface SimpleBpmnFlow {
  id: string
  sourceRef: string
  targetRef: string
}

export interface SimpleBpmnDiagram {
  nodes: SimpleBpmnNode[]
  flows: SimpleBpmnFlow[]
}

const FLOW_NODE_NAMES = new Set([
  'startEvent',
  'userTask',
  'serviceTask',
  'exclusiveGateway',
  'parallelGateway',
  'inclusiveGateway',
  'endEvent',
])

export function normalizeBpmnXmlForViewer(xml: string): string {
  if (!xml.trim()) return xml

  const parser = new DOMParser()
  const doc = parser.parseFromString(xml, 'application/xml')
  if (getElementsByLocalName(doc, 'parsererror').length > 0) {
    return xml
  }

  const definitions = getFirstElementByLocalName(doc, 'definitions')
  const process = getFirstElementByLocalName(doc, 'process')
  if (!definitions || !process) {
    return xml
  }

  const flowNodes = Array.from(process.children).filter((node) =>
    FLOW_NODE_NAMES.has(node.localName),
  )
  const sequenceFlows = getDirectChildrenByLocalName(process, 'sequenceFlow')
  if (flowNodes.length === 0) {
    return xml
  }

  const flowNodeIds = new Set(flowNodes.map((node) => node.getAttribute('id')).filter(Boolean))
  const sequenceFlowIds = new Set(sequenceFlows.map((flow) => flow.getAttribute('id')).filter(Boolean))
  if (hasCompleteDiagram(doc, flowNodeIds, sequenceFlowIds)) {
    return xml
  }

  definitions.setAttribute('xmlns:bpmndi', BPMNDI_NS)
  definitions.setAttribute('xmlns:omgdc', OMGDC_NS)
  definitions.setAttribute('xmlns:omgdi', OMGDI_NS)

  for (const diagram of getElementsByLocalName(doc, 'BPMNDiagram')) {
    diagram.parentElement?.removeChild(diagram)
  }

  const shapeByNodeId = new Map<string, DiagramShape>()
  const diagram = doc.createElementNS(BPMNDI_NS, 'bpmndi:BPMNDiagram')
  diagram.setAttribute('id', `BPMNDiagram_${process.getAttribute('id') || 'process'}`)

  const plane = doc.createElementNS(BPMNDI_NS, 'bpmndi:BPMNPlane')
  plane.setAttribute('id', `BPMNPlane_${process.getAttribute('id') || 'process'}`)
  plane.setAttribute('bpmnElement', process.getAttribute('id') || '')

  flowNodes.forEach((node, index) => {
    const nodeId = node.getAttribute('id')
    if (!nodeId) return

    const shape = createShape(node.localName, nodeId, index)
    shapeByNodeId.set(nodeId, shape)
    plane.appendChild(createBpmnShape(doc, shape))
  })

  sequenceFlows.forEach((flow) => {
    const flowId = flow.getAttribute('id')
    const sourceRef = flow.getAttribute('sourceRef')
    const targetRef = flow.getAttribute('targetRef')
    if (!flowId || !sourceRef || !targetRef) return

    const sourceShape = shapeByNodeId.get(sourceRef)
    const targetShape = shapeByNodeId.get(targetRef)
    if (!sourceShape || !targetShape) return

    plane.appendChild(createBpmnEdge(doc, flowId, sourceShape, targetShape))
  })

  diagram.appendChild(plane)
  definitions.appendChild(diagram)

  return new XMLSerializer().serializeToString(doc)
}

export function parseSimpleBpmnDiagram(xml: string): SimpleBpmnDiagram | null {
  if (!xml.trim()) return null

  const parser = new DOMParser()
  const doc = parser.parseFromString(xml, 'application/xml')
  if (getElementsByLocalName(doc, 'parsererror').length > 0) {
    return null
  }

  const process = getFirstElementByLocalName(doc, 'process')
  if (!process) {
    return null
  }

  const nodes = Array.from(process.children)
    .filter((node) => FLOW_NODE_NAMES.has(node.localName))
    .map((node) => ({
      id: node.getAttribute('id') || '',
      name: node.getAttribute('name') || node.getAttribute('id') || '',
      type: node.localName,
    }))
    .filter((node) => node.id)

  if (nodes.length === 0) {
    return null
  }

  const flows = getDirectChildrenByLocalName(process, 'sequenceFlow')
    .map((flow) => ({
      id: flow.getAttribute('id') || '',
      sourceRef: flow.getAttribute('sourceRef') || '',
      targetRef: flow.getAttribute('targetRef') || '',
    }))
    .filter((flow) => flow.id && flow.sourceRef && flow.targetRef)

  return { nodes, flows }
}

function hasCompleteDiagram(doc: Document, flowNodeIds: Set<string | null>, sequenceFlowIds: Set<string | null>) {
  const shapeIds = new Set(
    getElementsByLocalName(doc, 'BPMNShape').map((shape) => shape.getAttribute('bpmnElement')),
  )
  const edgeIds = new Set(
    getElementsByLocalName(doc, 'BPMNEdge').map((edge) => edge.getAttribute('bpmnElement')),
  )

  for (const nodeId of flowNodeIds) {
    if (nodeId && !shapeIds.has(nodeId)) return false
  }
  for (const flowId of sequenceFlowIds) {
    if (flowId && !edgeIds.has(flowId)) return false
  }

  return flowNodeIds.size > 0
}

function createShape(nodeType: string, nodeId: string, index: number): DiagramShape {
  const isEvent = nodeType === 'startEvent' || nodeType === 'endEvent'
  const isGateway = nodeType.endsWith('Gateway')
  const width = isEvent ? 36 : isGateway ? 50 : 120
  const height = isEvent ? 36 : isGateway ? 50 : 80

  return {
    id: nodeId,
    x: 120 + index * 180,
    y: 178 - height / 2,
    width,
    height,
  }
}

function createBpmnShape(doc: Document, shape: DiagramShape) {
  const bpmnShape = doc.createElementNS(BPMNDI_NS, 'bpmndi:BPMNShape')
  bpmnShape.setAttribute('id', `BPMNShape_${shape.id}`)
  bpmnShape.setAttribute('bpmnElement', shape.id)

  const bounds = doc.createElementNS(OMGDC_NS, 'omgdc:Bounds')
  bounds.setAttribute('x', String(shape.x))
  bounds.setAttribute('y', String(shape.y))
  bounds.setAttribute('width', String(shape.width))
  bounds.setAttribute('height', String(shape.height))

  bpmnShape.appendChild(bounds)
  return bpmnShape
}

function createBpmnEdge(doc: Document, flowId: string, source: DiagramShape, target: DiagramShape) {
  const edge = doc.createElementNS(BPMNDI_NS, 'bpmndi:BPMNEdge')
  edge.setAttribute('id', `BPMNEdge_${flowId}`)
  edge.setAttribute('bpmnElement', flowId)

  const sourceWaypoint = doc.createElementNS(OMGDI_NS, 'omgdi:waypoint')
  sourceWaypoint.setAttribute('x', String(source.x + source.width))
  sourceWaypoint.setAttribute('y', String(source.y + source.height / 2))

  const targetWaypoint = doc.createElementNS(OMGDI_NS, 'omgdi:waypoint')
  targetWaypoint.setAttribute('x', String(target.x))
  targetWaypoint.setAttribute('y', String(target.y + target.height / 2))

  edge.appendChild(sourceWaypoint)
  edge.appendChild(targetWaypoint)
  return edge
}

function getFirstElementByLocalName(doc: Document, localName: string) {
  return getElementsByLocalName(doc, localName)[0] ?? null
}

function getDirectChildrenByLocalName(parent: Element, localName: string) {
  return Array.from(parent.children).filter((child) => child.localName === localName)
}

function getElementsByLocalName(doc: Document, localName: string) {
  return Array.from(doc.getElementsByTagName('*')).filter((node) => node.localName === localName)
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

  const startEvents = getFlowElements(elementRegistry, ['bpmn:StartEvent'])
  const endEvents = getFlowElements(elementRegistry, ['bpmn:EndEvent'])
  const userTasks = getFlowElements(elementRegistry, ['bpmn:UserTask'])
  const gateways = getFlowElements(elementRegistry, [
    'bpmn:ExclusiveGateway',
    'bpmn:ParallelGateway',
  ])

  if (startEvents.length === 0) {
    errors.push({ nodeId: '', nodeName: '', message: '流程缺少开始事件' })
  }

  for (const start of startEvents) {
    const bo = start.businessObject
    if (connectionCount(start, 'outgoing') === 0) {
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
    if (connectionCount(end, 'incoming') === 0) {
      errors.push({
        nodeId: bo.id,
        nodeName: bo.name || '结束事件',
        message: '结束事件没有入口连线',
      })
    }
  }

  for (const task of userTasks) {
    const bo = task.businessObject
    if (connectionCount(task, 'incoming') === 0) {
      errors.push({
        nodeId: bo.id,
        nodeName: bo.name || '用户任务',
        message: `用户任务"${bo.name || bo.id}"没有入口连线`,
      })
    }
    if (connectionCount(task, 'outgoing') === 0) {
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

    if (connectionCount(gw, 'outgoing') < 2) {
      errors.push({
        nodeId: bo.id,
        nodeName: bo.name || typeName,
        message: `${typeName}"${bo.name || bo.id}"至少需要 2 个出口`,
      })
    }
  }

  return errors
}

function getFlowElements(elementRegistry: BpmnElementRegistry, types: string[]): BpmnElement[] {
  const typeSet = new Set(types)
  const uniqueById = new Map<string, BpmnElement>()

  elementRegistry
    .filter((element) => {
      const bo = element.businessObject
      return Boolean(
        bo?.id &&
        typeSet.has(bo.$type) &&
        element.type !== 'label' &&
        !element.labelTarget,
      )
    })
    .forEach((element) => {
      if (!uniqueById.has(element.businessObject.id)) {
        uniqueById.set(element.businessObject.id, element)
      }
    })

  return Array.from(uniqueById.values())
}

function connectionCount(element: BpmnElement, direction: 'incoming' | 'outgoing'): number {
  const elementConnections = element[direction]
  if (Array.isArray(elementConnections)) {
    return elementConnections.length
  }

  const businessConnections = element.businessObject?.[direction]
  if (Array.isArray(businessConnections)) {
    return businessConnections.length
  }

  return businessConnections ? 1 : 0
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
