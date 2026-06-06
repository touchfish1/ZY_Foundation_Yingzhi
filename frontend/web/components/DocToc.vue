<template>
  <nav class="doc-toc" v-if="headings.length">
    <h3 class="toc-title">本页内容</h3>
    <ul class="toc-list">
      <li
        v-for="h in headings"
        :key="h.id"
        class="toc-item"
        :class="{ 'toc-depth-2': h.depth === 2, 'toc-depth-3': h.depth >= 3 }"
      >
        <a :href="`#${h.id}`" class="toc-link" :class="{ active: activeId === h.id }">
          {{ h.text }}
        </a>
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface TocHeading {
  id: string
  text: string
  depth: number
}

const headings = ref<TocHeading[]>([])
const activeId = ref('')

function collectHeadings() {
  const els = document.querySelectorAll('.docs-content h1, .docs-content h2, .docs-content h3')
  const items: TocHeading[] = []
  els.forEach((el) => {
    if (!el.id) return
    const depth = parseInt(el.tagName[1], 10)
    if (depth <= 3) {
      items.push({
        id: el.id,
        text: el.textContent || '',
        depth
      })
    }
  })
  headings.value = items
}

function onScroll() {
  // Find the heading closest to the top
  let currentId = ''
  for (const h of headings.value) {
    const el = document.getElementById(h.id)
    if (el) {
      const rect = el.getBoundingClientRect()
      if (rect.top <= 100) {
        currentId = h.id
      }
    }
  }
  activeId.value = currentId
}

let ticking = false
function onScrollTick() {
  if (!ticking) {
    requestAnimationFrame(() => {
      onScroll()
      ticking = false
    })
    ticking = true
  }
}

onMounted(() => {
  // Wait for content to render
  setTimeout(collectHeadings, 300)
  window.addEventListener('scroll', onScrollTick, { passive: true })
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScrollTick)
})
</script>

<style scoped>
.doc-toc {
  font-size: 13px;
}
.toc-title {
  margin: 0 0 12px;
  font-size: 11px;
  font-weight: 700;
  color: var(--vp-c-text-3);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}
.toc-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.toc-item {
  margin: 0;
}
.toc-link {
  display: block;
  padding: 4px 12px;
  color: var(--vp-c-text-3);
  text-decoration: none;
  border-left: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
  line-height: 1.6;
}
.toc-link:hover {
  color: var(--vp-c-brand);
}
.toc-link.active {
  color: var(--vp-c-brand);
  border-left-color: var(--vp-c-brand);
  font-weight: 500;
}
.toc-depth-3 .toc-link {
  padding-left: 24px;
  font-size: 12px;
}
</style>
