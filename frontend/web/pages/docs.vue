<template>
  <div class="doc-page">
    <!-- Doc listing -->
    <div v-if="!selectedDoc" class="doc-index">
      <div class="index-header">
        <h1 class="index-title">开发者文档</h1>
        <p class="index-desc">快速集成，轻松上手。从入门到高级，尽在文档中心。</p>
        <div class="index-search">
          <DocSearch @close="searchOpen = false" v-if="searchOpen" />
          <button class="search-trigger" @click="searchOpen = true">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8" /><path d="M21 21l-4.35-4.35" />
            </svg>
            搜索文档...
            <span class="search-hint">⌘K</span>
          </button>
        </div>
      </div>

      <div class="index-grid">
        <NuxtLink v-for="doc in allDocs" :key="doc.to" :to="doc.to" class="doc-card">
          <h3 class="doc-card-title">{{ doc.title }}</h3>
          <p class="doc-card-desc">{{ doc.description || '查看文档' }}</p>
          <span class="doc-card-arrow">→</span>
        </NuxtLink>
      </div>
    </div>

    <!-- Single doc view -->
    <div v-else class="doc-view">
      <article class="doc-article">
        <h1>{{ selectedDoc.title }}</h1>
        <div class="doc-meta">
          <time v-if="selectedDoc.updatedAt">更新于 {{ formatDate(selectedDoc.updatedAt) }}</time>
        </div>
        <div class="doc-body" v-html="selectedDoc.content || '<p>文档内容加载中...</p>'" />
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'docs' })
usePageMeta('开发者文档', '快速集成，轻松上手。从入门到高级，尽在文档中心。')

import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const searchOpen = ref(false)

interface DocPage {
  title: string
  slug: string
  description?: string
  content?: string
  updatedAt?: string
  to: string
}

const allDocs = ref<DocPage[]>(getBuiltInDocs())
const docContent = ref<Record<string, DocPage>>(getBuiltInDocContent())

const selectedDoc = computed(() => {
  // Check if we're on a specific doc path
  const slug = route.path.replace('/docs/', '')
  if (!slug || slug === 'docs') return null
  return docContent.value[slug] || null
})

function formatDate(dateStr: string): string {
  try {
    return new Date(dateStr).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
  } catch { return dateStr }
}

async function loadDoc(slug: string) {
  if (docContent.value[slug]) return
  try {
    const config = useRuntimeConfig()
    const res: any = await $fetch(`${config.public.apiBase}/api/cms/page?slug=/docs/${slug}`)
    if (res.code === 0 && res.data) {
      docContent.value[slug] = {
        title: res.data.title,
        slug: res.data.slug,
        content: res.data.content,
        updatedAt: res.data.updatedAt,
        to: `/docs/${slug}`
      }
    }
  } catch (e) {
    console.error('Failed to load doc', e)
  }
}

onMounted(async () => {
  // Load doc list
  try {
    const config = useRuntimeConfig()
    const res: any = await $fetch(`${config.public.apiBase}/api/cms/pages/list?type=doc&page=1&pageSize=50`)
    if (res.code === 0) {
      allDocs.value = (res.data.items || []).map((d: any) => ({
        title: d.title || d.slug?.split('/').pop() || '文档',
        slug: d.slug?.replace(/^\/docs\//, '') || '',
        description: d.description || '',
        updatedAt: d.updatedAt,
        to: `/docs/${d.slug?.replace(/^\/docs\//, '') || ''}`
      }))
    }
  } catch (e) { /* silent */ }

  // Check if current route is a doc detail
  const slug = route.path.replace('/docs/', '')
  if (slug && slug !== 'docs') {
    await loadDoc(slug)
  }
})

// Watch for route changes
watch(() => route.path, async (path) => {
  const slug = path.replace('/docs/', '')
  if (slug && slug !== 'docs') {
    await loadDoc(slug)
  }
})

function getBuiltInDocs(): DocPage[] {
  return [
    { title: '快速开始', slug: 'getting-started', description: '5 分钟集成 AI API', to: '/docs/getting-started' },
    { title: 'API 认证', slug: 'authentication', description: 'API Key 获取与使用', to: '/docs/authentication' },
    { title: 'Chat Completions', slug: 'chat-completions', description: '对话补全接口参考', to: '/docs/chat-completions' },
    { title: '可用模型', slug: 'models', description: '支持的模型列表与规格', to: '/docs/models' },
    { title: '用量与配额', slug: 'usage', description: '查看用量、管理配额', to: '/docs/usage' },
    { title: '错误处理', slug: 'errors', description: 'API 错误码与处理方式', to: '/docs/errors' },
    { title: 'API Playground', slug: 'playground', description: '在线调试 API 接口', to: '/docs/playground' },
  ]
}

function getBuiltInDocContent(): Record<string, DocPage> {
  return {
    'getting-started': {
      title: '快速开始',
      slug: 'getting-started',
      to: '/docs/getting-started',
      content: `<h2>获取 API Key</h2>
<p>登录后进入<a href="/dashboard">控制台</a>，在 API Keys 页面获取你的密钥。</p>
<pre><code># 你的 API Key 格式
sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</code></pre>

<h2>发起第一个请求</h2>
<p>使用 curl 调用 Chat Completions 接口：</p>
<pre><code>curl https://api.zhangyuan.ai/v1/chat/completions \\
  -H "Authorization: Bearer sk-your-key" \\
  -H "Content-Type: application/json" \\
  -d '{
    "model": "gpt-4o",
    "messages": [
      {"role": "user", "content": "Hello!"}
    ]
  }'</code></pre>

<h2>使用 Python</h2>
<pre><code>import openai
client = openai.OpenAI(
    api_key="sk-your-key",
    base_url="https://api.zhangyuan.ai/v1"
)
response = client.chat.completions.create(
    model="gpt-4o",
    messages=[{"role": "user", "content": "Hello!"}]
)
print(response.choices[0].message.content)</code></pre>

<h2>下一步</h2>
<p>查看 <a href="/docs/authentication">API 认证文档</a> 了解更多。</p>`
    },
    'authentication': {
      title: 'API 认证',
      slug: 'authentication',
      to: '/docs/authentication',
      content: `<h2>认证方式</h2>
<p>所有 API 请求必须使用 Bearer Token 认证方式：</p>
<pre><code>Authorization: Bearer sk-your-api-key</code></pre>

<h2>获取 API Key</h2>
<ol>
<li>注册并登录账号</li>
<li>进入 <a href="/dashboard">控制台</a> → API Keys</li>
<li>点击创建或复制现有 Key</li>
</ol>

<h2>安全建议</h2>
<ul>
<li>不要将 API Key 暴露在客户端代码中</li>
<li>定期轮换密钥</li>
<li>为不同应用使用不同的 Key</li>
<li>如发现泄露，立即在控制台重新生成</li>
</ul>`
    },
    'chat-completions': {
      title: 'Chat Completions',
      slug: 'chat-completions',
      to: '/docs/chat-completions',
      content: `<h2>接口</h2>
<pre><code>POST /v1/chat/completions</code></pre>

<h2>请求体</h2>
<table>
<tr><th>参数</th><th>类型</th><th>必填</th><th>说明</th></tr>
<tr><td>model</td><td>string</td><td>是</td><td>模型 ID</td></tr>
<tr><td>messages</td><td>array</td><td>是</td><td>对话消息列表</td></tr>
<tr><td>temperature</td><td>number</td><td>否</td><td>采样温度 (0-2)，默认 1</td></tr>
<tr><td>max_tokens</td><td>integer</td><td>否</td><td>最大生成 token 数</td></tr>
<tr><td>stream</td><td>boolean</td><td>否</td><td>是否流式输出（即将支持）</td></tr>
</table>

<h2>示例请求</h2>
<pre><code>curl https://api.zhangyuan.ai/v1/chat/completions \\
  -H "Authorization: Bearer sk-your-key" \\
  -H "Content-Type: application/json" \\
  -d '{
    "model": "gpt-4o",
    "messages": [
      {"role": "system", "content": "You are helpful."},
      {"role": "user", "content": "Hello!"}
    ],
    "temperature": 0.7
  }'</code></pre>

<h2>响应格式</h2>
<pre><code>{
  "id": "chatcmpl-xxx",
  "object": "chat.completion",
  "created": 1717000000,
  "model": "gpt-4o",
  "choices": [{
    "index": 0,
    "message": {
      "role": "assistant",
      "content": "Hello! How can I help you today?"
    },
    "finish_reason": "stop"
  }],
  "usage": {
    "prompt_tokens": 10,
    "completion_tokens": 15,
    "total_tokens": 25
  }
}</code></pre>`
    },
    'models': {
      title: '可用模型',
      slug: 'models',
      to: '/docs/models',
      content: `<h2>OpenAI 兼容模型</h2>
<table>
<tr><th>模型 ID</th><th>提供商</th><th>说明</th></tr>
<tr><td>gpt-4o</td><td>OpenAI</td><td>最新多模态模型，高效且强大</td></tr>
<tr><td>gpt-4-turbo</td><td>OpenAI</td><td>GPT-4 Turbo，支持 128K 上下文</td></tr>
<tr><td>gpt-3.5-turbo</td><td>OpenAI</td><td>快速经济的 GPT-3.5</td></tr>
<tr><td>claude-3-opus</td><td>Anthropic</td><td>最强大的 Claude 3 模型</td></tr>
<tr><td>claude-3-sonnet</td><td>Anthropic</td><td>速度与智能的平衡</td></tr>
<tr><td>claude-3-haiku</td><td>Anthropic</td><td>最快最经济的 Claude 3</td></tr>
</table>

<p>所有模型可通过 <code>GET /v1/models</code> 获取实时列表。</p>`
    },
    'usage': {
      title: '用量与配额',
      slug: 'usage',
      to: '/docs/usage',
      content: `<h2>配额说明</h2>
<p>不同套餐的配额限制：</p>
<table>
<tr><th>套餐</th><th>每月配额</th><th>RPM 限制</th></tr>
<tr><td>Free Trial</td><td>10,000 tokens</td><td>10 RPM</td></tr>
<tr><td>Basic</td><td>100,000 tokens</td><td>60 RPM</td></tr>
<tr><td>Pro</td><td>1,000,000 tokens</td><td>300 RPM</td></tr>
<tr><td>Enterprise</td><td>10,000,000 tokens</td><td>1000 RPM</td></tr>
</table>

<h2>查看用量</h2>
<p>在<a href="/dashboard/usage">控制台用量页面</a>可以查看实时用量和配额使用情况。</p>

<h2>配额耗尽</h2>
<p>当配额耗尽时，API 返回 429 Too Many Requests。升级套餐或等待下个结算周期即可恢复。</p>`
    },
    'errors': {
      title: '错误处理',
      slug: 'errors',
      to: '/docs/errors',
      content: `<h2>错误码</h2>
<table>
<tr><th>HTTP 状态码</th><th>错误类型</th><th>说明</th></tr>
<tr><td>401</td><td>auth_error</td><td>API Key 无效或已过期</td></tr>
<tr><td>403</td><td>forbidden</td><td>账号已被禁用</td></tr>
<tr><td>429</td><td>quota_exceeded</td><td>配额不足或请求过于频繁</td></tr>
<tr><td>400</td><td>invalid_request</td><td>请求参数有误</td></tr>
<tr><td>500</td><td>server_error</td><td>服务端内部错误</td></tr>
</table>

<h2>错误响应格式</h2>
<pre><code>{
  "error": {
    "message": "Quota exhausted",
    "type": "quota_exceeded"
  }
}</code></pre>`
    },
    'playground': {
      title: 'API Playground',
      slug: 'playground',
      to: '/docs/playground',
      content: `<p>Playground 页面已移至 <a href="/playground">/playground</a>。<p>`
    },
  }
}
</script>

<style scoped>
.doc-page {
  min-height: calc(100vh - var(--vp-nav-height));
}

/* ===== Index / Listing ===== */
.doc-index {
  padding: 48px 0;
}
.index-header {
  margin-bottom: 48px;
}
.index-title {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--vp-c-text);
  margin: 0 0 12px;
  line-height: 1.2;
  font-family: var(--vp-font-family-display);
}
.index-desc {
  font-size: 16px;
  color: var(--vp-c-text-2);
  margin: 0 0 24px;
  line-height: 1.6;
}
.search-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid var(--vp-c-border);
  border-radius: 8px;
  background: var(--vp-c-bg);
  color: var(--vp-c-text-3);
  font-size: 14px;
  font-family: var(--vp-font-family-base);
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
  min-width: 260px;
}
.search-trigger:hover {
  border-color: var(--vp-c-brand);
  color: var(--vp-c-text-2);
}
.search-hint {
  margin-left: auto;
  font-size: 11px;
  font-family: var(--vp-font-family-mono);
  color: var(--vp-c-text-3);
  padding: 1px 6px;
  border: 1px solid var(--vp-c-border);
  border-radius: 4px;
}

.index-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.doc-card {
  display: flex;
  flex-direction: column;
  padding: 24px;
  border: 1px solid var(--vp-c-border);
  border-radius: 12px;
  text-decoration: none;
  background: var(--vp-c-bg);
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}
.doc-card:hover {
  border-color: var(--vp-c-brand);
  box-shadow: var(--vp-shadow-2);
  transform: translateY(-2px);
}
.doc-card-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--vp-c-text);
}
.doc-card-desc {
  margin: 0;
  font-size: 14px;
  color: var(--vp-c-text-3);
  line-height: 1.5;
  flex: 1;
}
.doc-card-arrow {
  margin-top: 12px;
  font-size: 14px;
  color: var(--vp-c-brand);
  font-weight: 600;
}

/* ===== Single Doc View ===== */
.doc-article {
  max-width: 720px;
}
.doc-article h1 {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--vp-c-text);
  margin: 0 0 8px;
  line-height: 1.2;
}
.doc-meta {
  margin-bottom: 32px;
  font-size: 13px;
  color: var(--vp-c-text-3);
}

/* ===== Doc body content styling ===== */
.doc-body {
  font-size: 15px;
  line-height: 1.8;
  color: var(--vp-c-text);
}
.doc-body :deep(h2) {
  font-size: 24px;
  font-weight: 700;
  margin: 40px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--vp-c-divider);
  color: var(--vp-c-text);
}
.doc-body :deep(h3) {
  font-size: 18px;
  font-weight: 600;
  margin: 32px 0 12px;
  color: var(--vp-c-text);
}
.doc-body :deep(p) {
  margin: 0 0 16px;
  color: var(--vp-c-text);
}
.doc-body :deep(ul), .doc-body :deep(ol) {
  margin: 0 0 16px;
  padding-left: 24px;
}
.doc-body :deep(li) {
  margin-bottom: 6px;
}
.doc-body :deep(code) {
  font-family: var(--vp-font-family-mono);
  font-size: 13px;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--vp-code-bg);
  color: var(--vp-code-color);
}
.doc-body :deep(pre) {
  margin: 16px 0;
  padding: 16px 20px;
  border-radius: 10px;
  background: var(--vp-code-block-bg);
  border: 1px solid var(--vp-c-border);
  overflow-x: auto;
}
.doc-body :deep(pre code) {
  background: none;
  padding: 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--vp-c-text);
}
.doc-body :deep(a) {
  color: var(--vp-c-brand);
  text-decoration: none;
}
.doc-body :deep(a:hover) {
  text-decoration: underline;
}
.doc-body :deep(blockquote) {
  margin: 16px 0;
  padding: 8px 16px;
  border-left: 3px solid var(--vp-c-brand);
  color: var(--vp-c-text-2);
  background: var(--vp-c-bg-soft);
  border-radius: 0 8px 8px 0;
}
.doc-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}
.doc-body :deep(th) {
  background: var(--vp-c-bg-soft);
  font-weight: 600;
  text-align: left;
  padding: 10px 16px;
  border: 1px solid var(--vp-c-divider);
  font-size: 13px;
  color: var(--vp-c-text);
}
.doc-body :deep(td) {
  padding: 10px 16px;
  border: 1px solid var(--vp-c-divider);
  font-size: 14px;
}
.doc-body :deep(hr) {
  border: none;
  border-top: 1px solid var(--vp-c-divider);
  margin: 32px 0;
}
.doc-body :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .index-grid { grid-template-columns: 1fr; }
  .index-title { font-size: 28px; }
  .search-trigger { min-width: auto; width: 100%; }
}
</style>
