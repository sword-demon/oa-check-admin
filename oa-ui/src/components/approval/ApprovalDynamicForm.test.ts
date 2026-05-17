import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ApprovalDynamicForm from './ApprovalDynamicForm.vue'

const elementStubs = {
  ElForm: { template: '<form><slot /></form>' },
  ElRow: { template: '<div><slot /></div>' },
  ElCol: { template: '<div><slot /></div>' },
  ElFormItem: { props: ['label'], template: '<label><span>{{ label }}</span><slot /></label>' },
  ElInput: {
    props: ['modelValue'],
    emits: ['update:modelValue'],
    template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
  },
  ElInputNumber: {
    props: ['modelValue'],
    emits: ['update:modelValue'],
    template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', Number($event.target.value))" />',
  },
  ElDatePicker: true,
  ElSelect: true,
  ElOption: true,
  ElRadioGroup: true,
  ElRadio: true,
  ElCheckboxGroup: true,
  ElCheckbox: true,
  ElEmpty: true,
  ElDescriptions: true,
  ElDescriptionsItem: true,
}

describe('ApprovalDynamicForm', () => {
  it('renders runtime fields from schema', () => {
    const wrapper = mount(ApprovalDynamicForm, {
      props: {
        schema: '{"fields":[{"fieldKey":"reason","label":"原因","type":"textarea"}]}',
        modelValue: { reason: '请假' },
      },
      global: { stubs: elementStubs },
    })

    expect(wrapper.text()).toContain('原因')
    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('请假')
  })

  it('emits updated model value when field changes', async () => {
    const wrapper = mount(ApprovalDynamicForm, {
      props: {
        schema: '{"fields":[{"fieldKey":"title","label":"标题","type":"text"}]}',
        modelValue: { title: '旧标题' },
      },
      global: { stubs: elementStubs },
    })

    await wrapper.find('input').setValue('新标题')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([{ title: '新标题' }])
  })
})
