<template>
  <section id="plans" class="pricing">
    <div class="section-head">
      <p class="section-tag">Plans</p>
      <h2>选择适合你的套餐</h2>
    </div>

    <div v-if="showBillingToggle" class="billing-toggle">
      <button
        v-for="cycle in allBillingCycles"
        :key="cycle"
        :class="{ active: billingCycle === cycle }"
        @click="billingCycle = cycle"
      >
        {{ billingCycleLabel(cycle) }}
      </button>
    </div>

    <p v-if="loadError" class="notice">{{ loadError }}</p>
    <div class="cards">
      <article v-for="plan in plans" :key="plan.code" class="card" :class="{ featured: plan.badge }">
        <div class="badge" v-if="plan.badge">{{ plan.badge }}</div>
        <h3>{{ plan.name }}</h3>
        <p class="desc">{{ plan.description }}</p>
        <div class="price">
          <span>{{ formatPrice(firstPrice(plan)?.amount) }}</span>
          <small>/ {{ cycleUnit(firstPrice(plan)?.billingCycle || billingCycle) }}</small>
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
import { onMounted } from 'vue'
import { createCheckoutOrder, createCheckoutPayment } from '~/utils/checkoutApi'

// 套餐定价展示与购买区块组件

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

// 解析 props 获取套餐分组编码和配置
const { props } = defineProps<{ props: Record<string, unknown> }>()
const config = useRuntimeConfig()
const code = String(props.planGroupCode || 'api_plans')
const snapshotGroup = computed(() => normalizePlanGroup(props.planGroup))
const snapshotPlans = computed(() => normalizePlans(props.plans))
// 仅当 props 未提供套餐数据时才发起远程请求
const shouldFetch = computed(() => !snapshotPlans.value.length && !snapshotGroup.value?.plans?.length)
const loadError = ref('')
const checkoutError = ref('')
const buyingCode = ref('')

// 当前选中的计费周期（月/年/两年/三年）
const billingCycle = ref(String(props.defaultBillingCycle || 'monthly'))

onMounted(() => {
  console.log('[Block] PricingBlock mounted')
})

// 远程获取套餐分组数据
const apiUrl = `${config.public.apiBase}/api/products/plan-groups/${code}`
console.log(`[PricingBlock] GET ${apiUrl}`)
const { data, error } = await useFetch<{ code: number; message: string; data: PlanGroup }>(
  apiUrl,
  { immediate: shouldFetch.value }
)

if (error.value) {
  console.error(`[PricingBlock] Failed: code=${code}`, error.value)
  loadError.value = '套餐加载失败，请稍后重试'
} else {
  console.log(`[PricingBlock] Success: code=${code}`)
}

// 合并最终套餐列表：优先使用 snapshot，其次远程数据
const remoteGroup = computed(() => data.value?.data)
const plans = computed(() => {
  if (snapshotPlans.value.length) return snapshotPlans.value
  if (snapshotGroup.value?.plans?.length) return snapshotGroup.value.plans
  return remoteGroup.value?.plans || []
})

// 收集所有套餐中出现的计费周期，用于切换按钮展示
const allBillingCycles = computed(() => {
  const cycles = new Set<string>()
  plans.value.forEach(plan => {
    plan.prices?.forEach(price => {
      if (price.billingCycle) cycles.add(price.billingCycle)
    })
  })
  return Array.from(cycles)
})

const showBillingToggle = computed(() => allBillingCycles.value.length > 1)

// 计费周期标签映射：月付 / 年付 / 两年付 / 三年付
function billingCycleLabel(cycle: string): string {
  const labels: Record<string, string> = { monthly: '月付', yearly: '年付', biennial: '两年付', triennial: '三年付' }
  return labels[cycle] || cycle
}

// 计费周期单位映射（用于价格展示后缀）
function cycleUnit(cycle: string): string {
  const units: Record<string, string> = { monthly: '月', yearly: '年', biennial: '两年', triennial: '三年' }
  return units[cycle] || cycle
}

// 标准化套餐分组数据（类型安全转换）
function normalizePlanGroup(value: unknown): PlanGroup | null {
  if (!value || typeof value !== 'object') return null
  const group = value as PlanGroup
  return { ...group, plans: normalizePlans(group.plans) }
}

// 标准化套餐列表数据（类型安全转换）
function normalizePlans(value: unknown): Plan[] {
  return Array.isArray(value) ? value as Plan[] : []
}

// 获取当前计费周期下第一个匹配的价格，无匹配则返回第一个价格
function firstPrice(plan: Plan) {
  return plan.prices?.find(price => price.billingCycle === billingCycle.value) || plan.prices?.[0]
}

// 格式化价格展示：数字前加 ¥，无价格显示"联系销售"
function formatPrice(value: unknown) {
  if (value === undefined || value === null) return '联系销售'
  return `￥${value}`
}

// 购买流程：创建订单 → 发起支付 → 跳转支付地址
async function buy(plan: Plan) {
  checkoutError.value = ''
  buyingCode.value = plan.code
  console.log(`[PricingBlock] Buying plan: code=${plan.code}`)
  try {
    const price = firstPrice(plan)
    const order = await createCheckoutOrder(config.public.apiBase, {
      planCode: plan.code,
      priceId: price?.id,
      billingCycle: price?.billingCycle || billingCycle.value,
      quantity: 1
    })
    const orderId = order.id || order.orderId || order.orderNo
    if (!orderId) throw new Error('订单创建成功，但未返回订单编号')
    console.log(`[PricingBlock] Order created: orderId=${orderId}`)

    const payment = await createCheckoutPayment(config.public.apiBase, { orderId })
    const checkoutUrl = payment.checkoutUrl || payment.payUrl || payment.paymentUrl
    if (!checkoutUrl) throw new Error('支付接口未返回 checkout 地址')
    console.log(`[PricingBlock] Payment ready, redirecting to checkoutUrl`)

    window.location.href = checkoutUrl
  } catch (error) {
    const message = error instanceof Error ? error.message : '创建订单失败，请稍后重试'
    console.error(`[PricingBlock] Buy failed: ${message}`)
    checkoutError.value = message
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
  margin-bottom: 40px;
}

.section-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: #f5f5f5;
  color: #606060;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  margin-bottom: 12px;
}

.section-head h2 {
  margin: 0;
  font-size: clamp(28px, 4vw, 42px);
  font-weight: 700;
  letter-spacing: -0.03em;
  color: #0a0a0a;
}

.billing-toggle {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin: 0 0 32px;
  padding: 4px;
  border-radius: 999px;
  background: #f5f5f5;
  width: fit-content;
  margin-left: auto;
  margin-right: auto;
}

.billing-toggle button {
  padding: 8px 20px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #606060;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.billing-toggle button.active {
  background: #fff;
  color: #0a0a0a;
  box-shadow: 0 1px 3px rgba(0,0,0,0.12);
}

.cards {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

@media (min-width: 1024px) {
  .cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

.card {
  position: relative;
  padding: 32px;
  border-radius: 0.75rem;
  background: #fff;
  border: 1px solid #e8e8e8;
  box-shadow: 0 1px 3px 0 rgba(0,0,0,0.10);
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
}

.card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.10), 0 8px 24px rgba(0,0,0,0.06);
}

.card.featured {
  border-color: #0a0a0a;
  box-shadow: 0 1px 3px 0 rgba(0,0,0,0.10), 0 0 0 1px #0a0a0a;
}

.badge {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  background: #0a0a0a;
  color: #fafafa;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.03em;
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
}

h3 {
  font-size: 22px;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0 0 8px;
}

.desc,
small {
  color: #606060;
  font-size: 14px;
}

.price {
  margin: 20px 0;
}

.price span {
  font-size: 42px;
  font-weight: 800;
  color: #0a0a0a;
  letter-spacing: -0.03em;
}

ul {
  min-height: 100px;
  padding-left: 18px;
  margin: 0 0 24px;
  flex: 1;
}

li {
  color: #606060;
  font-size: 14px;
  line-height: 1.8;
}

.buy {
  display: block;
  width: 100%;
  text-align: center;
  padding: 12px;
  border: 0;
  border-radius: 0.5rem;
  background: #070707;
  color: #fafafa;
  font-weight: 600;
  font-size: 15px;
  text-decoration: none;
  cursor: pointer;
  transition: opacity 0.2s;
}

.buy:hover {
  opacity: 0.85;
}

.buy:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.notice {
  text-align: center;
  color: #dc2626;
  margin: 0 0 18px;
}

@media (max-width: 768px) {
  .cards {
    grid-template-columns: 1fr !important;
  }
}
</style>
