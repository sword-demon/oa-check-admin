import { describe, expect, it } from 'vitest'
import {
  createApprovalFormField,
  formatApprovalFormValue,
  parseApprovalFormConfig,
  parseApprovalFormData,
  parseApprovalFormSchema,
  serializeApprovalFormSchema,
} from './approval-form'

describe('approval form utils', () => {
  it('parses form config fields and options', () => {
    const fields = parseApprovalFormConfig(
      '{"fields":[{"name":"leaveType","label":"请假类型","type":"select","options":["年假","事假"]},{"key":"reason","type":"textarea"}]}',
    )

    expect(fields).toEqual([
      {
        fieldKey: 'leaveType',
        name: 'leaveType',
        label: '请假类型',
        type: 'select',
        placeholder: undefined,
        required: false,
        options: [
          { label: '年假', value: '年假' },
          { label: '事假', value: '事假' },
        ],
        defaultValue: undefined,
        min: undefined,
        max: undefined,
        maxFiles: undefined,
        sortOrder: 0,
      },
      {
        fieldKey: 'reason',
        name: 'reason',
        label: 'reason',
        type: 'textarea',
        placeholder: undefined,
        required: false,
        options: [],
        defaultValue: undefined,
        min: undefined,
        max: undefined,
        maxFiles: undefined,
        sortOrder: 1,
      },
    ])
  })

  it('parses current schema fieldKey and keeps version', () => {
    const schema = parseApprovalFormSchema(
      '{"version":1,"fields":[{"fieldKey":"amount","label":"金额","type":"number","min":1,"max":100,"sortOrder":2}]}',
    )

    expect(schema.version).toBe(1)
    expect(schema.fields[0]).toMatchObject({
      fieldKey: 'amount',
      name: 'amount',
      label: '金额',
      type: 'number',
      min: 1,
      max: 100,
      sortOrder: 2,
    })
  })

  it('creates and serializes schema fields', () => {
    const field = createApprovalFormField('select', 0)
    field.fieldKey = 'leave_type'
    field.name = 'leave_type'
    field.label = '请假类型'
    const serialized = serializeApprovalFormSchema([field])

    expect(JSON.parse(serialized)).toMatchObject({
      version: 1,
      fields: [{ fieldKey: 'leave_type', label: '请假类型', type: 'select' }],
    })
  })

  it('parses form data json', () => {
    expect(parseApprovalFormData('{"leaveRequestId":2,"reason":"请假"}')).toEqual({
      leaveRequestId: 2,
      reason: '请假',
    })
  })

  it('formats nested values without raw json braces', () => {
    expect(formatApprovalFormValue({ leaveRequestId: 2, reason: '请假' })).toBe(
      'leaveRequestId: 2；reason: 请假',
    )
  })
})
