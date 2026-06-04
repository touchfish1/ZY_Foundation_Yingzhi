<template>
  <main class="docs-page">
    <section class="docs-hero section-paper">
      <div class="grid-bg" />
      <AnimReveal animation="fade-up">
        <p class="mono">DOCUMENTATION</p>
        <h1>开发者文档</h1>
        <p class="lead">快速集成，轻松上手。从入门到高级，尽在文档中心。</p>
        <div class="search-bar">
          <span class="search-icon">⌕</span>
          <input type="text" placeholder="搜索文档..." readonly />
          <span class="search-hint">⌘K</span>
        </div>
      </AnimReveal>
    </section>

    <section class="doc-cards section-paper">
      <AnimReveal animation="fade-up">
        <div class="cards-grid">
          <a href="#" class="doc-card">
            <span class="card-icon">→</span>
            <h3>快速开始</h3>
            <p>5 分钟集成 API Key，发送你的第一个请求。</p>
            <span class="card-meta">5 分钟阅读</span>
          </a>
          <a href="#" class="doc-card">
            <span class="card-icon">☰</span>
            <h3>API 参考</h3>
            <p>完整的端点和参数说明，涵盖所有模型接口。</p>
            <span class="card-meta">详细文档</span>
          </a>
          <a href="#" class="doc-card">
            <span class="card-icon">◇</span>
            <h3>SDK &amp; 库</h3>
            <p>Python、Node.js、Go 等多语言 SDK 下载与使用。</p>
            <span class="card-meta">多语言支持</span>
          </a>
          <a href="#" class="doc-card">
            <span class="card-icon">◎</span>
            <h3>最佳实践</h3>
            <p>缓存策略、错误处理、并发优化等生产级建议。</p>
            <span class="card-meta">进阶指南</span>
          </a>
          <a href="#" class="doc-card">
            <span class="card-icon">⚙</span>
            <h3>常见问题</h3>
            <p>计费、限流、模型兼容性等高频问题解答。</p>
            <span class="card-meta">FAQ</span>
          </a>
          <a href="#" class="doc-card">
            <span class="card-icon">⇌</span>
            <h3>更新日志</h3>
            <p>平台更新、模型上线、功能变更历史记录。</p>
            <span class="card-meta">Changelog</span>
          </a>
        </div>
      </AnimReveal>
    </section>

    <section class="code-preview section-paper">
      <AnimReveal animation="scale-in">
        <p class="mono">QUICK START</p>
        <h2>一行代码，开始调用</h2>
        <div class="code-block">
          <div class="code-header">
            <span class="code-lang">Python</span>
            <span class="code-copy">复制</span>
          </div>
          <pre><code>import openai

client = openai.OpenAI(
    api_key="sk-your-api-key",
    base_url="https://api.kamiapi.com/v1"
)

response = client.chat.completions.create(
    model="gpt-5.5",
    messages=[{"role": "user", "content": "Hello!"}]
)

print(response.choices[0].message.content)</code></pre>
        </div>
      </AnimReveal>
    </section>
  </main>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default' })
usePageMeta('文档中心 — 卡米 API', '卡米 API 开发者文档，快速开始、API 参考、SDK 下载')
</script>

<style scoped>
.docs-page {
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

.docs-hero {
  position: relative;
  min-height: 60vh;
  display: grid;
  place-items: center;
  text-align: center;
  border-bottom: 1px solid rgba(0,0,0,0.1);
  overflow: hidden;
}

.docs-hero h1 {
  margin: 24px 0 16px;
  font-size: clamp(72px, 6vw, 116px);
  font-weight: 500;
  line-height: 0.9;
}

.lead {
  max-width: 560px;
  margin: 0 auto 32px;
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

.search-bar {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 10px 18px;
  border: 1px solid rgba(0,0,0,0.14);
  border-radius: 999px;
  background: rgba(255,255,255,0.6);
  backdrop-filter: blur(8px);
  min-width: 400px;
}

.search-icon {
  font-size: 18px;
  color: rgba(0,0,0,0.3);
}

.search-bar input {
  border: 0;
  background: transparent;
  font: 15px Georgia, serif;
  color: #050505;
  outline: none;
  flex: 1;
}

.search-bar input::placeholder {
  color: rgba(0,0,0,0.3);
}

.search-hint {
  font-family: "Courier New", monospace;
  font-size: 11px;
  color: rgba(0,0,0,0.3);
  padding: 2px 8px;
  border: 1px solid rgba(0,0,0,0.1);
  border-radius: 4px;
}

.doc-cards {
  padding: 80px 0;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.doc-card {
  display: block;
  padding: 32px;
  border: 1px solid rgba(0,0,0,0.14);
  background: rgba(244,240,232,0.6);
  text-decoration: none;
  color: inherit;
  transition: all 0.25s ease;
}

.doc-card:hover {
  transform: translateY(-4px);
  border-color: rgba(0,0,0,0.3);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.card-icon {
  display: block;
  font-size: 28px;
  margin-bottom: 16px;
  color: rgba(0,0,0,0.5);
}

.doc-card h3 {
  font-size: 22px;
  font-weight: 500;
  margin-bottom: 8px;
}

.doc-card p {
  color: rgba(0,0,0,0.45);
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 16px;
}

.card-meta {
  font-family: "Courier New", monospace;
  font-size: 11px;
  color: rgba(0,0,0,0.35);
  letter-spacing: 0.08em;
}

.code-preview {
  padding: 80px 0 120px;
  text-align: center;
}

.code-preview h2 {
  margin: 24px 0 32px;
  font-size: clamp(42px, 4vw, 64px);
  font-weight: 500;
  line-height: 1;
}

.code-block {
  text-align: left;
  border: 1px solid rgba(0,0,0,0.18);
  border-radius: 12px;
  overflow: hidden;
  background: #1a1a1a;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #252525;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.code-lang {
  font-family: "Courier New", monospace;
  font-size: 12px;
  color: rgba(255,255,255,0.5);
  letter-spacing: 0.08em;
}

.code-copy {
  font-family: "Courier New", monospace;
  font-size: 11px;
  color: rgba(255,255,255,0.3);
  cursor: pointer;
}

.code-copy:hover {
  color: rgba(255,255,255,0.6);
}

.code-block pre {
  margin: 0;
  padding: 24px;
  overflow-x: auto;
}

.code-block code {
  font-family: "Courier New", monospace;
  font-size: 14px;
  line-height: 1.7;
  color: #e8e4dc;
}

@media (max-width: 900px) {
  .cards-grid { grid-template-columns: repeat(2, 1fr); }
  .search-bar { min-width: auto; width: 100%; }
}

@media (max-width: 640px) {
  .cards-grid { grid-template-columns: 1fr; }
}
</style>
