<template>
  <div class="flow-designer">
    <DesignerToolbar
      :template-status="templateStatus"
      :template-name="templateName"
      :can-undo="commandStack.canUndo.value"
      :can-redo="commandStack.canRedo.value"
      :saving="saving"
      @save="handleSave"
      @publish="handlePublish"
      @undo="commandStack.undo()"
      @redo="commandStack.redo()"
      @zoom-in="handleZoomIn"
      @zoom-out="handleZoomOut"
      @zoom-fit="handleZoomFit"
      @preview-xml="openXmlPreview"
      @new-version="handleNewVersion"
      @back="handleBack"
    />

    <div class="flow-designer__body">
      <div class="flow-designer__canvas">
        <BpmnCanvas
          ref="canvasRef"
          :loading="modelerState.loading.value"
          :read-only="isPublished"
          @modeler-ready="onModelerReady"
        />
      </div>

      <PropertiesPanel
        :selected-element="selection.selectedElement.value"
        :modeler="modelerState.modeler.value"
        :template-id="templateId"
        :read-only="isPublished"
        class="flow-designer__panel"
      />
    </div>

    <el-dialog v-model="showXmlPreview" title="BPMN XML 预览" width="700px" destroy-on-close>
      <el-input
        type="textarea"
        :model-value="xmlPreviewContent"
        :rows="20"
        readonly
        class="xml-preview-textarea"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import DesignerToolbar from './components/DesignerToolbar.vue'
import BpmnCanvas from './components/BpmnCanvas.vue'
import PropertiesPanel from './components/PropertiesPanel.vue'
import { useBpmnModeler, type BpmnInstance } from '@/composables/bpmn/useBpmnModeler'
import { useBpmnSelection } from '@/composables/bpmn/useBpmnSelection'
import { useBpmnCommandStack } from '@/composables/bpmn/useBpmnCommandStack'
import { getTemplateXml, saveTemplateXml, getNodeConfigs, saveNodeConfigs, publishTemplate, createNewVersion, getTemplate } from '@/api/template'
import { extractNodeConfigs, validateProcess } from '@/bpmn/bpmn-utils'
import { TEMPLATE_STATUS } from '@/bpmn/constants'

const route = useRoute()
const router = useRouter()

const templateId = computed(() => Number(route.params.id))
const templateStatus = ref<number>(TEMPLATE_STATUS.DRAFT)
const templateName = ref('')
const saving = ref(false)
const showXmlPreview = ref(false)
const xmlPreviewContent = ref('')
const dirty = ref(false)
const dirtyAfterLoad = ref(false)

const isPublished = computed(() => templateStatus.value === TEMPLATE_STATUS.PUBLISHED)

const modelerState = useBpmnModeler()
const selection = useBpmnSelection(modelerState.modeler)
const commandStack = useBpmnCommandStack(modelerState.modeler)

const canvasRef = ref<InstanceType<typeof BpmnCanvas> | null>(null)

const ZOOM_STEP = 1.1
const ZOOM_MAX = 4
const ZOOM_MIN = 0.2
const AUTO_SAVE_INTERVAL = 30_000

let autoSaveTimer: ReturnType<typeof setInterval> | null = null

function onModelerReady(modeler: BpmnInstance) {
  modelerState.setModeler(modeler)
  selection.startListening()
  commandStack.startListening()

  const eventBus = (modeler as any).get('eventBus')
  if (eventBus) {
    eventBus.on('commandStack.changed', () => {
      dirty.value = true
    })
  }

  loadTemplate()
  startAutoSave()
}

function startAutoSave() {
  stopAutoSave()
  autoSaveTimer = setInterval(() => {
    if (dirty.value && !isPublished.value && !saving.value) {
      handleSave(true)
    }
  }, AUTO_SAVE_INTERVAL)
}

function stopAutoSave() {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
  }
}

async function loadTemplate() {
  try {
    const xmlData: any = await getTemplateXml(templateId.value)
    const xml = typeof xmlData === 'string' ? xmlData : xmlData?.bpmnXml ?? xmlData?.data ?? ''

    if (xml) {
      dirtyAfterLoad.value = true
      await modelerState.importXML(xml)
    } else {
      await modelerState.createDiagram('process', '新流程')
    }

    dirty.value = false
    dirtyAfterLoad.value = false
  } catch (err: unknown) {
    ElMessage.error('加载流程失败: ' + (err instanceof Error ? err.message : '未知错误'))
  }
}

async function loadTemplateInfo() {
  if (!templateId.value || Number.isNaN(templateId.value)) return
  try {
    const data: any = await getTemplate(templateId.value)
    if (data) {
      templateStatus.value = data.status ?? TEMPLATE_STATUS.DRAFT
      templateName.value = data.templateName ?? ''
    }
  } catch {
    // Template info is supplementary
  }
}

async function handleSave(silent = false): Promise<boolean> {
  if (saving.value) return false
  saving.value = true
  try {
    const xml = await modelerState.saveXML()
    if (!xml) {
      if (!silent) ElMessage.error('保存失败: 无法生成 BPMN XML')
      return false
    }

    const configs = extractNodeConfigs(modelerState.modeler.value as any)

    await Promise.all([
      saveTemplateXml(templateId.value, xml),
      saveNodeConfigs(templateId.value, configs),
    ])

    dirty.value = false
    if (!silent) ElMessage.success('保存成功')
    return true
  } catch (err: unknown) {
    if (!silent) ElMessage.error('保存失败: ' + (err instanceof Error ? err.message : '未知错误'))
    return false
  } finally {
    saving.value = false
  }
}

async function handlePublish() {
  const errors = validateProcess(modelerState.modeler.value as any)
  if (errors.length > 0) {
    const messages = errors.map((e) => e.message).join('\n')
    ElMessage.error(`流程校验失败:\n${messages}`)
    return
  }

  try {
    await ElMessageBox.confirm('发布后流程将不可修改, 确认发布?', '发布确认', {
      confirmButtonText: '确认发布',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const saved = await handleSave()
    if (!saved) {
      ElMessage.error('发布失败: 保存未成功')
      return
    }

    await publishTemplate(templateId.value)

    ElMessage.success('发布成功')
    templateStatus.value = TEMPLATE_STATUS.PUBLISHED
    dirty.value = false
  } catch (err: unknown) {
    if (err !== 'cancel') {
      ElMessage.error('发布失败: ' + (err instanceof Error ? err.message : '未知错误'))
    }
  }
}

async function handleNewVersion() {
  try {
    const data: any = await createNewVersion(templateId.value)
    const newId = data?.id ?? data
    ElMessage.success('新版本已创建')
    router.push(`/approval/template/designer/${newId}`)
  } catch (err: unknown) {
    ElMessage.error('创建新版本失败: ' + (err instanceof Error ? err.message : '未知错误'))
  }
}

async function openXmlPreview() {
  const xml = await modelerState.saveXML()
  xmlPreviewContent.value = xml || ''
  showXmlPreview.value = true
}

function handleBack() {
  if (dirty.value) {
    ElMessageBox.confirm('有未保存的更改, 确认离开?', '提示', {
      confirmButtonText: '离开',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      router.push('/approval/template')
    }).catch(() => {})
  } else {
    router.push('/approval/template')
  }
}

function handleZoomIn() {
  const modeler = modelerState.getModeler()
  if (!modeler) return
  const canvas = (modeler as any).get('canvas')
  canvas.zoom(Math.min(canvas.zoom() * ZOOM_STEP, ZOOM_MAX))
}

function handleZoomOut() {
  const modeler = modelerState.getModeler()
  if (!modeler) return
  const canvas = (modeler as any).get('canvas')
  canvas.zoom(Math.max(canvas.zoom() / ZOOM_STEP, ZOOM_MIN))
}

function handleZoomFit() {
  const modeler = modelerState.getModeler()
  if (!modeler) return
  const canvas = (modeler as any).get('canvas')
  canvas.zoom('fit-viewport', 'auto')
}

const _beforeunload = (e: BeforeUnloadEvent) => {
  if (dirty.value) {
    e.preventDefault()
  }
}

onMounted(() => {
  loadTemplateInfo()
  window.addEventListener('beforeunload', _beforeunload)
})

onBeforeUnmount(() => {
  stopAutoSave()
  window.removeEventListener('beforeunload', _beforeunload)
  selection.stopListening()
  commandStack.stopListening()
})
</script>

<style scoped lang="scss">
.flow-designer {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
  background: #f5f7fa;
  margin: -20px;

  &__body {
    display: flex;
    flex: 1;
    overflow: hidden;
  }

  &__canvas {
    flex: 1;
    position: relative;
    overflow: hidden;
  }

  &__panel {
    width: 320px;
    border-left: 1px solid #e4e7ed;
    background: #fff;
    overflow-y: auto;
  }
}

.xml-preview-textarea {
  :deep(textarea) {
    font-family: 'Courier New', monospace;
    font-size: 12px;
  }
}
</style>
