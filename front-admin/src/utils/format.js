import dayjs from 'dayjs'

// 格式化日期时间
export function formatDateTime(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化日期
export function formatDate(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

// 格式化时间
export function formatTime(date) {
  if (!date) return ''
  return dayjs(date).format('HH:mm:ss')
}

// 格式化金额
export function formatMoney(amount) {
  if (!amount && amount !== 0) return ''
  return Number(amount).toFixed(2)
}

// 格式化百分比
export function formatPercent(value) {
  if (!value && value !== 0) return ''
  return `${(value * 100).toFixed(2)}%`
}

// 格式化文件大小
export function formatFileSize(bytes) {
  if (!bytes && bytes !== 0) return ''
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(2)} ${units[unitIndex]}`
}
