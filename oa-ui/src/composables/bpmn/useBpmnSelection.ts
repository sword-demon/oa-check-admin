import { ref, type Ref } from 'vue'
import type { BpmnInstance } from './useBpmnModeler'

export function useBpmnSelection(modeler: Ref<BpmnInstance | null>) {
  const selectedElement = ref<any>(null)

  function startListening() {
    if (!modeler.value) return

    const eventBus = (modeler.value as any).get('eventBus')
    if (!eventBus) return

    eventBus.on('selection.changed', (e: any) => {
      const selection = e.newSelection
      if (selection && selection.length === 1) {
        selectedElement.value = selection[0]
      } else {
        selectedElement.value = null
      }
    })
  }

  function stopListening() {
    if (!modeler.value) return

    const eventBus = (modeler.value as any).get('eventBus')
    if (!eventBus) return

    eventBus.off('selection.changed')
    selectedElement.value = null
  }

  return {
    selectedElement,
    startListening,
    stopListening,
  }
}
