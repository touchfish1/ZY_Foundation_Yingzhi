<template>
  <section id="plans" class="pricing">
    <div class="section-head">
      <p>Plans</p>
      <h2>选择适合你的套餐</h2>
    </div>

    <p v-if="loadError" class="notice">{{ loadError }}</p>
    <div class="cards">
      <article v-for="plan in plans" :key="plan.code" class="card">
        <div class="badge" v-if="plan.badge">{{ plan.badge }}</div>
        <h3>{{ plan.name }}</h3>
        <p class="desc">{{ plan.description }}</p>
        <div class="price">
          <span>{{ formatPrice(firstPrice(plan)?.amount) }}</span>
          <small>/ {{ firstPrice(plan)?.billingCycle || defaultBillingCycle }}</small>
        </div>
        <ul>
          <li v-for="feature in plan.features" :key="feature.id">{{ feature.featureName }} {{ feature.featureValue || '' }}</li>
        </ul>
        <button class="buy" type="button" :disabled="buyingCode === plan.code" @click="buy(plan)">
          {{ buyingCode === plan.code ? '正在创建订单...' : '立即购买' }}
        </button>
      </article>
    </div>
    <p v-if="checkoutError" class="notice">{{ checkoutError }}</p>
  </section>
</template>

<script setup lang="ts">
import { createCheckoutOrder, createCheckoutPayment } from '~/utils/checkoutApi'

interface PlanPrice {
  id?: number | string
  amount?: number | string | null
  billingCycle?: string
}

interface PlanFeature {
  id?: number | string
  featureName: string
  featureValue?: string
}

interface Plan {
  code: string
  name: string
  description?: string
  badge?: string
  prices?: PlanPrice[]
  features?: PlanFeature[]
}

interface PlanGroup {
  code?: string
  plans?: Plan[]
}

const { props } = defineProps<{ props: Record<string, unknown> }>()
const config = useRuntimeConfig()
const code = String(props.planGroupCode || 'api_plans')
const defaultBillingCycle = String(props.defaultBillingCycle || 'monthly')
const snapshotGroup = computed(() => normalizePlanGroup(props.planGroup))
const snapshotPlans = computed(() => normalizePlans(props.plans))
const shouldFetch = computed(() => !snapshotPlans.value.length && !snapshotGroup.value?.plans?.length)
const loadError = ref('')
const checkoutError = ref('')
const buyingCode = ref('')

const { data, error } = await useFetch<{ code: number; message: string; data: PlanGroup }>(
  `${config.public.apiBase}/api/products/plan-groups/${code}`,
  { immediate: shouldFetch.value }
)

if (error.value) {
  loadError.value = '套餐加载失败，请稍后重试'
}

const remoteGroup = computed(() => data.value?.data)
const plans = computed(() => {
  if (snapshotPlans.value.length) return snapshotPlans.value
  if (snapshotGroup.value?.plans?.length) return snapshotGroup.value.plans
  return remoteGroup.value?.plans || []
})

function normalizePlanGroup(value: unknown): PlanGroup | null {
  if (!value || typeof value !== 'object') return null
  const group = value as PlanGroup
  return { ...group, plans: normalizePlans(group.plans) }
}

function normalizePlans(value: unknown): Plan[] {
  return Array.isArray(value) ? value as Plan[] : []
}

function firstPrice(plan: Plan) {
  return plan.prices?.find(price => price.billingCycle === defaultBillingCycle) || plan.prices?.[0]
}

function formatPrice(value: unknown) {
  if (value === undefined || value === null) return '联系销售'
  return `￥${value}`
}

async function buy(plan: Plan) {
  checkoutError.value = ''
  buyingCode.value = plan.code
  try {
    const price = firstPrice(plan)
    const order = await createCheckoutOrder(config.public.apiBase, {
      planCode: plan.code,
      priceId: price?.id,
      billingCycle: price?.billingCycle || defaultBillingCycle,
      quantity: 1
    })
    const orderId = order.id || order.orderId || order.orderNo
    if (!orderId) throw new Error('订单创建成功，但未返回订单编号')

    const payment = await createCheckoutPayment(config.public.apiBase, { orderId })
    const checkoutUrl = payment.checkoutUrl || payment.payUrl || payment.paymentUrl
    if (!checkoutUrl) throw new Error('支付接口未返回 checkout 地址')

    window.location.href = checkoutUrl
  } catch (error) {
    checkoutError.value = error instanceof Error ? error.message : '创建订单失败，请稍后重试'
  } finally {
    buyingCode.value = ''
  }
}
</script>

<style scoped>
.pricing {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto 96px;
}

.section-head {
  text-align: center;
  margin-bottom: 30px;
}

.section-head p {
  color: #67e8f9;
  font-weight: 800;
}

.section-head h2 {
  margin: 0;
  font-size: clamp(30px, 5vw, 56px);
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
}

.card {
  position: relative;
  padding: 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(18px);
}

.badge {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(103, 232, 249, 0.16);
  color: #67e8f9;
  font-size: 12px;
}

h3 {
  font-size: 26px;
}

.desc,
li,
small {
  color: rgba(226, 232, 240, 0.72);
}

.price span {
  font-size: 42px;
  font-weight: 900;
}

ul {
  min-height: 110px;
  padding-left: 18px;
}

.buy {
  display: block;
  width: 100%;
  text-align: center;
  padding: 12px;
  border: 0;
  border-radius: 16px;
  background: #fff;
  color: #07111f;
  font-weight: 800;
  text-decoration: none;
  cursor: pointer;
}

.buy:disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.notice {
  text-align: center;
  color: #fecaca;
  margin: 0 0 18px;
}
</style>
