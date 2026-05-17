import { describe, it, expect } from 'vitest'
import {
  generateDefaultXml,
  extractNodeConfigs,
  validateProcess,
  injectTaskListeners,
  normalizeBpmnXmlForViewer,
  parseSimpleBpmnDiagram,
} from './bpmn-utils'

// ---------------------------------------------------------------------------
// Helper: create a minimal mock modeler with elementRegistry
// ---------------------------------------------------------------------------
function makeMockModeler(elements: any[]) {
  return {
    get: (service: string) => {
      if (service === 'elementRegistry') {
        return {
          filter: (fn: (e: any) => boolean) => elements.filter(fn),
        }
      }
      return null
    },
  }
}

// ---------------------------------------------------------------------------
// generateDefaultXml
// ---------------------------------------------------------------------------
describe('generateDefaultXml', () => {
  it('generates valid BPMN XML with given key and name', () => {
    const xml = generateDefaultXml('leave_request', '请假流程')
    expect(xml).toContain('id="leave_request"')
    expect(xml).toContain('name="请假流程"')
    expect(xml).toContain('<startEvent')
    expect(xml).toContain('<endEvent')
    expect(xml).toContain('<sequenceFlow')
    expect(xml).toContain('xmlns:flowable="http://flowable.org/bpmn"')
    expect(xml).toContain('isExecutable="true"')
  })

  it('includes BPMN diagram elements with coordinates', () => {
    const xml = generateDefaultXml('test', 'Test')
    expect(xml).toContain('bpmndi:BPMNDiagram')
    expect(xml).toContain('omgdc:Bounds')
    expect(xml).toContain('omgdi:waypoint')
  })
})

// ---------------------------------------------------------------------------
// normalizeBpmnXmlForViewer
// ---------------------------------------------------------------------------
describe('normalizeBpmnXmlForViewer', () => {
  it('generates BPMNDI when returned diagram references stale element ids', () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
             xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
  <process id="leave_request" name="请假审批流程" isExecutable="true">
    <startEvent id="startEvent" name="发起申请" />
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="task1" />
    <userTask id="task1" name="组长审批" />
    <sequenceFlow id="flow2" sourceRef="task1" targetRef="task2" />
    <userTask id="task2" name="经理审批" />
    <sequenceFlow id="flow3" sourceRef="task2" targetRef="ccTask" />
    <serviceTask id="ccTask" name="抄送" />
    <sequenceFlow id="flow4" sourceRef="ccTask" targetRef="endEvent" />
    <endEvent id="endEvent" name="流程结束" />
  </process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_leave_request">
    <bpmndi:BPMNPlane id="BPMNPlane_leave_request" bpmnElement="leave_request">
      <bpmndi:BPMNShape id="BPMNShape_start" bpmnElement="start">
        <omgdc:Bounds x="120" y="160" width="36" height="36"/>
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>`

    const normalized = normalizeBpmnXmlForViewer(xml)

    expect(normalized).toContain('bpmnElement="startEvent"')
    expect(normalized).toContain('bpmnElement="task1"')
    expect(normalized).toContain('bpmnElement="task2"')
    expect(normalized).toContain('bpmnElement="ccTask"')
    expect(normalized).toContain('bpmnElement="endEvent"')
    expect(normalized).toContain('bpmnElement="flow4"')
    expect(normalized).not.toContain('bpmnElement="start"')
  })

  it('keeps XML unchanged when diagram already covers all nodes and flows', () => {
    const xml = generateDefaultXml('leave_request', '请假流程')

    expect(normalizeBpmnXmlForViewer(xml)).toBe(xml)
  })

  it('returns original XML when parsing fails', () => {
    const xml = '<definitions><process></definitions>'

    expect(normalizeBpmnXmlForViewer(xml)).toBe(xml)
  })
})

describe('parseSimpleBpmnDiagram', () => {
  it('extracts process nodes and sequence flows without relying on BPMNDI', () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
  <process id="leave_request" name="请假审批流程" isExecutable="true">
    <startEvent id="startEvent" name="发起申请" />
    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="task1" />
    <userTask id="task1" name="组长审批" />
    <sequenceFlow id="flow2" sourceRef="task1" targetRef="endEvent" />
    <endEvent id="endEvent" name="流程结束" />
  </process>
  <bpmndi:BPMNDiagram xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI">
    <bpmndi:BPMNPlane bpmnElement="leave_request" />
  </bpmndi:BPMNDiagram>
</definitions>`

    const diagram = parseSimpleBpmnDiagram(xml)

    expect(diagram?.nodes.map((node) => node.id)).toEqual(['startEvent', 'task1', 'endEvent'])
    expect(diagram?.nodes[1].name).toBe('组长审批')
    expect(diagram?.flows.map((flow) => flow.id)).toEqual(['flow1', 'flow2'])
  })

  it('returns null for invalid XML', () => {
    expect(parseSimpleBpmnDiagram('<definitions><process></definitions>')).toBeNull()
  })
})

// ---------------------------------------------------------------------------
// extractNodeConfigs
// ---------------------------------------------------------------------------
describe('extractNodeConfigs', () => {
  it('extracts user tasks with assignee config from extension attrs', () => {
    const elements = [
      {
        businessObject: {
          $type: 'bpmn:UserTask',
          id: 'task1',
          name: '主管审批',
          extensionElements: {},
          $attrs: {
            'flowable:assigneeType': 'deptLeader',
            'flowable:assigneeConfig': '{"userId":1}',
            'flowable:multiInstanceType': 'countersign',
            'flowable:completionRatio': '80',
          },
        },
      },
    ]
    const modeler = makeMockModeler(elements)
    const configs = extractNodeConfigs(modeler as any)

    expect(configs).toHaveLength(1)
    expect(configs[0].nodeId).toBe('task1')
    expect(configs[0].nodeName).toBe('主管审批')
    expect(configs[0].nodeType).toBe('userTask')
    expect(configs[0].assigneeType).toBe('deptLeader')
    expect(configs[0].assigneeConfig).toBe('{"userId":1}')
    expect(configs[0].multiInstanceType).toBe('countersign')
    expect(configs[0].completionRatio).toBe(80)
  })

  it('extracts gateways, start, and end events', () => {
    const elements = [
      { businessObject: { $type: 'bpmn:StartEvent', id: 'start', name: '开始' } },
      { businessObject: { $type: 'bpmn:ExclusiveGateway', id: 'gw1', name: '排他' } },
      { businessObject: { $type: 'bpmn:ParallelGateway', id: 'gw2', name: '并行' } },
      { businessObject: { $type: 'bpmn:EndEvent', id: 'end', name: '结束' } },
    ]
    const modeler = makeMockModeler(elements)
    const configs = extractNodeConfigs(modeler as any)

    expect(configs).toHaveLength(4)
    expect(configs[0].nodeType).toBe('startEvent')
    expect(configs[1].nodeType).toBe('exclusiveGateway')
    expect(configs[2].nodeType).toBe('parallelGateway')
    expect(configs[3].nodeType).toBe('endEvent')
  })

  it('uses default values when extension attrs are missing on userTask', () => {
    const elements = [
      {
        businessObject: {
          $type: 'bpmn:UserTask',
          id: 'task2',
          name: '',
          $attrs: {},
        },
      },
    ]
    const modeler = makeMockModeler(elements)
    const configs = extractNodeConfigs(modeler as any)

    expect(configs[0].assigneeType).toBe('fixed')
    expect(configs[0].assigneeConfig).toBe('{}')
    expect(configs[0].multiInstanceType).toBe('none')
    expect(configs[0].completionRatio).toBeNull()
  })

  it('skips elements without businessObject', () => {
    const elements = [
      { businessObject: null },
      { businessObject: { $type: 'bpmn:SequenceFlow', id: 'flow1' } },
    ]
    const modeler = makeMockModeler(elements)
    const configs = extractNodeConfigs(modeler as any)

    expect(configs).toHaveLength(0)
  })

  it('uses empty string for nodeName when name is undefined', () => {
    const elements = [
      { businessObject: { $type: 'bpmn:UserTask', id: 'task3' } },
    ]
    const modeler = makeMockModeler(elements)
    const configs = extractNodeConfigs(modeler as any)

    expect(configs[0].nodeName).toBe('')
  })
})

// ---------------------------------------------------------------------------
// validateProcess
// ---------------------------------------------------------------------------
describe('validateProcess', () => {
  it('reports missing start event', () => {
    const elements = [
      { businessObject: { $type: 'bpmn:EndEvent', id: 'end', incoming: [{}] } },
    ]
    const modeler = makeMockModeler(elements)
    const errors = validateProcess(modeler as any)

    expect(errors.some((e) => e.message.includes('开始事件'))).toBe(true)
  })

  it('reports missing end event', () => {
    const elements = [
      { businessObject: { $type: 'bpmn:StartEvent', id: 'start', outgoing: [{}] } },
    ]
    const modeler = makeMockModeler(elements)
    const errors = validateProcess(modeler as any)

    expect(errors.some((e) => e.message.includes('结束事件'))).toBe(true)
  })

  it('reports start event with no outgoing flow', () => {
    const elements = [
      { businessObject: { $type: 'bpmn:StartEvent', id: 'start', name: '开始', outgoing: [] } },
      { businessObject: { $type: 'bpmn:EndEvent', id: 'end', incoming: [{}] } },
    ]
    const modeler = makeMockModeler(elements)
    const errors = validateProcess(modeler as any)

    expect(errors.some((e) => e.message.includes('出口连线'))).toBe(true)
  })

  it('reports end event with no incoming flow', () => {
    const elements = [
      { businessObject: { $type: 'bpmn:StartEvent', id: 'start', outgoing: [{}] } },
      { businessObject: { $type: 'bpmn:EndEvent', id: 'end', name: '结束', incoming: [] } },
    ]
    const modeler = makeMockModeler(elements)
    const errors = validateProcess(modeler as any)

    expect(errors.some((e) => e.message.includes('入口连线'))).toBe(true)
  })

  it('reports user task with no incoming or outgoing flows', () => {
    const elements = [
      { businessObject: { $type: 'bpmn:StartEvent', id: 'start', outgoing: [{}] } },
      { businessObject: { $type: 'bpmn:UserTask', id: 'task1', name: '审批', incoming: [], outgoing: [] } },
      { businessObject: { $type: 'bpmn:EndEvent', id: 'end', incoming: [{}] } },
    ]
    const modeler = makeMockModeler(elements)
    const errors = validateProcess(modeler as any)

    expect(errors.some((e) => e.message.includes('入口连线'))).toBe(true)
    expect(errors.some((e) => e.message.includes('出口连线'))).toBe(true)
  })

  it('reports gateway with fewer than 2 outgoing flows', () => {
    const elements = [
      { businessObject: { $type: 'bpmn:StartEvent', id: 'start', outgoing: [{}] } },
      { businessObject: { $type: 'bpmn:ExclusiveGateway', id: 'gw1', name: '排他', outgoing: [{}] } },
      { businessObject: { $type: 'bpmn:EndEvent', id: 'end', incoming: [{}] } },
    ]
    const modeler = makeMockModeler(elements)
    const errors = validateProcess(modeler as any)

    expect(errors.some((e) => e.message.includes('至少需要 2 个出口'))).toBe(true)
  })

  it('returns no errors for a valid minimal process', () => {
    const elements = [
      { businessObject: { $type: 'bpmn:StartEvent', id: 'start', outgoing: [{}] } },
      {
        businessObject: {
          $type: 'bpmn:ExclusiveGateway',
          id: 'gw1',
          outgoing: [{}, {}],
        },
      },
      { businessObject: { $type: 'bpmn:EndEvent', id: 'end1', incoming: [{}] } },
      { businessObject: { $type: 'bpmn:EndEvent', id: 'end2', incoming: [{}] } },
    ]
    const modeler = makeMockModeler(elements)
    const errors = validateProcess(modeler as any)

    expect(errors).toHaveLength(0)
  })

  it('uses diagram element connections when imported XML has no businessObject links', () => {
    const flow1 = {}
    const flow2 = {}
    const flow3 = {}
    const flow4 = {}
    const elements = [
      {
        type: 'bpmn:StartEvent',
        businessObject: { $type: 'bpmn:StartEvent', id: 'start', name: '提交申请' },
        outgoing: [flow1],
      },
      {
        type: 'bpmn:UserTask',
        businessObject: { $type: 'bpmn:UserTask', id: 'deptLeaderApprove', name: '部门负责人审批' },
        incoming: [flow1],
        outgoing: [flow2],
      },
      {
        type: 'bpmn:ExclusiveGateway',
        businessObject: { $type: 'bpmn:ExclusiveGateway', id: 'gateway1', name: '审批结果' },
        incoming: [flow2],
        outgoing: [flow3, flow4],
      },
      {
        type: 'bpmn:EndEvent',
        businessObject: { $type: 'bpmn:EndEvent', id: 'endApproved', name: '审批通过' },
        incoming: [flow3],
      },
      {
        type: 'bpmn:EndEvent',
        businessObject: { $type: 'bpmn:EndEvent', id: 'endRejected', name: '审批驳回' },
        incoming: [flow4],
      },
    ]
    const modeler = makeMockModeler(elements)
    const errors = validateProcess(modeler as any)

    expect(errors).toHaveLength(0)
  })

  it('ignores label elements that share the same businessObject', () => {
    const startBo = { $type: 'bpmn:StartEvent', id: 'start', name: '开始' }
    const endBo = { $type: 'bpmn:EndEvent', id: 'end', name: '结束' }
    const elements = [
      { type: 'label', businessObject: startBo, labelTarget: {} },
      { type: 'bpmn:StartEvent', businessObject: startBo, outgoing: [{}] },
      { type: 'label', businessObject: endBo, labelTarget: {} },
      { type: 'bpmn:EndEvent', businessObject: endBo, incoming: [{}] },
    ]
    const modeler = makeMockModeler(elements)
    const errors = validateProcess(modeler as any)

    expect(errors).toHaveLength(0)
  })

  it('validates parallel gateway similarly', () => {
    const elements = [
      { businessObject: { $type: 'bpmn:StartEvent', id: 'start', outgoing: [{}] } },
      { businessObject: { $type: 'bpmn:ParallelGateway', id: 'pg1', outgoing: [{}] } },
      { businessObject: { $type: 'bpmn:EndEvent', id: 'end', incoming: [{}] } },
    ]
    const modeler = makeMockModeler(elements)
    const errors = validateProcess(modeler as any)

    expect(errors.some((e) => e.message.includes('并行网关'))).toBe(true)
  })
})

// ---------------------------------------------------------------------------
// injectTaskListeners
// ---------------------------------------------------------------------------
describe('injectTaskListeners', () => {
  it('injects listener into self-closing userTask tags', () => {
    const xml = '<userTask id="task1" name="审批"/>'
    const result = injectTaskListeners(xml)

    expect(result).toContain('approvalTaskCreateListener')
    expect(result).toContain('delegateExpression')
  })

  it('injects listener into open userTask tags', () => {
    const xml = '<userTask id="task1" name="审批"><documentation/></userTask>'
    const result = injectTaskListeners(xml)

    expect(result).toContain('approvalTaskCreateListener')
  })

  it('skips injection when listener text appears in opening tag', () => {
    // The regex only checks the opening <userTask ...> tag for existing listener text
    const xml = '<userTask id="task1" approvalTaskCreateListener="true"/>'
    const result = injectTaskListeners(xml)

    const count = (result.match(/approvalTaskCreateListener/g) || []).length
    expect(count).toBe(1)
  })

  it('handles XML with no userTask elements', () => {
    const xml = '<startEvent id="start"/><endEvent id="end"/>'
    const result = injectTaskListeners(xml)

    expect(result).toBe(xml)
  })
})
