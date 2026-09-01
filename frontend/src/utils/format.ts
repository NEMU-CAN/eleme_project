export function formatCny(amount: number): string {
  // 金额统一显示为人民币格式，整元不保留小数位。
  const rounded = Math.round(amount * 100) / 100
  const text = Number.isInteger(rounded) ? rounded.toFixed(0) : rounded.toFixed(2)
  return `¥${text}`
}

export function formatDistance(distanceKm: number): string {
  // 距离用于商家列表和详情页展示，统一补上单位。
  return `${trimTrailingZeros(distanceKm)} km`
}

export function formatMinutes(minutes: number): string {
  // 配送时长直接展示为中文分钟。
  return `${minutes} 分钟`
}

export function formatOrderTime(value: string): string {
  // 把 ISO 时间转成页面里更易读的月日时分格式。
  const date = new Date(value)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  return `${month}月${day}日 ${hours}:${minutes}`
}

export function maskPhone(phone: string): string {
  // 号码脱敏，保留前 3 位和后 4 位。
  const digits = phone.replace(/\D/g, '')
  if (digits.length < 7) {
    return phone
  }

  return `${digits.slice(0, 3)}****${digits.slice(-4)}`
}

function trimTrailingZeros(value: number): string {
  // 去掉距离后多余的 0，让文案更自然。
  return value.toFixed(2).replace(/\.?0+$/, '')
}
