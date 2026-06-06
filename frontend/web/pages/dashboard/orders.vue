<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h2>订单记录</h2>
    </div>
    <div class="table-card" v-if="!loading">
      <table class="data-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>金额</th>
            <th>状态</th>
            <th>创建时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.orderNo">
            <td class="mono">{{ order.orderNo }}</td>
            <td>{{ order.amount }} {{ order.currency }}</td>
            <td>
              <span class="badge" :class="statusClass(order.status)">{{ order.status }}</span>
            </td>
            <td>{{ formatDate(order.createdAt) }}</td>
          </tr>
          <tr v-if="!orders.length">
            <td colspan="4" class="empty">暂无订单</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else class="loading-state">加载中...</div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const auth = useSaasAuth()
const orders = ref<any[]>([])
const loading = ref(true)
onMounted(async () => {
  if (!auth.user.value) await auth.fetchProfile()
  try {
    const res = await auth.authFetch<any>('/api/orders', {
      params: { userId: auth.user.value?.id }
    })
    orders.value = res?.data?.items || res?.data || []
  } catch (e) {
    console.error('Failed to load orders', e)
  } finally { loading.value = false }
})

function statusClass(s: string) {
  const map: Record<string, string> = { PENDING: 'badge-warning', PAID: 'badge-success', CANCELLED: 'badge-error' }
  return map[s] || ''
}

function formatDate(ts: string) {
  if (!ts) return '-'
  return new Date(ts).toLocaleString('zh-CN')
}
</script>

<style scoped>
.dashboard-page { padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 20px; font-weight: 700; margin: 0; }
.table-card { background: var(--vp-c-bg); border-radius: 12px; box-shadow: var(--vp-shadow-1); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 16px; text-align: left; font-size: 14px; }
.data-table th { background: var(--vp-c-bg-soft); font-weight: 600; color: var(--vp-c-text-2); font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; }
.data-table tr:not(:last-child) td { border-bottom: 1px solid var(--vp-c-bg-mute); }
.data-table tr:hover td { background: var(--vp-c-bg-soft); }
.mono { font-family: var(--vp-font-family-mono); font-size: 13px; }
.empty { text-align: center; padding: 40px; color: var(--vp-c-text-3); }
.badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.badge-success { background: var(--vp-c-success-soft); color: var(--vp-c-success); }
.badge-warning { background: var(--vp-c-warning-soft); color: var(--vp-c-warning); }
.badge-error { background: var(--vp-c-danger-soft); color: var(--vp-c-danger); }
.loading-state { text-align: center; padding: 60px; color: var(--vp-c-text-3); }
</style>
