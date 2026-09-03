export function formatCny(amount: number): string {
  // 金额统一显示为人民币格式，整元不保留小数位。
  const normalized = Number.isFinite(amount) ? amount : 0
  const rounded = Math.round(normalized * 100) / 100
  const text = Number.isInteger(rounded) ? rounded.toFixed(0) : rounded.toFixed(2)
  return `¥${text}`
}

export function formatOrderTime(value: string): string {
  // 把 ISO 时间转成页面里更易读的月日时分格式。
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
  // 号码脱敏，保留前 3 位和后 4 位。
  const digits = (phone || '').replace(/\D/g, '')
  if (digits.length < 7) {
    return phone
  }

  return `${digits.slice(0, 3)}****${digits.slice(-4)}`
}
