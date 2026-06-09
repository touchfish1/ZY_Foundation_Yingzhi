<template>
  <div class="preview-pane">
    <div v-if="!blocks.length" class="preview-empty">
      <p>暂无区块，请在左侧添加区块</p>
    </div>
    <div v-for="block in blocks" :key="block.id" class="preview-block" :class="`preview-${block.type}`">
      <div class="preview-header">
        <n-tag :bordered="false" size="tiny" :type="tagType(block.type)">
          {{ defName(block.type) }}
        </n-tag>
      </div>

      <!-- Hero preview -->
      <div v-if="block.type === 'hero'" class="preview-body">
        <h3 class="preview-title">{{ block.props.title || '标题' }}</h3>
        <p class="preview-desc">{{ block.props.subtitle || '副标题' }}</p>
      </div>

      <!-- Feature Grid preview -->
      <div v-else-if="block.type === 'feature-grid'" class="preview-body">
        <div class="preview-section-title">{{ block.props.title || '功能列表' }}</div>
        <div class="preview-items">
          <div v-for="(item, i) in (block.props.items as any[] || [])" :key="i" class="preview-item">
            <span class="preview-item-label">{{ item.name || `项 ${i+1}` }}</span>
          </div>
        </div>
      </div>

      <!-- Pricing preview -->
      <div v-else-if="block.type === 'pricing'" class="preview-body">
        <p class="preview-meta">套餐组: {{ block.props.planGroupCode || '-' }}</p>
        <p class="preview-meta">周期: {{ block.props.defaultBillingCycle || '-' }}</p>
      </div>

      <!-- FAQ preview -->
      <div v-else-if="block.type === 'faq'" class="preview-body">
        <div class="preview-section-title">{{ block.props.title || 'FAQ' }}</div>
        <div v-for="(item, i) in (block.props.items as any[] || [])" :key="i" class="preview-item">
          <span class="preview-item-label">Q{{ i+1 }}: {{ item.question || '' }}</span>
        </div>
      </div>

      <!-- CTA preview -->
      <div v-else-if="block.type === 'cta'" class="preview-body">
        <p class="preview-title" style="font-size: 14px;">{{ block.props.title || '按钮标题' }}</p>
        <p class="preview-meta">{{ block.props.buttonText || '按钮' }} → {{ block.props.buttonUrl || '#' }}</p>
      </div>

      <!-- Rich Text preview -->
      <div v-else-if="block.type === 'rich-text'" class="preview-body">
        <p class="preview-desc">{{ truncate(String(block.props.content || ''), 80) }}</p>
      </div>

      <!-- Fallback -->
      <div v-else class="preview-body">
        <pre class="preview-json">{{ JSON.stringify(block.props, null, 1) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NTag } from 'naive-ui'
import type { BlockDefinition, PageBlock } from '../../types/block'

defineProps<{
  blocks: PageBlock[]
  definitions: BlockDefinition[]
}>()

function defName(type: string): string {
  return type
}

function tagType(type: string) {
  const map: Record<string, string> = {
    hero: 'info',
    pricing: 'success',
    faq: 'warning',
    cta: 'primary',
    'rich-text': 'info',
    'feature-grid': 'success'
  }
  return (map[type] || 'default') as 'info' | 'success' | 'warning' | 'primary' | 'default'
}

function truncate(s: string, max: number): string {
  return s.length > max ? s.slice(0, max) + '…' : s
}
</script>

<style scoped>
.preview-pane {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 480px;
  overflow-y: auto;
  padding: 4px 0;
}
.preview-empty {
  text-align: center;
  color: #9ca3af;
  padding: 20px;
  font-size: 13px;
}
.preview-block {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fafafa;
  overflow: hidden;
}
.preview-header {
  padding: 6px 10px;
  background: #f3f4f6;
  border-bottom: 1px solid #e5e7eb;
}
.preview-body {
  padding: 8px 10px;
}
.preview-title {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}
.preview-desc {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}
.preview-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 4px;
}
.preview-meta {
  margin: 2px 0;
  font-size: 12px;
  color: #6b7280;
}
.preview-items {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.preview-item {
  font-size: 12px;
  color: #4b5563;
  padding: 2px 0;
}
.preview-item-label {
  font-weight: 500;
}
.preview-json {
  font-size: 11px;
  color: #6b7280;
  margin: 0;
  white-space: pre-wrap;
}
</style>
