<template>
  <main class="plans-page">
    <div class="orb orb-a" />
    <div class="orb orb-b" />
    <div class="grid-glow" />
    <header class="topbar">
      <NuxtLink to="/" class="brand">
        <span class="brand-mark">N</span>
        New API
      </NuxtLink>
      <nav class="nav-links" aria-label="Primary navigation">
        <NuxtLink to="/" class="nav-link active">价格</NuxtLink>
        <a class="nav-link" href="#faq">FAQ</a>
        <a class="console-link" href="#">控制台</a>
      </nav>
    </header>

    <section class="headline">
      <p class="eyebrow">Plans</p>
      <h1>选择适合你的计划</h1>
      <p class="subtitle">稳定、快速、简单的 AI API 中转服务，按需选择套餐即可开始使用。</p>
      <div class="billing-switch">
        <button :class="{ selected: billing === 'monthly' }" @click="billing = 'monthly'">月付</button>
        <button :class="{ selected: billing === 'yearly' }" @click="billing = 'yearly'">年付 <span>省 20%</span></button>
      </div>
    </section>

    <section class="plans-wrap" aria-label="Pricing plans">
      <article v-for="plan in currentPlans" :key="plan.name" class="plan-card" :class="{ recommended: plan.recommended }">
        <div v-if="plan.recommended" class="tag">推荐</div>
        <h2>{{ plan.name }}</h2>
        <p class="desc">{{ plan.description }}</p>
        <div class="price-line">
          <span class="currency">¥</span>
          <strong>{{ plan.price }}</strong>
          <span class="period">/{{ billing === 'monthly' ? '月' : '年' }}</span>
        </div>
        <NuxtLink class="buy-button" :class="{ primary: plan.recommended }" to="/plans">
          {{ plan.action }}
          <span>→</span>
        </NuxtLink>
        <ul>
          <li v-for="item in plan.features" :key="item">{{ item }}</li>
        </ul>
      </article>
    </section>

    <section class="notice">
      <div>
        <h2>所有套餐均包含</h2>
        <p>统一 API 入口、余额管理、调用统计、密钥管理、基础安全策略和在线支持。</p>
      </div>
      <NuxtLink to="/plans" class="notice-button">立即开始</NuxtLink>
    </section>

    <section id="faq" class="faq">
      <h2>常见问题</h2>
      <div class="faq-list">
        <details v-for="item in faqs" :key="item.q" class="faq-item">
          <summary>{{ item.q }}</summary>
          <p>{{ item.a }}</p>
        </details>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
const billing = ref<'monthly' | 'yearly'>('monthly')

const plans = {
  monthly: [
    {
      name: '基础版',
      description: '适合个人开发者和轻量测试场景。',
      price: '19',
      action: '选择基础版',
      recommended: false,
      features: ['每日 5,000 次请求', '3 个 API Key', '基础用量统计', '社区支持', '标准模型通道']
    },
    {
      name: '专业版',
      description: '适合高频调用、团队协作和正式业务。',
      price: '49',
      action: '选择专业版',
      recommended: true,
      features: ['每日 50,000 次请求', '不限 API Key 数量', '高级调用统计', '优先技术支持', '自定义限流策略']
    },
    {
      name: '企业版',
      description: '适合定制接入、专属容量和私有化需求。',
      price: '199',
      action: '联系购买',
      recommended: false,
      features: ['专属请求额度', '专属模型通道', '定制集成支持', '7x24 小时支持', 'SLA 服务保障']
    }
  ],
  yearly: [
    {
      name: '基础版',
      description: '适合个人开发者和轻量测试场景。',
      price: '182',
      action: '选择基础版',
      recommended: false,
      features: ['每日 5,000 次请求', '3 个 API Key', '基础用量统计', '社区支持', '标准模型通道']
    },
    {
      name: '专业版',
      description: '适合高频调用、团队协作和正式业务。',
      price: '470',
      action: '选择专业版',
      recommended: true,
      features: ['每日 50,000 次请求', '不限 API Key 数量', '高级调用统计', '优先技术支持', '自定义限流策略']
    },
    {
      name: '企业版',
      description: '适合定制接入、专属容量和私有化需求。',
      price: '1900',
      action: '联系购买',
      recommended: false,
      features: ['专属请求额度', '专属模型通道', '定制集成支持', '7x24 小时支持', 'SLA 服务保障']
    }
  ]
}

const currentPlans = computed(() => plans[billing.value])

const faqs = [
  { q: '套餐可以随时升级吗？', a: '可以。升级后会立即使用新的额度和功能。' },
  { q: '是否支持多个模型供应商？', a: '支持。系统可以统一接入多个模型供应商并通过同一 API 调用。' },
  { q: '余额和套餐额度如何查看？', a: '登录控制台后可以查看实时余额、调用次数、请求日志和密钥状态。' },
  { q: '企业版是否支持私有化？', a: '支持。企业版可根据需求提供专属部署、定制集成和 SLA。' }
]
</script>

<style scoped>
.plans-page {
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  padding: 0 20px 64px;
  color: #172033;
  background:
    radial-gradient(circle at 50% -160px, rgba(82, 142, 255, 0.22), transparent 420px),
    linear-gradient(180deg, #f7fbff 0%, #fff 46%, #f8fbff 100%);
}

.orb,
.grid-glow {
  position: absolute;
  pointer-events: none;
}

.orb {
  width: 300px;
  height: 300px;
  border-radius: 999px;
  filter: blur(60px);
  opacity: 0.22;
  animation: float-orb 8s ease-in-out infinite;
}

.orb-a {
  top: 120px;
  left: -130px;
  background: #60a5fa;
}

.orb-b {
  top: 90px;
  right: -120px;
  background: #93c5fd;
  animation-delay: -3s;
}

.grid-glow {
  inset: 0;
  opacity: 0.28;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.12) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.12) 1px, transparent 1px);
  background-size: 54px 54px;
  mask-image: linear-gradient(to bottom, #000 0%, transparent 58%);
}

.topbar {
  position: relative;
  z-index: 2;
  width: min(1120px, 100%);
  height: 72px;
  margin: 0 auto;
  padding: 0 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.brand,
.nav-link,
.console-link,
.buy-button,
.notice-button {
  text-decoration: none;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #172033;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.brand-mark {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  box-shadow: 0 10px 22px rgba(59, 130, 246, 0.22);
  font-size: 15px;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
}

.nav-link.active,
.nav-link:hover {
  color: #2563eb;
}

.console-link {
  padding: 9px 16px;
  color: #fff;
  border-radius: 999px;
  background: #2563eb;
  border: 1px solid #2563eb;
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.18);
}

.headline {
  position: relative;
  z-index: 2;
  width: min(850px, 100%);
  margin: 56px auto 34px;
  text-align: center;
  animation: fade-up 0.7s cubic-bezier(.16, 1, .3, 1) both;
}

.eyebrow {
  width: fit-content;
  margin: 0 auto 14px;
  padding: 7px 14px;
  color: #2563eb;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  background: #eff6ff;
  font-size: 13px;
  font-weight: 700;
}

.headline h1 {
  margin: 0;
  font-size: clamp(36px, 6vw, 64px);
  line-height: 1.08;
  letter-spacing: -0.06em;
  color: #101828;
}

.subtitle {
  width: min(640px, 100%);
  margin: 18px auto 0;
  color: #667085;
  font-size: 17px;
  line-height: 1.8;
}

.billing-switch {
  width: fit-content;
  margin: 28px auto 0;
  padding: 5px;
  display: flex;
  gap: 4px;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.1);
}

.billing-switch button {
  min-width: 82px;
  border: 0;
  border-radius: 999px;
  padding: 10px 18px;
  color: #64748b;
  background: transparent;
  cursor: pointer;
  font-weight: 700;
}

.billing-switch button.selected {
  color: #fff;
  background: #2563eb;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.22);
}

.billing-switch span {
  color: #22c55e;
  font-size: 12px;
}

.plans-wrap {
  position: relative;
  z-index: 2;
  width: min(1120px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  align-items: stretch;
}

.plan-card {
  position: relative;
  overflow: hidden;
  padding: 28px;
  color: #0f172a;
  border: 1px solid #e5eaf3;
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 16px 42px rgba(15, 23, 42, 0.07);
  transition: transform .22s ease, border-color .22s ease, box-shadow .22s ease;
  animation: card-rise 0.7s cubic-bezier(.16, 1, .3, 1) both;
}

.plan-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(180deg, rgba(239, 246, 255, 0.75), transparent 45%);
  opacity: 0;
  transition: opacity .22s ease;
}

.plan-card > * {
  position: relative;
  z-index: 1;
}

.plan-card:hover {
  transform: translateY(-5px);
  border-color: #bfdbfe;
  box-shadow: 0 22px 54px rgba(37, 99, 235, 0.12);
}

.plan-card:hover::before {
  opacity: 1;
}

.plan-card.recommended {
  color: #0f172a;
  border-color: #2563eb;
  background: linear-gradient(180deg, #f8fbff, #fff);
  transform: translateY(-8px);
  box-shadow: 0 24px 64px rgba(37, 99, 235, 0.16);
}

.plan-card.recommended:hover {
  transform: translateY(-12px);
  box-shadow: 0 30px 74px rgba(37, 99, 235, 0.2);
}

.tag {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 5px 10px;
  color: #fff;
  border-radius: 999px;
  background: #2563eb;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.22);
  font-size: 12px;
  font-weight: 800;
}

.plan-card h2 {
  margin: 0;
  font-size: 25px;
  letter-spacing: -0.04em;
}

.desc {
  min-height: 54px;
  margin: 12px 0 28px;
  color: #64748b;
  line-height: 1.7;
}

.price-line {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 24px;
}

.currency {
  font-size: 24px;
  font-weight: 800;
}

.price-line strong {
  font-size: 52px;
  line-height: 1;
  letter-spacing: -0.06em;
  background: linear-gradient(135deg, #101828, #2563eb);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.period {
  color: #64748b;
  font-weight: 700;
}

.buy-button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 46px;
  color: #2563eb;
  border: 1px solid #bfdbfe;
  border-radius: 12px;
  background: #eff6ff;
  font-weight: 800;
  transition: transform .18s ease, box-shadow .18s ease, background .18s ease;
}

.buy-button span {
  margin-left: 8px;
  transition: transform .18s ease;
}

.buy-button:hover {
  transform: translateY(-2px);
  background: #dbeafe;
  box-shadow: 0 14px 26px rgba(37, 99, 235, 0.18);
}

.buy-button:hover span {
  transform: translateX(3px);
}

.buy-button.primary {
  color: #fff;
  border-color: #2563eb;
  background: #2563eb;
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.24);
}

.plan-card ul {
  margin: 26px 0 0;
  padding: 0;
  display: grid;
  gap: 14px;
  list-style: none;
  color: #334155;
  font-size: 14px;
}

.plan-card li::before {
  content: '✓';
  margin-right: 10px;
  color: #2563eb;
  font-weight: 900;
}

.notice,
.faq {
  position: relative;
  z-index: 2;
  width: min(1120px, 100%);
  margin: 42px auto 0;
}

.notice {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 30px;
  border: 1px solid #dbeafe;
  border-radius: 22px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  box-shadow: 0 20px 50px rgba(37, 99, 235, 0.18);
}

.notice h2 {
  margin: 0 0 8px;
  font-size: 24px;
  letter-spacing: -0.04em;
}

.notice p {
  margin: 0;
  color: #cbd5e1;
  line-height: 1.7;
}

.notice-button {
  flex: 0 0 auto;
  padding: 12px 18px;
  color: #0f172a;
  border-radius: 999px;
  background: #fff;
  font-weight: 800;
}

.faq {
  padding-top: 18px;
}

.faq h2 {
  margin: 0 0 18px;
  text-align: center;
  color: #0f172a;
  font-size: 32px;
  letter-spacing: -0.04em;
}

.faq-list {
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.86);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 42px rgba(15, 23, 42, 0.06);
}

.faq-item + .faq-item {
  border-top: 1px solid #e5e7eb;
}

.faq-item summary {
  padding: 20px 24px;
  cursor: pointer;
  font-weight: 800;
}

.faq-item p {
  margin: 0;
  padding: 0 24px 20px;
  color: #64748b;
  line-height: 1.8;
}

@keyframes fade-up {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes card-rise {
  from { opacity: 0; transform: translateY(34px) scale(.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes float-orb {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(24px, 18px, 0) scale(1.08); }
}

@media (max-width: 900px) {
  .plans-wrap {
    grid-template-columns: 1fr;
  }

  .plan-card.recommended {
    transform: none;
  }

  .notice {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .nav-links {
    gap: 12px;
  }

  .nav-link {
    display: none;
  }

  .headline {
    margin-top: 34px;
  }

  .plan-card {
    padding: 24px;
  }
}
</style>
