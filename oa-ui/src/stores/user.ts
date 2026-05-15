import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getMe } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<Record<string, any> | null>(null)

  async function login(username: string, password: string) {
    const data: any = await loginApi({ username, password })
    token.value = data.token
    userInfo.value = data.user
    localStorage.setItem('token', data.token)
  }

  async function fetchUser() {
    const data: any = await getMe()
    userInfo.value = data
  }

  async function logout() {
    await logoutApi()
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    router.push('/login')
  }

  return { token, userInfo, login, fetchUser, logout }
})
