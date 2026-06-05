<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h2>交易记录</h2>
    </div>
    <div class="table-card" v-if="!loading">
      <table class="data-table">
        <thead>
          <tr>
            <th>交易号</th>
            <th>类型</th>
            <th>金额</th>
            <th>余额</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tx in transactions" :key="tx.id">
            <td class="mono">{{ tx.description || tx.id }}</td>
            <td>
              <span class="badge" :class="typeClass(tx.transactionType)">{{ typeLabel(tx.transactionType) }}</span>
            </td>
            <td :class="amountClass(tx.transactionType)">{{ tx.transactionType === 'consume' ? '-' : '+' }}¥{{ tx.amount }}</td>
            <td class="mono">¥{{ tx.balanceAfter }}</td>
            <td>{{ formatDate(tx.createdAt) }}</td>
          </tr>
          <tr v-if="!transactions.length">
            <td colspan="5" class="empty">暂无交易记录</td>
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
const config = useRuntimeConfig()
const token = import.meta.client ? localStorage.getItem('saas_token') : null
const userId = computed(() => auth.user.value?.id)

const transactions = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  if (!auth.user.value) await auth.fetchProfile()
  try {
    const res = await $fetch(`${config.public.apiBase}/api/balance/${userId.value}/transactions`, {
      headers: { Authorization: `Bearer ${token}` }
    }) as any
    transactions.value = res?.data || []
  } catch (e) {
    console.error('Failed to load transactions', e)
  } finally { loading.value = false }
})

function typeClass(type: string) {
  const map: Record<string, string> = { recharge: 'badge-success', consume: 'badge-error', refund: 'badge-warning' }
  return map[type] || ''
}

function typeLabel(type: string) {
  const map: Record<string, string> = { recharge: '充值', consume: '消费', refund: '退款' }
  return map[type] || type
}

function amountClass(type: string) {
  return type === 'consume' ? 'amount-negative' : 'amount-positive'
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
.amount-positive { color: var(--vp-c-success); }
.amount-negative { color: var(--vp-c-danger); }
</style>
