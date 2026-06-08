<template>
  <div class="payment-page">
    <div class="payment-card">
      <h2>支付处理中...</h2>
      <p>订单号: {{ orderNo }}</p>

      <!-- Mock payment -->
      <template v-if="isMock">
        <a v-if="fullPayUrl" :href="fullPayUrl" class="btn-primary" target="_blank">点击完成支付</a>
        <button class="btn-secondary" @click="verifyPayment" :disabled="verifying">
          {{ verifying ? '验证中...' : '我已支付完成' }}
        </button>
      </template>

      <!-- WeChat QR code -->
      <template v-else-if="isWeChat">
        <div class="qr-wrapper">
          <div ref="qrRef" class="qr-code"></div>
        </div>
        <p class="hint">请使用微信扫一扫完成支付</p>
        <p class="poll-status">{{ pollStatus }}</p>
      </template>

      <!-- Alipay auto-redirect -->
      <template v-else-if="isAlipay">
        <p class="hint">正在跳转到支付宝...</p>
      </template>

      <p v-if="verified" class="success">支付成功！正在跳转...</p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const route = useRoute()
const router = useRouter()
const auth = useSaasAuth()

const orderNo = ref(route.query.orderNo as string || '')
const paymentNo = ref(route.query.paymentNo as string || '')
const payUrl = ref(route.query.payUrl ? decodeURIComponent(route.query.payUrl as string) : '')
const codeUrl = ref(route.query.codeUrl ? decodeURIComponent(route.query.codeUrl as string) : '')
const verifying = ref(false)
const verified = ref(false)
const error = ref('')
const pollStatus = ref('等待支付...')
const qrRef = ref<HTMLElement | null>(null)
const pollingInterval = ref<ReturnType<typeof setInterval> | null>(null)

const isMock = computed(() => !!(payUrl.value && !codeUrl.value))
const isWeChat = computed(() => !!codeUrl.value)
const isAlipay = computed(() => !payUrl.value && !codeUrl.value && !!paymentNo.value)
const fullPayUrl = computed(() => payUrl.value ? `${useRuntimeConfig().public.apiBase}${payUrl.value}` : '')

onMounted(() => {
  if (isWeChat.value && qrRef.value) {
    renderQrCode()
    startPolling()
  }
})

onUnmounted(() => {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
})

function renderQrCode() {
  import('qrcode').then(QRCode => {
    QRCode.default.toCanvas(qrRef.value, codeUrl.value, { width: 220, margin: 2 })
  })
}

function startPolling() {
  pollingInterval.value = setInterval(async () => {
    if (!paymentNo.value) return
    try {
      const res = await auth.authFetch<any>(`/api/payments/${paymentNo.value}`)
      const status = res?.data?.status || res?.status
      if (status === 'SUCCESS') {
        verified.value = true
        stopPolling()
        setTimeout(() => router.push('/dashboard/orders'), 2000)
      } else if (status === 'FAILED' || status === 'CLOSED') {
        error.value = '支付失败，请重试'
        stopPolling()
      } else {
        pollStatus.value = '等待支付...'
      }
    } catch {
      pollStatus.value = '查询中...'
    }
  }, 3000)
}

function stopPolling() {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
}

async function verifyPayment() {
  verifying.value = true; error.value = ''
  try {
    await auth.authFetch<any>(payUrl.value, { method: 'POST' })
    verified.value = true
    setTimeout(() => router.push('/dashboard/orders'), 2000)
  } catch (e: any) { error.value = e?.data?.message || '验证失败' }
  finally { verifying.value = false }
}
</script>

<style scoped>
.payment-page { max-width: 500px; margin: 0 auto; padding: 80px 24px; font-family: var(--vp-font-family-base); }
.payment-card { background: var(--vp-c-bg); border-radius: 16px; padding: 40px; text-align: center; box-shadow: var(--vp-shadow-1); }
.payment-card h2 { margin: 0 0 16px; color: var(--vp-c-text); }
.payment-card p { color: var(--vp-c-text-2); margin-bottom: 24px; font-size: 14px; }
.btn-primary, .btn-secondary { display: block; width: 100%; padding: 12px; margin-bottom: 12px; border-radius: 8px; font-size: 15px; cursor: pointer; text-decoration: none; }
.btn-secondary { background: var(--vp-c-bg-mute); color: var(--vp-c-text); border: none; }
.success { color: var(--vp-c-success); }
.error { color: var(--vp-c-danger); }
.qr-wrapper { display: flex; justify-content: center; margin: 20px 0; }
.qr-code { width: 220px; height: 220px; }
.hint { font-size: 13px; color: var(--vp-c-text-3); margin-bottom: 8px; }
.poll-status { font-size: 13px; color: var(--vp-c-brand); }
</style>
