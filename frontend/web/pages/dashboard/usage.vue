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

    <div class="section">
      <h2>使用明细</h2>
      <div class="filter-bar">
        <label>
          开始日期
          <input type="date" v-model="startDate" class="date-input" />
        </label>
        <label>
          结束日期
          <input type="date" v-model="endDate" class="date-input" />
        </label>
        <button class="btn-filter" @click="fetchUsageDetail">查询</button>
      </div>
      <div class="table-card" v-if="!loadingDetail">
        <table class="data-table">
          <thead>
            <tr>
              <th>日期</th>
              <th>调用次数</th>
              <th>消耗配额</th>
              <th>模型</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in usageRecords" :key="record.id">
              <td>{{ formatDate(record.date || record.createdAt) }}</td>
              <td>{{ record.callCount || record.count || 0 }}</td>
              <td>{{ record.quotaUsed || 0 }}</td>
              <td class="mono">{{ record.model || '-' }}</td>
            </tr>
            <tr v-if="!usageRecords.length">
              <td colspan="4" class="empty">暂无使用记录</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="loading-state">加载中...</div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const auth = useSaasAuth()
const { user } = auth
const config = useRuntimeConfig()
const token = import.meta.client ? localStorage.getItem('saas_token') : null
const userId = computed(() => auth.user.value?.id)

const startDate = ref('')
const endDate = ref('')
const usageRecords = ref<any[]>([])
const loadingDetail = ref(false)

const usagePercent = computed(() => {
  if (!user.value || !user.value.quotaLimit) return 0
  return Math.round((user.value.quotaUsed / user.value.quotaLimit) * 100)
})

async function fetchUsageDetail() {
  if (!userId.value) return
  loadingDetail.value = true
  try {
    const params: Record<string, any> = { page: 1, pageSize: 20 }
    if (startDate.value) params.startDate = startDate.value
    if (endDate.value) params.endDate = endDate.value
    const res = await $fetch(`${config.public.apiBase}/api/usage/${userId.value}`, {
      params,
      headers: { Authorization: `Bearer ${token}` }
    }) as any
    usageRecords.value = res?.data?.items || res?.data || []
  } catch (e) {
    console.error('Failed to load usage details', e)
  } finally { loadingDetail.value = false }
}

onMounted(() => {
  fetchUsageDetail()
})

function formatDate(ts: string) {
  if (!ts) return '-'
  return new Date(ts).toLocaleString('zh-CN')
}
</script>

<style scoped>
.dashboard-page { padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 20px; font-weight: 700; margin: 0; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 32px; }
.stat-card { background: var(--vp-c-bg); border-radius: 12px; padding: 20px; box-shadow: var(--vp-shadow-1); }
.stat-label { font-size: 12px; color: var(--vp-c-text-3); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--vp-c-text); }
.progress-section { background: var(--vp-c-bg); border-radius: 12px; padding: 24px; box-shadow: var(--vp-shadow-1); margin-bottom: 32px; }
.progress-bar { height: 12px; background: var(--vp-c-bg-mute); border-radius: 999px; overflow: hidden; }
.progress-fill { height: 100%; background: var(--vp-c-brand-gradient); border-radius: 999px; transition: width 0.5s ease; }
.section { margin-bottom: 32px; }
.section h2 { font-size: 18px; font-weight: 600; margin: 0 0 16px; }
.filter-bar { display: flex; gap: 12px; align-items: flex-end; margin-bottom: 16px; }
.filter-bar label { font-size: 13px; color: var(--vp-c-text-2); display: flex; flex-direction: column; gap: 4px; }
.date-input { padding: 6px 10px; border: 1px solid var(--vp-c-border); border-radius: 6px; font-size: 13px; }
.btn-filter { padding: 6px 16px; border-radius: 6px; border: 1px solid var(--vp-c-brand); background: var(--vp-c-brand); color: var(--vp-c-bg); cursor: pointer; font-size: 13px; }
.btn-filter:hover { background: var(--vp-c-brand-dark); }
.table-card { background: var(--vp-c-bg); border-radius: 12px; box-shadow: var(--vp-shadow-1); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 16px; text-align: left; font-size: 14px; }
.data-table th { background: var(--vp-c-bg-soft); font-weight: 600; color: var(--vp-c-text-2); font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; }
.data-table tr:not(:last-child) td { border-bottom: 1px solid var(--vp-c-bg-mute); }
.data-table tr:hover td { background: var(--vp-c-bg-soft); }
.mono { font-family: var(--vp-font-family-mono); font-size: 13px; }
.empty { text-align: center; padding: 40px; color: var(--vp-c-text-3); }
.loading-state { text-align: center; padding: 60px; color: var(--vp-c-text-3); }
</style>
