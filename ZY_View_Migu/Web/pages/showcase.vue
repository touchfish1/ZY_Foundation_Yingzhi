<template>
  <main class="showcase-page">
    <section class="showcase-hero section-paper">
      <div class="rings rings-a" />
      <div class="rings rings-b" />
      <AnimReveal animation="fade-up">
        <p class="mono">SHOWCASE</p>
        <h1>为什么选择<br>我们</h1>
        <p class="lead">真实数据说话，透明定价为先。</p>
      </AnimReveal>
    </section>

    <!-- ROI Calculator comparison -->
    <section class="comparison section-paper">
      <AnimReveal animation="fade-up">
        <p class="mono">01 · PRICE COMPARISON</p>
        <h2>真实成本对比</h2>
        <div class="compare-table">
          <div class="compare-header">
            <span>方案</span>
            <span>账面价格</span>
            <span>实际消耗倍率</span>
            <span>真实成本（万 token）</span>
          </div>
          <div class="compare-row">
            <span class="row-label">同行 A</span>
            <span>1 元 = 100 万</span>
            <span>3.2 倍</span>
            <span class="bad-cost">≈ 3.2 元</span>
          </div>
          <div class="compare-row">
            <span class="row-label">同行 B</span>
            <span>1 元 = 50 万</span>
            <span>1.8 倍</span>
            <span class="bad-cost">≈ 0.9 元</span>
          </div>
          <div class="compare-row featured">
            <span class="row-label">KAMIAPI</span>
            <span>0.16 元 / 万</span>
            <span>1.0 倍</span>
            <span class="good-cost">0.16 元</span>
          </div>
        </div>
      </AnimReveal>
    </section>

    <!-- Case studies -->
    <section class="cases section-paper">
      <AnimReveal animation="fade-up">
        <p class="mono">02 · CUSTOMER STORIES</p>
        <h2>客户案例</h2>
      </AnimReveal>
      <div class="cases-grid">
        <AnimReveal v-for="(c, i) in cases" :key="i" animation="fade-up" :delay="i * 100">
          <div class="case-card">
            <div class="case-avatar">{{ c.initial }}</div>
            <h3>{{ c.name }}</h3>
            <p class="case-role">{{ c.role }}</p>
            <blockquote>{{ c.quote }}</blockquote>
            <div class="case-stats">
              <span>成本降低 <strong>{{ c.savings }}</strong></span>
              <span>性能提升 <strong>{{ c.performance }}</strong></span>
            </div>
          </div>
        </AnimReveal>
      </div>
    </section>

    <!-- Feature comparison -->
    <section class="feature-compare section-paper">
      <AnimReveal animation="scale-in">
        <p class="mono">03 · FEATURE COMPARISON</p>
        <h2>功能对比</h2>
        <div class="feat-table">
          <div class="feat-row header">
            <span>功能</span>
            <span>同行</span>
            <span class="our-col">KAMIAPI</span>
          </div>
          <div class="feat-row">
            <span>真实倍率</span><span class="no">✗ 隐藏</span><span class="yes">✓ 明示</span>
          </div>
          <div class="feat-row">
            <span>缓存真实命中</span><span class="no">✗ 掺水</span><span class="yes">✓ 透明</span>
          </div>
          <div class="feat-row">
            <span>号池质量</span><span class="no">✗ 混入缩水</span><span class="yes">✓ Plus+Pro</span>
          </div>
          <div class="feat-row">
            <span>多模型路由</span><span class="no">✗ 有限</span><span class="yes">✓ 全模型</span>
          </div>
          <div class="feat-row">
            <span>账单明细</span><span class="no">✗ 模糊</span><span class="yes">✓ 可追踪</span>
          </div>
        </div>
      </AnimReveal>
    </section>

    <!-- CTA -->
    <section class="showcase-cta section-paper">
      <AnimReveal animation="fade-up">
        <div class="cta-box">
          <h2>加入数千家信赖<br>KAMIAPI 的团队</h2>
          <p>注册即享 1,000 次免费调用</p>
          <a href="#" class="cta-btn">免费开始</a>
        </div>
      </AnimReveal>
    </section>
  </main>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default' })
usePageMeta('案例展示 — 卡米 API', '了解卡米 API 的定价透明度和真实客户案例')

const cases = [
  { initial: 'S', name: '智谱科技', role: 'AI 应用开发团队', quote: '迁移到 KAMIAPI 后，我们的 API 成本降低了 62%，而且再也不用担心隐藏倍率的问题。', savings: '62%', performance: '40%' },
  { initial: 'A', name: '云帆互动', role: '社交平台技术团队', quote: '多模型路由加上透明的缓存策略，让我们能专注于产品迭代而不是跟供应商博弈。', savings: '45%', performance: '55%' },
  { initial: 'C', name: '深度智造', role: '制造业 AI 部门', quote: 'Plus+Pro 号池的质量远超预期，客服响应速度快，技术支持专业。', savings: '38%', performance: '70%' },
]
</script>

<style scoped>
.showcase-page {
  min-height: 100vh;
  background: #f4f0e8;
  color: #050505;
  font-family: Georgia, "Times New Roman", "Noto Serif SC", SimSun, serif;
}

.section-paper {
  width: min(1120px, calc(100% - 40px));
  margin-left: auto;
  margin-right: auto;
}

.showcase-hero {
  position: relative;
  min-height: 70vh;
  display: grid;
  place-items: center;
  text-align: center;
  overflow: hidden;
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.showcase-hero::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(0,0,0,0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,0,0,0.035) 1px, transparent 1px);
  background-size: 72px 72px;
}

.showcase-hero h1 {
  margin: 30px 0 20px;
  font-size: clamp(72px, 6vw, 116px);
  font-weight: 500;
  line-height: 0.9;
}

.lead {
  color: rgba(0,0,0,0.48);
  font-size: 20px;
}

.rings {
  position: absolute;
  border: 1px solid rgba(0,0,0,0.14);
  border-radius: 50%;
}

.rings-a { width: 660px; height: 106px; left: 20%; top: 45%; }
.rings-b { width: 440px; height: 86px; right: 12%; top: 38%; opacity: 0.4; }

.mono {
  font-family: "Courier New", monospace;
  letter-spacing: 0.42em;
  color: rgba(0,0,0,0.58);
  font-size: 12px;
  text-transform: uppercase;
}

.comparison {
  padding: 80px 0;
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.comparison h2,
.cases h2,
.feature-compare h2 {
  margin: 24px 0 32px;
  font-size: clamp(48px, 4vw, 72px);
  font-weight: 500;
  line-height: 0.95;
}

.compare-table {
  border: 1px solid rgba(0,0,0,0.16);
  overflow: hidden;
}

.compare-header {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  padding: 16px 24px;
  font-family: "Courier New", monospace;
  font-size: 11px;
  letter-spacing: 0.12em;
  color: rgba(0,0,0,0.4);
  background: rgba(0,0,0,0.03);
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.compare-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
  font-size: 15px;
  align-items: center;
}

.compare-row:last-child {
  border-bottom: 0;
}

.compare-row.featured {
  background: rgba(168,50,43,0.04);
}

.row-label {
  font-weight: 600;
}

.bad-cost {
  color: #a8322b;
}

.good-cost {
  color: #2b6e3b;
  font-weight: 600;
}

.cases {
  padding: 80px 0;
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.cases-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.case-card {
  padding: 32px;
  border: 1px solid rgba(0,0,0,0.14);
  background: rgba(244,240,232,0.6);
  transition: transform 0.25s ease;
}

.case-card:hover {
  transform: translateY(-4px);
}

.case-avatar {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #050505;
  color: #f4f0e8;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  font-family: Georgia, serif;
}

.case-card h3 {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 4px;
}

.case-role {
  color: rgba(0,0,0,0.4);
  font-size: 13px;
  margin-bottom: 16px;
  font-family: "Courier New", monospace;
  letter-spacing: 0.06em;
}

blockquote {
  color: rgba(0,0,0,0.55);
  font-size: 15px;
  line-height: 1.7;
  font-style: italic;
  margin: 0 0 20px;
}

.case-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: rgba(0,0,0,0.45);
}

.case-stats strong {
  color: #2b6e3b;
  font-weight: 600;
}

.feature-compare {
  padding: 80px 0;
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.feat-table {
  border: 1px solid rgba(0,0,0,0.16);
}

.feat-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
  font-size: 15px;
  align-items: center;
}

.feat-row:last-child {
  border-bottom: 0;
}

.feat-row.header {
  font-family: "Courier New", monospace;
  font-size: 11px;
  letter-spacing: 0.12em;
  color: rgba(0,0,0,0.4);
  background: rgba(0,0,0,0.03);
}

.our-col {
  color: #050505;
  font-weight: 600;
}

.no {
  color: #a8322b;
  font-size: 13px;
}

.yes {
  color: #2b6e3b;
  font-weight: 600;
  font-size: 13px;
}

.showcase-cta {
  padding: 80px 0 120px;
  text-align: center;
}

.cta-box {
  padding: 64px 40px;
  border: 1px solid rgba(0,0,0,0.18);
  outline: 12px solid rgba(0,0,0,0.035);
  background: radial-gradient(circle, rgba(0,0,0,0.055) 1px, transparent 1px) 0 0 / 7px 7px, #e8e4dc;
}

.cta-box h2 {
  font-size: clamp(42px, 4vw, 64px);
  font-weight: 500;
  line-height: 1;
  margin-bottom: 16px;
}

.cta-box p {
  color: rgba(0,0,0,0.5);
  font-size: 18px;
  margin-bottom: 32px;
}

.cta-btn {
  display: inline-block;
  padding: 14px 36px;
  background: #050505;
  color: #f4f0e8;
  font-family: "Courier New", monospace;
  font-size: 14px;
  letter-spacing: 0.16em;
  text-decoration: none;
  border-radius: 999px;
  transition: opacity 0.2s;
}

.cta-btn:hover { opacity: 0.8; }

@media (max-width: 900px) {
  .cases-grid { grid-template-columns: 1fr; }
  .compare-header, .compare-row {
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
  .feat-row { grid-template-columns: 1fr 1fr; }
}

@media (max-width: 640px) {
  .compare-header, .compare-row {
    grid-template-columns: 1fr;
    gap: 4px;
    padding: 16px;
  }
}
</style>
