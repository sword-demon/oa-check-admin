export interface ApprovalFormOption {
  label: string
  value: string | number | boolean
}

export type ApprovalFormFieldType =
  | 'text'
  | 'number'
  | 'date'
  | 'datetime'
  | 'textarea'
  | 'select'
  | 'radio'
  | 'checkbox'
  | 'attachment'

export interface ApprovalFormField {
  fieldKey: string
  name: string
  label: string
  type: ApprovalFormFieldType
  placeholder?: string
  required: boolean
  options: ApprovalFormOption[]
  defaultValue?: unknown
  min?: number
  max?: number
  maxFiles?: number
  sortOrder?: number
}

export interface ApprovalFormSchema {
  version: number
  fields: ApprovalFormField[]
}

export interface ApprovalFormFieldRegistryItem {
  type: ApprovalFormFieldType
  label: string
  description: string
  defaultField: Partial<ApprovalFormField>
}

export const APPROVAL_FORM_SCHEMA_VERSION = 1

export const APPROVAL_FORM_FIELD_REGISTRY: ApprovalFormFieldRegistryItem[] = [
  { type: 'text', label: '单行文本', description: '姓名、标题、简短说明', defaultField: {} },
  { type: 'textarea', label: '多行文本', description: '原因、备注、说明', defaultField: {} },
  { type: 'number', label: '数字', description: '金额、天数、数量', defaultField: { min: undefined, max: undefined } },
  { type: 'date', label: '日期', description: '开始日期、截止日期', defaultField: {} },
  { type: 'datetime', label: '日期时间', description: '精确到时间点', defaultField: {} },
  {
    type: 'select',
    label: '下拉选择',
    description: '单选且选项较多',
    defaultField: { options: [{ label: '选项一', value: 'option_1' }] },
  },
  {
    type: 'radio',
    label: '单选',
    description: '少量互斥选项',
    defaultField: { options: [{ label: '选项一', value: 'option_1' }] },
  },
  {
    type: 'checkbox',
    label: '多选',
    description: '可选择多个结果',
    defaultField: { options: [{ label: '选项一', value: 'option_1' }] },
  },
  { type: 'attachment', label: '附件', description: '提交文件 URL 列表', defaultField: { maxFiles: 1 } },
]

function toText(value: unknown): string {
  return String(value ?? '').trim()
}

function parseOptions(options: unknown): ApprovalFormOption[] {
  if (!Array.isArray(options)) return []

  return options
    .map((option) => {
      if (typeof option === 'string' || typeof option === 'number' || typeof option === 'boolean') {
        return { label: String(option), value: option }
      }
      if (!option || typeof option !== 'object') return null

      const raw = option as Record<string, unknown>
      const value = raw.value ?? raw.id ?? raw.key ?? raw.label ?? raw.name ?? ''
      const label = raw.label ?? raw.name ?? raw.text ?? value
      if (value === '') return null
      if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
        return { label: String(label), value }
      }
      return { label: String(label), value: String(value) }
    })
    .filter((option): option is ApprovalFormOption => Boolean(option))
}

function normalizeFieldType(type: unknown): ApprovalFormFieldType {
  const raw = toText(type).toLowerCase()
  const map: Record<string, ApprovalFormFieldType> = {
    string: 'text',
    varchar: 'text',
    text: 'text',
    input: 'text',
    textarea: 'textarea',
    integer: 'number',
    int: 'number',
    long: 'number',
    decimal: 'number',
    bigdecimal: 'number',
    number: 'number',
    localdate: 'date',
    date: 'date',
    localdatetime: 'datetime',
    datetime: 'datetime',
    select: 'select',
    radio: 'radio',
    checkbox: 'checkbox',
    'checkbox-group': 'checkbox',
    attachment: 'attachment',
    file: 'attachment',
  }
  return map[raw] || 'text'
}

function toNumber(value: unknown): number | undefined {
  if (value === null || value === undefined || value === '') return undefined
  const num = Number(value)
  return Number.isNaN(num) ? undefined : num
}

function normalizeField(field: unknown, index: number): ApprovalFormField | null {
  if (!field || typeof field !== 'object') return null
  const raw = field as Record<string, unknown>
  const fieldKey = toText(raw.fieldKey ?? raw.name ?? raw.key)
  if (!fieldKey) return null

  return {
    fieldKey,
    name: fieldKey,
    label: toText(raw.label ?? raw.name ?? raw.key) || fieldKey,
    type: normalizeFieldType(raw.type),
    placeholder: toText(raw.placeholder) || undefined,
    required: Boolean(raw.required),
    options: parseOptions(raw.options),
    defaultValue: raw.defaultValue,
    min: toNumber(raw.min),
    max: toNumber(raw.max),
    maxFiles: toNumber(raw.maxFiles),
    sortOrder: toNumber(raw.sortOrder) ?? index,
  }
}

export function parseApprovalFormSchema(config: unknown): ApprovalFormSchema {
  if (!config) return { version: APPROVAL_FORM_SCHEMA_VERSION, fields: [] }

  try {
    const parsed = typeof config === 'string' ? JSON.parse(config) : config
    const fields = Array.isArray((parsed as { fields?: unknown }).fields)
      ? (parsed as { fields: unknown[] }).fields
      : []

    const normalizedFields = fields
      .map(normalizeField)
      .filter((field): field is ApprovalFormField => Boolean(field))
      .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
    return {
      version: toNumber((parsed as { version?: unknown }).version) ?? APPROVAL_FORM_SCHEMA_VERSION,
      fields: normalizedFields,
    }
  } catch {
    return { version: APPROVAL_FORM_SCHEMA_VERSION, fields: [] }
  }
}

export function parseApprovalFormConfig(config: unknown): ApprovalFormField[] {
  return parseApprovalFormSchema(config).fields
}

export function serializeApprovalFormSchema(fields: ApprovalFormField[]): string {
  return JSON.stringify({
    version: APPROVAL_FORM_SCHEMA_VERSION,
    fields: fields.map((field, index) => ({
      fieldKey: field.fieldKey,
      label: field.label,
      type: field.type,
      placeholder: field.placeholder,
      required: field.required,
      defaultValue: field.defaultValue,
      options: field.options,
      min: field.min,
      max: field.max,
      maxFiles: field.maxFiles,
      sortOrder: index,
    })),
  })
}

export function createApprovalFormField(type: ApprovalFormFieldType, index: number): ApprovalFormField {
  const registry = APPROVAL_FORM_FIELD_REGISTRY.find((item) => item.type === type)
  const fieldKey = `${type}_${Date.now()}_${index}`
  return {
    fieldKey,
    name: fieldKey,
    label: registry?.label || '字段',
    type,
    required: false,
    options: [],
    sortOrder: index,
    ...registry?.defaultField,
  }
}

export function parseApprovalFormData(formData: unknown): Record<string, unknown> {
  if (!formData) return {}

  try {
    const parsed = typeof formData === 'string' ? JSON.parse(formData) : formData
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed)
      ? (parsed as Record<string, unknown>)
      : {}
  } catch {
    return {}
  }
}

export function formatApprovalFormValue(value: unknown): string {
  if (value === null || value === undefined || value === '') return '-'
  if (Array.isArray(value)) return value.map(formatApprovalFormValue).join('、')
  if (value instanceof Date) return value.toISOString().replace('T', ' ').replace(/\.\d{3}Z$/, '')
  if (typeof value === 'object') {
    return Object.entries(value)
      .map(([key, item]) => `${key}: ${formatApprovalFormValue(item)}`)
      .join('；')
  }
  return String(value)
}
