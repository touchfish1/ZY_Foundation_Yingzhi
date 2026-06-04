<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h2>用量统计</h2>
    </div>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">已用配额</div>
        <div class="stat-value">{{ user?.quotaUsed || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">配额上限</div>
        <div class="stat-value">{{ user?.quotaLimit || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">使用率</div>
        <div class="stat-value">{{ usagePercent }}%</div>
      </div>
    </div>
    <div class="progress-section">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: Math.min(usagePercent, 100) + '%' }"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const auth = useSaasAuth()
const { user } = auth

const usagePercent = computed(() => {
  if (!user.value || !user.value.quotaLimit) return 0
  return Math.round((user.value.quotaUsed / user.value.quotaLimit) * 100)
})
</script>

<style scoped>
.dashboard-page { padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 20px; font-weight: 700; margin: 0; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 32px; }
.stat-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.stat-label { font-size: 12px; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: 700; color: #1e293b; }
.progress-section { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.progress-bar { height: 12px; background: #f1f5f9; border-radius: 999px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #6366f1, #8b5cf6); border-radius: 999px; transition: width 0.5s ease; }
</style>
