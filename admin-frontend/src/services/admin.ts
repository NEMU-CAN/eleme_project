import { api, queryString } from './api'
import type {
  Business,
  BusinessDetail,
  Order,
  OrderDetail,
  OrderSummary,
  PageResult,
  Taste,
  User,
} from '@/types'

// ============================================================
// 用户（管理员）
// ============================================================

export type AdminUserFilters = {
  keyword?: string
  role?: number | null
  status?: number | null
}

export type AdminUserPayload = {
  nickname?: string
  phone?: string
  avatar?: string
  gender?: number
  role?: number
  status?: number
}

export function listUsers(filters: AdminUserFilters = {}) {
  return api<User[]>(`/admin/users${queryString(filters)}`)
}

export function updateUser(id: number, payload: AdminUserPayload) {
  return api<User>(`/admin/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

// ============================================================
// 商家
// ============================================================

export type BusinessFilters = {
  tasteId?: number | null
  status?: number | null
  keyword?: string
}

export type BusinessPayload = {
  name: string
  address: string
  description?: string | null
  image?: string | null
  tasteId: number
  startPrice?: number
  deliveryPrice?: number
  status?: number
}

export function listBusinesses(filters: BusinessFilters = {}) {
  return api<Business[]>(`/businesses${queryString(filters)}`)
}

export function getBusiness(id: number) {
  return api<BusinessDetail>(`/businesses/${id}`)
}

export function createBusiness(payload: BusinessPayload) {
  return api<BusinessDetail>('/businesses', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateBusiness(id: number, payload: BusinessPayload) {
  return api<BusinessDetail>(`/businesses/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function setBusinessStatus(id: number, status: number) {
  return api<null>(`/businesses/${id}/status`, {
    method: 'PUT',
    body: JSON.stringify({ status }),
  })
}

// ============================================================
// 口味
// ============================================================

export function listTastes() {
  return api<Taste[]>('/tastes')
}

// ============================================================
// 订单（只读）
// ============================================================

export type OrderFilters = {
  businessId?: number | null
  order_status?: number | null
  page?: number
  page_size?: number
}

export function listOrders(filters: OrderFilters = {}) {
  return api<PageResult<OrderSummary>>(`/orders${queryString(filters)}`)
}

export function getOrder(id: number) {
  return api<OrderDetail>(`/orders/${id}`)
}
