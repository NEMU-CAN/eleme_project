// 用户输入校验规则：与「修改需求01」保持一致，纯前端校验，不依赖后端。

// 中国手机号：11 位，1 开头。
export function isValidPhone(phone: string): boolean {
  return /^1\d{10}$/.test((phone ?? '').trim())
}

// 密码：至少 6 位、最多 40 位；仅支持数字、大小写英文字母、以及「除下划线以外」的标准特殊字符。
const PASSWORD_PATTERN = /^[A-Za-z0-9!@#$%^&*()\-+=[\]{}|;:'",.<>?/~`\\]+$/

export function isValidPassword(password: string): boolean {
  const value = password ?? ''
  if (value.length < 6 || value.length > 40) {
    return false
  }
  return PASSWORD_PATTERN.test(value)
}

export function phoneError(phone: string): string {
  if (!phone.trim()) {
    return '请输入手机号'
  }
  if (!isValidPhone(phone)) {
    return '请输入 11 位有效手机号（1 开头）'
  }
  return ''
}

export function passwordError(password: string): string {
  if (!password) {
    return '请输入密码'
  }
  if (password.length < 6 || password.length > 40) {
    return '密码长度需为 6~40 位'
  }
  if (!PASSWORD_PATTERN.test(password)) {
    return '密码仅支持数字、大小写字母及除下划线外的特殊字符'
  }
  return ''
}
