<template>
  <div class="checkout-page">
    <div class="checkout-card" v-if="plan">
      <h2>确认订单</h2>
      <div class="order-summary">
        <div class="summary-row"><span>套餐</span><span>{{ plan.name }}</span></div>
        <div class="summary-row"><span>周期</span><span>月付</span></div>
        <div class="summary-row total"><span>金额</span><span>¥{{ amount }}</span></div>
      </div>
      <div class="channel-select">
        <p class="channel-label">支付方式</p>
        <div class="channel-options">
          <label class="channel-option" :class="{ active: channel === 'alipay' }">
            <input type="radio" v-model="channel" value="alipay" />
            <span>支付宝</span>
          </label>
          <label class="channel-option" :class="{ active: channel === 'wxpay' }">
            <input type="radio" v-model="channel" value="wxpay" />
            <span>微信支付</span>
          </label>
          <label class="channel-option" :class="{ active: channel === 'mock' }">
            <input type="radio" v-model="channel" value="mock" />
            <span>模拟支付 (开发)</span>
          </label>
        </div>
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
const auth = useSaasAuth()
const plan = ref<any>(null)
const loading = ref(false)
const amount = ref('0')
const error = ref('')
const channel = ref('alipay')

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
    const orderRes = await auth.authFetch<any>('/api/orders', {
      method: 'POST',
      body: { planCode: plan.value.code, billingCycle: 'monthly', currency: 'CNY' }
    })
    const orderNo = orderRes?.data?.orderNo
    if (!orderNo) { throw new Error('创建订单失败') }
    // Create payment
    const payRes = await auth.authFetch<any>('/api/payments/checkout', {
      method: 'POST',
      body: { orderNo, channel: channel.value }
    })
    const data = payRes?.data
    const payUrl = data?.checkoutUrl || data?.mockPayUrl
    const paymentNo = data?.paymentNo
    if (payUrl) {
      if (channel.value === 'alipay') {
        document.open()
        document.write(payUrl)
        document.close()
      } else if (channel.value === 'wxpay') {
        navigateTo(`/payment?orderNo=${orderNo}&paymentNo=${paymentNo}&codeUrl=${encodeURIComponent(payUrl)}`)
      } else {
        navigateTo(`/payment?orderNo=${orderNo}&payUrl=${encodeURIComponent(payUrl)}&paymentNo=${paymentNo}`)
      }
    }
  } catch (e: any) { error.value = e?.data?.message || e?.message || '下单失败' }
  finally { loading.value = false }
}
</script>

<style scoped>
.checkout-page { max-width: 600px; margin: 0 auto; padding: 80px 24px; font-family: var(--vp-font-family-base); }
.checkout-card { background: var(--vp-c-bg); border-radius: 16px; padding: 40px; box-shadow: var(--vp-shadow-1); }
.checkout-card h2 { margin: 0 0 24px; font-size: 22px; color: var(--vp-c-text); }
.order-summary { border-top: 1px solid var(--vp-c-divider); padding-top: 16px; margin-bottom: 24px; }
.summary-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: var(--vp-c-text-2); }
.summary-row.total { border-top: 1px solid var(--vp-c-divider); margin-top: 8px; padding-top: 16px; font-weight: 700; font-size: 18px; color: var(--vp-c-text); }
.checkout-btn { width: 100%; padding: 14px; font-size: 16px; }
.channel-select { margin-bottom: 20px; }
.channel-label { font-size: 14px; color: var(--vp-c-text-2); margin-bottom: 10px; }
.channel-options { display: flex; gap: 12px; }
.channel-option { flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px; padding: 12px; border: 2px solid var(--vp-c-divider); border-radius: 10px; cursor: pointer; font-size: 14px; color: var(--vp-c-text-2); transition: all 0.2s; }
.channel-option input { display: none; }
.channel-option.active { border-color: var(--vp-c-brand); color: var(--vp-c-brand); background: var(--vp-c-bg-soft); }
.error { color: var(--vp-c-danger); font-size: 13px; margin-top: 12px; text-align: center; }
.loading-state { text-align: center; padding: 60px; color: var(--vp-c-text-3); }
</style>
