<template>
  <div class="payment-page">
    <div class="payment-card">
      <h2>支付处理中...</h2>
      <p>订单号: {{ orderNo }}</p>
      <a v-if="fullPayUrl" :href="fullPayUrl" class="btn-primary" target="_blank">点击完成支付</a>
      <button class="btn-secondary" @click="verifyPayment" :disabled="verifying">
        {{ verifying ? '验证中...' : '我已支付完成' }}
      </button>
      <p v-if="verified" class="success">支付成功！正在跳转...</p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const route = useRoute()
const router = useRouter()
const config = useRuntimeConfig()
const token = import.meta.client ? localStorage.getItem('saas_token') : null
const orderNo = ref(route.query.orderNo as string || '')
const payUrl = ref(route.query.payUrl ? decodeURIComponent(route.query.payUrl as string) : '')
const verifying = ref(false)
const verified = ref(false)
const error = ref('')
const fullPayUrl = computed(() => payUrl.value ? `${config.public.apiBase}${payUrl.value}` : '')

async function verifyPayment() {
  verifying.value = true; error.value = ''
  try {
    // Call mock payment success - payUrl is already a full API path like /api/payments/mock/PAYxxx/success
    await $fetch(`${config.public.apiBase}${payUrl.value}`, { method: 'POST' })
    verified.value = true
    setTimeout(() => router.push('/dashboard/orders'), 2000)
  } catch (e: any) { error.value = e?.data?.message || '验证失败' }
  finally { verifying.value = false }
}
</script>

<style scoped>
.payment-page { max-width: 500px; margin: 0 auto; padding: 80px 24px; }
.payment-card { background: #fff; border-radius: 16px; padding: 40px; text-align: center; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.payment-card h2 { margin: 0 0 16px; }
.payment-card p { color: #64748b; margin-bottom: 24px; font-size: 14px; }
.btn-primary, .btn-secondary { display: block; width: 100%; padding: 12px; margin-bottom: 12px; border-radius: 8px; font-size: 15px; cursor: pointer; text-decoration: none; }
.btn-secondary { background: #f1f5f9; color: #1e293b; border: none; }
.success { color: #16a34a; }
.error { color: #ef4444; }
</style>
