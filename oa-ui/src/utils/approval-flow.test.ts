import { describe, expect, it } from 'vitest'
import {
  appendApprovalFlowBranchChild,
  createApprovalFlowNode,
  createDefaultApprovalFlow,
  findApprovalFlowBranch,
  generateApprovalFlowArtifacts,
  getApprovalFlowBranchChildren,
  getApprovalFlowLinearNodes,
  insertApprovalFlowNodeAfter,
  normalizeApprovalFlowModel,
  parseApprovalFlowModelFromBpmnXml,
  removeApprovalFlowNode,
  validateApprovalFlow,
  validateApprovalFlowForDesigner,
  type ApprovalFlowNode,
} from './approval-flow'

describe('approval flow utils', () => {
  it('generates BPMN and node configs for linear approval flow', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      children: [
        {
          id: 'deptApprove',
          type: 'approval',
          name: '部门负责人审批',
          assigneeType: 'deptLeader',
          assigneeConfig: {},
          children: [{ id: 'end', type: 'end', name: '流程结束' }],
        },
      ],
    }

    const result = generateApprovalFlowArtifacts(flow, 'leave_request', '请假审批')

    expect(result.bpmnXml).toContain('<userTask id="deptApprove"')
    expect(result.bpmnXml).toContain('approvalTaskCreateListener')
    expect(result.bpmnXml).toContain('documentation id="approvalFlowModel"')
    expect(result.bpmnXml).toContain('sourceRef="start" targetRef="deptApprove"')
    expect(sequenceFlowIds(result.bpmnXml).filter((id) => id === 'flow_deptApprove_end')).toHaveLength(1)
    expect(result.nodeConfigs).toMatchObject([
      { nodeId: 'deptApprove', nodeName: '部门负责人审批', nodeType: 'userTask', assigneeType: 'deptLeader' },
    ])
    expect(parseApprovalFlowModelFromBpmnXml(result.bpmnXml)?.children?.[0]?.id).toBe('deptApprove')
  })

  it('generates exclusive gateway conditions and default branch', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      children: [
        {
          id: 'gateway1',
          type: 'exclusive',
          name: '金额判断',
          branches: [
            {
              id: 'large',
              name: '大额',
              condition: { fieldKey: 'amount', operator: '>', value: 1000 },
              children: [{ id: 'manager', type: 'approval', name: '经理审批', assigneeType: 'fixed', assigneeConfig: { userId: 1 } }],
            },
            {
              id: 'default',
              name: '默认',
              isDefault: true,
              children: [],
            },
          ],
          children: [{ id: 'end', type: 'end', name: '流程结束' }],
        },
      ],
    }

    const result = generateApprovalFlowArtifacts(flow, 'expense', '费用审批')

    expect(result.bpmnXml).toContain('<exclusiveGateway id="gateway1"')
    expect(result.bpmnXml).toContain('default="flow_gateway1_default"')
    expect(result.bpmnXml).toContain('${amount > 1000}')
    expect(sequenceFlowIds(result.bpmnXml)).toEqual([...new Set(sequenceFlowIds(result.bpmnXml))])
  })

  it('deduplicates sequence flow ids when old node children contain end node', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      children: [
        {
          id: 'approval_1',
          type: 'approval',
          name: '审批',
          assigneeType: 'deptLeader',
          assigneeConfig: {},
          children: [{ id: 'end', type: 'end', name: '流程结束' }],
        },
        { id: 'end', type: 'end', name: '流程结束' },
      ],
    }

    const result = generateApprovalFlowArtifacts(flow, 'legacy_flow', '旧草稿')
    const ids = sequenceFlowIds(result.bpmnXml)

    expect(ids).toEqual([...new Set(ids)])
    expect(ids.filter((id) => id === 'flow_approval_1_end')).toHaveLength(1)
  })

  it('generates deployable no-op implementation for cc service task', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      children: [
        { id: 'approval1', type: 'approval', name: '审批', assigneeType: 'deptLeader', assigneeConfig: {} },
        { id: 'cc1', type: 'cc', name: '抄送节点' },
        { id: 'end', type: 'end', name: '流程结束' },
      ],
    }

    const result = generateApprovalFlowArtifacts(flow, 'cc_flow', '抄送流程')

    expect(result.bpmnXml).toContain('<serviceTask id="cc1" name="抄送节点" flowable:expression="${true}"/>')
    expect(result.nodeConfigs).toEqual(expect.arrayContaining([
      expect.objectContaining({ nodeId: 'cc1', nodeType: 'ccTask' }),
    ]))
  })

  it('supports lowflow-style next chain model', () => {
    const flow = createDefaultApprovalFlow()

    expect(flow.next?.type).toBe('approval')
    expect(flow.next?.next?.type).toBe('end')
    expect(getApprovalFlowLinearNodes(flow).map((node) => node.type)).toEqual(['approval', 'end'])

    const result = generateApprovalFlowArtifacts(flow, 'next_flow', '链式流程')

    expect(result.bpmnXml).toContain('sourceRef="start" targetRef="')
    expect(result.bpmnXml).toContain('targetRef="end"')
    expect(parseApprovalFlowModelFromBpmnXml(result.bpmnXml)?.next?.type).toBe('approval')
  })

  it('normalizes legacy children chain to next chain', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      children: [
        { id: 'approval1', type: 'approval', name: '审批', assigneeType: 'deptLeader', assigneeConfig: {} },
        { id: 'end', type: 'end', name: '流程结束' },
      ],
    }

    normalizeApprovalFlowModel(flow)

    expect(flow.children).toBeUndefined()
    expect(flow.next?.id).toBe('approval1')
    expect(flow.next?.next?.id).toBe('end')
  })

  it('inserts and removes nodes by rewiring next chain', () => {
    const flow = createDefaultApprovalFlow()
    const approval = flow.next!
    const cc = createApprovalFlowNode('cc')

    expect(insertApprovalFlowNodeAfter(flow, approval.id, cc)).toBe(true)
    expect(approval.next?.id).toBe(cc.id)
    expect(cc.next?.type).toBe('end')

    expect(removeApprovalFlowNode(flow, cc.id)).toBe(true)
    expect(approval.next?.type).toBe('end')
  })

  it('appends branch children as an entry node plus next chain', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      next: {
        id: 'gateway',
        type: 'exclusive',
        name: '条件',
        branches: [{ id: 'b1', name: '条件1', children: [] }],
      },
    }
    const branch = findApprovalFlowBranch(flow, 'b1')!
    const approval = createApprovalFlowNode('approval')
    const cc = createApprovalFlowNode('cc')

    appendApprovalFlowBranchChild(branch, approval)
    appendApprovalFlowBranchChild(branch, cc)

    expect(branch.children).toHaveLength(1)
    expect(getApprovalFlowBranchChildren(branch).map((node) => node.id)).toEqual([approval.id, cc.id])
  })

  it('maps designer validation errors to node and branch ids', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      next: {
        id: 'approval1',
        type: 'approval',
        name: '审批',
        next: {
          id: 'gateway1',
          type: 'exclusive',
          name: '判断',
          branches: [
            { id: 'branch1', name: '条件1', condition: { fieldKey: '', operator: '==', value: '' }, children: [] },
            { id: 'branch2', name: '默认', isDefault: true, children: [] },
          ],
          next: { id: 'end', type: 'end', name: '流程结束' },
        },
      },
    }

    const errors = validateApprovalFlowForDesigner(flow)

    expect(errors.approval1.some((item) => item.message.includes('审批节点未配置审批人'))).toBe(true)
    expect(errors.branch1.some((item) => item.message.includes('未选择表单字段'))).toBe(true)
  })


  it('validates assignee and default branch', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      children: [
        { id: 'approval1', type: 'approval', name: '审批' },
        { id: 'gateway1', type: 'exclusive', name: '判断', branches: [{ id: 'a', name: 'A', condition: { fieldKey: '', operator: '==', value: '' }, children: [] }] },
      ],
    }

    const errors = validateApprovalFlow(flow)

    expect(errors.some((item) => item.includes('审批节点未配置审批人'))).toBe(true)
    expect(errors.some((item) => item.includes('条件分支至少需要 2 个出口'))).toBe(true)
    expect(errors.some((item) => item.includes('条件分支缺少默认出口'))).toBe(true)
    expect(errors.some((item) => item.includes('条件分支未选择表单字段'))).toBe(true)
  })

  it('validates fixed, role and expression assignee configs', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      children: [
        { id: 'fixed1', type: 'approval', name: '指定成员', assigneeType: 'fixed', assigneeConfig: {} },
        { id: 'role1', type: 'approval', name: '角色审批', assigneeType: 'role', assigneeConfig: {} },
        { id: 'expr1', type: 'approval', name: '表达式审批', assigneeType: 'expression', assigneeConfig: { expression: 'initiator' } },
        { id: 'end', type: 'end', name: '流程结束' },
      ],
    }

    const errors = validateApprovalFlow(flow)

    expect(errors.some((item) => item.includes('未配置指定成员'))).toBe(true)
    expect(errors.some((item) => item.includes('未配置角色'))).toBe(true)
    expect(errors.some((item) => item.includes('表达式格式不合法'))).toBe(true)
  })
})

function sequenceFlowIds(xml: string) {
  return [...xml.matchAll(/<sequenceFlow id="([^"]+)"/g)].map((match) => match[1])
}
