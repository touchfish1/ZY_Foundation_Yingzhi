/**
 * Format an ISO timestamp string to local date/time display.
 * Input: "2026-06-04T12:00:00Z" (UTC ISO)
 * Output: "2026-06-04 20:00:00" (local time)
 */
export function formatDate(isoString: string | null | undefined): string {
  if (!isoString) return '-'
  try {
    const date = new Date(isoString)
    if (isNaN(date.getTime())) return isoString
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch {
    return isoString
  }
}

/**
 * Format a file size in bytes to a human-readable string.
 */
export function formatFileSize(bytes: number | null | undefined): string {
  if (bytes == null) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
