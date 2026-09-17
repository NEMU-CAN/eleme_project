import { ApiError } from '@/services/api'

export function money(value?: number | null): string {
  const n = Number(value ?? 0)
  return `¥${n.toFixed(2)}`
}

export function formatDate(value?: string | null): string {
  return value || '—'
}

export function avatarFallback(name?: string | null): string {
  const s = (name || 'U').trim()
  return s.charAt(0).toUpperCase() || 'U'
}

/**
 * 从后端异常中提取可供用户阅读的提示。
 * 后端校验失败时会返回 `data` 为 FieldErrorVO[]，这里拼接成一句话。
 */
export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    const details = error.details
    if (Array.isArray(details) && details.length > 0) {
      const messages = details
        .map((d) => (d && typeof d === 'object' && 'message' in d ? String(d.message) : ''))
        .filter(Boolean)
      if (messages.length) return messages.join('；')
    }
    return error.message
  }
  return error instanceof Error ? error.message : '操作失败，请稍后重试'
}
