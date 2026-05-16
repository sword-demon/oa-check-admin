export interface NodeTypeOption {
  value: string
  label: string
  bpmnType: string
  icon: string
}

export interface AssigneeTypeOption {
  value: string
  label: string
  description: string
  uelTemplate: (config: Record<string, unknown>) => string
}

export interface MultiInstanceTypeOption {
  value: string
  label: string
  completionCondition: string
}

export const NODE_TYPE_OPTIONS: NodeTypeOption[] = [
  { value: 'startEvent', label: '开始事件', bpmnType: 'bpmn:StartEvent', icon: 'VideoPlay' },
  { value: 'endEvent', label: '结束事件', bpmnType: 'bpmn:EndEvent', icon: 'CircleClose' },
  { value: 'userTask', label: '用户任务', bpmnType: 'bpmn:UserTask', icon: 'User' },
  { value: 'exclusiveGateway', label: '排他网关', bpmnType: 'bpmn:ExclusiveGateway', icon: 'Switch' },
  { value: 'parallelGateway', label: '并行网关', bpmnType: 'bpmn:ParallelGateway', icon: 'Operation' },
]

export const ASSIGNEE_TYPE_OPTIONS: AssigneeTypeOption[] = [
  {
    value: 'fixed',
    label: '指定用户',
    description: '指定固定用户 ID 作为审批人',
    uelTemplate: (config) => String(config.userId ?? ''),
  },
  {
    value: 'deptLeader',
    label: '部门主管',
    description: '自动解析为发起人的直属部门主管',
    uelTemplate: () => '${assigneeResolver.resolveDeptLeader(initiator)}',
  },
  {
    value: 'upwardDeptLeader',
    label: '上级部门主管',
    description: '解析为发起人上级部门的主管',
    uelTemplate: (config) => `\${assigneeResolver.resolveUpwardDeptLeader(initiator, ${config.level ?? 1})}`,
  },
  {
    value: 'role',
    label: '角色',
    description: '指定角色, 所有拥有该角色的用户参与审批',
    uelTemplate: (config) => `\${candidateUserResolver.resolveRoleUsers(${config.roleId ?? 0})}`,
  },
  {
    value: 'initiator',
    label: '发起人',
    description: '审批人为流程发起人本人',
    uelTemplate: () => '${initiator}',
  },
  {
    value: 'expression',
    label: '自定义表达式',
    description: '输入自定义 UEL 表达式',
    uelTemplate: (config) => String(config.expression ?? ''),
  },
]

export const MULTI_INSTANCE_TYPE_OPTIONS: MultiInstanceTypeOption[] = [
  { value: 'none', label: '普通', completionCondition: '' },
  { value: 'countersign', label: '会签', completionCondition: '${nrOfCompletedInstances == nrOfInstances}' },
  { value: 'orSign', label: '或签', completionCondition: '${nrOfCompletedInstances == 1}' },
]

export const NODE_TYPE_LABEL_MAP: Record<string, string> = Object.fromEntries(
  NODE_TYPE_OPTIONS.map((o) => [o.value, o.label]),
)

export const ASSIGNEE_TYPE_LABEL_MAP: Record<string, string> = Object.fromEntries(
  ASSIGNEE_TYPE_OPTIONS.map((o) => [o.value, o.label]),
)

export const MULTI_INSTANCE_TYPE_LABEL_MAP: Record<string, string> = Object.fromEntries(
  MULTI_INSTANCE_TYPE_OPTIONS.map((o) => [o.value, o.label]),
)

export const TEMPLATE_STATUS = {
  DRAFT: 1,
  PUBLISHED: 2,
} as const

export const TEMPLATE_STATUS_LABEL_MAP: Record<number, string> = {
  [TEMPLATE_STATUS.DRAFT]: '草稿',
  [TEMPLATE_STATUS.PUBLISHED]: '已发布',
}
