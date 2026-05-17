import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getMe } from '@/api/auth'
import router from '@/router'
import type { SysUser } from '@/types/system'

type CurrentUser = Partial<SysUser> & {
  realName?: string
  name?: string
  account?: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<CurrentUser | null>(null)

  async function login(username: string, password: string) {
    const data = await loginApi({ username, password })
    token.value = data.token
    userInfo.value = data.user
    localStorage.setItem('token', data.token)
  }

  async function fetchUser() {
    const data = await getMe()
    userInfo.value = data
    return data
  }

  async function hydrate() {
    if (!token.value || userInfo.value) return userInfo.value
    return fetchUser()
  }

  async function logout() {
    await logoutApi()
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    router.push('/login')
  }

  return { token, userInfo, login, fetchUser, hydrate, logout }
})
