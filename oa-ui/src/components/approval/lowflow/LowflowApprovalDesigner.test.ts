import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import LowflowApprovalDesigner from './LowflowApprovalDesigner.vue'
import type { ApprovalFlowNode } from '@/utils/approval-flow'

vi.mock('@/api/user', () => ({
  getUserList: vi.fn().mockResolvedValue({ list: [] }),
}))

vi.mock('@/api/system', () => ({
  getRoleList: vi.fn().mockResolvedValue({ list: [] }),
}))

const elementStubs = {
  ElButtonGroup: { template: '<div><slot /></div>' },
  ElButton: { template: '<button type="button" @click="$emit(\'click\', $event)"><slot /></button>' },
  ElPopover: { template: '<div><slot name="reference" /><slot /></div>' },
  ElPopconfirm: { template: '<div><slot name="reference" /></div>' },
  ElTooltip: { template: '<span><slot /><slot name="content" /></span>' },
  ElIcon: { template: '<i><slot /></i>' },
  ElDrawer: { props: ['modelValue'], template: '<aside v-if="modelValue"><slot name="header" /><slot /></aside>' },
  ElInput: {
    props: ['modelValue'],
    emits: ['update:modelValue', 'change'],
    template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" @change="$emit(\'change\')" />',
  },
  ElForm: { template: '<form><slot /></form>' },
  ElFormItem: { props: ['label'], template: '<label><span>{{ label }}</span><slot /></label>' },
  ElSelect: true,
  ElOption: true,
  ElInputNumber: true,
  ElSwitch: true,
  ElAlert: true,
  ElDatePicker: true,
  ElEmpty: true,
}

describe('LowflowApprovalDesigner', () => {
  it('renders the main next chain with lowflow add entries', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      next: {
        id: 'approval1',
        type: 'approval',
        name: '主管审批',
        assigneeType: 'deptLeader',
        assigneeConfig: {},
        next: { id: 'end', type: 'end', name: '流程结束' },
      },
    }

    const wrapper = mount(LowflowApprovalDesigner, {
      props: { modelValue: flow },
      global: { stubs: elementStubs },
    })

    expect(wrapper.text()).toContain('发起申请')
    expect(wrapper.text()).toContain('主管审批')
    expect(wrapper.text()).toContain('流程结束')
    expect(wrapper.text()).toContain('审批人')
    expect(wrapper.text()).toContain('条件分支')
  })

  it('renders exclusive branch lanes and validation warnings', () => {
    const flow: ApprovalFlowNode = {
      id: 'start',
      type: 'start',
      name: '发起申请',
      next: {
        id: 'gateway',
        type: 'exclusive',
        name: '金额判断',
        branches: [
          { id: 'large', name: '大额', condition: { fieldKey: '', operator: '==', value: '' }, children: [] },
          { id: 'default', name: '默认', isDefault: true, children: [] },
        ],
        next: { id: 'end', type: 'end', name: '流程结束' },
      },
    }

    const wrapper = mount(LowflowApprovalDesigner, {
      props: { modelValue: flow },
      global: { stubs: elementStubs },
    })

    expect(wrapper.text()).toContain('金额判断')
    expect(wrapper.text()).toContain('大额')
    expect(wrapper.text()).toContain('默认出口')
    expect(wrapper.text()).toContain('条件分支未选择表单字段')
  })
})
