import { reactive } from 'vue'

export type ToastType = 'success' | 'error' | 'info'

export interface ToastItem {
  id: number
  type: ToastType
  message: string
}

let seq = 0

export const toasts = reactive<ToastItem[]>([])

export function toast(message: string, type: ToastType = 'info') {
  const id = ++seq
  toasts.push({ id, type, message })
  setTimeout(() => {
    const index = toasts.findIndex((t) => t.id === id)
    if (index >= 0) toasts.splice(index, 1)
  }, 3200)
}
