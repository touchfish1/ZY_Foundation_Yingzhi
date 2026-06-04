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
.pricing-header h1 { font-size: 36px; font-weight: 800; margin: 0 0 12px; }
.pricing-header p { color: #64748b; font-size: 16px; }
.pricing-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 24px; }
.plan-card {
  background: #fff; border-radius: 16px; padding: 32px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06); border: 1px solid #e2e8f0;
  position: relative; transition: all 0.2s;
}
.plan-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,0.1); transform: translateY(-2px); }
.plan-card.featured { border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.plan-badge {
  position: absolute; top: -12px; left: 50%; transform: translateX(-50%);
  background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff;
  padding: 4px 16px; border-radius: 999px; font-size: 12px; font-weight: 600;
}
.plan-name { font-size: 20px; font-weight: 700; margin: 0 0 8px; }
.plan-desc { color: #64748b; font-size: 14px; margin: 0 0 20px; }
.plan-price { margin-bottom: 24px; }
.price-amount { font-size: 36px; font-weight: 800; }
.price-period { color: #94a3b8; font-size: 14px; }
.plan-features { list-style: none; padding: 0; margin: 0 0 24px; }
.feature-item { padding: 6px 0; font-size: 14px; color: #475569; }
.check { color: #16a34a; font-weight: 700; margin-right: 8px; }
.plan-btn { width: 100%; padding: 12px; font-size: 15px; }
.loading-state { text-align: center; padding: 60px; color: #94a3b8; }
</style>
