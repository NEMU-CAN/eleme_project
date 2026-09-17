// 营业时间：数据库与后端未提供该字段，按需求在前端以 localStorage 兜底实现。
// 商家入驻时填写并保存；列表/详情展示时读取，无记录则回退到默认营业时间。

export interface BusinessHours {
  open: string
  close: string
}

const STORAGE_KEY = 'tju-hungry-business-hours-v1'
const DEFAULT_HOURS: BusinessHours = { open: '09:00', close: '22:00' }

function readMap(): Record<string, BusinessHours> {
  if (typeof window === 'undefined') {
    return {}
  }
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as Record<string, BusinessHours>) : {}
  } catch {
    return {}
  }
}

export function getBusinessHours(businessId: string | number): BusinessHours {
  return readMap()[String(businessId)] ?? DEFAULT_HOURS
}

export function saveBusinessHours(businessId: string | number, hours: BusinessHours): void {
  if (typeof window === 'undefined') {
    return
  }
  try {
    const map = readMap()
    map[String(businessId)] = hours
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(map))
  } catch {
    // 本地存储不可写时忽略，展示默认营业时间即可。
  }
}

export function formatBusinessHours(hours: BusinessHours): string {
  return `${hours.open}-${hours.close}`
}
