<template>
  <div class="pricing-page">
    <div class="pricing-header">
      <h1>选择适合你的套餐</h1>
      <p>灵活定价，按需选择</p>
    </div>

    <!-- Loading Skeleton -->
    <div class="pricing-grid" v-if="loading">
      <div class="skeleton-card" v-for="n in 3" :key="n">
        <div class="skeleton-badge skeleton-pulse" v-if="n === 2" />
        <div class="skeleton-name skeleton-pulse" />
        <div class="skeleton-desc skeleton-pulse" />
        <div class="skeleton-price skeleton-pulse" />
        <div class="skeleton-features">
          <div class="skeleton-feature skeleton-pulse" v-for="m in 4" :key="m" />
        </div>
        <div class="skeleton-btn skeleton-pulse" />
      </div>
    </div>

    <!-- Error State -->
    <div class="error-state" v-else-if="error">
      <div class="error-icon">⚠</div>
      <p class="error-text">{{ error }}</p>
      <button class="btn-primary" @click="fetchPlans">重新加载</button>
    </div>

    <!-- Plans Grid -->
    <div class="pricing-grid" v-else-if="plans.length">
      <div
        v-for="plan in plans"
        :key="plan.id"
        class="plan-card"
        :class="{ featured: plan.badge === 'popular', subscribed: isSubscribed(plan) }"
      >
        <div class="plan-badge" v-if="plan.badge">{{ plan.badge }}</div>
        <div class="subscribed-badge" v-if="isSubscribed(plan)">已订阅</div>
        <h3 class="plan-name">{{ plan.name }}</h3>
        <p class="plan-desc">{{ plan.description }}</p>
        <div class="plan-price">
          <span class="price-amount">¥{{ getDisplayPrice(plan) }}</span>
          <span class="price-period">/月</span>
        </div>
        <ul class="plan-features">
          <li v-for="f in plan.features" :key="f.featureName" class="feature-item">
            <span class="check">&#10003;</span> {{ f.featureName }}: {{ f.featureValue }}
          </li>
        </ul>
        <button
          v-if="isSubscribed(plan)"
          class="btn-primary plan-btn subscribed-btn"
          disabled
        >已订阅 ✓</button>
        <button
          v-else
          class="btn-primary plan-btn"
          @click="selectPlan(plan)"
        >立即订阅</button>
      </div>
    </div>
    <div v-else class="empty-state">暂无可选套餐</div>
  </div>
</template>

<script setup lang="ts">
const router = useRouter()
const config = useRuntimeConfig()
const auth = useSaasAuth()
const { user } = auth

const plans = ref<any[]>([])
const loading = ref(true)
const error = ref('')
const activeSubscription = ref<any>(null)

async function fetchPlans() {
  loading.value = true
  error.value = ''
  try {
    const res = await $fetch(`${config.public.apiBase}/api/products/plans`) as any
    plans.value = res?.data || []
  } catch (e: any) {
    console.error('Failed to load plans', e)
    error.value = e?.data?.message || e?.message || '加载套餐列表失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

async function fetchActiveSubscription() {
  if (!user.value?.id) return
  try {
    const res = await auth.authFetch<any>('/api/subscriptions/active', {
      params: { userId: user.value.id }
    })
    activeSubscription.value = res?.data || res || null
  } catch {
    // not subscribed or not logged in – not an error
    activeSubscription.value = null
  }
}

onMounted(async () => {
  await fetchPlans()
  if (auth.isLoggedIn.value && !user.value) {
    await auth.fetchProfile()
  }
  if (auth.isLoggedIn.value) {
    await fetchActiveSubscription()
  }
})

function isSubscribed(plan: any): boolean {
  if (!activeSubscription.value) return false
  const sub = activeSubscription.value
  // Match by planCode or planId
  return (
    (sub.planCode && sub.planCode === plan.code) ||
    (sub.planId && (sub.planId === plan.id || sub.planId === plan.code))
  )
}

function getDisplayPrice(plan: any) {
  const price = plan.prices?.find((p: any) => p.billingCycle === 'monthly')
  return price ? price.amount : (plan.prices?.[0]?.amount || '—')
}

function selectPlan(plan: any) {
  const token = import.meta.client ? localStorage.getItem('saas_token') : null
  if (!token) return router.push('/login?redirect=pricing')
  router.push(`/checkout?planCode=${plan.code}`)
}
</script>

<style scoped>
.pricing-page { max-width: 1200px; margin: 0 auto; padding: 80px 24px; }
.pricing-header { text-align: center; margin-bottom: 60px; }
.pricing-header h1 { font-size: 36px; font-weight: 800; margin: 0 0 12px; font-family: var(--vp-font-family-display); }
.pricing-header p { color: var(--vp-c-text-2); font-size: 16px; }
.pricing-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 24px; }

/* ── Plan Cards ── */
.plan-card {
  background: var(--vp-c-bg); border-radius: 16px; padding: 32px;
  box-shadow: var(--vp-shadow-1); border: 1px solid var(--vp-c-border);
  position: relative; transition: all 0.2s;
}
.plan-card:hover { box-shadow: var(--vp-shadow-3); transform: translateY(-2px); }
.plan-card.featured { border-color: var(--vp-c-brand); box-shadow: 0 0 0 2px var(--vp-c-brand-dimmer); }
.plan-card.subscribed { border-color: var(--vp-c-success); box-shadow: 0 0 0 2px var(--vp-c-success-soft); }
.plan-badge {
  position: absolute; top: -12px; left: 50%; transform: translateX(-50%);
  background: var(--vp-c-brand-gradient); color: #fff;
  padding: 4px 16px; border-radius: 999px; font-size: 12px; font-weight: 600;
}
.subscribed-badge {
  position: absolute; top: 12px; right: 12px;
  background: var(--vp-c-success-soft); color: var(--vp-c-success);
  padding: 4px 12px; border-radius: 999px; font-size: 11px; font-weight: 700;
  letter-spacing: 0.02em;
}
.plan-name { font-size: 20px; font-weight: 700; margin: 0 0 8px; color: var(--vp-c-text); }
.plan-desc { color: var(--vp-c-text-2); font-size: 14px; margin: 0 0 20px; }
.plan-price { margin-bottom: 24px; }
.price-amount { font-size: 36px; font-weight: 800; color: var(--vp-c-text); }
.price-period { color: var(--vp-c-text-3); font-size: 14px; }
.plan-features { list-style: none; padding: 0; margin: 0 0 24px; }
.feature-item { padding: 6px 0; font-size: 14px; color: var(--vp-c-text); }
.check { color: var(--vp-c-success); font-weight: 700; margin-right: 8px; }
.plan-btn { width: 100%; padding: 12px; font-size: 15px; }
.subscribed-btn { background: var(--vp-c-success-soft); color: var(--vp-c-success); border-color: transparent; cursor: default; opacity: 0.9; }

/* ── Skeleton Loading ── */
.skeleton-card {
  background: var(--vp-c-bg); border-radius: 16px; padding: 32px;
  box-shadow: var(--vp-shadow-1); border: 1px solid var(--vp-c-border);
  position: relative;
}
.skeleton-pulse {
  background: linear-gradient(90deg, var(--vp-c-bg-mute) 25%, var(--vp-c-bg-soft) 50%, var(--vp-c-bg-mute) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
  border-radius: 8px;
}
.skeleton-badge { width: 80px; height: 24px; margin: 0 auto 16px; border-radius: 999px; }
.skeleton-name { width: 60%; height: 24px; margin-bottom: 12px; }
.skeleton-desc { width: 80%; height: 14px; margin-bottom: 24px; }
.skeleton-price { width: 40%; height: 36px; margin-bottom: 24px; }
.skeleton-features { margin-bottom: 24px; }
.skeleton-feature { width: 70%; height: 14px; margin-bottom: 10px; }
.skeleton-btn { width: 100%; height: 44px; border-radius: 10px; }

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* ── Error State ── */
.error-state {
  text-align: center; padding: 80px 24px;
}
.error-icon { font-size: 48px; margin-bottom: 16px; }
.error-text { color: var(--vp-c-text-2); font-size: 15px; margin-bottom: 20px; }

/* ── Empty State ── */
.empty-state { text-align: center; padding: 80px 24px; color: var(--vp-c-text-3); font-size: 15px; }
</style>
