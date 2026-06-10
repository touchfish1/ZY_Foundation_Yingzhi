<template>
  <div class="checkout-page">
    <!-- Step Indicators -->
    <div class="step-indicators">
      <div class="step" :class="{ active: currentStep >= 1, done: currentStep > 1 }">
        <div class="step-circle">1</div>
        <span class="step-label">创建订单</span>
      </div>
      <div class="step-connector" :class="{ active: currentStep >= 2 }" />
      <div class="step" :class="{ active: currentStep >= 2, done: currentStep > 2 }">
        <div class="step-circle">2</div>
        <span class="step-label">创建支付</span>
      </div>
      <div class="step-connector" :class="{ active: currentStep >= 3 }" />
      <div class="step" :class="{ active: currentStep >= 3 }">
        <div class="step-circle">3</div>
        <span class="step-label">支付结果</span>
      </div>
    </div>

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
        <span v-if="loading" class="btn-spinner" />
        {{ loading ? '处理中...' : '确认支付 ¥' + amount }}
      </button>
      <p v-if="error" class="error-msg">{{ error }}</p>
    </div>
    <div v-else class="loading-state">
      <div v-if="!error" class="loading-text">加载套餐信息...</div>
      <div v-else class="error-state">
        <p class="error-text">{{ error }}</p>
        <button class="btn-primary" @click="router.push('/pricing')">返回套餐列表</button>
      </div>
    </div>
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
const currentStep = ref(0)

onMounted(async () => {
  const planCode = route.query.planCode as string
  if (!planCode) {
    return navigateTo('/pricing')
  }
  try {
    const res = await $fetch(`${config.public.apiBase}/api/products/plans`) as any
    const found = (res?.data || []).find((p: any) => p.code === planCode)
    if (!found) {
      error.value = '套餐不存在或已下架'
      return
    }
    plan.value = found
    const price = found.prices?.find((p: any) => p.billingCycle === 'monthly')
    amount.value = price?.amount || '—'
  } catch (e: any) {
    error.value = e?.data?.message || e?.message || '加载套餐信息失败'
  }
})

async function submitOrder() {
  loading.value = true
  error.value = ''
  currentStep.value = 1
  try {
    // Step 1: Create order
    const orderRes = await auth.authFetch<any>('/api/orders', {
      method: 'POST',
      body: { planCode: plan.value.code, billingCycle: 'monthly', currency: 'CNY' }
    })
    const orderNo = orderRes?.data?.orderNo
    if (!orderNo) {
      throw new Error(orderRes?.message || '创建订单失败')
    }
    currentStep.value = 2

    // Step 2: Create payment
    const payRes = await auth.authFetch<any>('/api/payments/checkout', {
      method: 'POST',
      body: { orderNo, channel: channel.value }
    })
    const data = payRes?.data
    const paymentNo = data?.paymentNo
    const ch = data?.channel || channel.value
    currentStep.value = 3

    // Step 3: Handle payment channel
    if (ch === 'mock') {
      // Mock payment succeeds immediately
      navigateTo('/dashboard/orders')
    } else if (ch === 'alipay' && data?.checkoutUrl) {
      window.location.href = data.checkoutUrl
    } else if (ch === 'wxpay' && data?.checkoutUrl) {
      navigateTo(`/payment?orderNo=${orderNo}&paymentNo=${paymentNo}&codeUrl=${encodeURIComponent(data.checkoutUrl)}`)
    } else if (data?.payUrl) {
      navigateTo(`/payment?orderNo=${orderNo}&payUrl=${encodeURIComponent(data.payUrl)}&paymentNo=${paymentNo}`)
    }
  } catch (e: any) {
    error.value = e?.data?.message || e?.message || '下单失败，请稍后重试'
    currentStep.value = 0
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.checkout-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 80px 24px;
  font-family: var(--vp-font-family-base);
}

/* ── Step Indicators ── */
.step-indicators {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40px;
  gap: 0;
}
.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.step-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  background: var(--vp-c-bg-mute);
  color: var(--vp-c-text-3);
  border: 2px solid var(--vp-c-border);
  transition: all 0.3s;
}
.step.active .step-circle {
  background: var(--vp-c-brand);
  color: #fff;
  border-color: var(--vp-c-brand);
}
.step.done .step-circle {
  background: var(--vp-c-success-soft);
  color: var(--vp-c-success);
  border-color: var(--vp-c-success);
}
.step-label {
  font-size: 12px;
  color: var(--vp-c-text-3);
  white-space: nowrap;
}
.step.active .step-label { color: var(--vp-c-brand); font-weight: 600; }
.step.done .step-label { color: var(--vp-c-success); }
.step-connector {
  width: 48px;
  height: 2px;
  background: var(--vp-c-border);
  margin: 0 8px;
  margin-bottom: 26px;
  transition: background 0.3s;
}
.step-connector.active { background: var(--vp-c-brand); }

/* ── Checkout Card ── */
.checkout-card { background: var(--vp-c-bg); border-radius: 16px; padding: 40px; box-shadow: var(--vp-shadow-1); }
.checkout-card h2 { margin: 0 0 24px; font-size: 22px; color: var(--vp-c-text); }
.order-summary { border-top: 1px solid var(--vp-c-divider); padding-top: 16px; margin-bottom: 24px; }
.summary-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: var(--vp-c-text-2); }
.summary-row.total { border-top: 1px solid var(--vp-c-divider); margin-top: 8px; padding-top: 16px; font-weight: 700; font-size: 18px; color: var(--vp-c-text); }
.checkout-btn { width: 100%; padding: 14px; font-size: 16px; display: flex; align-items: center; justify-content: center; gap: 8px; }
.checkout-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/* ── Button Spinner ── */
.btn-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: btn-spin 0.7s linear infinite;
}
@keyframes btn-spin { to { transform: rotate(360deg); } }

.channel-select { margin-bottom: 20px; }
.channel-label { font-size: 14px; color: var(--vp-c-text-2); margin-bottom: 10px; }
.channel-options { display: flex; gap: 12px; }
.channel-option { flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px; padding: 12px; border: 2px solid var(--vp-c-divider); border-radius: 10px; cursor: pointer; font-size: 14px; color: var(--vp-c-text-2); transition: all 0.2s; }
.channel-option input { display: none; }
.channel-option.active { border-color: var(--vp-c-brand); color: var(--vp-c-brand); background: var(--vp-c-bg-soft); }

/* ── Error & State ── */
.error-msg { color: var(--vp-c-danger); font-size: 13px; margin-top: 12px; text-align: center; }
.error-state { text-align: center; padding: 40px 24px; }
.error-text { color: var(--vp-c-text-2); font-size: 14px; margin-bottom: 16px; }
.loading-state { text-align: center; padding: 60px; color: var(--vp-c-text-3); }
.loading-text { font-size: 14px; color: var(--vp-c-text-3); }
</style>
