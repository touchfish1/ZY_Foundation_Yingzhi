<template>
  <div class="code-block">
    <div class="code-header" v-if="lang || showCopy">
      <span v-if="lang" class="code-lang">{{ lang }}</span>
      <button v-if="showCopy" class="code-copy" @click="copy" :title="copied ? '已复制' : '复制代码'">
        {{ copied ? '已复制' : '复制' }}
      </button>
    </div>
    <div class="code-body">
      <pre><code><slot /></code></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

withDefaults(defineProps<{
  lang?: string
  showCopy?: boolean
}>(), { showCopy: true })

const copied = ref(false)

async function copy() {
  const el = document.querySelector('.code-body code')
  if (!el) return
  try {
    await navigator.clipboard.writeText(el.textContent || '')
    copied.value = true
    setTimeout(() => { copied.value = false }, 1500)
  } catch { /* silent */ }
}
</script>

<style scoped>
.code-block {
  margin: 20px 0;
  border: 1px solid var(--vp-c-border);
  border-radius: 10px;
  overflow: hidden;
  background: var(--vp-code-block-bg);
  transition: border-color var(--vp-t-border), background var(--vp-t-bg);
}
.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
}
.code-lang {
  font-family: var(--vp-font-family-mono);
  font-size: 12px;
  color: var(--vp-c-text-3);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.code-copy {
  font-family: var(--vp-font-family-mono);
  font-size: 12px;
  color: var(--vp-c-text-3);
  background: var(--vp-code-copy-code-bg);
  border: 1px solid var(--vp-c-border);
  padding: 3px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.code-copy:hover {
  background: var(--vp-code-copy-code-hover-bg);
  color: var(--vp-c-text);
}
.code-body {
  overflow-x: auto;
  padding: 16px 20px;
}
.code-body pre {
  margin: 0;
  font-family: var(--vp-font-family-mono);
  font-size: var(--vp-code-font-size);
  line-height: 1.7;
  color: var(--vp-c-text);
}
.code-body code {
  font-family: inherit;
}
</style>
