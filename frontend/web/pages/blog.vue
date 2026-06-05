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
          <button
            v-for="filter in filters"
            :key="filter"
            class="filter"
            :class="{ active: activeFilter === filter }"
            @click="activeFilter = filter"
          >{{ filter }}</button>
        </div>
      </AnimReveal>

      <div class="posts">
        <AnimReveal v-for="(post, i) in filteredPosts" :key="post.id" animation="fade-up" :delay="i * 80">
          <article class="post-card" @click="navigateTo(post.slug)">
            <div class="post-tag">{{ extractTag(post.slug) }}</div>
            <h3>{{ post.title }}</h3>
            <div class="post-meta">
              <span class="post-date">{{ post.createdAt.slice(0, 10) }}</span>
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

interface BlogPost {
  id: number
  slug: string
  title: string
  pageType: string
  status: string
  createdAt: string
  updatedAt: string
}

const config = useRuntimeConfig()
const allPosts = ref<BlogPost[]>([])
const activeFilter = ref('全部')
const filters = ['全部', '更新公告', '技术分享', '行业资讯']

const tagMap: Record<string, string> = {
  announcements: '更新公告',
  tech: '技术分享',
  industry: '行业资讯'
}

function extractTag(slug: string): string {
  const parts = slug.replace(/^\/blog\//, '').split('/')
  return (parts[0] && tagMap[parts[0]]) || '更新公告'
}

const filteredPosts = computed(() => {
  if (activeFilter.value === '全部') return allPosts.value
  return allPosts.value.filter(p => extractTag(p.slug) === activeFilter.value)
})

onMounted(async () => {
  try {
    const res: any = await $fetch(`${config.public.apiBase}/api/cms/pages/list?type=blog&page=1&pageSize=20`)
    if (res.code === 0) {
      allPosts.value = res.data.items
    }
  } catch (e) {
    console.error('Failed to fetch blog posts', e)
  }
})
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
