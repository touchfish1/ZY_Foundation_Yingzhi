export interface AuditLog {
  id: number
  userId: number
  username: string
  action: string
  detail: string
  ipAddress: string
  createdAt: string
}

export interface MonitorStats {
  jvm: { maxMemory: number; totalMemory: number; freeMemory: number; usedMemory: number; usagePercent: number }
  system: { osName: string; osArch: string; javaVersion: string; availableProcessors: number }
  uptime: string
  startTime: string
  settings: { totalCount: number }
}
