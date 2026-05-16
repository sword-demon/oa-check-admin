import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockPut = vi.fn()
const mockDelete = vi.fn()

vi.mock('@/utils/request', () => ({
  default: {
    get: mockGet,
    post: mockPost,
    put: mockPut,
    delete: mockDelete,
  },
}))

describe('template API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getTemplateXml calls GET /approval/template/:id/xml', async () => {
    mockGet.mockResolvedValue('<xml/>')
    const { getTemplateXml } = await import('@/api/template')

    const result = await getTemplateXml(1)
    expect(mockGet).toHaveBeenCalledWith('/approval/template/1/xml')
  })

  it('saveTemplateXml calls PUT /approval/template/:id/xml with bpmnXml', async () => {
    mockPut.mockResolvedValue(undefined)
    const { saveTemplateXml } = await import('@/api/template')

    await saveTemplateXml(5, '<bpmn/>')
    expect(mockPut).toHaveBeenCalledWith('/approval/template/5/xml', { bpmnXml: '<bpmn/>' })
  })

  it('getNodeConfigs calls GET /approval/template/:id/node-config', async () => {
    mockGet.mockResolvedValue([])
    const { getNodeConfigs } = await import('@/api/template')

    await getNodeConfigs(3)
    expect(mockGet).toHaveBeenCalledWith('/approval/template/3/node-config')
  })

  it('saveNodeConfigs calls PUT /approval/template/:id/node-config with configs', async () => {
    mockPut.mockResolvedValue(undefined)
    const { saveNodeConfigs } = await import('@/api/template')

    const configs = [{ nodeId: 'task1', nodeName: '审批', nodeType: 'userTask' }]
    await saveNodeConfigs(2, configs)
    expect(mockPut).toHaveBeenCalledWith('/approval/template/2/node-config', configs)
  })

  it('publishTemplate calls POST /approval/template/:id/publish', async () => {
    mockPost.mockResolvedValue(undefined)
    const { publishTemplate } = await import('@/api/template')

    await publishTemplate(10)
    expect(mockPost).toHaveBeenCalledWith('/approval/template/10/publish')
  })

  it('createNewVersion calls POST /approval/template/:id/new-version', async () => {
    mockPost.mockResolvedValue({ id: 99 })
    const { createNewVersion } = await import('@/api/template')

    await createNewVersion(10)
    expect(mockPost).toHaveBeenCalledWith('/approval/template/10/new-version')
  })

  it('getTemplate calls GET /approval/template/:id', async () => {
    mockGet.mockResolvedValue({ id: 1, templateName: 'Test' })
    const { getTemplate } = await import('@/api/template')

    await getTemplate(1)
    expect(mockGet).toHaveBeenCalledWith('/approval/template/1')
  })

  it('updateTemplate calls PUT /approval/template/:id with data', async () => {
    mockPut.mockResolvedValue(undefined)
    const { updateTemplate } = await import('@/api/template')

    await updateTemplate(1, { templateName: 'Updated' })
    expect(mockPut).toHaveBeenCalledWith('/approval/template/1', { templateName: 'Updated' })
  })

  it('deleteTemplate calls DELETE /approval/template/:id', async () => {
    mockDelete.mockResolvedValue(undefined)
    const { deleteTemplate } = await import('@/api/template')

    await deleteTemplate(5)
    expect(mockDelete).toHaveBeenCalledWith('/approval/template/5')
  })
})
