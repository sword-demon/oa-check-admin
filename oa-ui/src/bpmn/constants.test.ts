import { describe, it, expect } from 'vitest'
import {
  NODE_TYPE_OPTIONS,
  ASSIGNEE_TYPE_OPTIONS,
  MULTI_INSTANCE_TYPE_OPTIONS,
  NODE_TYPE_LABEL_MAP,
  ASSIGNEE_TYPE_LABEL_MAP,
  MULTI_INSTANCE_TYPE_LABEL_MAP,
  TEMPLATE_STATUS,
  TEMPLATE_STATUS_LABEL_MAP,
} from './constants'

describe('constants', () => {
  it('NODE_TYPE_OPTIONS has 5 entries with required fields', () => {
    expect(NODE_TYPE_OPTIONS).toHaveLength(5)
    for (const opt of NODE_TYPE_OPTIONS) {
      expect(opt).toHaveProperty('value')
      expect(opt).toHaveProperty('label')
      expect(opt).toHaveProperty('bpmnType')
      expect(opt).toHaveProperty('icon')
    }
  })

  it('ASSIGNEE_TYPE_OPTIONS has 6 entries with uelTemplate function', () => {
    expect(ASSIGNEE_TYPE_OPTIONS).toHaveLength(6)
    for (const opt of ASSIGNEE_TYPE_OPTIONS) {
      expect(typeof opt.uelTemplate).toBe('function')
    }
  })

  it('ASSIGNEE_TYPE_OPTIONS uelTemplate generates expected output', () => {
    const fixed = ASSIGNEE_TYPE_OPTIONS.find((o) => o.value === 'fixed')!
    expect(fixed.uelTemplate({ userId: 42 })).toBe('42')

    const deptLeader = ASSIGNEE_TYPE_OPTIONS.find((o) => o.value === 'deptLeader')!
    expect(deptLeader.uelTemplate({})).toBe('${assigneeResolver.resolveDeptLeader(initiator)}')

    const initiator = ASSIGNEE_TYPE_OPTIONS.find((o) => o.value === 'initiator')!
    expect(initiator.uelTemplate({})).toBe('${initiator}')

    const expression = ASSIGNEE_TYPE_OPTIONS.find((o) => o.value === 'expression')!
    expect(expression.uelTemplate({ expression: '${custom}' })).toBe('${custom}')

    const upward = ASSIGNEE_TYPE_OPTIONS.find((o) => o.value === 'upwardDeptLeader')!
    expect(upward.uelTemplate({ level: 2 })).toContain('resolveUpwardDeptLeader(initiator, 2)')

    const role = ASSIGNEE_TYPE_OPTIONS.find((o) => o.value === 'role')!
    expect(role.uelTemplate({ roleId: 5 })).toContain('resolveRoleUsers(5)')
  })

  it('MULTI_INSTANCE_TYPE_OPTIONS has 3 entries', () => {
    expect(MULTI_INSTANCE_TYPE_OPTIONS).toHaveLength(3)

    const none = MULTI_INSTANCE_TYPE_OPTIONS.find((o) => o.value === 'none')!
    expect(none.completionCondition).toBe('')

    const countersign = MULTI_INSTANCE_TYPE_OPTIONS.find((o) => o.value === 'countersign')!
    expect(countersign.completionCondition).toContain('nrOfCompletedInstances == nrOfInstances')

    const orSign = MULTI_INSTANCE_TYPE_OPTIONS.find((o) => o.value === 'orSign')!
    expect(orSign.completionCondition).toContain('nrOfCompletedInstances == 1')
  })

  it('label maps are consistent with options', () => {
    expect(Object.keys(NODE_TYPE_LABEL_MAP)).toHaveLength(NODE_TYPE_OPTIONS.length)
    expect(Object.keys(ASSIGNEE_TYPE_LABEL_MAP)).toHaveLength(ASSIGNEE_TYPE_OPTIONS.length)
    expect(Object.keys(MULTI_INSTANCE_TYPE_LABEL_MAP)).toHaveLength(MULTI_INSTANCE_TYPE_OPTIONS.length)
  })

  it('TEMPLATE_STATUS has DRAFT=1 and PUBLISHED=2', () => {
    expect(TEMPLATE_STATUS.DRAFT).toBe(1)
    expect(TEMPLATE_STATUS.PUBLISHED).toBe(2)
  })

  it('TEMPLATE_STATUS_LABEL_MAP maps correctly', () => {
    expect(TEMPLATE_STATUS_LABEL_MAP[1]).toBe('草稿')
    expect(TEMPLATE_STATUS_LABEL_MAP[2]).toBe('已发布')
  })
})
