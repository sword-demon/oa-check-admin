import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockGet = vi.fn()
const mockPost = vi.fn()

vi.mock('@/utils/request', () => ({
  default: {
    get: mockGet,
    post: mockPost,
  },
}))

describe('approval API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('submitApproval calls POST /approval/submit', async () => {
    mockPost.mockResolvedValue({ id: 1 })
    const { submitApproval } = await import('@/api/approval')

    await submitApproval({ templateId: 1, title: 'Leave', formData: '{}' })

    expect(mockPost).toHaveBeenCalledWith('/approval/submit', {
      templateId: 1,
      title: 'Leave',
      formData: '{}',
    })
  })

  it('approveTask calls POST with taskId', async () => {
    mockPost.mockResolvedValue(undefined)
    const { approveTask } = await import('@/api/approval')

    await approveTask(42, { result: 1, comment: 'OK' })

    expect(mockPost).toHaveBeenCalledWith('/approval/task/42/approve', {
      result: 1,
      comment: 'OK',
    })
  })

  it('getMyTodo calls GET /approval/my-todo', async () => {
    mockGet.mockResolvedValue([])
    const { getMyTodo } = await import('@/api/approval')

    await getMyTodo()
    expect(mockGet).toHaveBeenCalledWith('/approval/my-todo')
  })

  it('getMyDone calls GET /approval/my-done', async () => {
    mockGet.mockResolvedValue([])
    const { getMyDone } = await import('@/api/approval')

    await getMyDone()
    expect(mockGet).toHaveBeenCalledWith('/approval/my-done')
  })

  it('withdrawInstance calls POST /approval/:id/withdraw', async () => {
    mockPost.mockResolvedValue(undefined)
    const { withdrawInstance } = await import('@/api/approval')

    await withdrawInstance(100)
    expect(mockPost).toHaveBeenCalledWith('/approval/100/withdraw')
  })

  it('getTemplates calls GET /approval/template', async () => {
    mockGet.mockResolvedValue([])
    const { getTemplates } = await import('@/api/approval')

    await getTemplates()
    expect(mockGet).toHaveBeenCalledWith('/approval/template', { params: undefined })
  })

  it('getTemplates passes pagination params', async () => {
    mockGet.mockResolvedValue([])
    const { getTemplates } = await import('@/api/approval')

    await getTemplates({ page: 2, size: 20 })
    expect(mockGet).toHaveBeenCalledWith('/approval/template', { params: { page: 2, size: 20 } })
  })

  it('createTemplate calls POST /approval/template', async () => {
    mockPost.mockResolvedValue({ id: 1 })
    const { createTemplate } = await import('@/api/approval')

    await createTemplate({ name: 'Test' })
    expect(mockPost).toHaveBeenCalledWith('/approval/template', { name: 'Test' })
  })

  it('getMyCc calls GET /approval/cc', async () => {
    mockGet.mockResolvedValue([])
    const { getMyCc } = await import('@/api/approval')

    await getMyCc()
    expect(mockGet).toHaveBeenCalledWith('/approval/cc')
  })

  it('markCcRead calls POST /approval/cc/:id/read', async () => {
    mockPost.mockResolvedValue(undefined)
    const { markCcRead } = await import('@/api/approval')

    await markCcRead(5)
    expect(mockPost).toHaveBeenCalledWith('/approval/cc/5/read')
  })
})
