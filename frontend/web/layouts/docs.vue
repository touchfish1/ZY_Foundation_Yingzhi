<template>
  <div class="docs-layout">
    <header class="navbar" :class="{ 'navbar-scrolled': isScrolled }">
      <div class="navbar-inner">
        <NuxtLink to="/" class="brand">
          <svg class="brand-logo" viewBox="0 0 28 28" fill="none">
            <rect width="28" height="28" rx="7" fill="var(--vp-c-brand)" />
            <path d="M7 14h4l3 8-3-8h5l-3 8h4" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none" />
          </svg>
          <span class="brand-text">ZHANGYUAN</span>
        </NuxtLink>

        <nav class="desktop-nav">
          <NuxtLink to="/" class="nav-link">首页</NuxtLink>
          <NuxtLink to="/docs" class="nav-link active-docs">文档</NuxtLink>
          <NuxtLink to="/pricing" class="nav-link">定价</NuxtLink>
          <NuxtLink to="/about" class="nav-link">关于</NuxtLink>
        </nav>

        <div class="navbar-right">
          <ColorSchemeToggle />
          <button class="sidebar-toggle" @click="sidebarOpen = !sidebarOpen" aria-label="Toggle sidebar">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <path d="M9 3v18" />
            </svg>
          </button>
        </div>
      </div>
    </header>

    <div class="docs-body">
      <!-- Sidebar overlay (mobile) -->
      <Teleport to="body">
        <Transition name="mobile-slide">
          <div v-if="sidebarOpen" class="sidebar-overlay" @click.self="sidebarOpen = false">
            <DocSidebar @navigate="sidebarOpen = false" />
          </div>
        </Transition>
      </Teleport>

      <!-- Sidebar (desktop) -->
      <aside class="docs-sidebar">
        <DocSidebar />
      </aside>

      <!-- Main content -->
      <main class="docs-content">
        <slot />
      </main>

      <!-- TOC -->
      <aside class="docs-toc">
        <DocToc />
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const { init: initColorMode } = useColorMode()
const sidebarOpen = ref(false)
const isScrolled = ref(false)

let scrollTicking = false
function onScroll() {
  if (!scrollTicking) {
    requestAnimationFrame(() => {
      isScrolled.value = window.scrollY > 10
      scrollTicking = false
    })
    scrollTicking = true
  }
}

onMounted(() => {
  initColorMode()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.docs-layout {
  min-height: 100vh;
  background: var(--vp-c-bg);
  color: var(--vp-c-text);
  font-family: var(--vp-font-family-base);
  -webkit-font-smoothing: antialiased;
  transition: background var(--vp-t-bg), color var(--vp-t-color);
}

/* ===== Navbar ===== */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  height: var(--vp-nav-height);
  display: flex;
  align-items: center;
  background: var(--vp-c-bg);
  border-bottom: 1px solid var(--vp-c-divider);
  transition: box-shadow 0.3s, border-color 0.3s, background var(--vp-t-bg);
}
.navbar-scrolled { box-shadow: var(--vp-shadow-nav); }
.navbar-inner {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
}
.brand { display: flex; align-items: center; gap: 9px; text-decoration: none; flex-shrink: 0; }
.brand-logo { width: 28px; height: 28px; }
.brand-text { font-size: 17px; font-weight: 800; color: var(--vp-c-text); letter-spacing: -0.02em; }
.desktop-nav { display: flex; align-items: center; gap: 4px; flex: 1; padding-left: 32px; }
.nav-link {
  padding: 6px 14px;
  font-size: 14px;
  font-weight: 500;
  color: var(--vp-c-text-2);
  text-decoration: none;
  border-radius: 6px;
  transition: color 0.2s, background 0.2s;
}
.nav-link:hover { color: var(--vp-c-brand); background: var(--vp-c-brand-dimmer); }
.active-docs { color: var(--vp-c-brand); font-weight: 600; }
.navbar-right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }

.sidebar-toggle {
  display: none;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid var(--vp-c-border);
  border-radius: 8px;
  background: transparent;
  color: var(--vp-c-text-2);
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s;
}
.sidebar-toggle:hover { color: var(--vp-c-brand); border-color: var(--vp-c-brand); }

/* ===== Body ===== */
.docs-body {
  display: flex;
  padding-top: var(--vp-nav-height);
  max-width: 1440px;
  margin: 0 auto;
  min-height: calc(100vh - var(--vp-nav-height));
}

/* ===== Sidebar ===== */
.docs-sidebar {
  width: var(--vp-sidebar-width);
  flex-shrink: 0;
  border-right: 1px solid var(--vp-c-divider);
  height: calc(100vh - var(--vp-nav-height));
  position: sticky;
  top: var(--vp-nav-height);
  overflow-y: auto;
  padding: 16px 0;
}

/* ===== Content ===== */
.docs-content {
  flex: 1;
  max-width: var(--vp-content-max-width);
  padding: 48px 48px 80px;
  min-width: 0;
}

/* ===== TOC ===== */
.docs-toc {
  width: 224px;
  flex-shrink: 0;
  height: calc(100vh - var(--vp-nav-height));
  position: sticky;
  top: var(--vp-nav-height);
  overflow-y: auto;
  padding: 48px 16px 24px;
  display: block;
}

/* ===== Mobile sidebar overlay ===== */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
}
.mobile-slide-enter-active { transition: opacity 0.2s; }
.mobile-slide-leave-active { transition: opacity 0.2s; }
.mobile-slide-enter-from, .mobile-slide-leave-to { opacity: 0; }

/* ===== Responsive ===== */
@media (max-width: 1024px) {
  .docs-toc { display: none; }
  .docs-content { padding: 32px 32px 60px; }
}
@media (max-width: 768px) {
  .docs-sidebar { display: none; }
  .sidebar-toggle { display: flex; }
  .desktop-nav { padding-left: 16px; }
  .docs-content { padding: 24px 20px 40px; max-width: 100%; }
  .navbar-inner { padding: 0 16px; }
}
</style>
