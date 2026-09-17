import { computed, ref } from 'vue'
import { api, setToken } from './api'
import type { User } from '@/types'

const USER_KEY = 'eleme_admin_user'

function loadStoredUser(): User | null {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null') as User | null
  } catch {
    return null
  }
}

export const currentUser = ref<User | null>(loadStoredUser())
export const isAuthenticated = computed(() => currentUser.value?.role === 2)

export async function login(username: string, password: string) {
  if (username !== 'admin' || password !== 'admin') {
    throw new Error('账号或密码错误')
  }

  // 后端 /login 直接返回带 token 的 UserVO（平铺结构）。
  const data = await api<User & { token?: string | null }>('/login', {
    method: 'POST',
    body: JSON.stringify({ phone: username, password }),
  })
  if (!data.token || data.role !== 2) throw new Error('该账号不是管理员账号')

  setToken(data.token)
  currentUser.value = { ...data, token: null }
  localStorage.setItem(USER_KEY, JSON.stringify(currentUser.value))
}

export async function logout() {
  try {
    await api<null>('/logout', { method: 'DELETE' })
  } finally {
    setToken(null)
    currentUser.value = null
    localStorage.removeItem(USER_KEY)
  }
}
