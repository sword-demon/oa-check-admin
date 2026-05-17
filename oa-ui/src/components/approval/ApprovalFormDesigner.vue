<template>
  <div class="approval-form-designer">
    <aside class="approval-form-designer__palette">
      <button
        v-for="item in APPROVAL_FORM_FIELD_REGISTRY"
        :key="item.type"
        type="button"
        class="approval-form-designer__palette-item"
        @click="addField(item.type)"
      >
        <span>{{ item.label }}</span>
        <small>{{ item.description }}</small>
      </button>
    </aside>

    <section class="approval-form-designer__canvas">
      <div class="approval-form-designer__canvas-header">
        <strong>表单字段</strong>
        <span>{{ fields.length }} 个字段</span>
      </div>

      <div v-if="fields.length" class="approval-form-designer__field-list">
        <button
          v-for="(field, index) in fields"
          :key="field.fieldKey"
          type="button"
          class="approval-form-designer__field"
          :class="{ 'is-active': selectedField?.fieldKey === field.fieldKey }"
          @click="selectField(field.fieldKey)"
        >
          <span class="approval-form-designer__field-main">
            <strong>{{ field.label || field.fieldKey }}</strong>
            <small>{{ field.fieldKey }} · {{ field.type }}</small>
          </span>
          <span class="approval-form-designer__field-actions" @click.stop>
            <el-button link size="small" :disabled="index === 0" @click="moveField(index, -1)">上移</el-button>
            <el-button link size="small" :disabled="index === fields.length - 1" @click="moveField(index, 1)">下移</el-button>
            <el-button link size="small" @click="duplicateField(index)">复制</el-button>
            <el-button link size="small" type="danger" @click="removeField(index)">删除</el-button>
          </span>
        </button>
      </div>
      <el-empty v-else description="从左侧添加字段" :image-size="72" />
    </section>

    <aside class="approval-form-designer__properties">
      <template v-if="selectedField">
        <div class="approval-form-designer__properties-header">
          <strong>字段属性</strong>
          <span>{{ selectedField.type }}</span>
        </div>
        <el-form label-position="top">
          <el-form-item label="字段标题">
            <el-input v-model="selectedField.label" @input="emitChange" />
          </el-form-item>
          <el-form-item label="字段标识">
            <el-input v-model="selectedField.fieldKey" @input="syncSelectedKey" />
          </el-form-item>
          <el-form-item label="占位提示">
            <el-input v-model="selectedField.placeholder" clearable @input="emitChange" />
          </el-form-item>
          <el-form-item label="必填">
            <el-switch v-model="selectedField.required" @change="emitChange" />
          </el-form-item>

          <template v-if="selectedField.type === 'number'">
            <el-form-item label="最小值">
              <el-input-number v-model="selectedField.min" :controls="false" style="width: 100%" @change="emitChange" />
            </el-form-item>
            <el-form-item label="最大值">
              <el-input-number v-model="selectedField.max" :controls="false" style="width: 100%" @change="emitChange" />
            </el-form-item>
          </template>

          <template v-if="isOptionField(selectedField.type)">
            <el-form-item label="选项">
              <div class="approval-form-designer__options">
                <div
                  v-for="(option, index) in selectedField.options"
                  :key="index"
                  class="approval-form-designer__option"
                >
                  <el-input v-model="option.label" placeholder="显示名" @input="emitChange" />
                  <el-input
                    :model-value="String(option.value ?? '')"
                    placeholder="值"
                    @update:model-value="updateOptionValue(index, $event)"
                  />
                  <el-button link type="danger" @click="removeOption(index)">删除</el-button>
                </div>
                <el-button @click="addOption">添加选项</el-button>
              </div>
            </el-form-item>
          </template>

          <template v-if="selectedField.type === 'attachment'">
            <el-form-item label="最大上传数量">
              <el-input-number v-model="selectedField.maxFiles" :min="1" :controls="false" style="width: 100%" @change="emitChange" />
            </el-form-item>
          </template>
        </el-form>
      </template>
      <el-empty v-else description="选择字段后编辑属性" :image-size="72" />
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  APPROVAL_FORM_FIELD_REGISTRY,
  createApprovalFormField,
  parseApprovalFormConfig,
  serializeApprovalFormSchema,
  type ApprovalFormField,
  type ApprovalFormFieldType,
} from '@/utils/approval-form'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const fields = ref<ApprovalFormField[]>([])
const selectedFieldKey = ref('')

const selectedField = computed(() =>
  fields.value.find((field) => field.fieldKey === selectedFieldKey.value) || null,
)

watch(
  () => props.modelValue,
  (value) => {
    fields.value = parseApprovalFormConfig(value)
    if (!fields.value.some((field) => field.fieldKey === selectedFieldKey.value)) {
      selectedFieldKey.value = fields.value[0]?.fieldKey || ''
    }
  },
  { immediate: true },
)

function emitChange() {
  fields.value = fields.value.map((field, index) => ({ ...field, name: field.fieldKey, sortOrder: index }))
  emit('update:modelValue', serializeApprovalFormSchema(fields.value))
}

function addField(type: ApprovalFormFieldType) {
  const field = createApprovalFormField(type, fields.value.length)
  fields.value.push(field)
  selectedFieldKey.value = field.fieldKey
  emitChange()
}

function selectField(fieldKey: string) {
  selectedFieldKey.value = fieldKey
}

function moveField(index: number, offset: number) {
  const target = index + offset
  if (target < 0 || target >= fields.value.length) return
  const next = [...fields.value]
  const [item] = next.splice(index, 1)
  next.splice(target, 0, item)
  fields.value = next
  emitChange()
}

function duplicateField(index: number) {
  const source = fields.value[index]
  if (!source) return
  const copiedKey = `${source.fieldKey}_copy_${Date.now()}`
  const field = {
    ...source,
    fieldKey: copiedKey,
    name: copiedKey,
    label: `${source.label} 副本`,
    options: source.options.map((option) => ({ ...option })),
  }
  fields.value.splice(index + 1, 0, field)
  selectedFieldKey.value = field.fieldKey
  emitChange()
}

function removeField(index: number) {
  fields.value.splice(index, 1)
  selectedFieldKey.value = fields.value[Math.min(index, fields.value.length - 1)]?.fieldKey || ''
  emitChange()
}

function syncSelectedKey() {
  if (selectedField.value) {
    selectedField.value.name = selectedField.value.fieldKey
    selectedFieldKey.value = selectedField.value.fieldKey
  }
  emitChange()
}

function isOptionField(type: ApprovalFormFieldType) {
  return ['select', 'radio', 'checkbox'].includes(type)
}

function addOption() {
  if (!selectedField.value) return
  const index = selectedField.value.options.length + 1
  selectedField.value.options.push({ label: `选项${index}`, value: `option_${index}` })
  emitChange()
}

function removeOption(index: number) {
  if (!selectedField.value) return
  selectedField.value.options.splice(index, 1)
  emitChange()
}

function updateOptionValue(index: number, value: string) {
  if (!selectedField.value?.options[index]) return
  selectedField.value.options[index].value = value
  emitChange()
}
</script>

<style scoped lang="scss">
.approval-form-designer {
  display: grid;
  grid-template-columns: 180px minmax(260px, 1fr) 260px;
  min-height: 460px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  overflow: hidden;
  background: var(--el-bg-color);

  &__palette,
  &__properties {
    padding: 14px;
    background: var(--el-fill-color-lighter);
  }

  &__palette {
    display: flex;
    flex-direction: column;
    gap: 8px;
    border-right: 1px solid var(--el-border-color-light);
  }

  &__palette-item,
  &__field {
    width: 100%;
    border: 1px solid var(--el-border-color-light);
    border-radius: 6px;
    background: var(--el-bg-color);
    text-align: left;
    cursor: pointer;
  }

  &__palette-item {
    padding: 10px;

    span,
    small {
      display: block;
    }

    small {
      margin-top: 3px;
      color: var(--el-text-color-secondary);
    }
  }

  &__canvas {
    padding: 14px;
  }

  &__canvas-header,
  &__properties-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    color: var(--el-text-color-regular);
  }

  &__field-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__field {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px;

    &.is-active {
      border-color: var(--el-color-primary);
      box-shadow: 0 0 0 1px var(--el-color-primary-light-7);
    }
  }

  &__field-main {
    strong,
    small {
      display: block;
    }

    small {
      margin-top: 4px;
      color: var(--el-text-color-secondary);
    }
  }

  &__field-actions {
    white-space: nowrap;
  }

  &__properties {
    border-left: 1px solid var(--el-border-color-light);
  }

  &__options {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }

  &__option {
    display: grid;
    grid-template-columns: 1fr 1fr auto;
    gap: 6px;
    align-items: center;
  }
}
</style>
