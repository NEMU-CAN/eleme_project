import type { BusinessStatus, FoodStatus, GenderType, OrderStatus } from '@/types'

export function formatCny(amount: number): string {
  const normalized = Number.isFinite(amount) ? amount : 0
  const rounded = Math.round(normalized * 100) / 100
  const text = Number.isInteger(rounded) ? rounded.toFixed(0) : rounded.toFixed(2)
  return `¥${text}`
}

export function formatOrderTime(value: string): string {
  const normalized = value.includes('T') ? value : value.replace(' ', 'T')
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) {
    return value || '时间待同步'
  }
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  return `${month}月${day}日 ${hours}:${minutes}`
}

export function maskPhone(phone: string): string {
  const digits = (phone || '').replace(/\D/g, '')
  if (digits.length < 7) {
    return phone
  }

  return `${digits.slice(0, 3)}****${digits.slice(-4)}`
}

export function formatGender(value: GenderType | number | null | undefined): string {
  if (value === 1) {
    return '男'
  }

  if (value === 2) {
    return '女'
  }

  return '保密'
}

export function formatUserRole(value: number | null | undefined): string {
  if (value === 1) {
    return '商家'
  }

  if (value === 2) {
    return '管理员'
  }

  return '普通用户'
}

export function formatOrderStatus(value: OrderStatus | 'pending' | null | undefined): string {
  if (value === 'canceled') {
    return '已取消'
  }

  if (value === 'unpaid' || value === 'pending') {
    return '待支付'
  }

  if (value === 'paid') {
    return '已支付'
  }

  if (value === 'completed') {
    return '已完成'
  }

  return '待支付'
}

export function formatBusinessStatus(value: BusinessStatus | null | undefined): string {
  if (value === 'open') {
    return '营业中'
  }

  if (value === 'closed') {
    return '已打烊'
  }

  return '已注销'
}

export function formatFoodStatus(value: FoodStatus | null | undefined): string {
  return value === 'online' ? '上架' : '下架'
}
