<template>
  <button
    class="color-scheme-toggle"
    :title="colorMode === 'dark' ? '切换到亮色模式' : '切换到暗色模式'"
    @click="toggle"
  >
    <span class="sun-icon" :class="{ active: colorMode === 'light' }">
      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="5" />
        <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
      </svg>
    </span>
    <span class="moon-icon" :class="{ active: colorMode === 'dark' }">
      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" />
      </svg>
    </span>
  </button>
</template>

<script setup lang="ts">
const { colorMode, toggle } = useColorMode()
</script>

<style scoped>
.color-scheme-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  border: 1px solid var(--vp-c-border);
  border-radius: 8px;
  background: transparent;
  color: var(--vp-c-text-2);
  cursor: pointer;
  position: relative;
  transition: color var(--vp-t-color), border-color var(--vp-t-border), background var(--vp-t-bg);
}
.color-scheme-toggle:hover {
  border-color: var(--vp-c-brand);
  color: var(--vp-c-brand);
}

.sun-icon,
.moon-icon {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.5);
  transition: opacity 0.2s, transform 0.2s;
}
.sun-icon.active,
.moon-icon.active {
  opacity: 1;
  transform: scale(1);
}
</style>
