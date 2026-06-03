<template>
  <div class="landing">
    <header class="navbar">
      <div class="nav-inner">
        <NuxtLink to="/" class="brand">New API</NuxtLink>
        <div class="nav-links">
          <NuxtLink to="/" class="nav-link">首页</NuxtLink>
          <NuxtLink to="/plans" class="nav-link">套餐</NuxtLink>
          <NuxtLink to="/plans" class="nav-link">文档</NuxtLink>
          <NuxtLink to="/plans" class="nav-link">控制台</NuxtLink>
        </div>
      </div>
    </header>

    <section class="hero">
      <div class="hero-bg" />
      <div class="hero-content">
        <h1 class="hero-title">New API</h1>
        <p class="hero-subtitle">Unified AI API gateway and admin dashboard</p>
        <div class="hero-actions">
          <a href="#pricing" class="btn btn-primary">Get Started</a>
          <a href="#features" class="btn btn-outline">View Docs</a>
        </div>
      </div>
    </section>

    <section id="features" class="section features-section">
      <div class="container">
        <h2 class="section-title">特性</h2>
        <div class="features-grid">
          <div v-for="f in features" :key="f.title" class="feature-card">
            <span class="feature-icon">{{ f.icon }}</span>
            <h3 class="feature-title">{{ f.title }}</h3>
            <p class="feature-desc">{{ f.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <section id="pricing" class="section pricing-section">
      <div class="container">
        <h2 class="section-title">Pricing Plans</h2>
        <div class="pricing-grid">
          <div v-for="p in plans" :key="p.name" class="pricing-card" :class="{ popular: p.popular }">
            <div v-if="p.popular" class="popular-badge">推荐</div>
            <h3 class="plan-name">{{ p.name }}</h3>
            <div class="plan-price">
              <span class="price-amount">{{ p.price }}</span>
              <span v-if="p.period" class="price-period">/{{ p.period }}</span>
            </div>
            <ul class="plan-features">
              <li v-for="f in p.features" :key="f" class="plan-feature-item">{{ f }}</li>
            </ul>
            <a href="/plans" class="btn" :class="p.popular ? 'btn-primary' : 'btn-outline'">立即开始</a>
          </div>
        </div>
      </div>
    </section>

    <section class="section faq-section">
      <div class="container">
        <h2 class="section-title">Frequently asked questions</h2>
        <div class="faq-list">
          <div v-for="(item, i) in faqs" :key="i" class="faq-item">
            <button class="faq-question" @click="toggleFaq(i)">
              <span>{{ item.q }}</span>
              <span class="faq-arrow" :class="{ open: openFaq === i }">&#9662;</span>
            </button>
            <div v-show="openFaq === i" class="faq-answer">{{ item.a }}</div>
          </div>
        </div>
      </div>
    </section>

    <section class="section cta-section">
      <div class="cta-bg" />
      <div class="cta-content">
        <h2 class="cta-title">Ready to get started?</h2>
        <a href="/plans" class="btn btn-cta">Start building for free</a>
      </div>
    </section>

    <footer class="footer">
      <div class="footer-inner">
        <span>&copy; 2026 New API. All rights reserved.</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
const openFaq = ref<number | null>(null)

function toggleFaq(i: number) {
  openFaq.value = openFaq.value === i ? null : i
}

const features = [
  { icon: '🧠', title: 'Multi-Model Support', desc: 'Access GPT-4, Claude, Gemini and more through a single unified API gateway.' },
  { icon: '⚡', title: 'Rate Limiting', desc: 'Configure per-user and per-key rate limits to protect your infrastructure.' },
  { icon: '📊', title: 'Usage Analytics', desc: 'Track token usage, request volumes, and costs with real-time dashboards.' },
  { icon: '🔑', title: 'Key Management', desc: 'Generate, rotate, and revoke API keys with granular permissions.' },
  { icon: '📝', title: 'Logging', desc: 'Complete request and response logging with search and filtering capabilities.' },
  { icon: '🖥️', title: 'Admin Dashboard', desc: 'Full administrative interface for managing users, keys, and system settings.' }
]

const plans = [
  {
    name: 'Starter',
    price: '$19',
    period: 'mo',
    popular: false,
    features: ['5,000 requests/day', '3 API keys', 'Basic analytics', 'Email support', '1 model endpoint']
  },
  {
    name: 'Pro',
    price: '$49',
    period: 'mo',
    popular: true,
    features: ['50,000 requests/day', 'Unlimited API keys', 'Advanced analytics', 'Priority support', 'All model endpoints', 'Custom rate limits']
  },
  {
    name: 'Enterprise',
    price: 'Custom',
    period: null,
    popular: false,
    features: ['Unlimited requests', 'Dedicated infrastructure', 'Custom integrations', '24/7 phone support', 'SLA guarantee', 'On-premise deployment']
  }
]

const faqs = [
  { q: '什么是 New API？', a: 'New API 是一个统一的 AI API 网关和管理后台，让您可以通过单个接口访问多种 AI 模型，并提供密钥管理、用量监控、速率限制等企业级功能。' },
  { q: '如何开始使用？', a: '注册账号后，您可以立即创建一个项目并生成 API 密钥。我们提供 Starter 免费套餐供您试用，无需绑定信用卡。' },
  { q: '支持哪些 AI 模型？', a: '我们支持 OpenAI GPT-4、Anthropic Claude、Google Gemini、Meta Llama 等主流模型，并且持续增加新的模型提供商。' },
  { q: '如何计费？', a: '我们提供按月的订阅套餐。Starter 套餐 $19/月，Pro 套餐 $49/月。Enterprise 套餐根据您的需求定制价格。所有套餐无隐藏费用。' },
  { q: '你们提供什么样的技术支持？', a: 'Starter 用户享受邮件支持，Pro 用户享受优先支持，Enterprise 用户享受 24/7 电话支持和专属客户经理。' }
]
</script>

<style scoped>
.landing {
  min-height: 100vh;
  background: #fff;
  color: #0a0a0a;
  font-family: "Public Sans", ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

/* Navbar */
.navbar {
  position: sticky;
  top: 0;
  z-index: 50;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.nav-inner {
  width: min(1120px, calc(100% - 40px));
  height: 72px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.02em;
  text-decoration: none;
  color: #0a0a0a;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  color: #606060;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #0a0a0a;
}

/* Hero */
.hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100px 20px 80px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #e0f2fe 0%, #fff 50%, #ede9fe 100%);
  z-index: 0;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 720px;
}

.hero-title {
  font-size: clamp(48px, 8vw, 80px);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.1;
  margin: 0 0 16px;
  color: #0a0a0a;
  animation: fadeUp 0.6s ease-out;
}

.hero-subtitle {
  font-size: clamp(18px, 2.5vw, 24px);
  color: #606060;
  margin: 0 0 40px;
  line-height: 1.5;
  animation: fadeUp 0.6s ease-out 0.15s both;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
  animation: fadeUp 0.6s ease-out 0.3s both;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 28px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.btn-primary {
  background: #0a0a0a;
  color: #fff;
  border-color: #0a0a0a;
}

.btn-primary:hover {
  background: #262626;
}

.btn-outline {
  background: #fff;
  color: #0a0a0a;
  border-color: #d4d4d4;
}

.btn-outline:hover {
  border-color: #0a0a0a;
}

.btn-cta {
  background: #fff;
  color: #0a0a0a;
  font-weight: 700;
  padding: 14px 36px;
}

.btn-cta:hover {
  background: #f5f5f5;
}

/* Sections */
.section {
  padding: 80px 0;
}

.container {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto;
}

.section-title {
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 700;
  letter-spacing: -0.02em;
  text-align: center;
  margin: 0 0 48px;
  color: #0a0a0a;
}

/* Features */
.features-section {
  background: #fff;
}

.features-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

@media (min-width: 640px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.feature-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 28px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  animation: fadeUp 0.6s ease-out both;
}

.feature-card:nth-child(1) { animation-delay: 0s; }
.feature-card:nth-child(2) { animation-delay: 0.08s; }
.feature-card:nth-child(3) { animation-delay: 0.16s; }
.feature-card:nth-child(4) { animation-delay: 0.24s; }
.feature-card:nth-child(5) { animation-delay: 0.32s; }
.feature-card:nth-child(6) { animation-delay: 0.4s; }

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 12px;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #0a0a0a;
}

.feature-desc {
  font-size: 14px;
  line-height: 1.6;
  color: #606060;
  margin: 0;
}

/* Pricing */
.pricing-section {
  background: #f5f5f5;
}

.pricing-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  align-items: start;
}

@media (min-width: 640px) {
  .pricing-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .pricing-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.pricing-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 32px 28px;
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  animation: fadeUp 0.6s ease-out both;
}

.pricing-card:nth-child(2) { animation-delay: 0.1s; }
.pricing-card:nth-child(3) { animation-delay: 0.2s; }

.pricing-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.pricing-card.popular {
  border-color: #0a0a0a;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.popular-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  background: #0a0a0a;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 4px 16px;
  border-radius: 20px;
  letter-spacing: 0.05em;
}

.plan-name {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 12px;
  color: #0a0a0a;
}

.plan-price {
  margin-bottom: 24px;
}

.price-amount {
  font-size: 40px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: #0a0a0a;
}

.price-period {
  font-size: 16px;
  color: #606060;
}

.plan-features {
  list-style: none;
  padding: 0;
  margin: 0 0 28px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-feature-item {
  font-size: 14px;
  color: #606060;
  padding-left: 20px;
  position: relative;
}

.plan-feature-item::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #0a0a0a;
  font-weight: 700;
}

.pricing-card .btn {
  width: 100%;
}

/* FAQ */
.faq-section {
  background: #fff;
}

.faq-list {
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 0;
  border-top: 1px solid #e8e8e8;
}

.faq-item {
  border-bottom: 1px solid #e8e8e8;
}

.faq-question {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: #0a0a0a;
  text-align: left;
  font-family: inherit;
}

.faq-question:hover {
  color: #404040;
}

.faq-arrow {
  font-size: 14px;
  transition: transform 0.3s;
  color: #a0a0a0;
}

.faq-arrow.open {
  transform: rotate(180deg);
}

.faq-answer {
  padding: 0 0 20px;
  font-size: 15px;
  line-height: 1.7;
  color: #606060;
}

/* CTA */
.cta-section {
  position: relative;
  padding: 100px 20px;
  text-align: center;
  overflow: hidden;
}

.cta-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0a0a0a 0%, #262626 100%);
  z-index: 0;
}

.cta-content {
  position: relative;
  z-index: 1;
}

.cta-title {
  font-size: clamp(32px, 5vw, 48px);
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #fff;
  margin: 0 0 32px;
}

/* Footer */
.footer {
  background: #fff;
  border-top: 1px solid #e8e8e8;
}

.footer-inner {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto;
  padding: 24px 0;
  text-align: center;
  font-size: 14px;
  color: #a0a0a0;
}

/* Animation */
@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
