import { describe, it, expect, vi, beforeEach } from 'vitest'

// Capture interceptor functions
let requestInterceptorFn: (config: any) => any
let responseSuccessFn: (response: any) => any
let responseErrorFn: (error: any) => any

vi.mock('axios', () => {
  const mockInstance = {
    interceptors: {
      request: {
        use(fn: any) { requestInterceptorFn = fn },
      },
      response: {
        use(successFn: any, errorFn: any) {
          responseSuccessFn = successFn
          responseErrorFn = errorFn
        },
      },
    },
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  }
  return {
    default: {
      create: vi.fn(() => mockInstance),
    },
  }
})

vi.mock('element-plus', () => ({
  ElMessage: { error: vi.fn() },
}))

vi.mock('@/router', () => ({
  default: { push: vi.fn() },
}))

describe('request interceptor', () => {
  beforeEach(() => {
    localStorage.clear()
    // Re-import to re-register interceptors
    vi.resetModules()
    requestInterceptorFn = null as any
    responseSuccessFn = null as any
    responseErrorFn = null as any
  })

  it('adds satoken header when token exists in localStorage', async () => {
    localStorage.setItem('token', 'test-token-123')
    await import('@/utils/request')

    const config = { headers: {} }
    const result = requestInterceptorFn(config)

    expect(result.headers['satoken']).toBe('test-token-123')
  })

  it('skips satoken header when no token in localStorage', async () => {
    await import('@/utils/request')

    const config = { headers: {} }
    const result = requestInterceptorFn(config)

    expect(result.headers['satoken']).toBeUndefined()
  })

  it('returns data on success response (code 200)', async () => {
    await import('@/utils/request')

    const response = { data: { code: 200, msg: 'success', data: { id: 1 } } }
    const result = responseSuccessFn(response)

    expect(result).toEqual({ id: 1 })
  })

  it('rejects on non-200 business code', async () => {
    await import('@/utils/request')

    const response = { data: { code: 1001, msg: '系统异常', data: null } }

    await expect(responseSuccessFn(response)).rejects.toThrow('系统异常')
  })

  it('handles 401 error by clearing token and redirecting', async () => {
    localStorage.setItem('token', 'old-token')
    await import('@/utils/request')
    const { default: router } = await import('@/router')

    const error = { response: { status: 401 }, message: 'Unauthorized' }

    await expect(responseErrorFn(error)).rejects.toThrow('Unauthorized')
    expect(localStorage.getItem('token')).toBeNull()
    expect(router.push).toHaveBeenCalledWith('/login')
  })

  it('handles network error without response', async () => {
    await import('@/utils/request')

    const error = { message: 'Network Error' }

    await expect(responseErrorFn(error)).rejects.toThrow('Network Error')
  })
})
