<template>
  <section id="plans" class="pricing">
    <div class="section-head">
      <p>Plans</p>
      <h2>选择适合你的套餐</h2>
    </div>

    <div class="cards">
      <article v-for="plan in group?.plans || []" :key="plan.code" class="card">
        <div class="badge" v-if="plan.badge">{{ plan.badge }}</div>
        <h3>{{ plan.name }}</h3>
        <p class="desc">{{ plan.description }}</p>
        <div class="price">
          <span>{{ formatPrice(plan.prices[0]?.amount) }}</span>
          <small>/ {{ plan.prices[0]?.billingCycle || 'monthly' }}</small>
        </div>
        <ul>
          <li v-for="feature in plan.features" :key="feature.id">{{ feature.featureName }} {{ feature.featureValue || '' }}</li>
        </ul>
        <a class="buy" href="#">立即购买</a>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
const { props } = defineProps<{ props: Record<string, unknown> }>()
const config = useRuntimeConfig()
const code = String(props.planGroupCode || 'api_plans')
const { data } = await useFetch<any>(`${config.public.apiBase}/api/products/plan-groups/${code}`)
const group = computed(() => data.value?.data)

function formatPrice(value: unknown) {
  if (value === undefined || value === null) return '联系销售'
  return `￥${value}`
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
  text-align: center;
  padding: 12px;
  border-radius: 16px;
  background: #fff;
  color: #07111f;
  font-weight: 800;
  text-decoration: none;
}
</style>
