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
      @preview-xml="showXmlPreview = true"
      @new-version="handleNewVersion"
      @back="router.push('/approval/template')"
    />

    <div class="flow-designer__body">
      <div class="flow-designer__canvas">
        <BpmnCanvas
          ref="canvasRef"
          :loading="modelerState.loading.value"
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import DesignerToolbar from './components/DesignerToolbar.vue'
import BpmnCanvas from './components/BpmnCanvas.vue'
import PropertiesPanel from './components/PropertiesPanel.vue'
import { useBpmnModeler } from '@/composables/bpmn/useBpmnModeler'
import { useBpmnSelection } from '@/composables/bpmn/useBpmnSelection'
import { useBpmnCommandStack } from '@/composables/bpmn/useBpmnCommandStack'
import { getTemplateXml, saveTemplateXml, getNodeConfigs, saveNodeConfigs, publishTemplate, createNewVersion } from '@/api/template'
import { getTemplates, createTemplate } from '@/api/approval'
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

const isPublished = computed(() => templateStatus.value === TEMPLATE_STATUS.PUBLISHED)

const canvasContainerRef = ref<HTMLElement | null>(null)
const modelerState = useBpmnModeler(canvasContainerRef, isPublished.value)
const selection = useBpmnSelection(modelerState.modeler)
const commandStack = useBpmnCommandStack(modelerState.modeler)

const canvasRef = ref<InstanceType<typeof BpmnCanvas> | null>(null)

function onModelerReady() {
  selection.startListening()
  commandStack.startListening()
}

async function loadTemplate() {
  try {
    const xmlData: any = await getTemplateXml(templateId.value)
    const xml = xmlData as string

    if (xml) {
      await modelerState.importXML(xml)
    } else {
      await modelerState.createDiagram('process', '新流程')
    }

    onModelerReady()
  } catch (err: unknown) {
    ElMessage.error('加载流程失败: ' + (err instanceof Error ? err.message : '未知错误'))
  }
}

async function loadTemplateInfo() {
  try {
    const data: any = await getTemplates()
    const templates = Array.isArray(data) ? data : (data?.records ?? data?.list ?? [])
    const template = templates.find((t: any) => t.id === templateId.value)
    if (template) {
      templateStatus.value = template.status ?? TEMPLATE_STATUS.DRAFT
      templateName.value = template.templateName ?? ''
    }
  } catch {
    // Silently ignore - template info is supplementary
  }
}

async function handleSave() {
  saving.value = true
  try {
    const xml = await modelerState.saveXML()
    if (!xml) {
      ElMessage.error('保存失败: 无法生成 BPMN XML')
      return
    }

    const configs = extractNodeConfigs(modelerState.modeler.value as any)

    await Promise.all([
      saveTemplateXml(templateId.value, xml),
      saveNodeConfigs(templateId.value, configs),
    ])

    ElMessage.success('保存成功')
  } catch (err: unknown) {
    ElMessage.error('保存失败: ' + (err instanceof Error ? err.message : '未知错误'))
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

    await handleSave()
    await publishTemplate(templateId.value)

    ElMessage.success('发布成功')
    templateStatus.value = TEMPLATE_STATUS.PUBLISHED
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

function handleZoomIn() {
  const modeler = modelerState.getModeler()
  if (!modeler) return
  const canvas = (modeler as any).get('canvas')
  canvas.zoom(Math.min(canvas.zoom() * 1.1, 4))
}

function handleZoomOut() {
  const modeler = modelerState.getModeler()
  if (!modeler) return
  const canvas = (modeler as any).get('canvas')
  canvas.zoom(Math.max(canvas.zoom() / 1.1, 0.2))
}

function handleZoomFit() {
  const modeler = modelerState.getModeler()
  if (!modeler) return
  const canvas = (modeler as any).get('canvas')
  canvas.zoom('fit-viewport', 'auto')
}

onMounted(() => {
  loadTemplateInfo()
  loadTemplate()
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
