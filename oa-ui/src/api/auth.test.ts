import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockGet = vi.fn()
const mockPost = vi.fn()

vi.mock('@/utils/request', () => ({
  default: {
    get: mockGet,
    post: mockPost,
  },
}))

describe('auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('login calls POST /auth/login', async () => {
    mockPost.mockResolvedValue({ token: 'abc', user: { id: 1 } })
    const { login } = await import('@/api/auth')

    const result = await login({ username: 'admin', password: '123' })

    expect(mockPost).toHaveBeenCalledWith('/auth/login', {
      username: 'admin',
      password: '123',
    })
  })

  it('logout calls POST /auth/logout', async () => {
    mockPost.mockResolvedValue(undefined)
    const { logout } = await import('@/api/auth')

    await logout()
    expect(mockPost).toHaveBeenCalledWith('/auth/logout')
  })

  it('getMe calls GET /auth/me', async () => {
    mockGet.mockResolvedValue({ id: 1, username: 'admin' })
    const { getMe } = await import('@/api/auth')

    const result = await getMe()
    expect(mockGet).toHaveBeenCalledWith('/auth/me')
  })
})
