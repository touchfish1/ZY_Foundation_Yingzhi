<template>
  <div class="dashboard-layout">
    <aside class="dashboard-sidebar">
      <div class="sidebar-user">
        <div class="user-avatar">{{ user?.nickname?.[0] || 'U' }}</div>
        <div class="user-info">
          <div class="user-name">{{ user?.nickname }}</div>
          <div class="user-email">{{ user?.email }}</div>
        </div>
      </div>
      <nav class="sidebar-nav">
        <NuxtLink to="/dashboard" class="nav-item active">
          <span class="nav-icon">📊</span>
          <span class="nav-label">概览</span>
        </NuxtLink>
        <NuxtLink to="/dashboard/orders" class="nav-item">
          <span class="nav-icon">📋</span>
          <span class="nav-label">订单记录</span>
        </NuxtLink>
        <NuxtLink to="/dashboard/transactions" class="nav-item">
          <span class="nav-icon">💰</span>
          <span class="nav-label">交易记录</span>
        </NuxtLink>
        <NuxtLink to="/dashboard/keys" class="nav-item">
          <span class="nav-icon">🔑</span>
          <span class="nav-label">API Keys</span>
        </NuxtLink>
        <NuxtLink to="/dashboard/usage" class="nav-item">
          <span class="nav-icon">📈</span>
          <span class="nav-label">用量统计</span>
        </NuxtLink>
        <NuxtLink to="/settings" class="nav-item">
          <span class="nav-icon">⚙️</span>
          <span class="nav-label">设置</span>
        </NuxtLink>
      </nav>
      <div class="sidebar-footer">
        <button @click="auth.logout()" class="logout-btn">退出登录</button>
      </div>
    </aside>
    <main class="dashboard-main">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">API Key</div>
          <div class="stat-value code">{{ user?.apiKey?.slice(0, 20) }}...</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">账户余额</div>
          <div class="stat-value">¥{{ balance }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已用配额</div>
          <div class="stat-value">{{ user?.quotaUsed || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">配额上限</div>
          <div class="stat-value">{{ user?.quotaLimit || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">订阅状态</div>
          <div class="stat-value">
            <span v-if="loadingSubscription" class="loading-text">加载中...</span>
            <span v-else class="badge" :class="subscriptionStatusClass">
              {{ subscriptionStatus || '无订阅' }}
            </span>
          </div>
        </div>
      </div>

      <div class="section">
        <h2>最近订单</h2>
        <div class="table-card" v-if="recentOrders.length">
          <table class="data-table">
            <thead>
              <tr>
                <th>订单号</th>
                <th>金额</th>
                <th>状态</th>
                <th>时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in recentOrders" :key="order.orderNo">
                <td class="mono">{{ order.orderNo }}</td>
                <td>{{ order.amount }} {{ order.currency }}</td>
                <td>
                  <span class="badge" :class="orderStatusClass(order.status)">{{ order.status }}</span>
                </td>
                <td>{{ formatDate(order.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="empty-text">{{ loadingOrders ? '加载中...' : '暂无订单' }}</p>
      </div>

      <div class="section">
        <h2>订阅历史</h2>
        <div class="table-card" v-if="subscriptions.length">
          <table class="data-table">
            <thead>
              <tr>
                <th>套餐名称</th>
                <th>金额</th>
                <th>周期</th>
                <th>开始时间</th>
                <th>结束时间</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="sub in subscriptions" :key="sub.id">
                <td>{{ sub.planName || sub.planId }}</td>
                <td>{{ sub.amount }} {{ sub.currency }}</td>
                <td>{{ sub.period || '-' }}</td>
                <td>{{ formatDate(sub.startDate) }}</td>
                <td>{{ formatDate(sub.endDate) }}</td>
                <td>
                  <span class="badge" :class="subStatusClass(sub.status)">{{ sub.status }}</span>
                </td>
              </tr>
              <tr v-if="!subscriptions.length">
                <td colspan="6" class="empty-text">暂无订阅记录</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="empty-text">{{ loadingSubscriptions ? '加载中...' : '暂无订阅记录' }}</p>
      </div>

      <div class="section">
        <h2>快速开始</h2>
        <div class="quickstart-card">
          <p>使用你的 API Key 调用接口：</p>
          <pre class="code-block">curl -H "Authorization: Bearer {{ user?.apiKey }}" https://api.example.com/v1/chat</pre>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const auth = useSaasAuth()
const { user } = auth
const config = useRuntimeConfig()
const userId = computed(() => auth.user.value?.id)

const balance = ref(0)
const usageSummary = ref<any>(null)

const subscriptionStatus = ref('')
const loadingSubscription = ref(true)
const recentOrders = ref<any[]>([])
const loadingOrders = ref(true)
const subscriptions = ref<any[]>([])
const loadingSubscriptions = ref(true)

const subscriptionStatusClass = computed(() => {
  const map: Record<string, string> = { active: 'badge-success', expired: 'badge-error', pending: 'badge-warning' }
  return map[subscriptionStatus.value?.toLowerCase()] || 'badge-warning'
})

function subStatusClass(s: string) {
  const map: Record<string, string> = { ACTIVE: 'badge-success', EXPIRED: 'badge-error', CANCELLED: 'badge-error', PENDING: 'badge-warning' }
  return map[s?.toUpperCase()] || ''
}

async function fetchBalance() {
  if (!userId.value) return
  try {
    const res = await $fetch(`${config.public.apiBase}/api/balance/${userId.value}`) as any
    balance.value = res.balance || 0
  } catch (e) { console.error('Failed to load balance', e) }
}

async function fetchUsage() {
  if (!userId.value) return
  try {
    const res = await auth.authFetch<any>(`/api/usage/${userId.value}/summary`)
    usageSummary.value = res?.data
  } catch (e) { /* usage may not have data yet */ }
}

async function fetchSubscription() {
  if (user.value?.id) {
    try {
      const subRes = await auth.authFetch<any>('/api/subscriptions/active', {
        params: { userId: user.value.id }
      })
      subscriptionStatus.value = subRes?.data?.status || subRes?.status || 'none'
    } catch {
      subscriptionStatus.value = 'none'
    } finally { loadingSubscription.value = false }
  } else {
    loadingSubscription.value = false
  }
}

async function fetchSubscriptions() {
  if (user.value?.id) {
    try {
      const res = await auth.authFetch<any>('/api/subscriptions', {
        params: { userId: user.value.id }
      })
      subscriptions.value = res?.data?.items || res?.data || []
    } catch {
      // silently fail
    } finally { loadingSubscriptions.value = false }
  } else {
    loadingSubscriptions.value = false
  }
}

async function fetchOrders() {
  if (user.value?.id) {
    try {
      const orderRes = await auth.authFetch<any>('/api/orders', {
        params: { userId: user.value.id }
      })
      recentOrders.value = (orderRes?.data?.items || orderRes?.data || []).slice(0, 5)
    } catch {
      // silently fail
    } finally { loadingOrders.value = false }
  } else {
    loadingOrders.value = false
  }
}

onMounted(async () => {
  if (!user.value) await auth.fetchProfile()
  await Promise.all([
    fetchBalance(),
    fetchSubscription(),
    fetchOrders(),
    fetchUsage(),
    fetchSubscriptions()
  ])
})

function orderStatusClass(s: string) {
  const map: Record<string, string> = { PENDING: 'badge-warning', PAID: 'badge-success', CANCELLED: 'badge-error' }
  return map[s] || ''
}

function formatDate(ts: string) {
  if (!ts) return '-'
  return new Date(ts).toLocaleString('zh-CN')
}
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}
.dashboard-sidebar {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  padding: 24px 0;
  flex-shrink: 0;
}
.sidebar-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 16px;
}
.user-avatar {
  width: 40px; height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
}
.user-info { flex: 1; min-width: 0; }
.user-name { font-size: 14px; font-weight: 600; }
.user-email { font-size: 12px; color: #64748b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sidebar-nav { flex: 1; }
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  color: #475569;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.15s;
  border-left: 3px solid transparent;
}
.nav-item:hover { background: #f1f5f9; color: #1e293b; }
.nav-item.active { background: #eef2ff; color: #6366f1; border-left-color: #6366f1; font-weight: 600; }
.nav-icon { font-size: 16px; }
.nav-label { flex: 1; }
.sidebar-footer { padding: 16px 20px 0; border-top: 1px solid #e2e8f0; margin-top: 16px; }
.logout-btn {
  width: 100%; padding: 8px; border: 1px solid #e2e8f0; border-radius: 8px;
  background: #fff; cursor: pointer; font-size: 13px; color: #64748b;
  transition: all 0.15s;
}
.logout-btn:hover { background: #fef2f2; color: #ef4444; border-color: #fecaca; }
.dashboard-main { flex: 1; padding: 32px; overflow-y: auto; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 32px; }
.stat-card {
  background: #fff; border-radius: 12px; padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.stat-label { font-size: 12px; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 8px; }
.stat-value { font-size: 20px; font-weight: 700; color: #1e293b; }
.stat-value.code { font-size: 13px; font-family: monospace; color: #6366f1; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.badge-success { background: #dcfce7; color: #16a34a; }
.badge-warning { background: #fef3c7; color: #d97706; }
.badge-error { background: #fee2e2; color: #dc2626; }
.section { margin-bottom: 32px; }
.section h2 { font-size: 18px; font-weight: 600; margin: 0 0 16px; }
.table-card { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 16px; text-align: left; font-size: 14px; }
.data-table th { background: #f8fafc; font-weight: 600; color: #475569; font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; }
.data-table tr:not(:last-child) td { border-bottom: 1px solid #f1f5f9; }
.data-table tr:hover td { background: #f8fafc; }
.mono { font-family: monospace; font-size: 13px; }
.empty-text { text-align: center; padding: 24px; color: #94a3b8; font-size: 14px; }
.loading-text { font-size: 13px; color: #94a3b8; }
.quickstart-card { background: #1e293b; border-radius: 12px; padding: 24px; color: #e2e8f0; }
.quickstart-card p { margin: 0 0 12px; font-size: 14px; }
.code-block { background: #0f172a; padding: 16px; border-radius: 8px; font-size: 13px; overflow-x: auto; }
</style>
