import type { ApiEnvelope } from '@/types'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api'
const TOKEN_KEY = 'eleme_admin_token'

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status = 0,
    public readonly details: unknown = null,
  ) {
    super(message)
  }
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  const token = getToken()
  if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)

  let response: Response
  try {
    response = await fetch(`${API_BASE}${path}`, { ...options, headers })
  } catch {
    throw new ApiError('无法连接后端服务，请确认服务已启动')
  }

  const payload = (await response.json().catch(() => null)) as ApiEnvelope<T> | null
  if (!response.ok || !payload || payload.code !== 1) {
    if (response.status === 401) setToken(null)
    throw new ApiError(payload?.msg || `请求失败 (${response.status})`, response.status, payload?.data)
  }
  return payload.data
}

export function queryString(params: Record<string, string | number | null | undefined>) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) query.set(key, String(value))
  })
  const value = query.toString()
  return value ? `?${value}` : ''
}
