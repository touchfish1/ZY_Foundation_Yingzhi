<template>
  <main class="blog-page">
    <section class="blog-hero section-paper">
      <div class="grid-bg" />
      <AnimReveal animation="fade-up">
        <p class="mono">BLOG</p>
        <h1>最新动态</h1>
        <p class="lead">产品更新、模型上架、技术分享与行业洞察。</p>
      </AnimReveal>
    </section>

    <section class="blog-list section-paper">
      <AnimReveal animation="fade-up">
        <div class="blog-filters">
          <button class="filter active">全部</button>
          <button class="filter">更新公告</button>
          <button class="filter">技术分享</button>
          <button class="filter">行业资讯</button>
        </div>
      </AnimReveal>

      <div class="posts">
        <AnimReveal v-for="(post, i) in posts" :key="i" animation="fade-up" :delay="i * 80">
          <article class="post-card">
            <div class="post-tag">{{ post.tag }}</div>
            <h3>{{ post.title }}</h3>
            <p>{{ post.excerpt }}</p>
            <div class="post-meta">
              <span class="post-date">{{ post.date }}</span>
              <span class="post-read">{{ post.readTime }} 分钟阅读</span>
            </div>
          </article>
        </AnimReveal>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default' })
usePageMeta('博客动态 — 卡米 API', '卡米 API 最新动态、产品更新与技术分享')

const posts = [
  { tag: '更新公告', title: 'GPT-5.5 旗舰模型正式上线', excerpt: '全新 GPT-5.5 旗舰模型已接入平台，推理能力大幅提升，支持 128K 上下文窗口，价格与 GPT-5.4 保持一致。', date: '2026-06-01', readTime: 3 },
  { tag: '技术分享', title: 'API 缓存命中率优化实践', excerpt: '如何将缓存命中率从 60% 提升至 90%+？本文分享我们在缓存策略、预热机制和过期策略上的实践经验。', date: '2026-05-25', readTime: 8 },
  { tag: '更新公告', title: 'Claude 系列模型同步接入', excerpt: 'Claude Opus 和 Claude Sonnet 现已同步接入平台，支持官网所有功能，价格更具竞争力。', date: '2026-05-18', readTime: 2 },
  { tag: '行业资讯', title: '2026 Q2 AI 模型价格趋势分析', excerpt: '主流 AI 模型价格持续走低，推理成本同比下降 60%。我们整理了详细的对比数据与趋势预测。', date: '2026-05-10', readTime: 12 },
  { tag: '技术分享', title: '多模型路由的最佳实践', excerpt: '根据任务类型自动路由到最优模型，兼顾性能与成本。本文介绍基于规则的动态路由方案。', date: '2026-04-28', readTime: 6 },
  { tag: '更新公告', title: '周卡余额四分特惠上线', excerpt: '新用户周卡余额四分特惠活动已开启，高频试用用户可大幅降低测试成本。', date: '2026-04-20', readTime: 2 },
]
</script>

<style scoped>
.blog-page {
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

.blog-hero {
  position: relative;
  min-height: 50vh;
  display: grid;
  place-items: center;
  text-align: center;
  border-bottom: 1px solid rgba(0,0,0,0.1);
  overflow: hidden;
}

.blog-hero h1 {
  margin: 24px 0 16px;
  font-size: clamp(72px, 6vw, 116px);
  font-weight: 500;
  line-height: 0.9;
}

.lead {
  max-width: 560px;
  margin: 0 auto;
  color: rgba(0,0,0,0.48);
  font-size: 20px;
}

.mono {
  font-family: "Courier New", monospace;
  letter-spacing: 0.42em;
  color: rgba(0,0,0,0.58);
  font-size: 12px;
  text-transform: uppercase;
}

.grid-bg {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,0,0,0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,0,0,0.035) 1px, transparent 1px);
  background-size: 72px 72px;
  pointer-events: none;
}

.blog-list {
  padding: 60px 0 120px;
}

.blog-filters {
  display: flex;
  gap: 8px;
  margin-bottom: 48px;
  flex-wrap: wrap;
}

.filter {
  padding: 8px 20px;
  border: 1px solid rgba(0,0,0,0.14);
  border-radius: 999px;
  background: transparent;
  font: 13px Georgia, serif;
  color: rgba(0,0,0,0.5);
  cursor: pointer;
  transition: all 0.2s;
}

.filter:hover {
  border-color: rgba(0,0,0,0.3);
  color: #050505;
}

.filter.active {
  background: #050505;
  color: #f4f0e8;
  border-color: #050505;
}

.posts {
  display: grid;
  gap: 24px;
}

.post-card {
  padding: 32px;
  border: 1px solid rgba(0,0,0,0.12);
  background: rgba(244,240,232,0.6);
  transition: all 0.25s ease;
  cursor: pointer;
}

.post-card:hover {
  transform: translateY(-3px);
  border-color: rgba(0,0,0,0.25);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}

.post-tag {
  display: inline-block;
  padding: 4px 10px;
  font-family: "Courier New", monospace;
  font-size: 10px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(0,0,0,0.4);
  border: 1px solid rgba(0,0,0,0.12);
  margin-bottom: 12px;
}

.post-card h3 {
  font-size: 24px;
  font-weight: 500;
  margin-bottom: 8px;
  line-height: 1.3;
}

.post-card p {
  color: rgba(0,0,0,0.45);
  font-size: 15px;
  line-height: 1.7;
  margin-bottom: 16px;
}

.post-meta {
  display: flex;
  gap: 20px;
  font-family: "Courier New", monospace;
  font-size: 11px;
  color: rgba(0,0,0,0.35);
  letter-spacing: 0.06em;
}

@media (max-width: 768px) {
  .blog-filters { overflow-x: auto; flex-wrap: nowrap; padding-bottom: 8px; }
  .blog-filters::-webkit-scrollbar { display: none; }
}
</style>
