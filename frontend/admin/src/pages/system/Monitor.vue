<template>
  <div class="monitor">
    <div class="page-head fade-in-up">
      <div class="page-head-inner">
        <div>
          <h2>系统监控</h2>
          <p>查看系统运行状态和统计信息</p>
        </div>
        <div class="page-head-actions">
          <n-button quaternary @click="loadStats" :loading="loading" size="small">
            <template #icon>
              <n-icon size="15">
                <RefreshIcon />
              </n-icon>
            </template>
            刷新
          </n-button>
        </div>
      </div>
    </div>

    <n-spin :show="loading && !hasData">
      <template v-if="hasData">
        <n-grid :cols="3" :x-gap="20" :y-gap="20" class="fade-in-up">
          <n-grid-item>
            <n-card class="section-card" :segmented="{ content: true }">
              <template #header><span class="section-title">JVM 内存</span></template>
              <div class="info-list">
                <div class="info-row">
                  <span class="info-label">最大内存</span>
                  <span class="info-mono">{{ formatBytes(stats.jvm.maxMemory) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">已分配内存</span>
                  <span class="info-mono">{{ formatBytes(stats.jvm.totalMemory) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">空闲内存</span>
                  <span class="info-mono">{{ formatBytes(stats.jvm.freeMemory) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">已用内存</span>
                  <span class="info-mono">{{ formatBytes(stats.jvm.usedMemory) }}</span>
                </div>
              </div>
              <div class="progress-section">
                <div class="progress-label">
                  <span>内存使用率</span>
                  <span class="info-mono">{{ stats.jvm.usagePercent.toFixed(1) }}%</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="stats.jvm.usagePercent"
                  :color="progressColor(stats.jvm.usagePercent)"
                  :height="8"
                  :border-radius="4"
                />
              </div>
            </n-card>
          </n-grid-item>

          <n-grid-item>
            <n-card class="section-card" :segmented="{ content: true }">
              <template #header><span class="section-title">系统信息</span></template>
              <div class="info-list">
                <div class="info-row">
                  <span class="info-label">操作系统</span>
                  <span class="info-mono">{{ stats.system.osName }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">系统架构</span>
                  <span class="info-mono">{{ stats.system.osArch }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">Java 版本</span>
                  <span class="info-mono">{{ stats.system.javaVersion }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">CPU 核心数</span>
                  <span class="info-mono">{{ stats.system.availableProcessors }}</span>
                </div>
              </div>
            </n-card>
          </n-grid-item>

          <n-grid-item>
            <n-card class="section-card" :segmented="{ content: true }">
              <template #header><span class="section-title">运行时间</span></template>
              <div class="info-list">
                <div class="info-row">
                  <span class="info-label">已运行时长</span>
                  <span class="info-mono">{{ stats.uptime }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">启动时间</span>
                  <span class="info-mono">{{ stats.startTime }}</span>
                </div>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>

        <n-grid :cols="1" :x-gap="20" :y-gap="20" style="margin-top: 20px" class="fade-in-up fade-in-up-d2">
          <n-grid-item>
            <n-card class="section-card" :segmented="{ content: true }">
              <template #header><span class="section-title">系统设置</span></template>
              <div class="info-list">
                <div class="info-row">
                  <span class="info-label">配置项总数</span>
                  <span class="info-mono">{{ stats.settings.totalCount }}</span>
                </div>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
      </template>

      <n-empty v-else-if="!loading" description="暂无监控数据" class="fade-in-up" />
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, onUnmounted, reactive, ref } from 'vue'
import { NButton, NCard, NEmpty, NGrid, NGridItem, NIcon, NProgress, NSpin, useMessage } from 'naive-ui'
import { getMonitorStats } from '../../api/system'

const RefreshIcon = () => h('svg', { xmlns: 'http://www.w3.org/2000/svg', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
  h('polyline', { points: '23 4 23 10 17 10' }),
  h('polyline', { points: '1 20 1 14 7 14' }),
  h('path', { d: 'M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15' })
])

const message = useMessage()
const loading = ref(false)
const hasData = ref(false)

const stats = reactive({
  jvm: { maxMemory: 0, totalMemory: 0, freeMemory: 0, usedMemory: 0, usagePercent: 0 },
  system: { osName: '', osArch: '', javaVersion: '', availableProcessors: 0 },
  uptime: '',
  startTime: '',
  settings: { totalCount: 0 }
})

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 MB'
  const mb = bytes / (1024 * 1024)
  return mb >= 1024 ? `${(mb / 1024).toFixed(1)} GB` : `${mb.toFixed(0)} MB`
}

function progressColor(percent: number): string {
  if (percent > 85) return '#ef4444'
  if (percent > 60) return '#f59e0b'
  return '#10b981'
}

async function loadStats() {
  loading.value = true
  try {
    const data = await getMonitorStats()
    Object.assign(stats, data)
    hasData.value = true
  } catch (err: any) {
    message.error(err.message || '加载失败')
  } finally {
    loading.value = false
  }
}

let timer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  loadStats()
  timer = setInterval(() => {
    getMonitorStats()
      .then(data => Object.assign(stats, data))
      .catch(() => {})
  }, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.monitor {
  max-width: 1200px;
}

.section-card {
  border-radius: 14px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 13px;
  border-bottom: 1px solid #f1f5f9;
}

html.dark .info-row {
  border-bottom-color: #1e293b;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: #64748b;
}

html.dark .info-label {
  color: #94a3b8;
}

.info-mono {
  font-family: ui-monospace, monospace;
  font-size: 12px;
  color: #334155;
}

html.dark .info-mono {
  color: #cbd5e1;
}

.progress-section {
  padding: 16px 0 8px;
  border-top: 1px solid #f1f5f9;
}

html.dark .progress-section {
  border-top-color: #1e293b;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
  color: #64748b;
}

html.dark .progress-label {
  color: #94a3b8;
}
</style>
