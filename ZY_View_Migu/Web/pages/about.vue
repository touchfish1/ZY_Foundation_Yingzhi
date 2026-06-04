<template>
  <main class="about-page">
    <!-- Hero -->
    <section class="about-hero section-paper">
      <div class="rings rings-a" />
      <div class="rings rings-b" />
      <div class="hero-content">
        <AnimReveal animation="fade-up">
          <p class="mono">ABOUT · KAMIAPI</p>
          <h1>重新定义<br>API 服务</h1>
          <p class="lead">我们致力于为开发者和企业提供最透明、最可靠的 AI 模型接入服务。</p>
        </AnimReveal>
      </div>
    </section>

    <!-- Stats -->
    <section class="stats-bar">
      <AnimReveal animation="fade-up">
        <div class="stats-grid">
          <div class="stat-item"><strong>10万+</strong><span>注册用户</span></div>
          <div class="stat-item"><strong>99.9%</strong><span>服务可用率</span></div>
          <div class="stat-item"><strong>50+</strong><span>接入模型</span></div>
          <div class="stat-item"><strong>500ms</strong><span>平均响应</span></div>
        </div>
      </AnimReveal>
    </section>

    <!-- Story -->
    <section class="story section-paper">
      <div class="story-grid">
        <AnimReveal animation="fade-up">
          <div class="story-text">
            <p class="mono">OUR STORY</p>
            <h2>从开发者中来<br>到开发者中去</h2>
            <p>我们曾经也是 API 的重度用户。面对市场上混乱的定价、不透明的倍率、掺水的缓存命中率，我们决定自己做点什么。</p>
            <p>KAMIAPI 诞生于 2024 年，团队核心成员来自一线互联网公司和 AI 实验室。我们相信，API 服务应该像水电煤一样透明、稳定、按需付费。</p>
          </div>
        </AnimReveal>
        <AnimReveal animation="scale-in" :delay="150">
          <div class="story-values">
            <div class="value-card"><span class="value-num">01</span><h3>透明</h3><p>所有定价、倍率、消耗关系明示，拒绝套路。</p></div>
            <div class="value-card"><span class="value-num">02</span><h3>可靠</h3><p>多集群冗余部署，99.9% SLA 保障。</p></div>
            <div class="value-card"><span class="value-num">03</span><h3>开放</h3><p>标准 API 接口，兼容主流生态，零迁移成本。</p></div>
          </div>
        </AnimReveal>
      </div>
    </section>

    <!-- Team -->
    <section class="team section-paper">
      <AnimReveal animation="fade-up">
        <p class="mono">TEAM</p>
        <h2>核心团队</h2>
        <div class="team-grid">
          <div class="team-card">
            <div class="avatar">K</div>
            <h3>Kai</h3>
            <p>创始人 &amp; CEO</p>
            <span>前字节跳动技术总监</span>
          </div>
          <div class="team-card">
            <div class="avatar">M</div>
            <h3>Mia</h3>
            <p>CTO</p>
            <span>前阿里云高级架构师</span>
          </div>
          <div class="team-card">
            <div class="avatar">J</div>
            <h3>Jay</h3>
            <p>CPO</p>
            <span>连续创业者，AI 产品专家</span>
          </div>
        </div>
      </AnimReveal>
    </section>

    <!-- CTA -->
    <section class="about-cta section-paper">
      <AnimReveal animation="fade-up">
        <div class="cta-box">
          <h2>准备好开始了吗？</h2>
          <p>注册即享 1,000 次免费调用额度</p>
          <a href="#" class="cta-btn">免费开始</a>
        </div>
      </AnimReveal>
    </section>
  </main>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default' })
usePageMeta('关于我们 — 卡米 API', '了解卡米 API 的故事、团队和价值观')
</script>

<style scoped>
.about-page {
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

.about-hero {
  position: relative;
  min-height: 80vh;
  display: grid;
  place-items: center;
  text-align: center;
  overflow: hidden;
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.about-hero::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(0,0,0,0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,0,0,0.035) 1px, transparent 1px);
  background-size: 72px 72px;
}

.hero-content {
  position: relative;
  z-index: 2;
  margin-top: 42px;
}

.about-hero h1 {
  margin: 30px 0 20px;
  font-size: clamp(72px, 6vw, 116px);
  font-weight: 500;
  line-height: 0.9;
  letter-spacing: 0.02em;
}

.lead {
  max-width: 600px;
  margin: 0 auto;
  color: rgba(0,0,0,0.48);
  font-size: 20px;
  line-height: 1.6;
}

.rings {
  position: absolute;
  border: 1px solid rgba(0,0,0,0.14);
  border-radius: 50%;
}

.rings-a {
  width: 660px; height: 106px;
  left: 20%; top: 45%;
}

.rings-b {
  width: 440px; height: 86px;
  right: 12%; top: 38%;
  opacity: 0.4;
}

.mono {
  font-family: "Courier New", monospace;
  letter-spacing: 0.42em;
  color: rgba(0,0,0,0.58);
  font-size: 12px;
  text-transform: uppercase;
}

.stats-bar {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto;
  padding: 60px 0;
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  text-align: center;
}

.stat-item strong {
  display: block;
  font-size: 48px;
  font-weight: 500;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-item span {
  font-family: "Courier New", monospace;
  font-size: 12px;
  color: rgba(0,0,0,0.48);
  letter-spacing: 0.12em;
}

.story {
  padding: 100px 0;
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.story-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: start;
}

.story-text h2 {
  margin: 24px 0 20px;
  font-size: clamp(48px, 4vw, 72px);
  font-weight: 500;
  line-height: 0.95;
}

.story-text p {
  color: rgba(0,0,0,0.5);
  font-size: 18px;
  line-height: 1.7;
  margin-bottom: 16px;
}

.value-card {
  padding: 28px;
  margin-bottom: 20px;
  border: 1px solid rgba(0,0,0,0.14);
  background: rgba(244,240,232,0.8);
}

.value-num {
  font-family: "Courier New", monospace;
  color: rgba(0,0,0,0.3);
  font-size: 11px;
}

.value-card h3 {
  margin: 8px 0;
  font-size: 22px;
  font-weight: 500;
}

.value-card p {
  color: rgba(0,0,0,0.45);
  line-height: 1.7;
}

.team {
  padding: 100px 0;
  text-align: center;
}

.team h2 {
  margin: 24px 0 40px;
  font-size: clamp(48px, 4vw, 72px);
  font-weight: 500;
  line-height: 0.95;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.team-card {
  padding: 32px;
  border: 1px solid rgba(0,0,0,0.14);
  background: rgba(244,240,232,0.6);
  transition: transform 0.25s ease;
}

.team-card:hover {
  transform: translateY(-4px);
}

.avatar {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #050505;
  color: #f4f0e8;
  font-size: 24px;
  font-weight: 600;
  font-family: Georgia, serif;
}

.team-card h3 {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 4px;
}

.team-card p {
  color: rgba(0,0,0,0.5);
  font-size: 14px;
  margin-bottom: 8px;
}

.team-card span {
  font-family: "Courier New", monospace;
  font-size: 11px;
  color: rgba(0,0,0,0.35);
  letter-spacing: 0.08em;
}

.about-cta {
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

.cta-btn:hover {
  opacity: 0.8;
}

@media (max-width: 900px) {
  .story-grid { grid-template-columns: 1fr; }
  .team-grid { grid-template-columns: 1fr; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .stat-item strong { font-size: 36px; }
}
</style>
