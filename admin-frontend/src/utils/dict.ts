export type Tone = 'positive' | 'warning' | 'danger' | 'neutral' | 'info'

export interface Meta {
  label: string
  tone: Tone
}

function meta<T extends Meta>(dict: Record<number, T>, fallback: T, code?: number | null): T {
  return code == null ? fallback : dict[code] ?? fallback
}

const GENDER: Record<number, Meta> = {
  0: { label: '保密', tone: 'neutral' },
  1: { label: '男', tone: 'info' },
  2: { label: '女', tone: 'info' },
}

const USER_ROLE: Record<number, Meta> = {
  0: { label: '顾客', tone: 'info' },
  1: { label: '商家', tone: 'warning' },
  2: { label: '管理员', tone: 'danger' },
}

const USER_STATUS: Record<number, Meta> = {
  0: { label: '正常', tone: 'positive' },
  [-1]: { label: '禁用', tone: 'danger' },
}

const BUSINESS_STATUS: Record<number, Meta> = {
  0: { label: '停业', tone: 'neutral' },
  1: { label: '营业中', tone: 'positive' },
  [-1]: { label: '已删除', tone: 'danger' },
}

const ORDER_STATUS: Record<number, Meta> = {
  [-1]: { label: '已取消', tone: 'danger' },
  0: { label: '待支付', tone: 'warning' },
  1: { label: '已支付', tone: 'info' },
  2: { label: '已完成', tone: 'positive' },
}

export const genderMeta = (code?: number | null) =>
  meta(GENDER, { label: '保密', tone: 'neutral' }, code)

export const userRoleMeta = (code?: number | null) =>
  meta(USER_ROLE, { label: '顾客', tone: 'info' }, code)

export const userStatusMeta = (code?: number | null) =>
  meta(USER_STATUS, { label: '正常', tone: 'positive' }, code)

export const businessStatusMeta = (code?: number | null) =>
  meta(BUSINESS_STATUS, { label: '停业', tone: 'neutral' }, code)

export const orderStatusMeta = (code?: number | null) =>
  meta(ORDER_STATUS, { label: '待支付', tone: 'warning' }, code)

export const GENDER_OPTIONS = [
  { value: 0, label: '保密' },
  { value: 1, label: '男' },
  { value: 2, label: '女' },
]

export const USER_ROLE_OPTIONS = [
  { value: 0, label: '顾客' },
  { value: 1, label: '商家' },
  { value: 2, label: '管理员' },
]

export const USER_STATUS_OPTIONS = [
  { value: 0, label: '正常' },
  { value: -1, label: '禁用' },
]

export const BUSINESS_STATUS_OPTIONS = [
  { value: 1, label: '营业中' },
  { value: 0, label: '停业' },
]
