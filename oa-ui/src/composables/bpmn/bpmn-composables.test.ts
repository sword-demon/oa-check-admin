import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref, nextTick } from 'vue'

// ---------------------------------------------------------------------------
// useBpmnSelection tests
// ---------------------------------------------------------------------------
describe('useBpmnSelection', () => {
  beforeEach(() => {
    vi.resetModules()
  })

  it('updates selectedElement when a single element is selected', async () => {
    const { useBpmnSelection } = await import('@/composables/bpmn/useBpmnSelection')
    const eventHandlers: Record<string, Function> = {}
    const mockEventBus = {
      on: vi.fn((event: string, handler: Function) => { eventHandlers[event] = handler }),
      off: vi.fn(),
    }
    const modeler = ref({
      get: (service: string) => service === 'eventBus' ? mockEventBus : null,
    } as any)

    const { selectedElement, startListening } = useBpmnSelection(modeler)
    startListening()

    // Simulate selection.changed event with one element
    eventHandlers['selection.changed']({ newSelection: [{ id: 'task1' }] })
    await nextTick()

    expect(selectedElement.value).toEqual({ id: 'task1' })
  })

  it('clears selectedElement when selection is empty', async () => {
    const { useBpmnSelection } = await import('@/composables/bpmn/useBpmnSelection')
    const eventHandlers: Record<string, Function> = {}
    const mockEventBus = {
      on: vi.fn((event: string, handler: Function) => { eventHandlers[event] = handler }),
      off: vi.fn(),
    }
    const modeler = ref({
      get: (service: string) => service === 'eventBus' ? mockEventBus : null,
    } as any)

    const { selectedElement, startListening } = useBpmnSelection(modeler)
    startListening()

    // First select, then clear
    eventHandlers['selection.changed']({ newSelection: [{ id: 'task1' }] })
    await nextTick()
    expect(selectedElement.value).not.toBeNull()

    eventHandlers['selection.changed']({ newSelection: [] })
    await nextTick()
    expect(selectedElement.value).toBeNull()
  })

  it('clears selectedElement when multiple elements are selected', async () => {
    const { useBpmnSelection } = await import('@/composables/bpmn/useBpmnSelection')
    const eventHandlers: Record<string, Function> = {}
    const mockEventBus = {
      on: vi.fn((event: string, handler: Function) => { eventHandlers[event] = handler }),
      off: vi.fn(),
    }
    const modeler = ref({
      get: (service: string) => service === 'eventBus' ? mockEventBus : null,
    } as any)

    const { selectedElement, startListening } = useBpmnSelection(modeler)
    startListening()

    eventHandlers['selection.changed']({ newSelection: [{ id: 'a' }, { id: 'b' }] })
    await nextTick()

    expect(selectedElement.value).toBeNull()
  })

  it('does nothing when modeler is null', async () => {
    const { useBpmnSelection } = await import('@/composables/bpmn/useBpmnSelection')
    const modeler = ref(null)

    const { selectedElement, startListening, stopListening } = useBpmnSelection(modeler)
    startListening()
    stopListening()

    expect(selectedElement.value).toBeNull()
  })

  it('stopListening clears selection and unregisters handler', async () => {
    const { useBpmnSelection } = await import('@/composables/bpmn/useBpmnSelection')
    const mockEventBus = {
      on: vi.fn(),
      off: vi.fn(),
    }
    const modeler = ref({
      get: (service: string) => service === 'eventBus' ? mockEventBus : null,
    } as any)

    const { selectedElement, startListening, stopListening } = useBpmnSelection(modeler)
    startListening()
    stopListening()

    expect(mockEventBus.off).toHaveBeenCalledWith('selection.changed')
    expect(selectedElement.value).toBeNull()
  })
})

// ---------------------------------------------------------------------------
// useBpmnCommandStack tests
// ---------------------------------------------------------------------------
describe('useBpmnCommandStack', () => {
  beforeEach(() => {
    vi.resetModules()
  })

  it('updates canUndo/canRedo from commandStack', async () => {
    const { useBpmnCommandStack } = await import('@/composables/bpmn/useBpmnCommandStack')
    const eventHandlers: Record<string, Function> = {}
    const mockCommandStack = {
      canUndo: vi.fn(() => true),
      canRedo: vi.fn(() => false),
      undo: vi.fn(),
      redo: vi.fn(),
    }
    const mockEventBus = {
      on: vi.fn((event: string, handler: Function) => { eventHandlers[event] = handler }),
      off: vi.fn(),
    }
    const modeler = ref({
      get: (service: string) => {
        if (service === 'commandStack') return mockCommandStack
        if (service === 'eventBus') return mockEventBus
        return null
      },
    } as any)

    const { canUndo, canRedo, startListening } = useBpmnCommandStack(modeler)
    startListening()

    // Simulate commandStack.changed event
    eventHandlers['commandStack.changed']()
    await nextTick()

    expect(canUndo.value).toBe(true)
    expect(canRedo.value).toBe(false)
  })

  it('undo calls commandStack.undo when canUndo is true', async () => {
    const { useBpmnCommandStack } = await import('@/composables/bpmn/useBpmnCommandStack')
    const mockCommandStack = {
      canUndo: vi.fn(() => true),
      canRedo: vi.fn(() => false),
      undo: vi.fn(),
      redo: vi.fn(),
    }
    const mockEventBus = { on: vi.fn(), off: vi.fn() }
    const modeler = ref({
      get: (service: string) => {
        if (service === 'commandStack') return mockCommandStack
        if (service === 'eventBus') return mockEventBus
        return null
      },
    } as any)

    const { undo, startListening } = useBpmnCommandStack(modeler)
    startListening()
    undo()

    expect(mockCommandStack.undo).toHaveBeenCalled()
  })

  it('redo calls commandStack.redo when canRedo is true', async () => {
    const { useBpmnCommandStack } = await import('@/composables/bpmn/useBpmnCommandStack')
    const mockCommandStack = {
      canUndo: vi.fn(() => false),
      canRedo: vi.fn(() => true),
      undo: vi.fn(),
      redo: vi.fn(),
    }
    const mockEventBus = { on: vi.fn(), off: vi.fn() }
    const modeler = ref({
      get: (service: string) => {
        if (service === 'commandStack') return mockCommandStack
        if (service === 'eventBus') return mockEventBus
        return null
      },
    } as any)

    const { redo, startListening } = useBpmnCommandStack(modeler)
    startListening()
    redo()

    expect(mockCommandStack.redo).toHaveBeenCalled()
  })

  it('does nothing when modeler is null', async () => {
    const { useBpmnCommandStack } = await import('@/composables/bpmn/useBpmnCommandStack')
    const modeler = ref(null)

    const { canUndo, canRedo, undo, redo, startListening, stopListening } = useBpmnCommandStack(modeler)
    startListening()
    undo()
    redo()
    stopListening()

    expect(canUndo.value).toBe(false)
    expect(canRedo.value).toBe(false)
  })

  it('undo does nothing when canUndo is false', async () => {
    const { useBpmnCommandStack } = await import('@/composables/bpmn/useBpmnCommandStack')
    const mockCommandStack = {
      canUndo: vi.fn(() => false),
      canRedo: vi.fn(() => false),
      undo: vi.fn(),
      redo: vi.fn(),
    }
    const mockEventBus = { on: vi.fn(), off: vi.fn() }
    const modeler = ref({
      get: (service: string) => {
        if (service === 'commandStack') return mockCommandStack
        if (service === 'eventBus') return mockEventBus
        return null
      },
    } as any)

    const { undo } = useBpmnCommandStack(modeler)
    undo()

    expect(mockCommandStack.undo).not.toHaveBeenCalled()
  })

  it('stopListening unregisters event handler', async () => {
    const { useBpmnCommandStack } = await import('@/composables/bpmn/useBpmnCommandStack')
    const mockCommandStack = {
      canUndo: vi.fn(() => false),
      canRedo: vi.fn(() => false),
    }
    const mockEventBus = {
      on: vi.fn(),
      off: vi.fn(),
    }
    const modeler = ref({
      get: (service: string) => {
        if (service === 'commandStack') return mockCommandStack
        if (service === 'eventBus') return mockEventBus
        return null
      },
    } as any)

    const { startListening, stopListening } = useBpmnCommandStack(modeler)
    startListening()
    stopListening()

    expect(mockEventBus.off).toHaveBeenCalledWith('commandStack.changed', expect.any(Function))
  })
})

// ---------------------------------------------------------------------------
// useBpmnModeler tests
// ---------------------------------------------------------------------------
describe('useBpmnModeler', () => {
  beforeEach(() => {
    vi.resetModules()
  })

  it('returns initial state with null modeler, not loading, no error', async () => {
    const { useBpmnModeler } = await import('@/composables/bpmn/useBpmnModeler')

    const { modeler, loading, error } = useBpmnModeler()

    expect(modeler.value).toBeNull()
    expect(loading.value).toBe(false)
    expect(error.value).toBeNull()
  })

  it('getModeler returns null initially', async () => {
    const { useBpmnModeler } = await import('@/composables/bpmn/useBpmnModeler')

    const { getModeler } = useBpmnModeler()

    expect(getModeler()).toBeNull()
  })
})
