<template>
  <div class="page-shell">
    <section class="page-header">
      <div class="page-header__titles">
        <p class="page-subtitle page-subtitle--eyebrow">Process Assets</p>
        <h1 class="page-title">审批模板</h1>
        <p class="page-subtitle">维护流程模板版本、状态和设计入口，支撑审批流程的持续演进。</p>
      </div>
    </section>

    <el-card class="page-panel">
      <div class="toolbar">
        <div class="search">
          <el-input
            v-model="search.templateName"
            placeholder="模板名称"
            clearable
            style="width: 200px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="search.status"
            placeholder="状态"
            clearable
            style="width: 120px; margin-left: 10px"
            @change="handleSearch"
          >
            <el-option label="草稿" :value="TEMPLATE_STATUS.DRAFT" />
            <el-option label="已发布" :value="TEMPLATE_STATUS.PUBLISHED" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
        </div>
        <el-button type="primary" @click="openWizard()">创建模板</el-button>
      </div>
      <el-table :data="templates" stripe v-loading="loading" class="page-table">
        <el-table-column prop="templateName" label="模板名称" min-width="140" />
        <el-table-column prop="templateKey" label="模板标识" min-width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === TEMPLATE_STATUS.PUBLISHED ? 'success' : 'info'">
              {{ TEMPLATE_STATUS_LABEL_MAP[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="380" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openFlowPreview(row)">
              查看
            </el-button>
            <el-button link type="primary" @click="openDesigner(row)">
              {{ row.status === TEMPLATE_STATUS.PUBLISHED ? '查看流程' : '高级设计' }}
            </el-button>
            <el-button
              v-if="row.status !== TEMPLATE_STATUS.PUBLISHED"
              link
              type="primary"
              @click="openWizard(row)"
            >
              编辑向导
            </el-button>
            <el-button
              v-if="row.status !== TEMPLATE_STATUS.PUBLISHED"
              link
              type="success"
              @click="openWizard(row, 3)"
            >
              预览发布
            </el-button>
            <el-button
              v-if="row.status === TEMPLATE_STATUS.PUBLISHED"
              link
              type="warning"
              @click="handleNewVersion(row)"
            >
              新建版本
            </el-button>
            <el-popconfirm
              v-if="row.status !== TEMPLATE_STATUS.PUBLISHED"
              title="确认删除此模板?"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="wizardVisible"
      :title="editing ? '编辑审批模板' : '新建审批模板'"
      width="1180px"
      destroy-on-close
      class="template-wizard-dialog"
    >
      <el-steps :active="activeStep" finish-status="success" simple class="template-wizard__steps">
        <el-step title="基础信息" />
        <el-step title="动态表单" />
        <el-step title="流程设计" />
        <el-step title="预览发布" />
      </el-steps>

      <section class="template-wizard__body">
        <el-form v-show="activeStep === 0" :model="form" label-width="100px" class="template-wizard__basic">
          <el-form-item label="模板名称" required>
            <el-input v-model="form.templateName" placeholder="如 请假审批" />
          </el-form-item>
          <el-form-item label="模板标识" required>
            <el-input v-model="form.templateKey" :disabled="!!editing" placeholder="如 leave_request" />
          </el-form-item>
        </el-form>

        <ApprovalFormDesigner v-show="activeStep === 1" v-model="form.formConfig" />

        <ApprovalFlowDesigner
          v-show="activeStep === 2"
          v-model="form.flowModel"
          :form-schema="form.formConfig"
        />

        <div v-show="activeStep === 3" class="template-preview">
          <section class="template-preview__section">
            <div class="template-preview__title">表单预览</div>
            <ApprovalDynamicForm v-model="previewFormData" :schema="form.formConfig" />
          </section>
          <section class="template-preview__section">
            <div class="template-preview__title">流程预览</div>
            <ApprovalFlowDesigner
              v-model="form.flowModel"
              :form-schema="form.formConfig"
              readonly
            />
          </section>
          <section class="template-preview__section">
            <div class="template-preview__title">BPMN XML</div>
            <el-input :model-value="previewBpmnXml" type="textarea" :rows="10" readonly />
          </section>
        </div>
      </section>

      <template #footer>
        <div class="template-wizard__footer">
          <div class="template-wizard__status">
            <el-tag v-if="form.id" type="info">草稿 ID {{ form.id }}</el-tag>
          </div>
          <div class="template-wizard__actions">
            <el-button @click="wizardVisible = false">关闭</el-button>
            <el-button :disabled="activeStep === 0" @click="activeStep--">上一步</el-button>
            <el-button :loading="savingDraft" @click="handleSaveDraft">保存草稿</el-button>
            <el-button v-if="activeStep < 3" type="primary" @click="goNext">下一步</el-button>
            <el-button v-else type="success" :loading="publishing" @click="handleWizardPublish">发布</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="flowPreviewVisible"
      :title="flowPreviewTemplate ? `查看流程 - ${flowPreviewTemplate.templateName}` : '查看流程'"
      width="1180px"
      destroy-on-close
      class="template-flow-preview-dialog"
    >
      <el-skeleton v-if="flowPreviewLoading" :rows="8" animated />
      <ApprovalFlowDesigner
        v-else
        v-model="flowPreviewModel"
        :form-schema="flowPreviewTemplate?.formConfig || ''"
        readonly
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTemplates, createTemplate } from '@/api/approval'
import type { ProcessTemplate } from '@/types'
import {
  createNewVersion,
  deleteTemplate,
  getTemplateXml,
  publishTemplate,
  saveNodeConfigs,
  saveTemplateXml,
  updateTemplate,
} from '@/api/template'
import { TEMPLATE_STATUS, TEMPLATE_STATUS_LABEL_MAP } from '@/bpmn/constants'
import ApprovalFormDesigner from '@/components/approval/ApprovalFormDesigner.vue'
import ApprovalDynamicForm from '@/components/approval/ApprovalDynamicForm.vue'
import ApprovalFlowDesigner from '@/components/approval/ApprovalFlowDesigner.vue'
import {
  createDefaultApprovalFlow,
  generateApprovalFlowArtifacts,
  parseApprovalFlowModelFromBpmnXml,
  validateApprovalFlow,
  type ApprovalFlowNode,
} from '@/utils/approval-flow'
import { parseApprovalFormConfig } from '@/utils/approval-form'

const router = useRouter()

const loading = ref(false)
const templates = ref<ProcessTemplate[]>([])
const wizardVisible = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })
const search = reactive({
  templateName: '',
  status: undefined as number | undefined,
})
const editing = ref<ProcessTemplate | null>(null)
const activeStep = ref(0)
const savingDraft = ref(false)
const publishing = ref(false)
const previewFormData = ref<Record<string, unknown>>({})
const flowPreviewVisible = ref(false)
const flowPreviewLoading = ref(false)
const flowPreviewTemplate = ref<ProcessTemplate | null>(null)
const flowPreviewModel = ref<ApprovalFlowNode>(createDefaultApprovalFlow())

const form = reactive<{
  id?: number
  templateName: string
  templateKey: string
  formConfig: string
  flowModel: ApprovalFlowNode
}>({
  templateName: '',
  templateKey: '',
  formConfig: '',
  flowModel: createDefaultApprovalFlow(),
})

const previewArtifacts = computed(() => {
  try {
    return generateApprovalFlowArtifacts(
      form.flowModel,
      normalizedProcessId(form.templateKey),
      form.templateName || '审批流程',
      { validate: false },
    )
  } catch {
    return { bpmnXml: '', nodeConfigs: [] }
  }
})

const previewBpmnXml = computed(() => previewArtifacts.value.bpmnXml)

async function loadData() {
  loading.value = true
  try {
    const result = await getTemplates({
      templateName: search.templateName || undefined,
      status: search.status,
      page: pagination.page,
      pageSize: pagination.size,
    })
    templates.value = result?.list ?? []
    pagination.total = result?.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

async function openWizard(tpl?: ProcessTemplate, step = 0) {
  editing.value = tpl || null
  activeStep.value = step
  previewFormData.value = {}

  Object.assign(form, {
    id: tpl?.id,
    templateName: tpl?.templateName || '',
    templateKey: tpl?.templateKey || '',
    formConfig: tpl?.formConfig || '',
    flowModel: createDefaultApprovalFlow(),
  })

  if (tpl?.id) {
    try {
      const xmlData: any = await getTemplateXml(tpl.id)
      const xml = typeof xmlData === 'string' ? xmlData : xmlData?.bpmnXml ?? ''
      form.flowModel = parseApprovalFlowModelFromBpmnXml(xml) || createDefaultApprovalFlow()
    } catch {
      form.flowModel = createDefaultApprovalFlow()
    }
  }

  wizardVisible.value = true
}

async function openFlowPreview(tpl: ProcessTemplate) {
  flowPreviewTemplate.value = tpl
  flowPreviewModel.value = createDefaultApprovalFlow()
  flowPreviewVisible.value = true
  flowPreviewLoading.value = true
  try {
    const xmlData: any = await getTemplateXml(tpl.id)
    const xml = typeof xmlData === 'string' ? xmlData : xmlData?.bpmnXml ?? ''
    flowPreviewModel.value = parseApprovalFlowModelFromBpmnXml(xml) || createDefaultApprovalFlow()
  } catch (err) {
    ElMessage.error('加载流程失败: ' + (err instanceof Error ? err.message : '未知错误'))
  } finally {
    flowPreviewLoading.value = false
  }
}

async function handleSaveDraft() {
  const saved = await saveDraft()
  if (saved) {
    ElMessage.success('草稿已保存')
    await loadData()
  }
}

async function saveDraft(): Promise<boolean> {
  if (!form.templateName.trim() || !form.templateKey.trim()) {
    ElMessage.error('请填写模板名称和模板标识')
    activeStep.value = 0
    return false
  }

  savingDraft.value = true
  try {
    const templatePayload = {
      templateName: form.templateName.trim(),
      templateKey: form.templateKey.trim(),
      formConfig: form.formConfig,
    }
    const savedTemplate = form.id
      ? await updateTemplate(form.id, templatePayload)
      : await createTemplate(templatePayload)
    const savedId = (savedTemplate as ProcessTemplate | undefined)?.id || form.id
    if (!savedId) {
      ElMessage.error('草稿保存失败: 未返回模板 ID')
      return false
    }
    form.id = savedId

    const artifacts = generateApprovalFlowArtifacts(
      form.flowModel,
      normalizedProcessId(form.templateKey),
      form.templateName,
      { validate: false },
    )
    await Promise.all([
      saveTemplateXml(savedId, artifacts.bpmnXml),
      saveNodeConfigs(savedId, artifacts.nodeConfigs),
    ])
    return true
  } catch (err) {
    ElMessage.error('草稿保存失败: ' + (err instanceof Error ? err.message : '未知错误'))
    return false
  } finally {
    savingDraft.value = false
  }
}

async function goNext() {
  if (activeStep.value === 0 && (!form.templateName.trim() || !form.templateKey.trim())) {
    ElMessage.error('请填写模板名称和模板标识')
    return
  }
  if (activeStep.value === 1 && parseApprovalFormConfig(form.formConfig).length === 0) {
    ElMessage.error('至少需要一个表单字段')
    return
  }
  if (activeStep.value === 2) {
    const errors = validateApprovalFlow(form.flowModel)
    if (errors.length > 0) {
      ElMessage.error(errors.join('\n'))
      return
    }
  }
  await saveDraft()
  activeStep.value = Math.min(activeStep.value + 1, 3)
}

async function handleWizardPublish() {
  if (parseApprovalFormConfig(form.formConfig).length === 0) {
    ElMessage.error('表单配置不完整: 至少需要一个表单字段')
    activeStep.value = 1
    return
  }
  const flowErrors = validateApprovalFlow(form.flowModel)
  if (flowErrors.length > 0) {
    ElMessage.error(flowErrors.join('\n'))
    activeStep.value = 2
    return
  }

  publishing.value = true
  try {
    await ElMessageBox.confirm('发布后当前版本将不可修改, 确认发布?', '发布确认', {
      confirmButtonText: '确认发布',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const saved = await saveDraft()
    if (!saved || !form.id) return
    await publishTemplate(form.id)
    ElMessage.success('发布成功')
    wizardVisible.value = false
    await loadData()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('发布失败: ' + (err instanceof Error ? err.message : '未知错误'))
    }
  } finally {
    publishing.value = false
  }
}

function openDesigner(row: ProcessTemplate) {
  router.push(`/approval/template/designer/${row.id}`)
}

async function handleNewVersion(row: ProcessTemplate) {
  try {
    const data: any = await createNewVersion(row.id)
    const newId = data?.id ?? data
    ElMessage.success('新版本已创建')
    router.push(`/approval/template/designer/${newId}`)
  } catch {
    ElMessage.error('创建新版本失败')
  }
}

async function handleDelete(row: ProcessTemplate) {
  try {
    await deleteTemplate(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    ElMessage.error('删除失败')
  }
}

function normalizedProcessId(raw: string) {
  const normalized = raw.trim().replace(/[^A-Za-z0-9_]/g, '_') || 'approval_process'
  return /^[A-Za-z_]/.test(normalized) ? normalized : `process_${normalized}`
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.template-wizard__steps {
  margin-bottom: 18px;
}

.template-wizard__body {
  min-height: 600px;
  max-height: calc(100vh - 260px);
  overflow: auto;
  padding-right: 4px;
}

.template-wizard__basic {
  max-width: 640px;
  padding-top: 12px;
}

.template-preview {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.template-preview__section {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  padding: 16px;
}

.template-preview__title {
  margin-bottom: 12px;
  color: #303133;
  font-weight: 700;
}

.template-wizard__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.template-wizard__actions {
  display: flex;
  gap: 8px;
}

.template-wizard__status {
  min-height: 24px;
}
</style>
