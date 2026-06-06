<template>
  <div class="pricing-page">
    <div class="pricing-header">
      <h1>选择适合你的套餐</h1>
      <p>灵活定价，按需选择</p>
    </div>
    <div class="pricing-grid" v-if="plans.length">
      <div v-for="plan in plans" :key="plan.id" class="plan-card" :class="{ featured: plan.badge === 'popular' }">
        <div class="plan-badge" v-if="plan.badge">{{ plan.badge }}</div>
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
        <button class="btn-primary plan-btn" @click="selectPlan(plan)">立即订阅</button>
      </div>
    </div>
    <div v-else class="loading-state">加载中...</div>
  </div>
</template>

<script setup lang="ts">
const router = useRouter()
const config = useRuntimeConfig()
const plans = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await $fetch(`${config.public.apiBase}/api/products/plans`) as any
    plans.value = res?.data || []
  } catch (e) {
    console.error('Failed to load plans', e)
  } finally { loading.value = false }
})

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
.plan-card {
  background: var(--vp-c-bg); border-radius: 16px; padding: 32px;
  box-shadow: var(--vp-shadow-1); border: 1px solid var(--vp-c-border);
  position: relative; transition: all 0.2s;
}
.plan-card:hover { box-shadow: var(--vp-shadow-3); transform: translateY(-2px); }
.plan-card.featured { border-color: var(--vp-c-brand); box-shadow: 0 0 0 2px var(--vp-c-brand-dimmer); }
.plan-badge {
  position: absolute; top: -12px; left: 50%; transform: translateX(-50%);
  background: var(--vp-c-brand-gradient); color: #fff;
  padding: 4px 16px; border-radius: 999px; font-size: 12px; font-weight: 600;
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
.loading-state { text-align: center; padding: 60px; color: var(--vp-c-text-3); }
</style>
