<template>
  <main class="landing">
    <!-- ======== HERO ======== -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-gradient-base" />
        <div class="hero-gradient-orb" />
        <div class="hero-gradient-orb hero-gradient-orb-2" />
        <div class="hero-grid" />
        <AnimParticles
          :particle-count="35"
          :connect-distance="100"
          :line-opacity="0.06"
          particle-color="99, 102, 241"
          line-color="99, 102, 241"
          :speed="0.2"
        />
      </div>

      <div class="hero-content">
        <AnimReveal animation="fade-up">
          <div class="hero-badge">
            <span class="hero-badge-dot" />
            AI 模型 API 服务平台
          </div>
        </AnimReveal>

        <AnimReveal animation="fade-up" :delay="150">
          <h1 class="hero-title">
            <span class="hero-title-line">顶级 AI 模型</span>
            <span class="hero-title-line hero-title-accent">即刻接入</span>
          </h1>
        </AnimReveal>

        <AnimReveal animation="fade-up" :delay="300">
          <p class="hero-sub">
            稳定、高性能的 API 服务平台，支持 GPT、Claude、Grok 等主流模型。<br>
            毫秒级响应，企业级可靠性。
          </p>
        </AnimReveal>

        <AnimReveal animation="fade-up" :delay="450">
          <div class="hero-actions">
            <a href="/login" class="btn-primary hero-cta">
              开始使用
              <svg class="btn-arrow" viewBox="0 0 16 16" fill="none"><path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </a>
            <a href="#models" class="btn-outline hero-cta-outline">查看模型</a>
          </div>
        </AnimReveal>

        <AnimReveal animation="scale-in" :delay="600">
          <div class="hero-terminal">
            <div class="term-header">
              <div class="term-dots">
                <span class="term-dot" />
                <span class="term-dot" />
                <span class="term-dot" />
              </div>
              <span class="term-title">api/chat/completions</span>
            </div>
            <pre class="term-body"><code><span class="tk-comment"># 一行代码接入</span>
<span class="tk-const">curl</span> https://api.zhangyuan.ai/v1/chat/completions <span class="tk-flag">\</span>
  <span class="tk-flag">-H</span> <span class="tk-string">"Authorization: Bearer sk-xxx"</span> <span class="tk-flag">\</span>
  <span class="tk-flag">-d</span> <span class="tk-string">'{
    "model": "gpt-5.5",
    "messages": [{"role": "user", "content": "Hello"}]
  }'</span></code></pre>
            <div class="term-glow" />
          </div>
        </AnimReveal>
      </div>
    </section>

    <!-- ======== MODELS ======== -->
    <section id="models" class="models">
      <AnimReveal animation="fade-up">
        <div class="section-label">MODELS</div>
        <h2 class="section-title">主流模型 · 一站接入</h2>
        <p class="section-sub">覆盖全球顶级 AI 模型，统一 API 接入，无需四处对接</p>
      </AnimReveal>
      <div class="models-grid">
        <AnimReveal v-for="(m, i) in models" :key="m.name" animation="scale-in" :delay="i * 80">
          <div class="model-card">
            <div class="model-icon" v-html="m.icon" />
            <div class="model-info">
              <div class="model-head">
                <h3>{{ m.name }}</h3>
                <span class="model-badge">{{ m.badge }}</span>
              </div>
              <p class="model-desc">{{ m.desc }}</p>
              <div class="model-price">{{ m.price }}</div>
            </div>
          </div>
        </AnimReveal>
      </div>
    </section>

    <!-- ======== STATS ======== -->
    <section class="stats">
      <div class="stats-bg" />
      <div class="stats-row">
        <AnimReveal v-for="(s, i) in stats" :key="s.label" animation="scale-in" :delay="i * 100">
          <div class="stat-item">
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </AnimReveal>
      </div>
    </section>

    <!-- ======== FEATURES ======== -->
    <section id="features" class="features">
      <AnimReveal animation="fade-up">
        <div class="section-label">FEATURES</div>
        <h2 class="section-title">为什么选择我们</h2>
        <p class="section-sub">为开发者打造的企业级 AI 接入体验</p>
      </AnimReveal>
      <div class="features-grid">
        <AnimReveal v-for="(f, i) in features" :key="f.title" animation="fade-up" :delay="i * 80">
          <div class="feature-card">
            <div class="feature-icon" v-html="f.icon" />
            <h3>{{ f.title }}</h3>
            <p>{{ f.desc }}</p>
          </div>
        </AnimReveal>
      </div>
    </section>

    <!-- ======== PRICING ======== -->
    <section id="pricing" class="pricing">
      <AnimReveal animation="fade-up">
        <div class="section-label">PRICING</div>
        <h2 class="section-title">透明定价</h2>
        <p class="section-sub">按量计费，无需预充大额，随时接入</p>
      </AnimReveal>
      <div class="pricing-row">
        <AnimReveal v-for="(p, i) in plans" :key="p.name" animation="fade-up" :delay="i * 120">
          <div :class="['plan-card', { featured: p.featured }]">
            <div v-if="p.featured" class="plan-tag">推荐</div>
            <h3 class="plan-name">{{ p.name }}</h3>
            <div class="plan-price">
              <span class="plan-amount">{{ p.price }}</span>
              <span v-if="p.unit" class="plan-unit">/ {{ p.unit }}</span>
            </div>
            <p class="plan-desc">{{ p.desc }}</p>
            <ul class="plan-features">
              <li v-for="item in p.features" :key="item">{{ item }}</li>
            </ul>
            <a href="/login" :class="['btn-plan', { 'btn-plan-primary': p.featured }, { 'btn-plan-outline': !p.featured }]">
              {{ p.cta }}
            </a>
          </div>
        </AnimReveal>
      </div>
    </section>

    <!-- ======== CTA ======== -->
    <section class="cta-section">
      <AnimReveal animation="scale-in">
        <div class="cta-card">
          <h2>准备好开始了吗？</h2>
          <p>注册即享 API 额度，立即体验顶级 AI 模型接入。</p>
          <a href="/login" class="btn-primary btn-lg cta-btn-inline">
            立即使用
            <svg class="btn-arrow" viewBox="0 0 16 16" fill="none"><path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </a>
        </div>
      </AnimReveal>
    </section>
  </main>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default' })

const models = [
  {
    name: 'GPT-5.5',
    badge: '旗舰',
    desc: 'OpenAI 最新旗舰模型，推理能力最强，支持多模态',
    price: '按官方汇率计费',
    icon: '<svg viewBox="0 0 32 32" fill="none"><rect width="32" height="32" rx="8" fill="#10a37f"/><path d="M8 12h6l3 8-3-8h6l-4 8h6l-4 8" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/></svg>',
  },
  {
    name: 'GPT-5.4',
    badge: '稳定',
    desc: '性价比最优的 GPT 模型，适合生产环境日常调用',
    price: '按官方汇率计费',
    icon: '<svg viewBox="0 0 32 32" fill="none"><rect width="32" height="32" rx="8" fill="#10a37f"/><path d="M8 12h6l3 8-3-8h6l-4 8h6l-4 8" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none" opacity=".7"/></svg>',
  },
  {
    name: 'Claude',
    badge: '同步',
    desc: 'Anthropic 出品，长上下文理解与安全对齐领先',
    price: '按官方汇率计费',
    icon: '<svg viewBox="0 0 32 32" fill="none"><rect width="32" height="32" rx="8" fill="#d97706"/><path d="M16 6c-3 0-5 2-5 6 0 2 1 4 3 5-2 1-3 3-3 5 0 3 2 5 5 5s5-2 5-5c0-2-1-4-3-5 2-1 3-3 3-5 0-4-2-6-5-6z" fill="#fff"/></svg>',
  },
  {
    name: 'Grok',
    badge: '想象',
    desc: 'xAI 推出的实时知识增强模型，图像生成能力突出',
    price: '按官方汇率计费',
    icon: '<svg viewBox="0 0 32 32" fill="none"><rect width="32" height="32" rx="8" fill="#1d1d1d"/><text x="16" y="22" text-anchor="middle" fill="#fff" font-size="20" font-weight="900">G</text></svg>',
  },
  {
    name: 'Gemini',
    badge: '视觉',
    desc: 'Google 多模态模型，原生支持超长上下文窗口',
    price: '按官方汇率计费',
    icon: '<svg viewBox="0 0 32 32" fill="none"><rect width="32" height="32" rx="8" fill="#4285f4"/><path d="M16 6l12 6v10l-12 6-12-6V12z" fill="none" stroke="#fff" stroke-width="1.5"/></svg>',
  },
  {
    name: 'Codex',
    badge: '特惠',
    desc: 'OpenAI 代码专用模型，编程辅助与 Code Agent 首选',
    price: '周卡特惠 4/天',
    icon: '<svg viewBox="0 0 32 32" fill="none"><rect width="32" height="32" rx="8" fill="#6366f1"/><path d="M10 12l-4 4 4 4M22 12l4 4-4 4M19 9l-6 14" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
  },
]

const stats = [
  { value: '50+', label: '接入模型' },
  { value: '99.9%', label: '服务可用性' },
  { value: '10K+', label: '活跃开发者' },
  { value: '100ms', label: '平均延迟' },
]

const features = [
  {
    title: '稳定可靠',
    desc: '多区域部署，自动故障切换，保障服务高可用',
    icon: '<svg viewBox="0 0 40 40" fill="none"><rect width="40" height="40" rx="10" fill="#eef2ff"/><path d="M20 10v8l4 4M28 20a8 8 0 11-16 0 8 8 0 0116 0z" stroke="#6366f1" stroke-width="2" stroke-linecap="round"/><path d="M12 20h2M26 20h2M20 12v2M20 26v2" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round" opacity=".4"/></svg>',
  },
  {
    title: '价格透明',
    desc: '按官方汇率实时计价，无隐藏费用，用量明细可查',
    icon: '<svg viewBox="0 0 40 40" fill="none"><rect width="40" height="40" rx="10" fill="#eef2ff"/><circle cx="20" cy="18" r="10" stroke="#6366f1" stroke-width="2"/><path d="M20 13v10M16 18h8" stroke="#6366f1" stroke-width="2" stroke-linecap="round"/></svg>',
  },
  {
    title: '快速接入',
    desc: '标准 OpenAI SDK 兼容，3 行代码即可完成接入',
    icon: '<svg viewBox="0 0 40 40" fill="none"><rect width="40" height="40" rx="10" fill="#eef2ff"/><path d="M14 20l4 4 8-8" stroke="#6366f1" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/></svg>',
  },
  {
    title: '技术支持',
    desc: '开发者社区 + 专属技术支持，7x24 小时响应',
    icon: '<svg viewBox="0 0 40 40" fill="none"><rect width="40" height="40" rx="10" fill="#eef2ff"/><path d="M20 10c-6 0-9 3-9 9 0 3 1.5 5.5 4 7l-1 4 4.5-2c.5.1 1 .2 1.5.2 6 0 9-3 9-9 0-6-3-9.2-9-9.2z" stroke="#6366f1" stroke-width="2"/><circle cx="16" cy="19" r="1.5" fill="#6366f1"/><circle cx="20" cy="19" r="1.5" fill="#6366f1"/><circle cx="24" cy="19" r="1.5" fill="#6366f1"/></svg>',
  },
  {
    title: '持续更新',
    desc: '新模型发布后第一时间接入，始终保持最新能力',
    icon: '<svg viewBox="0 0 40 40" fill="none"><rect width="40" height="40" rx="10" fill="#eef2ff"/><path d="M20 8v6M20 26v6M8 20h6M26 20h6" stroke="#6366f1" stroke-width="2" stroke-linecap="round"/><circle cx="20" cy="20" r="5" stroke="#6366f1" stroke-width="2"/><path d="M20 15v5l3 3" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round"/></svg>',
  },
  {
    title: '安全可靠',
    desc: '企业级数据加密，API Key 安全隔离，日志审计完善',
    icon: '<svg viewBox="0 0 40 40" fill="none"><rect width="40" height="40" rx="10" fill="#eef2ff"/><rect x="10" y="16" width="20" height="16" rx="3" stroke="#6366f1" stroke-width="2"/><path d="M16 16v-4a4 4 0 118 0v4" stroke="#6366f1" stroke-width="2" stroke-linecap="round"/><circle cx="20" cy="24" r="2" fill="#6366f1"/></svg>',
  },
]

const plans = [
  {
    name: '基础',
    price: '按量',
    unit: 'token',
    desc: '适合个人开发者和轻度使用',
    features: ['所有模型可用', '按 token 计费', '社区支持', 'API Key 管理'],
    cta: '免费注册',
    featured: false,
  },
  {
    name: 'Pro',
    price: '特惠',
    unit: '月',
    desc: '适合专业开发者和中小团队',
    features: ['所有模型可用', 'Plus+Pro 号池 0.16x', '缓存命中率 90%+', '优先技术支持', '用量报表'],
    cta: '立即升级',
    featured: true,
  },
  {
    name: '企业',
    price: '定制',
    unit: '',
    desc: '适合大型团队和业务对接',
    features: ['专属号池', '企业级 SLA', '独立部署', '7x24 专属支持', '定制计费方案'],
    cta: '联系销售',
    featured: false,
  },
]
</script>

<style scoped>
/* ===== BASE ===== */
.landing {
  min-height: 100vh;
  color: var(--vp-c-text);
  background: var(--vp-c-bg);
  font-family: var(--vp-font-family-base);
  -webkit-font-smoothing: antialiased;
  transition: background var(--vp-t-bg), color var(--vp-t-color);
}

.section-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.15em;
  color: var(--vp-c-brand);
  text-align: center;
  margin-bottom: 12px;
}

.section-title {
  margin: 0 0 12px;
  text-align: center;
  font-size: clamp(32px, 4vw, 48px);
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--vp-c-text);
  line-height: 1.1;
}

.section-sub {
  margin: 0 auto 56px;
  max-width: 480px;
  text-align: center;
  font-size: 18px;
  line-height: 1.6;
  color: var(--vp-c-text-2);
}

/* ===== HERO ===== */
.hero {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 100px 24px 80px;
  background: #070B14;
  clip-path: polygon(0% 0%, 100% 0%, 100% 90%, 0% 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.hero-gradient-base {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 50% at 50% 0%, rgba(99, 102, 241, 0.15), transparent),
    radial-gradient(ellipse 60% 50% at 80% 40%, rgba(139, 92, 246, 0.08), transparent),
    radial-gradient(ellipse 50% 40% at 20% 60%, rgba(59, 130, 246, 0.06), transparent);
  animation: hero-glow 8s ease-in-out infinite alternate;
}

@keyframes hero-glow {
  0% { opacity: 0.7; }
  100% { opacity: 1; }
}

.hero-gradient-orb {
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.08), transparent 60%);
  top: -200px;
  right: -100px;
  animation: orb-drift 20s ease-in-out infinite alternate;
}

.hero-gradient-orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.06), transparent 60%);
  top: auto;
  bottom: -150px;
  left: -150px;
  right: auto;
  animation: orb-drift-2 25s ease-in-out infinite alternate;
}

@keyframes orb-drift {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(-60px, 60px) scale(1.15); }
}

@keyframes orb-drift-2 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(60px, -60px) scale(1.1); }
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(99, 102, 241, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.04) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(ellipse 50% 60% at 50% 50%, #000 20%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse 50% 60% at 50% 50%, #000 20%, transparent 70%);
  animation: grid-pulse 6s ease-in-out infinite;
}

@keyframes grid-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 0.8; }
}

.hero::after {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 820px;
  width: 100%;
}

/* ===== Hero Badge ===== */
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 28px;
  padding: 7px 18px;
  font-size: 13px;
  font-weight: 600;
  color: #a5b4fc;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 999px;
  backdrop-filter: blur(8px);
}

.hero-badge-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22c55e;
  animation: badge-pulse 2s ease-in-out infinite;
}

@keyframes badge-pulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.5); }
  50% { opacity: 0.7; box-shadow: 0 0 0 6px rgba(34, 197, 94, 0); }
}

/* ===== Hero Title ===== */
.hero-title {
  margin: 0 0 24px;
  line-height: 1.05;
}

.hero-title-line {
  display: block;
  font-size: clamp(48px, 7vw, 80px);
  font-weight: 800;
  letter-spacing: 0.02em;
  color: #fff;
}

.hero-title-accent {
  letter-spacing: -0.05em;
  background: linear-gradient(135deg, #818cf8, #a78bfa, #6366f1, #818cf8);
  background-size: 300% 300%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: accent-shimmer 6s ease-in-out infinite;
  filter: drop-shadow(0 0 24px rgba(129, 140, 248, 0.25));
}

@keyframes accent-shimmer {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

/* ===== Hero subtitle ===== */
.hero-sub {
  margin: 0 auto 40px;
  max-width: 560px;
  font-size: 18px;
  line-height: 1.7;
  color: #94a3b8;
}

/* ===== Hero Actions ===== */
.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.hero-cta {
  padding: 14px 32px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: var(--vp-c-brand-gradient);
  border: none;
  border-radius: 12px;
  text-decoration: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.25s ease;
  box-shadow: 0 4px 24px rgba(100, 108, 255, 0.3);
}

.hero-cta:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(100, 108, 255, 0.45);
}

.hero-cta-outline {
  padding: 14px 32px;
  font-size: 16px;
  font-weight: 600;
  color: #cbd5e1;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  text-decoration: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  transition: all 0.25s ease;
}

.hero-cta-outline:hover {
  border-color: rgba(100, 108, 255, 0.4);
  background: rgba(100, 108, 255, 0.08);
  color: #fff;
  transform: translateY(-2px);
}

.btn-arrow {
  width: 16px;
  height: 16px;
  transition: transform 0.2s ease;
}

.hero-cta:hover .btn-arrow {
  transform: translateX(3px);
}

/* ===== Terminal ===== */
.hero-terminal {
  position: relative;
  margin-top: 56px;
  width: min(580px, calc(100vw - 48px));
  text-align: left;
  border-radius: 12px;
  border: 1px solid rgba(100, 108, 255, 0.15);
  background: rgba(12, 17, 29, 0.95);
  overflow: hidden;
  transition: border-color 0.3s ease;
}

.hero-terminal:hover {
  border-color: rgba(100, 108, 255, 0.3);
}

.term-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.term-dots {
  display: flex;
  gap: 6px;
}

.term-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
}

.term-dot:nth-child(1) { background: #ff5f57; }
.term-dot:nth-child(2) { background: #fdbc40; }
.term-dot:nth-child(3) { background: #33c748; }

.term-title {
  margin-left: 8px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.25);
  font-family: var(--vp-font-family-mono);
}

.term-body {
  margin: 0;
  padding: 20px 24px;
  font-family: var(--vp-font-family-mono);
  font-size: 13px;
  line-height: 1.75;
  color: #e2e8f0;
  overflow-x: auto;
}

.term-body code {
  font: inherit;
}

.tk-comment { color: #475569; }
.tk-const   { color: #818cf8; }
.tk-flag    { color: #6b7280; }
.tk-string  { color: #6ee7b7; }

.term-glow {
  position: absolute;
  inset: 0;
  border-radius: 12px;
  pointer-events: none;
  background: linear-gradient(135deg, rgba(100, 108, 255, 0.03), transparent 50%);
}

/* ===== MODELS ===== */
.models {
  padding: 60px 24px 80px;
  max-width: 1200px;
  margin: -40px auto 0;
  position: relative;
  z-index: 1;
}

.models-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.model-card {
  display: flex;
  gap: 18px;
  padding: 24px;
  border-radius: 16px;
  border: 1px solid var(--vp-c-brand-dimmer);
  background: var(--vp-c-bg-elavate);
  backdrop-filter: blur(8px);
  transform-style: preserve-3d;
  perspective: 800px;
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  will-change: transform;
  cursor: default;
}

.model-card:hover {
  transform: perspective(800px) rotateY(-2deg) rotateX(2deg) translateY(-4px);
  box-shadow: 0 12px 40px rgba(100, 108, 255, 0.1);
  border-color: rgba(100, 108, 255, 0.25);
}

.model-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
}

.model-icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.model-info {
  min-width: 0;
}

.model-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.model-head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.model-badge {
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
  color: var(--vp-c-brand);
  background: var(--vp-c-brand-dimmer);
  border-radius: 999px;
}

.model-desc {
  margin: 0 0 8px;
  font-size: 14px;
  line-height: 1.5;
  color: var(--vp-c-text-2);
}

.model-price {
  font-size: 13px;
  font-weight: 600;
  color: var(--vp-c-brand);
}

/* ===== STATS ===== */
.stats {
  position: relative;
  padding: 80px 24px;
  overflow: hidden;
}

.stats-bg {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(135deg, rgba(100, 108, 255, 0.04), rgba(139, 92, 246, 0.03), rgba(100, 108, 255, 0.02)),
    repeating-linear-gradient(45deg, transparent, transparent 2px, rgba(100, 108, 255, 0.008) 2px, rgba(100, 108, 255, 0.008) 4px);
  background-size: 200% 200%, auto;
  animation: stats-drift 12s ease-in-out infinite alternate;
}

@keyframes stats-drift {
  0%   { background-position: 0% 0%; }
  50%  { background-position: 100% 100%; }
  100% { background-position: 0% 50%; }
}

.stats-row {
  position: relative;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  max-width: 900px;
  margin: 0 auto;
  text-align: center;
}

.stat-item {
  position: relative;
  padding: 0 16px;
}

.stat-item::after {
  content: '';
  position: absolute;
  right: 0;
  top: 20%;
  bottom: 20%;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(100, 108, 255, 0.12), transparent);
}

.stat-item:last-child::after {
  display: none;
}

.stat-value {
  font-size: clamp(36px, 5vw, 52px);
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--vp-c-brand);
  line-height: 1.1;
}

.stat-label {
  margin-top: 4px;
  font-size: 15px;
  color: var(--vp-c-text-2);
  font-weight: 500;
}

/* ===== FEATURES ===== */
.features {
  position: relative;
  padding: 80px 24px;
  max-width: 1100px;
  margin: 0 auto;
}

.features::before {
  content: '';
  position: absolute;
  top: 0;
  left: 10%;
  right: 10%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(100, 108, 255, 0.15), transparent);
  pointer-events: none;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.feature-card {
  padding: 32px 28px;
  border-radius: 16px;
  border: 1px solid var(--vp-c-border);
  background: var(--vp-c-bg-elavate);
  transform-style: preserve-3d;
  perspective: 800px;
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  will-change: transform;
}

.feature-card:hover {
  transform: perspective(800px) rotateY(-1.5deg) rotateX(1.5deg) translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.06);
  border-color: rgba(100, 108, 255, 0.2);
}

.feature-icon {
  width: 40px;
  height: 40px;
  margin-bottom: 20px;
}

.feature-icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.feature-card h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
}

.feature-card p {
  margin: 0;
  font-size: 15px;
  line-height: 1.65;
  color: var(--vp-c-text-2);
}

/* ===== PRICING ===== */
.pricing {
  position: relative;
  padding: 80px 24px;
  background: linear-gradient(180deg, rgba(100, 108, 255, 0.04), transparent);
}

.pricing::before {
  content: '';
  position: absolute;
  top: 0;
  left: 10%;
  right: 10%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(100, 108, 255, 0.15), transparent);
  pointer-events: none;
}

.pricing-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  max-width: 1000px;
  margin: 0 auto;
  align-items: start;
}

.plan-card {
  position: relative;
  padding: 36px 28px;
  border-radius: 16px;
  border: 1px solid var(--vp-c-border);
  background: var(--vp-c-bg-elavate);
  text-align: center;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  will-change: transform;
}

.plan-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 48px var(--vp-shadow-2);
}

.plan-card.featured {
  border-color: transparent;
  box-shadow: 0 8px 40px rgba(100, 108, 255, 0.12);
  transform: scale(1.03);
  background-clip: padding-box;
}

.plan-card.featured::before {
  content: '';
  position: absolute;
  inset: -1px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--vp-c-brand), #a78bfa, var(--vp-c-brand), #818cf8, var(--vp-c-brand));
  background-size: 300% 300%;
  animation: border-shine 3s ease-in-out infinite;
  z-index: -1;
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  padding: 1px;
}

@keyframes border-shine {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.plan-card.featured:hover {
  transform: scale(1.03) translateY(-6px);
  box-shadow: 0 20px 60px rgba(100, 108, 255, 0.18);
}

.plan-tag {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 16px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: var(--vp-c-brand-gradient);
  border-radius: 999px;
}

.plan-name {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 700;
}

.plan-price {
  margin-bottom: 12px;
}

.plan-amount {
  font-size: 40px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--vp-c-text);
}

.plan-unit {
  font-size: 15px;
  color: var(--vp-c-text-3);
}

.plan-desc {
  margin: 0 0 24px;
  font-size: 15px;
  color: var(--vp-c-text-2);
}

.plan-features {
  list-style: none;
  margin: 0 0 28px;
  padding: 0;
  text-align: left;
}

.plan-features li {
  padding: 8px 0;
  font-size: 14px;
  color: var(--vp-c-text);
  border-bottom: 1px solid var(--vp-c-divider);
}

.plan-features li::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 10px;
  border-radius: 50%;
  background: var(--vp-c-brand);
  vertical-align: middle;
}

.plan-features li:last-child {
  border-bottom: 0;
}

.btn-plan {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-plan-outline {
  color: var(--vp-c-text);
  background: var(--vp-c-bg-soft);
  border: 1px solid var(--vp-c-border);
}

.btn-plan-outline:hover {
  border-color: var(--vp-c-brand);
  background: var(--vp-c-brand-dimmer);
  color: var(--vp-c-brand);
}

.btn-plan-primary {
  color: #fff;
  background: var(--vp-c-brand-gradient);
  border: none;
  box-shadow: 0 4px 16px rgba(100, 108, 255, 0.25);
}

.btn-plan-primary:hover {
  box-shadow: 0 8px 24px rgba(100, 108, 255, 0.4);
  transform: translateY(-1px);
}

/* ===== CTA ===== */
.cta-section {
  padding: 80px 24px;
}

.cta-card {
  max-width: 680px;
  margin: 0 auto;
  padding: 64px 48px;
  text-align: center;
  border-radius: 24px;
  background: linear-gradient(135deg, #070B14, #0f1424);
  border: 1px solid rgba(100, 108, 255, 0.15);
  position: relative;
  overflow: hidden;
}

.cta-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 24px;
  background:
    radial-gradient(ellipse 80% 50% at 50% 0%, rgba(100, 108, 255, 0.08), transparent),
    radial-gradient(ellipse 40% 40% at 80% 80%, rgba(139, 92, 246, 0.05), transparent),
    linear-gradient(135deg, transparent 30%, rgba(100, 108, 255, 0.06) 50%, transparent 70%);
  background-size: 100% 100%, 100% 100%, 200% 200%;
  animation: cta-sheen 6s ease-in-out infinite;
  pointer-events: none;
}

@keyframes cta-sheen {
  0%, 100% { background-position: 0% 0%, 0% 0%, 0% 50%; }
  50% { background-position: 0% 0%, 0% 0%, 100% 50%; }
}

.cta-card::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 24px;
  opacity: 0.025;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 0;
}

.cta-card h2 {
  margin: 0 0 16px;
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 800;
  letter-spacing: -0.03em;
  color: #fff;
  position: relative;
}

.cta-card p {
  margin: 0 auto 32px;
  max-width: 440px;
  font-size: 17px;
  line-height: 1.6;
  color: #94a3b8;
  position: relative;
}

.cta-btn-inline {
  position: relative;
}

.btn-lg {
  padding: 14px 36px;
  font-size: 16px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.btn-lg:hover {
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 8px 32px rgba(100, 108, 255, 0.4);
}

.cta-btn-inline:hover .btn-arrow {
  transform: translateX(3px);
}

/* ===== RESPONSIVE ===== */
@media (max-width: 900px) {
  .models-grid { grid-template-columns: repeat(2, 1fr); }
  .features-grid { grid-template-columns: repeat(2, 1fr); }
  .pricing-row { grid-template-columns: 1fr; max-width: 400px; }
  .plan-card.featured { transform: none; }
  .plan-card.featured:hover { transform: translateY(-6px); }
}

@media (max-width: 640px) {
  .hero { padding: 100px 16px 80px; }
  .hero-title-line { font-size: 38px; }

  .models { padding: 60px 16px; }
  .models-grid { grid-template-columns: 1fr; }

  .stats { padding: 48px 16px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 32px 16px; }
  .stat-item:nth-child(even)::after { display: none; }

  .features { padding: 60px 16px; }
  .features-grid { grid-template-columns: 1fr; }

  .pricing { padding: 60px 16px; }

  .cta-section { padding: 60px 16px; }
  .cta-card { padding: 40px 24px; }

  .section-title { font-size: 28px; }
  .section-sub { font-size: 16px; margin-bottom: 36px; }
}

@media (max-width: 480px) {
  .hero-cta, .hero-cta-outline {
    width: 100%;
    justify-content: center;
  }
}

/* ===== REDUCED MOTION ===== */
@media (prefers-reduced-motion: reduce) {
  .hero-gradient-base,
  .hero-grid,
  .stats-bg {
    animation: none !important;
  }
  .hero-title-accent {
    background: linear-gradient(135deg, #818cf8, #a78bfa);
    -webkit-background-clip: text;
    background-clip: text;
  }
  .plan-card.featured::before {
    animation: none !important;
  }
  .cta-card::before {
    animation: none !important;
  }
  .model-card:hover,
  .feature-card:hover {
    transform: translateY(-4px) !important;
  }
}
</style>
