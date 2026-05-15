import { ref, type Ref } from 'vue'
import type { BpmnInstance } from './useBpmnModeler'

export function useBpmnCommandStack(modeler: Ref<BpmnInstance | null>) {
  const canUndo = ref(false)
  const canRedo = ref(false)

  function updateStackState() {
    if (!modeler.value) return
    const commandStack = (modeler.value as any).get('commandStack')
    if (!commandStack) return
    canUndo.value = commandStack.canUndo()
    canRedo.value = commandStack.canRedo()
  }

  function undo() {
    if (!modeler.value) return
    const commandStack = (modeler.value as any).get('commandStack')
    if (commandStack && commandStack.canUndo()) {
      commandStack.undo()
      updateStackState()
    }
  }

  function redo() {
    if (!modeler.value) return
    const commandStack = (modeler.value as any).get('commandStack')
    if (commandStack && commandStack.canRedo()) {
      commandStack.redo()
      updateStackState()
    }
  }

  function startListening() {
    if (!modeler.value) return
    const eventBus = (modeler.value as any).get('eventBus')
    if (!eventBus) return

    eventBus.on('commandStack.changed', updateStackState)
  }

  function stopListening() {
    if (!modeler.value) return
    const eventBus = (modeler.value as any).get('eventBus')
    if (!eventBus) return

    eventBus.off('commandStack.changed', updateStackState)
  }

  return {
    canUndo,
    canRedo,
    undo,
    redo,
    startListening,
    stopListening,
  }
}
