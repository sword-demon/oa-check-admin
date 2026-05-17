import type { NodeConfig } from '@/api/template'
import { ASSIGNEE_TYPE_OPTIONS } from '@/bpmn/constants'
import { normalizeBpmnXmlForViewer } from '@/bpmn/bpmn-utils'

export type ApprovalFlowNodeType = 'start' | 'approval' | 'cc' | 'exclusive' | 'parallel' | 'end'

export interface ApprovalFlowCondition {
  fieldKey: string
  operator: '==' | '!=' | '>' | '>=' | '<' | '<=' | 'contains'
  value: string | number | boolean
}

export interface ApprovalFlowBranch {
  id: string
  name: string
  isDefault?: boolean
  condition?: ApprovalFlowCondition
  children: ApprovalFlowNode[]
}

export interface ApprovalFlowNode {
  id: string
  type: ApprovalFlowNodeType
  name: string
  assigneeType?: string
  assigneeConfig?: Record<string, unknown>
  branches?: ApprovalFlowBranch[]
  next?: ApprovalFlowNode
  /**
   * Legacy shape used by earlier drafts. New designer mutations use `next`,
   * but generation keeps this for compatibility with saved template XML.
   */
  children?: ApprovalFlowNode[]
}

export interface GeneratedApprovalFlow {
  bpmnXml: string
  nodeConfigs: NodeConfig[]
}

export interface ApprovalFlowValidationError {
  id: string
  name: string
  message: string
}

const FLOW_MODEL_DOCUMENTATION_ID = 'approvalFlowModel'

export function createDefaultApprovalFlow(): ApprovalFlowNode {
  return {
    id: 'start',
    type: 'start',
    name: '发起申请',
    next: {
      id: `approval_${Date.now()}`,
      type: 'approval',
      name: '审批',
      assigneeType: 'deptLeader',
      assigneeConfig: {},
      next: { id: 'end', type: 'end', name: '流程结束' },
    },
  }
}

export function normalizeApprovalFlowModel(root: ApprovalFlowNode): ApprovalFlowNode {
  normalizeNode(root)
  return root
}

export function getApprovalFlowLinearNodes(root: ApprovalFlowNode): ApprovalFlowNode[] {
  return childChain(root)
}

export function createApprovalFlowNode(type: ApprovalFlowNodeType): ApprovalFlowNode {
  const id = newId(type)
  if (type === 'start') {
    return { id, type, name: '发起申请' }
  }
  if (type === 'approval') {
    return { id, type, name: '审批节点', assigneeType: 'deptLeader', assigneeConfig: {} }
  }
  if (type === 'cc') {
    return { id, type, name: '抄送节点', assigneeType: 'initiator', assigneeConfig: {} }
  }
  if (type === 'exclusive') {
    return {
      id,
      type,
      name: '条件分支',
      branches: [
        { id: newId('branch'), name: '条件1', condition: { fieldKey: '', operator: '==', value: '' }, children: [] },
        { id: newId('branch'), name: '默认', isDefault: true, children: [] },
      ],
    }
  }
  if (type === 'end') {
    return { id, type, name: '流程结束' }
  }
  return { id, type, name: type === 'parallel' ? '并行网关' : '流程节点' }
}

export function findApprovalFlowNode(
  node: ApprovalFlowNode,
  nodeId: string,
  seen = new Set<string>(),
): ApprovalFlowNode | null {
  if (seen.has(node.id)) return null
  seen.add(node.id)
  if (node.id === nodeId) return node
  if (node.next) {
    const found = findApprovalFlowNode(node.next, nodeId, seen)
    if (found) return found
  }
  for (const child of node.children || []) {
    const found = findApprovalFlowNode(child, nodeId, seen)
    if (found) return found
  }
  for (const branch of node.branches || []) {
    for (const child of branch.children) {
      const found = findApprovalFlowNode(child, nodeId, seen)
      if (found) return found
    }
  }
  return null
}

export function findApprovalFlowBranch(
  node: ApprovalFlowNode,
  branchId: string,
  seen = new Set<string>(),
): ApprovalFlowBranch | null {
  if (seen.has(node.id)) return null
  seen.add(node.id)
  for (const branch of node.branches || []) {
    if (branch.id === branchId) return branch
    for (const child of branch.children) {
      const found = findApprovalFlowBranch(child, branchId, seen)
      if (found) return found
    }
  }
  if (node.next) {
    const found = findApprovalFlowBranch(node.next, branchId, seen)
    if (found) return found
  }
  for (const child of node.children || []) {
    const found = findApprovalFlowBranch(child, branchId, seen)
    if (found) return found
  }
  return null
}

export function findApprovalFlowBranchOwner(
  node: ApprovalFlowNode,
  branchId: string,
  seen = new Set<string>(),
): ApprovalFlowNode | null {
  if (seen.has(node.id)) return null
  seen.add(node.id)
  if (node.branches?.some((branch) => branch.id === branchId)) return node
  for (const branch of node.branches || []) {
    for (const child of branch.children) {
      const found = findApprovalFlowBranchOwner(child, branchId, seen)
      if (found) return found
    }
  }
  if (node.next) {
    const found = findApprovalFlowBranchOwner(node.next, branchId, seen)
    if (found) return found
  }
  for (const child of node.children || []) {
    const found = findApprovalFlowBranchOwner(child, branchId, seen)
    if (found) return found
  }
  return null
}

export function getApprovalFlowBranchChildren(branch: ApprovalFlowBranch): ApprovalFlowNode[] {
  const nodes: ApprovalFlowNode[] = []
  const seen = new Set<string>()
  let current = branch.children[0]
  while (current && !seen.has(current.id)) {
    seen.add(current.id)
    nodes.push(current)
    current = current.next as ApprovalFlowNode
  }
  return nodes
}

export function insertApprovalFlowNodeAfter(
  current: ApprovalFlowNode,
  targetId: string,
  node: ApprovalFlowNode,
): boolean {
  if (current.id === targetId && current.type !== 'end') {
    node.next = current.next
    current.next = node
    return true
  }
  if (current.next && insertApprovalFlowNodeAfter(current.next, targetId, node)) return true
  for (const branch of current.branches || []) {
    const first = branch.children[0]
    if (first && insertApprovalFlowNodeAfter(first, targetId, node)) return true
  }
  return false
}

export function appendApprovalFlowBranchChild(branch: ApprovalFlowBranch, node: ApprovalFlowNode) {
  const nodes = getApprovalFlowBranchChildren(branch)
  if (nodes.length === 0) {
    branch.children = [node]
    return
  }
  nodes[nodes.length - 1].next = node
  branch.children = [nodes[0]]
}

export function removeApprovalFlowNode(root: ApprovalFlowNode, nodeId: string): boolean {
  if (root.next?.id === nodeId && root.next.type !== 'end') {
    root.next = root.next.next
    return true
  }
  if (root.next && removeApprovalFlowNode(root.next, nodeId)) return true
  for (const branch of root.branches || []) {
    const first = branch.children[0]
    if (!first) continue
    if (first.id === nodeId && first.type !== 'end') {
      branch.children = first.next ? [first.next] : []
      return true
    }
    if (removeApprovalFlowNode(first, nodeId)) {
      branch.children = branch.children[0] ? [branch.children[0]] : []
      return true
    }
  }
  return false
}

export function linkApprovalFlowChain(root: ApprovalFlowNode, nodes: ApprovalFlowNode[]) {
  root.next = nodes[0]
  nodes.forEach((node, index) => {
    node.next = nodes[index + 1]
    delete node.children
  })
}

export function moveApprovalFlowBranch(owner: ApprovalFlowNode, branchId: string, offset: number): boolean {
  const branches = owner.branches
  if (!branches) return false
  const index = branches.findIndex((branch) => branch.id === branchId)
  const targetIndex = index + offset
  if (index < 0 || targetIndex < 0 || targetIndex >= branches.length) return false
  const [branch] = branches.splice(index, 1)
  branches.splice(targetIndex, 0, branch)
  return true
}

export function validateApprovalFlowForDesigner(root: ApprovalFlowNode): Record<string, ApprovalFlowValidationError[]> {
  const errors: Record<string, ApprovalFlowValidationError[]> = {}
  const pushError = (id: string, name: string, message: string) => {
    if (!errors[id]) errors[id] = []
    errors[id].push({ id, name, message })
  }

  if (root.type !== 'start') {
    pushError(root.id || 'root', root.name || '流程', '流程必须从开始节点发起')
  }
  if (!childChain(root).length) {
    pushError(root.id, root.name || '开始节点', '开始节点没有出口连线')
  }
  if (!findEndNode(root)) {
    pushError(root.id, root.name || '流程', '流程缺少结束节点')
  }

  walkNodes(root, (node) => {
    const nodeName = node.name || node.id
    if (!node.id) pushError(root.id, root.name || '流程', '节点缺少 ID')
    if (!node.name) pushError(node.id || root.id, nodeName, `节点缺少名称: ${node.id}`)
    if (node.type === 'approval' && !node.assigneeType) {
      pushError(node.id, nodeName, `审批节点未配置审批人: ${nodeName}`)
    }
    if (node.type === 'approval') {
      validateAssignee(node).forEach((error) => pushError(node.id, nodeName, error))
    }
    if (node.type === 'exclusive') {
      const branches = node.branches || []
      if (branches.length < 2) {
        pushError(node.id, nodeName, `条件分支至少需要 2 个出口: ${nodeName}`)
      }
      if (!branches.some((branch) => branch.isDefault)) {
        pushError(node.id, nodeName, `条件分支缺少默认出口: ${nodeName}`)
      }
      branches
        .filter((branch) => !branch.isDefault)
        .forEach((branch) => {
          const branchName = branch.name || branch.id
          if (!branch.condition) {
            pushError(branch.id, branchName, `条件分支缺少条件: ${branchName}`)
            return
          }
          if (!branch.condition.fieldKey) {
            pushError(branch.id, branchName, `条件分支未选择表单字段: ${branchName}`)
          }
          if (!branch.condition.operator) {
            pushError(branch.id, branchName, `条件分支未选择操作符: ${branchName}`)
          }
        })
    }
  })

  return errors
}

export function validateApprovalFlow(root: ApprovalFlowNode): string[] {
  const errors: string[] = []
  if (root.type !== 'start') {
    errors.push('流程必须从开始节点发起')
  }
  if (!childChain(root).length) {
    errors.push('开始节点没有出口连线')
  }
  if (!findEndNode(root)) {
    errors.push('流程缺少结束节点')
  }
  walkNodes(root, (node) => {
    if (!node.id) errors.push('节点缺少 ID')
    if (!node.name) errors.push(`节点缺少名称: ${node.id}`)
    if (node.type === 'approval' && !node.assigneeType) {
      errors.push(`审批节点未配置审批人: ${node.name || node.id}`)
    }
    if (node.type === 'approval') {
      validateAssignee(node).forEach((error) => errors.push(error))
    }
    if (node.type === 'exclusive') {
      const branches = node.branches || []
      if (branches.length < 2) {
        errors.push(`条件分支至少需要 2 个出口: ${node.name || node.id}`)
      }
      if (!branches.some((branch) => branch.isDefault)) {
        errors.push(`条件分支缺少默认出口: ${node.name || node.id}`)
      }
      branches
        .filter((branch) => !branch.isDefault)
        .forEach((branch) => {
          if (!branch.condition) {
            errors.push(`条件分支缺少条件: ${branch.name || branch.id}`)
            return
          }
          if (!branch.condition.fieldKey) {
            errors.push(`条件分支未选择表单字段: ${branch.name || branch.id}`)
          }
          if (!branch.condition.operator) {
            errors.push(`条件分支未选择操作符: ${branch.name || branch.id}`)
          }
        })
    }
  })
  return errors
}

export function generateApprovalFlowArtifacts(
  root: ApprovalFlowNode,
  processId: string,
  processName: string,
  options: { validate?: boolean } = {},
): GeneratedApprovalFlow {
  const errors = validateApprovalFlow(root)
  if (options.validate !== false && errors.length > 0) {
    throw new Error(errors.join('; '))
  }

  const flows: string[] = []
  const nodes: string[] = []
  const nodeConfigs: NodeConfig[] = []
  const nodeConfigIds = new Set<string>()
  const flowIds = new Set<string>()
  const flowEdges = new Set<string>()
  const endId = findEndNode(root)?.id || 'end'

  function appendNodeConfig(node: ApprovalFlowNode) {
    if (nodeConfigIds.has(node.id)) return
    if (node.type === 'approval') {
      nodeConfigs.push(toNodeConfig(node, 'userTask'))
      nodeConfigIds.add(node.id)
    } else if (node.type === 'cc') {
      nodeConfigs.push(toNodeConfig(node, 'ccTask'))
      nodeConfigIds.add(node.id)
    } else if (node.type === 'exclusive') {
      nodeConfigs.push(toNodeConfig(node, 'exclusiveGateway'))
      nodeConfigIds.add(node.id)
    } else if (node.type === 'parallel') {
      nodeConfigs.push(toNodeConfig(node, 'parallelGateway'))
      nodeConfigIds.add(node.id)
    }
  }

  function appendFlow(baseId: string, sourceRef: string, targetRef: string, condition?: ApprovalFlowCondition) {
    if (!sourceRef || !targetRef || sourceRef === targetRef) return

    const edgeKey = `${sourceRef}->${targetRef}->${condition ? JSON.stringify(condition) : ''}`
    if (flowEdges.has(edgeKey)) return
    flowEdges.add(edgeKey)

    let flowId = baseId
    let suffix = 1
    while (flowIds.has(flowId)) {
      flowId = `${baseId}_${suffix++}`
    }
    flowIds.add(flowId)
    flows.push(sequenceFlow(flowId, sourceRef, targetRef, condition))
  }

  function appendLinearChain(
    sourceId: string,
    rawChain: ApprovalFlowNode[] | undefined,
    fallbackNextId?: string,
    firstFlow?: { id: string; condition?: ApprovalFlowCondition },
  ): string {
    const chain = normalizeChain(rawChain || [])
    if (chain.length === 0) {
      if (fallbackNextId) {
        appendFlow(`flow_${sourceId}_${fallbackNextId}`, sourceId, fallbackNextId)
      }
      return sourceId
    }

    appendNodeOnce(chain[0], nodes)
    appendNodeConfig(chain[0])
    appendFlow(firstFlow?.id || `flow_${sourceId}_${chain[0].id}`, sourceId, chain[0].id, firstFlow?.condition)

    for (let index = 0; index < chain.length; index++) {
      const current = chain[index]
      const next = chain[index + 1]
      appendNodeOnce(current, nodes)
      appendNodeConfig(current)

      if (current.type === 'exclusive' && current.branches?.length) {
        const joinTargetId = next?.id || fallbackNextId || endId
        for (const branch of current.branches) {
          const branchChain = normalizeChain(branch.children)
          const branchCondition = branch.isDefault ? undefined : branch.condition
          if (branchChain.length > 0) {
            appendLinearChain(current.id, branchChain, joinTargetId, {
              id: `flow_${current.id}_${branch.id}`,
              condition: branchCondition,
            })
          } else {
            appendFlow(`flow_${current.id}_${branch.id}`, current.id, joinTargetId, branchCondition)
          }
        }
        continue
      }

      if (next) {
        appendNodeOnce(next, nodes)
        appendFlow(`flow_${current.id}_${next.id}`, current.id, next.id)
      } else if (fallbackNextId) {
        appendFlow(`flow_${current.id}_${fallbackNextId}`, current.id, fallbackNextId)
      }
    }

    return chain[chain.length - 1].id
  }

  appendNodeOnce(root, nodes)
  appendLinearChain(root.id, childChain(root), undefined)

  const bpmnXml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             targetNamespace="http://oa.admin/approval">
  <process id="${escapeXml(processId)}" name="${escapeXml(processName)}" isExecutable="true">
    <documentation id="${FLOW_MODEL_DOCUMENTATION_ID}">${escapeXml(serializeFlowModel(root))}</documentation>
${indent([...nodes, ...flows].join('\n'), 4)}
  </process>
</definitions>`

  return {
    bpmnXml: normalizeBpmnXmlForViewer(bpmnXml),
    nodeConfigs,
  }
}

export function parseApprovalFlowModelFromBpmnXml(xml: unknown): ApprovalFlowNode | null {
  if (typeof xml !== 'string' || !xml.trim()) return null

  const match = xml.match(new RegExp(`<documentation\\s+id=["']${FLOW_MODEL_DOCUMENTATION_ID}["']>([\\s\\S]*?)<\\/documentation>`))
  if (!match?.[1]) return null

  try {
    const decoded = decodeURIComponent(unescapeXml(match[1].trim()))
    const parsed = JSON.parse(decoded) as ApprovalFlowNode
    return parsed?.id && parsed?.type ? parsed : null
  } catch {
    return null
  }
}

function appendNodeOnce(node: ApprovalFlowNode, nodes: string[]) {
  const marker = `id="${escapeXml(node.id)}"`
  if (nodes.some((item) => item.includes(marker))) return

  if (node.type === 'start') {
    nodes.push(`<startEvent id="${escapeXml(node.id)}" name="${escapeXml(node.name)}"/>`)
  } else if (node.type === 'end') {
    nodes.push(`<endEvent id="${escapeXml(node.id)}" name="${escapeXml(node.name)}"/>`)
  } else if (node.type === 'approval') {
    const expression = assigneeExpression(node)
    nodes.push(`<userTask id="${escapeXml(node.id)}" name="${escapeXml(node.name)}" flowable:assignee="${escapeXml(expression)}">
  <extensionElements>
    <flowable:taskListener event="create" delegateExpression="\${approvalTaskCreateListener}"/>
  </extensionElements>
</userTask>`)
  } else if (node.type === 'cc') {
    nodes.push(`<serviceTask id="${escapeXml(node.id)}" name="${escapeXml(node.name)}" flowable:expression="\${true}"/>`)
  } else if (node.type === 'exclusive') {
    const defaultBranch = node.branches?.find((branch) => branch.isDefault)
    const defaultFlowId = defaultBranch ? `flow_${node.id}_${defaultBranch.id}` : ''
    nodes.push(`<exclusiveGateway id="${escapeXml(node.id)}" name="${escapeXml(node.name)}"${defaultFlowId ? ` default="${escapeXml(defaultFlowId)}"` : ''}/>`)
  } else if (node.type === 'parallel') {
    nodes.push(`<parallelGateway id="${escapeXml(node.id)}" name="${escapeXml(node.name)}"/>`)
  }
}

function normalizeNode(node: ApprovalFlowNode) {
  node.branches?.forEach((branch) => {
    const branchChain = normalizeChain(branch.children)
    linkChain(branchChain)
    branch.children = branchChain[0] ? [branchChain[0]] : []
    branch.children.forEach((child) => normalizeNode(child))
  })

  if (!node.next && node.children?.length) {
    const chain = normalizeChain(node.children)
    linkChain(chain)
    node.next = chain[0]
  }

  if (node.next) {
    normalizeNode(node.next)
  }

  delete node.children
}

function childChain(node: ApprovalFlowNode): ApprovalFlowNode[] {
  if (node.next) return linkedChain(node.next)
  return normalizeChain(node.children || [])
}

function linkedChain(node: ApprovalFlowNode): ApprovalFlowNode[] {
  const chain: ApprovalFlowNode[] = []
  const seen = new Set<string>()
  let current: ApprovalFlowNode | undefined = node
  while (current && !seen.has(current.id)) {
    seen.add(current.id)
    chain.push(current)
    if (current.next) {
      current = current.next
    } else if (current.children?.length === 1) {
      current = current.children[0]
    } else {
      current = undefined
    }
  }
  return chain
}

function normalizeChain(chain: ApprovalFlowNode[]): ApprovalFlowNode[] {
  const normalized: ApprovalFlowNode[] = []
  for (const node of chain) {
    normalized.push(...linkedChain(node))
  }
  return normalized
}

function linkChain(chain: ApprovalFlowNode[]) {
  for (let index = 0; index < chain.length; index++) {
    chain[index].next = chain[index + 1]
    delete chain[index].children
  }
}

function sequenceFlow(id: string, sourceRef: string, targetRef: string, condition?: ApprovalFlowCondition) {
  if (!condition) {
    return `<sequenceFlow id="${escapeXml(id)}" sourceRef="${escapeXml(sourceRef)}" targetRef="${escapeXml(targetRef)}"/>`
  }
  return `<sequenceFlow id="${escapeXml(id)}" sourceRef="${escapeXml(sourceRef)}" targetRef="${escapeXml(targetRef)}">
  <conditionExpression xsi:type="tFormalExpression"><![CDATA[${conditionExpression(condition)}]]></conditionExpression>
</sequenceFlow>`
}

function conditionExpression(condition: ApprovalFlowCondition) {
  if (condition.operator === 'contains') {
    return `\${${condition.fieldKey} != null && ${condition.fieldKey}.contains('${condition.value}')}`
  }
  const value = typeof condition.value === 'number' || typeof condition.value === 'boolean'
    ? String(condition.value)
    : `'${String(condition.value).replaceAll("'", "\\'")}'`
  return `\${${condition.fieldKey} ${condition.operator} ${value}}`
}

function assigneeExpression(node: ApprovalFlowNode) {
  const option = ASSIGNEE_TYPE_OPTIONS.find((item) => item.value === node.assigneeType)
  return option?.uelTemplate(node.assigneeConfig || {}) || ''
}

function validateAssignee(node: ApprovalFlowNode) {
  const errors: string[] = []
  const config = node.assigneeConfig || {}
  const nodeName = node.name || node.id
  if (node.assigneeType === 'fixed' && !config.userId && !(Array.isArray(config.userIds) && config.userIds.length > 0)) {
    errors.push(`审批节点未配置指定成员: ${nodeName}`)
  }
  if (node.assigneeType === 'role' && !config.roleId) {
    errors.push(`审批节点未配置角色: ${nodeName}`)
  }
  if (node.assigneeType === 'expression') {
    const expression = String(config.expression ?? '').trim()
    if (!expression || !expression.startsWith('${') || !expression.endsWith('}')) {
      errors.push(`审批节点表达式格式不合法: ${nodeName}`)
    }
  }
  return errors
}

function toNodeConfig(node: ApprovalFlowNode, nodeType: string): NodeConfig {
  return {
    nodeId: node.id,
    nodeName: node.name,
    nodeType,
    assigneeType: node.assigneeType,
    assigneeConfig: node.assigneeConfig ? JSON.stringify(node.assigneeConfig) : undefined,
    multiInstanceType: 'none',
    sortOrder: 0,
  }
}

function walkNodes(node: ApprovalFlowNode, visitor: (node: ApprovalFlowNode) => void, seen = new Set<string>()) {
  if (seen.has(node.id)) return
  seen.add(node.id)
  visitor(node)
  if (node.next) walkNodes(node.next, visitor, seen)
  node.children?.forEach((child) => walkNodes(child, visitor, seen))
  node.branches?.forEach((branch) => branch.children.forEach((child) => walkNodes(child, visitor, seen)))
}

function findEndNode(root: ApprovalFlowNode): ApprovalFlowNode | null {
  let found: ApprovalFlowNode | null = null
  walkNodes(root, (node) => {
    if (node.type === 'end') found = node
  })
  return found
}

function escapeXml(value: string) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('"', '&quot;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
}

function unescapeXml(value: string) {
  return value
    .replaceAll('&quot;', '"')
    .replaceAll('&lt;', '<')
    .replaceAll('&gt;', '>')
    .replaceAll('&amp;', '&')
}

function serializeFlowModel(root: ApprovalFlowNode) {
  return encodeURIComponent(JSON.stringify(root))
}

function newId(prefix: string) {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
}

function indent(value: string, spaces: number) {
  const prefix = ' '.repeat(spaces)
  return value.split('\n').map((line) => `${prefix}${line}`).join('\n')
}
