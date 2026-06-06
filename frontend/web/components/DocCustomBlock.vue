<template>
  <div class="custom-block" :class="[type, { 'has-title': !!title }]">
    <p v-if="title" class="custom-block-title">{{ title }}</p>
    <div class="custom-block-body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  type: 'tip' | 'warning' | 'danger' | 'info'
  title?: string
}>()
</script>

<style scoped>
.custom-block {
  margin: 24px 0;
  padding: 16px 20px;
  border-radius: 10px;
  border: 1px solid;
  font-size: 14px;
  line-height: 1.6;
}
.custom-block-title {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.custom-block-body :deep(p) { margin: 0; }
.custom-block-body :deep(p + p) { margin-top: 8px; }
.custom-block-body :deep(code) {
  font-family: var(--vp-font-family-mono);
  font-size: 13px;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--vp-code-bg);
  color: var(--vp-code-color);
}

/* Tip */
.custom-block.tip {
  background: var(--vp-c-tip-soft);
  border-color: color-mix(in srgb, var(--vp-c-tip) 20%, transparent);
  color: var(--vp-c-text);
}
.tip .custom-block-title { color: var(--vp-c-tip); }

/* Warning */
.custom-block.warning {
  background: var(--vp-c-warning-soft);
  border-color: color-mix(in srgb, var(--vp-c-warning) 20%, transparent);
  color: var(--vp-c-text);
}
.warning .custom-block-title { color: var(--vp-c-warning); }

/* Danger */
.custom-block.danger {
  background: var(--vp-c-danger-soft);
  border-color: color-mix(in srgb, var(--vp-c-danger) 20%, transparent);
  color: var(--vp-c-text);
}
.danger .custom-block-title { color: var(--vp-c-danger); }

/* Info */
.custom-block.info {
  background: var(--vp-c-brand-dimmer);
  border-color: color-mix(in srgb, var(--vp-c-brand) 15%, transparent);
  color: var(--vp-c-text);
}
.info .custom-block-title { color: var(--vp-c-brand); }
</style>
