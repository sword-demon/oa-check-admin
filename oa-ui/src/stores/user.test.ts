import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const mockGetMe = vi.fn()
const mockLogin = vi.fn()
const mockLogout = vi.fn()
const mockPush = vi.fn()

vi.mock('@/api/auth', () => ({
  getMe: mockGetMe,
  login: mockLogin,
  logout: mockLogout,
}))

vi.mock('@/router', () => ({
  default: { push: mockPush },
}))

describe('user store', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.resetModules()
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('hydrates current user when token exists', async () => {
    localStorage.setItem('token', 'token-123')
    mockGetMe.mockResolvedValue({ id: 1, username: 'admin', nickname: '超级管理员' })

    const { useUserStore } = await import('@/stores/user')
    const store = useUserStore()

    await store.hydrate()

    expect(mockGetMe).toHaveBeenCalledTimes(1)
    expect(store.userInfo).toEqual({ id: 1, username: 'admin', nickname: '超级管理员' })
  })

  it('skips hydrate when token is missing', async () => {
    const { useUserStore } = await import('@/stores/user')
    const store = useUserStore()

    await store.hydrate()

    expect(mockGetMe).not.toHaveBeenCalled()
    expect(store.userInfo).toBeNull()
  })

  it('stores token and current user on login', async () => {
    mockLogin.mockResolvedValue({
      token: 'token-456',
      user: { id: 2, username: 'demo', nickname: '' },
    })

    const { useUserStore } = await import('@/stores/user')
    const store = useUserStore()

    await store.login('demo', '123456')

    expect(store.token).toBe('token-456')
    expect(store.userInfo).toEqual({ id: 2, username: 'demo', nickname: '' })
    expect(localStorage.getItem('token')).toBe('token-456')
  })
})
