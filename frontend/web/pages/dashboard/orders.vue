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
const config = useRuntimeConfig()
const token = import.meta.client ? localStorage.getItem('saas_token') : null

onMounted(async () => {
  if (!auth.user.value) await auth.fetchProfile()
  try {
    const res = await $fetch(`${config.public.apiBase}/api/orders`, {
      params: { userId: auth.user.value?.id },
      headers: { Authorization: `Bearer ${token}` }
    }) as any
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
.table-card { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 16px; text-align: left; font-size: 14px; }
.data-table th { background: #f8fafc; font-weight: 600; color: #475569; font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; }
.data-table tr:not(:last-child) td { border-bottom: 1px solid #f1f5f9; }
.data-table tr:hover td { background: #f8fafc; }
.mono { font-family: monospace; font-size: 13px; }
.empty { text-align: center; padding: 40px; color: #94a3b8; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.badge-success { background: #dcfce7; color: #16a34a; }
.badge-warning { background: #fef3c7; color: #d97706; }
.badge-error { background: #fee2e2; color: #dc2626; }
.loading-state { text-align: center; padding: 60px; color: #94a3b8; }
</style>
