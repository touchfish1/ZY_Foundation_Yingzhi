<template>
  <div v-if="pageLoading" class="page-loading">
    <div class="loading-spinner" />
    <p>加载中...</p>
  </div>
  <div v-else class="dashboard-layout">
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
        <NuxtLink to="/dashboard/subscription" class="stat-card-link stat-card">
          <div class="stat-label">
            订阅状态
            <span class="manage-link">管理</span>
          </div>
          <div class="stat-value">
            <span v-if="loadingSubscription" class="loading-text">加载中...</span>
            <span v-else class="badge" :class="subscriptionStatusClass">
              {{ activeSub?.planName || subscriptionStatus || '无订阅' }}
            </span>
          </div>
        </NuxtLink>
      </div>

      <!-- Usage Progress Bar -->
      <div class="usage-bar-section" v-if="user?.quotaLimit">
        <div class="usage-bar-header">
          <span class="usage-bar-label">本月用量</span>
          <span class="usage-bar-text">{{ user?.quotaUsed || 0 }} / {{ user?.quotaLimit }}</span>
        </div>
        <div class="usage-bar-track">
          <div class="usage-bar-fill" :style="{ width: usagePercent + '%' }" :class="usageBarClass" />
        </div>
      </div>

      <div class="section">
        <div class="section-header">
          <h2>最近订单</h2>
          <NuxtLink to="/dashboard/orders" class="section-link">查看全部</NuxtLink>
        </div>
        <div class="table-card" v-if="!loadingOrders && recentOrders.length">
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
        <div v-else-if="!loadingOrders && !recentOrders.length" class="empty-state-card">
          <div class="empty-icon">📋</div>
          <p class="empty-text">暂无订单记录</p>
          <NuxtLink to="/pricing" class="btn-primary btn-sm">去订阅套餐</NuxtLink>
        </div>
        <p v-else class="loading-hint">{{ loadingOrders ? '加载中...' : '' }}</p>
      </div>

      <div class="section">
        <div class="section-header">
          <h2>订阅历史</h2>
          <NuxtLink to="/dashboard/subscription" class="section-link" v-if="hasActiveSubscription">管理订阅</NuxtLink>
        </div>
        <div class="table-card" v-if="!loadingSubscriptions && subscriptions.length">
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
            </tbody>
          </table>
        </div>
        <div v-else-if="!loadingSubscriptions && !subscriptions.length" class="empty-state-card">
          <div class="empty-icon">📄</div>
          <p class="empty-text">暂无订阅记录</p>
          <NuxtLink to="/pricing" class="btn-primary btn-sm">去订阅套餐</NuxtLink>
        </div>
        <p v-else class="loading-hint">{{ loadingSubscriptions ? '加载中...' : '' }}</p>
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

const pageLoading = ref(true)
const activeSub = ref<any>(null)
const subscriptionStatus = ref('')
const loadingSubscription = ref(true)
const recentOrders = ref<any[]>([])
const loadingOrders = ref(true)
const subscriptions = ref<any[]>([])
const loadingSubscriptions = ref(true)

const hasActiveSubscription = computed(() => {
  return activeSub.value?.status === 'ACTIVE' || activeSub.value?.status === 'active'
})

const usagePercent = computed(() => {
  const limit = user.value?.quotaLimit || 0
  const used = user.value?.quotaUsed || 0
  if (!limit) return 0
  return Math.min(100, Math.round((used / limit) * 100))
})

const usageBarClass = computed(() => {
  if (usagePercent.value >= 90) return 'usage-danger'
  if (usagePercent.value >= 70) return 'usage-warning'
  return ''
})

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
    const res = await auth.authFetch<any>(`/api/balance/${userId.value}`) as any
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
      const data = subRes?.data || subRes
      if (data && data.status && data.status !== 'none') {
        activeSub.value = data
        subscriptionStatus.value = data.status
      } else {
        subscriptionStatus.value = 'none'
      }
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
  pageLoading.value = false
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
  background: var(--vp-c-bg-soft);
}
.dashboard-sidebar {
  width: 240px;
  background: var(--vp-c-bg);
  border-right: 1px solid var(--vp-c-border);
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
  border-bottom: 1px solid var(--vp-c-border);
  margin-bottom: 16px;
}
.user-avatar {
  width: 40px; height: 40px;
  border-radius: 10px;
  background: var(--vp-c-brand-gradient);
  color: var(--vp-c-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
}
.user-info { flex: 1; min-width: 0; }
.user-name { font-size: 14px; font-weight: 600; }
.user-email { font-size: 12px; color: var(--vp-c-text-3); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sidebar-nav { flex: 1; }
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  color: var(--vp-c-text-2);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.15s;
  border-left: 3px solid transparent;
}
.nav-item:hover { background: var(--vp-c-bg-mute); color: var(--vp-c-text); }
.nav-item.active { background: var(--vp-c-brand-dimmer); color: var(--vp-c-brand); border-left-color: var(--vp-c-brand); font-weight: 600; }
.nav-icon { font-size: 16px; }
.nav-label { flex: 1; }
.sidebar-footer { padding: 16px 20px 0; border-top: 1px solid var(--vp-c-border); margin-top: 16px; }
.logout-btn {
  width: 100%; padding: 8px; border: 1px solid var(--vp-c-border); border-radius: 8px;
  background: var(--vp-c-bg); cursor: pointer; font-size: 13px; color: var(--vp-c-text-3);
  transition: all 0.15s;
}
.logout-btn:hover { background: var(--vp-c-danger-soft); color: var(--vp-c-danger); border-color: var(--vp-c-danger-soft); }
.dashboard-main { flex: 1; padding: 32px; overflow-y: auto; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 32px; }
.stat-card {
  background: var(--vp-c-bg); border-radius: 12px; padding: 20px;
  box-shadow: var(--vp-shadow-1);
}
.stat-card-link {
  display: block;
  text-decoration: none;
  color: inherit;
  transition: all 0.15s;
  cursor: pointer;
}
.stat-card-link:hover {
  box-shadow: var(--vp-shadow-3);
  transform: translateY(-1px);
}
.stat-label { font-size: 12px; color: var(--vp-c-text-3); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
.manage-link { font-size: 11px; font-weight: 600; color: var(--vp-c-brand); text-transform: none; letter-spacing: 0; }
.stat-value { font-size: 20px; font-weight: 700; color: var(--vp-c-text); }
.stat-value.code { font-size: 13px; font-family: var(--vp-font-family-mono); color: var(--vp-c-brand); }
.badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.badge-success { background: var(--vp-c-success-soft); color: var(--vp-c-success); }
.badge-warning { background: var(--vp-c-warning-soft); color: var(--vp-c-warning); }
.badge-error { background: var(--vp-c-danger-soft); color: var(--vp-c-danger); }
.section { margin-bottom: 32px; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.section-header h2 { font-size: 18px; font-weight: 600; margin: 0; font-family: var(--vp-font-family-display); }
.section-link { font-size: 13px; color: var(--vp-c-brand); text-decoration: none; font-weight: 500; }
.section-link:hover { text-decoration: underline; }
.table-card { background: var(--vp-c-bg); border-radius: 12px; box-shadow: var(--vp-shadow-1); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 16px; text-align: left; font-size: 14px; }
.data-table th { background: var(--vp-c-bg-soft); font-weight: 600; color: var(--vp-c-text-2); font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; }
.data-table tr:not(:last-child) td { border-bottom: 1px solid var(--vp-c-bg-mute); }
.data-table tr:hover td { background: var(--vp-c-bg-soft); }
.mono { font-family: var(--vp-font-family-mono); font-size: 13px; }
.empty-text { text-align: center; padding: 12px; color: var(--vp-c-text-3); font-size: 14px; }
.loading-text { font-size: 13px; color: var(--vp-c-text-3); }
.loading-hint { text-align: center; padding: 12px; color: var(--vp-c-text-3); font-size: 13px; }

/* ── Empty State ── */
.empty-state-card {
  background: var(--vp-c-bg);
  border-radius: 12px;
  padding: 48px 24px;
  text-align: center;
  box-shadow: var(--vp-shadow-1);
}
.empty-icon { font-size: 40px; margin-bottom: 12px; opacity: 0.6; }
.empty-state-card .empty-text { font-size: 14px; margin-bottom: 16px; padding: 0; }
.btn-sm { display: inline-block; padding: 8px 20px; font-size: 13px; text-decoration: none; }

/* ── Usage Bar ── */
.usage-bar-section {
  background: var(--vp-c-bg);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 32px;
  box-shadow: var(--vp-shadow-1);
}
.usage-bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.usage-bar-label { font-size: 13px; color: var(--vp-c-text-2); font-weight: 600; }
.usage-bar-text { font-size: 13px; color: var(--vp-c-text-3); }
.usage-bar-track {
  height: 8px;
  background: var(--vp-c-bg-mute);
  border-radius: 999px;
  overflow: hidden;
}
.usage-bar-fill {
  height: 100%;
  border-radius: 999px;
  background: var(--vp-c-brand);
  transition: width 0.5s ease;
}
.usage-bar-fill.usage-warning { background: var(--vp-c-warning); }
.usage-bar-fill.usage-danger { background: var(--vp-c-danger); }

.page-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 16px;
  color: var(--vp-c-text-2);
}
.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--vp-c-border);
  border-top-color: var(--vp-c-brand);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.quickstart-card { background: var(--vp-c-bg-alt); border-radius: 12px; padding: 24px; color: var(--vp-c-text); }
.quickstart-card p { margin: 0 0 12px; font-size: 14px; }
.code-block { background: var(--vp-code-block-bg); padding: 16px; border-radius: 8px; font-size: 13px; overflow-x: auto; }
</style>
