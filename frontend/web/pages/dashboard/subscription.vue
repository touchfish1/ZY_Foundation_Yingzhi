<template>
  <div class="dashboard-page">
    <!-- Page Header -->
    <div class="page-header">
      <h2>订阅管理</h2>
    </div>

    <!-- Loading State -->
    <div v-if="pageLoading" class="loading-state">加载中...</div>

    <template v-else>
      <!-- Stats Grid: Plan + Usage + Actions -->
      <div class="stats-grid">
        <!-- Current Plan Card -->
        <div class="stat-card">
          <div class="stat-label">当前套餐</div>
          <div v-if="activeSubscription" class="plan-detail">
            <div class="plan-name">{{ activeSubscription.planName || activeSubscription.planCode || '-' }}</div>
            <div class="plan-meta">
              <span class="badge" :class="statusBadgeClass(activeSubscription.status)">
                {{ statusLabel(activeSubscription.status) }}
              </span>
            </div>
            <div class="plan-info">
              <div class="info-row">
                <span class="info-label">周期</span>
                <span class="info-value">{{ activeSubscription.period || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">金额</span>
                <span class="info-value price">¥{{ activeSubscription.amount || 0 }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">开始时间</span>
                <span class="info-value">{{ formatDate(activeSubscription.startDate) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">到期时间</span>
                <span class="info-value">{{ formatDate(activeSubscription.endDate) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-plan">
            <p>暂无订阅</p>
            <button class="btn btn-primary" @click="upgradePlan">前往购买</button>
          </div>
        </div>

        <!-- Usage Card -->
        <div class="stat-card">
          <div class="stat-label">配额使用</div>
          <div class="usage-detail">
            <div class="usage-numbers">
              <span class="usage-used">{{ user?.quotaUsed || 0 }}</span>
              <span class="usage-sep">/</span>
              <span class="usage-limit">{{ user?.quotaLimit || 0 }}</span>
            </div>
            <div class="usage-percent">{{ usagePercent }}%</div>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: Math.min(usagePercent, 100) + '%' }"></div>
          </div>
          <div v-if="activeSubscription" class="usage-renew">
            <span>下次重置：{{ formatDate(activeSubscription.endDate) }}</span>
          </div>
        </div>

        <!-- Actions Card -->
        <div class="stat-card actions-card">
          <div class="stat-label">操作</div>
          <div class="actions-list">
            <div class="action-row">
              <div class="action-info">
                <div class="action-title">自动续费</div>
                <div class="action-desc">到期自动扣费续期</div>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="autoRenew" @change="toggleAutoRenew" :disabled="!activeSubscription" />
                <span class="toggle-slider"></span>
              </label>
            </div>
            <div class="action-divider"></div>
            <button class="btn btn-primary action-btn" @click="upgradePlan">
              升级套餐
            </button>
            <button
              class="btn btn-danger action-btn"
              @click="confirmCancel"
              :disabled="!activeSubscription || activeSubscription.status === 'CANCELLED' || activeSubscription.status === 'EXPIRED'"
            >
              取消订阅
            </button>
          </div>
        </div>
      </div>

      <!-- Recent Orders -->
      <div class="section">
        <h2>最近订单</h2>
        <div class="table-card" v-if="!loadingOrders">
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
              <tr v-if="!recentOrders.length">
                <td colspan="4" class="empty">暂无订单</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="loading-state">加载中...</div>
      </div>

      <!-- Subscription History -->
      <div class="section">
        <h2>订阅历史</h2>
        <div class="table-card" v-if="!loadingSubscriptions">
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
                <td>{{ sub.planName || sub.planCode || '-' }}</td>
                <td>{{ sub.amount }} {{ sub.currency }}</td>
                <td>{{ sub.period || '-' }}</td>
                <td>{{ formatDate(sub.startDate) }}</td>
                <td>{{ formatDate(sub.endDate) }}</td>
                <td>
                  <span class="badge" :class="statusBadgeClass(sub.status)">{{ statusLabel(sub.status) }}</span>
                </td>
              </tr>
              <tr v-if="!subscriptions.length">
                <td colspan="6" class="empty">暂无订阅记录</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="loading-state">加载中...</div>
      </div>
    </template>

    <!-- Cancel Confirmation Modal -->
    <Teleport to="body">
      <div v-if="showCancelModal" class="modal-overlay" @click.self="closeCancelModal">
        <div class="modal-dialog">
          <div class="modal-header">
            <h3>确认取消订阅</h3>
            <button class="modal-close" @click="closeCancelModal">&times;</button>
          </div>
          <div class="modal-body">
            <p>取消订阅后，当前周期结束后将不再续费，相关服务将受限。</p>
            <p class="modal-warning">此操作不可撤销，确定要继续吗？</p>
          </div>
          <div class="modal-footer">
            <button class="btn btn-ghost" @click="closeCancelModal">再想想</button>
            <button class="btn btn-danger" @click="cancelSubscription" :disabled="cancelling">
              {{ cancelling ? '处理中...' : '确认取消' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const auth = useSaasAuth()
const { user } = auth
const config = useRuntimeConfig()
const userId = computed(() => auth.user.value?.id)

const pageLoading = ref(true)
const activeSubscription = ref<any>(null)
const subscriptions = ref<any[]>([])
const loadingSubscriptions = ref(true)
const recentOrders = ref<any[]>([])
const loadingOrders = ref(true)
const autoRenew = ref(false)
const showCancelModal = ref(false)
const cancelling = ref(false)

const usagePercent = computed(() => {
  if (!user.value || !user.value.quotaLimit) return 0
  return Math.round((user.value.quotaUsed / user.value.quotaLimit) * 100)
})

function statusBadgeClass(s: string) {
  const map: Record<string, string> = {
    ACTIVE: 'badge-success',
    EXPIRED: 'badge-error',
    CANCELLED: 'badge-error',
    PENDING: 'badge-warning'
  }
  return map[s?.toUpperCase()] || 'badge-warning'
}

function statusLabel(s: string) {
  const map: Record<string, string> = {
    ACTIVE: '激活',
    EXPIRED: '已过期',
    CANCELLED: '已取消',
    PENDING: '待处理'
  }
  return map[s?.toUpperCase()] || s || '-'
}

function orderStatusClass(s: string) {
  const map: Record<string, string> = {
    PENDING: 'badge-warning',
    PAID: 'badge-success',
    CANCELLED: 'badge-error'
  }
  return map[s] || ''
}

function formatDate(ts: string) {
  if (!ts) return '-'
  return new Date(ts).toLocaleString('zh-CN')
}

async function fetchActiveSubscription() {
  if (!userId.value) return
  try {
    const res = await auth.authFetch<any>('/api/subscriptions/active', {
      params: { userId: userId.value }
    })
    activeSubscription.value = res?.data || null
    if (activeSubscription.value) {
      autoRenew.value = activeSubscription.value.autoRenew !== false
    }
  } catch {
    activeSubscription.value = null
  }
}

async function fetchSubscriptions() {
  if (!userId.value) return
  try {
    const res = await auth.authFetch<any>('/api/subscriptions', {
      params: { userId: userId.value }
    })
    subscriptions.value = res?.data?.items || res?.data || []
  } catch {
    // silently fail
  } finally {
    loadingSubscriptions.value = false
  }
}

async function fetchOrders() {
  if (!userId.value) return
  try {
    const res = await auth.authFetch<any>('/api/orders', {
      params: { userId: userId.value }
    })
    recentOrders.value = (res?.data?.items || res?.data || []).slice(0, 10)
  } catch {
    // silently fail
  } finally {
    loadingOrders.value = false
  }
}

async function toggleAutoRenew() {
  if (!activeSubscription.value) return
  // Placeholder: UI toggle only; API call will be implemented later
  const newVal = autoRenew.value
  try {
    await auth.authFetch<any>(`/api/subscriptions/${activeSubscription.value.id}/auto-renew`, {
      method: 'PUT',
      body: { autoRenew: newVal }
    })
  } catch {
    // Revert toggle if API fails
    autoRenew.value = !newVal
  }
}

function upgradePlan() {
  navigateTo('/pricing')
}

function confirmCancel() {
  showCancelModal.value = true
}

function closeCancelModal() {
  if (cancelling.value) return
  showCancelModal.value = false
}

async function cancelSubscription() {
  if (!activeSubscription.value) return
  cancelling.value = true
  try {
    await auth.authFetch<any>(`/api/subscriptions/${activeSubscription.value.id}/cancel`, {
      method: 'POST'
    })
    activeSubscription.value.status = 'CANCELLED'
    showCancelModal.value = false
  } catch {
    // handle error gracefully
  } finally {
    cancelling.value = false
  }
}

onMounted(async () => {
  if (!user.value) await auth.fetchProfile()
  await Promise.all([
    fetchActiveSubscription(),
    fetchSubscriptions(),
    fetchOrders()
  ])
  pageLoading.value = false
})
</script>

<style scoped>
.dashboard-page { padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 20px; font-weight: 700; margin: 0; }

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}
.stat-card {
  background: var(--vp-c-bg);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--vp-shadow-1);
}
.stat-label {
  font-size: 12px;
  color: var(--vp-c-text-3);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 16px;
}

/* Plan Detail */
.plan-detail { display: flex; flex-direction: column; gap: 12px; }
.plan-name { font-size: 22px; font-weight: 700; color: var(--vp-c-text); }
.plan-meta { display: flex; gap: 8px; align-items: center; }
.plan-info { display: flex; flex-direction: column; gap: 8px; margin-top: 4px; }
.info-row { display: flex; justify-content: space-between; align-items: center; font-size: 14px; }
.info-label { color: var(--vp-c-text-3); }
.info-value { color: var(--vp-c-text); font-weight: 500; }
.info-value.price { color: var(--vp-c-brand); font-weight: 700; }
.empty-plan { text-align: center; padding: 20px 0; }
.empty-plan p { color: var(--vp-c-text-3); margin: 0 0 16px; }

/* Usage */
.usage-detail { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 12px; }
.usage-numbers { display: flex; align-items: baseline; gap: 4px; }
.usage-used { font-size: 28px; font-weight: 700; color: var(--vp-c-text); }
.usage-sep { font-size: 16px; color: var(--vp-c-text-3); }
.usage-limit { font-size: 16px; color: var(--vp-c-text-2); }
.usage-percent { font-size: 20px; font-weight: 700; color: var(--vp-c-brand); }
.progress-bar {
  height: 10px;
  background: var(--vp-c-bg-mute);
  border-radius: 999px;
  overflow: hidden;
  margin-bottom: 8px;
}
.progress-fill {
  height: 100%;
  background: var(--vp-c-brand-gradient);
  border-radius: 999px;
  transition: width 0.5s ease;
}
.usage-renew { font-size: 12px; color: var(--vp-c-text-3); margin-top: 8px; }

/* Actions Card */
.actions-list { display: flex; flex-direction: column; gap: 4px; }
.action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}
.action-info { flex: 1; }
.action-title { font-size: 14px; font-weight: 600; color: var(--vp-c-text); }
.action-desc { font-size: 12px; color: var(--vp-c-text-3); margin-top: 2px; }
.action-divider { height: 1px; background: var(--vp-c-bg-mute); margin: 8px 0; }
.action-btn { width: 100%; margin-top: 8px; }

/* Toggle Switch */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  flex-shrink: 0;
}
.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}
.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--vp-c-bg-mute);
  border-radius: 999px;
  transition: background 0.2s;
}
.toggle-slider::before {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  left: 3px;
  bottom: 3px;
  background: var(--vp-c-bg);
  border-radius: 50%;
  transition: transform 0.2s;
}
.toggle-switch input:checked + .toggle-slider {
  background: var(--vp-c-brand);
}
.toggle-switch input:checked + .toggle-slider::before {
  transform: translateX(20px);
}
.toggle-switch input:disabled + .toggle-slider {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.15s;
}
.btn-primary {
  background: var(--vp-c-brand);
  color: var(--vp-c-bg);
  border-color: var(--vp-c-brand);
}
.btn-primary:hover { opacity: 0.9; }
.btn-danger {
  background: var(--vp-c-danger-soft);
  color: var(--vp-c-danger);
  border-color: var(--vp-c-danger-soft);
}
.btn-danger:hover { background: var(--vp-c-danger); color: var(--vp-c-bg); }
.btn-danger:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-ghost {
  background: transparent;
  color: var(--vp-c-text-2);
  border-color: var(--vp-c-border);
}
.btn-ghost:hover { background: var(--vp-c-bg-mute); }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.15s ease;
}
.modal-dialog {
  background: var(--vp-c-bg);
  border-radius: 16px;
  width: 420px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.2s ease;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 0;
}
.modal-header h3 { font-size: 18px; font-weight: 700; margin: 0; }
.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--vp-c-text-3);
  cursor: pointer;
  padding: 0;
  line-height: 1;
}
.modal-close:hover { color: var(--vp-c-text); }
.modal-body { padding: 16px 24px 20px; }
.modal-body p { margin: 0 0 8px; font-size: 14px; color: var(--vp-c-text-2); line-height: 1.6; }
.modal-warning { color: var(--vp-c-danger) !important; font-weight: 600; }
.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 0 24px 20px;
}
.modal-footer .btn { min-width: 100px; }

/* Section */
.section { margin-bottom: 32px; }
.section h2 { font-size: 18px; font-weight: 600; margin: 0 0 16px; font-family: var(--vp-font-family-display); }

/* Table */
.table-card { background: var(--vp-c-bg); border-radius: 12px; box-shadow: var(--vp-shadow-1); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 16px; text-align: left; font-size: 14px; }
.data-table th { background: var(--vp-c-bg-soft); font-weight: 600; color: var(--vp-c-text-2); font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; }
.data-table tr:not(:last-child) td { border-bottom: 1px solid var(--vp-c-bg-mute); }
.data-table tr:hover td { background: var(--vp-c-bg-soft); }
.mono { font-family: var(--vp-font-family-mono); font-size: 13px; }

/* Badges */
.badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.badge-success { background: var(--vp-c-success-soft); color: var(--vp-c-success); }
.badge-warning { background: var(--vp-c-warning-soft); color: var(--vp-c-warning); }
.badge-error { background: var(--vp-c-danger-soft); color: var(--vp-c-danger); }

/* States */
.empty { text-align: center; padding: 40px; color: var(--vp-c-text-3); }
.loading-state { text-align: center; padding: 60px; color: var(--vp-c-text-3); }

/* Animations */
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
</style>
