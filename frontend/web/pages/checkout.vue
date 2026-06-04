<template>
  <div class="checkout-page">
    <div class="checkout-card" v-if="plan">
      <h2>确认订单</h2>
      <div class="order-summary">
        <div class="summary-row"><span>套餐</span><span>{{ plan.name }}</span></div>
        <div class="summary-row"><span>周期</span><span>月付</span></div>
        <div class="summary-row total"><span>金额</span><span>¥{{ amount }}</span></div>
      </div>
      <button class="btn-primary checkout-btn" :disabled="loading" @click="submitOrder">
        {{ loading ? '处理中...' : '确认支付 ¥' + amount }}
      </button>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
    <div v-else class="loading-state">加载中...</div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const route = useRoute()
const router = useRouter()
const config = useRuntimeConfig()
const token = import.meta.client ? localStorage.getItem('saas_token') : null
const plan = ref<any>(null)
const loading = ref(false)
const amount = ref('0')
const error = ref('')

onMounted(async () => {
  const planCode = route.query.planCode as string
  if (!planCode) return navigateTo('/pricing')
  try {
    const res = await $fetch(`${config.public.apiBase}/api/products/plans`) as any
    const found = (res?.data || []).find((p: any) => p.code === planCode)
    if (!found) { error.value = '套餐不存在'; return }
    plan.value = found
    const price = found.prices?.find((p: any) => p.billingCycle === 'monthly')
    amount.value = price?.amount || '—'
  } catch (e) {
    error.value = '加载失败'
  }
})

async function submitOrder() {
  loading.value = true; error.value = ''
  try {
    const orderRes = await $fetch(`${config.public.apiBase}/api/orders`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      body: { planCode: plan.value.code, billingCycle: 'monthly', currency: 'CNY' }
    }) as any
    const orderNo = orderRes?.data?.orderNo
    if (!orderNo) { throw new Error('创建订单失败') }
    // Create payment
    const payRes = await $fetch(`${config.public.apiBase}/api/payments/checkout`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      body: { orderNo, channel: 'mock' }
    }) as any
    const payUrl = payRes?.data?.mockPayUrl
    // Redirect to mock payment
    if (payUrl) navigateTo(`/payment?orderNo=${orderNo}&payUrl=${encodeURIComponent(payUrl)}`)
  } catch (e: any) { error.value = e?.data?.message || e?.message || '下单失败' }
  finally { loading.value = false }
}
</script>

<style scoped>
.checkout-page { max-width: 600px; margin: 0 auto; padding: 80px 24px; }
.checkout-card { background: #fff; border-radius: 16px; padding: 40px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.checkout-card h2 { margin: 0 0 24px; font-size: 22px; }
.order-summary { border-top: 1px solid #e2e8f0; padding-top: 16px; margin-bottom: 24px; }
.summary-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; }
.summary-row.total { border-top: 1px solid #e2e8f0; margin-top: 8px; padding-top: 16px; font-weight: 700; font-size: 18px; }
.checkout-btn { width: 100%; padding: 14px; font-size: 16px; }
.error { color: #ef4444; font-size: 13px; margin-top: 12px; text-align: center; }
.loading-state { text-align: center; padding: 60px; color: #94a3b8; }
</style>
