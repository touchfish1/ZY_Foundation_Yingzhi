<template>
  <div class="layout-root">
    <AnimOrbs />
    <AnimWaves />
    <AnimTrail :count="8" :size="3" color="rgba(99,102,241,0.25)" />

    <header class="navbar" :class="{ 'navbar-scrolled': isScrolled }">
      <div class="navbar-inner">
        <NuxtLink to="/" class="brand">
          <svg class="brand-logo" viewBox="0 0 28 28" fill="none">
            <rect width="28" height="28" rx="7" fill="#6366f1" />
            <path d="M7 14h4l3 8-3-8h5l-3 8h4" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none" />
          </svg>
          <span class="brand-text">ZHANGYUAN</span>
        </NuxtLink>

        <nav class="desktop-nav" aria-label="Primary navigation">
          <NuxtLink to="/" class="nav-link">首页</NuxtLink>
          <NuxtLink to="/#models" class="nav-link nav-hash">模型广场</NuxtLink>
          <NuxtLink to="/#pricing" class="nav-link nav-hash">定价</NuxtLink>
          <NuxtLink to="/docs" class="nav-link">文档</NuxtLink>
          <NuxtLink to="/about" class="nav-link">关于</NuxtLink>
        </nav>

        <div class="navbar-right">
          <NuxtLink to="/console" class="cta-btn">控制台</NuxtLink>
          <button
            class="hamburger"
            :class="{ open: mobileOpen }"
            aria-label="Toggle menu"
            @click="mobileOpen = !mobileOpen"
          >
            <span />
            <span />
            <span />
          </button>
        </div>
      </div>
    </header>

    <Teleport to="body">
      <Transition name="mobile-slide">
        <div v-if="mobileOpen" class="mobile-overlay" @click.self="mobileOpen = false">
          <div class="mobile-overlay-inner">
            <NuxtLink to="/" class="mobile-link" @click="mobileOpen = false">首页</NuxtLink>
            <NuxtLink to="/#models" class="mobile-link" @click="mobileOpen = false">模型广场</NuxtLink>
            <NuxtLink to="/#pricing" class="mobile-link" @click="mobileOpen = false">定价</NuxtLink>
            <NuxtLink to="/docs" class="mobile-link" @click="mobileOpen = false">文档</NuxtLink>
            <NuxtLink to="/about" class="mobile-link" @click="mobileOpen = false">关于</NuxtLink>
            <NuxtLink to="/console" class="mobile-cta" @click="mobileOpen = false">控制台</NuxtLink>
          </div>
        </div>
      </Transition>
    </Teleport>

    <main class="main-content">
      <slot />
    </main>

    <footer class="footer">
      <div class="footer-grid">
        <div class="footer-col footer-brand">
          <div class="footer-brand-row">
            <svg class="brand-logo" viewBox="0 0 28 28" fill="none">
              <rect width="28" height="28" rx="7" fill="#6366f1" />
              <path d="M7 14h4l3 8-3-8h5l-3 8h4" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none" />
            </svg>
            <span class="footer-brand-text">ZHANGYUAN</span>
          </div>
          <p class="footer-brand-desc">顶级 AI 模型 · 即刻接入。稳定、高性能的 API 服务平台。</p>
        </div>
        <div class="footer-col">
          <h4 class="footer-heading">产品</h4>
          <NuxtLink to="/#models" class="footer-link">模型广场</NuxtLink>
          <NuxtLink to="/#pricing" class="footer-link">定价</NuxtLink>
          <NuxtLink to="/docs" class="footer-link">文档</NuxtLink>
        </div>
        <div class="footer-col">
          <h4 class="footer-heading">公司</h4>
          <NuxtLink to="/about" class="footer-link">关于</NuxtLink>
          <a href="#" class="footer-link">博客</a>
        </div>
        <div class="footer-col">
          <h4 class="footer-heading">法律</h4>
          <a href="#" class="footer-link">隐私</a>
          <a href="#" class="footer-link">条款</a>
        </div>
        <div class="footer-col">
          <h4 class="footer-heading">社交</h4>
          <a href="#" class="footer-link" target="_blank" rel="noopener">GitHub</a>
          <a href="#" class="footer-link" target="_blank" rel="noopener">Twitter</a>
        </div>
      </div>
      <div class="footer-bottom">
        <span>&copy; {{ new Date().getFullYear() }} {{ site.siteName || 'ZHANGYUAN' }}. All rights reserved.</span>
        <span v-if="site.icpFiling">{{ site.icpFiling }}</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const site = await useSiteSettings()

const mobileOpen = ref(false)
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
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style>
html {
  scroll-behavior: smooth;
  scroll-padding-top: 80px;
}
</style>

<style scoped>
.layout-root {
  min-height: 100vh;
  background: #f8fafc;
  color: #0f172a;
  font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ===== Navbar ===== */

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  height: 64px;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  border-bottom: 1px solid transparent;
  transition: box-shadow 0.3s ease, border-color 0.3s ease, background 0.3s ease;
}

.navbar-scrolled {
  background: rgba(255, 255, 255, 0.88);
  border-bottom-color: rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.04);
}

.navbar-inner {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 32px;
  height: 100%;
}

/* ===== Brand ===== */

.brand {
  display: flex;
  align-items: center;
  gap: 9px;
  text-decoration: none;
  flex-shrink: 0;
}

.brand-logo {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  transition: transform 0.25s ease;
}

.brand:hover .brand-logo {
  transform: scale(1.06);
}

.brand-text {
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}

/* ===== Desktop Nav ===== */

.desktop-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  flex: 1;
}

.nav-link {
  position: relative;
  padding: 6px 14px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  text-decoration: none;
  border-radius: 6px;
  transition: color 0.2s ease, background 0.2s ease;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: #6366f1;
  border-radius: 2px;
  transform: translateX(-50%);
  transition: width 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.nav-link:hover {
  color: #6366f1;
  background: rgba(99, 102, 241, 0.06);
}

.nav-link:hover::after {
  width: 24px;
}

.nav-link.router-link-exact-active:not(.nav-hash) {
  color: #6366f1;
  font-weight: 600;
}

.nav-link.router-link-exact-active:not(.nav-hash)::after {
  width: 24px;
}

/* ===== CTA Button ===== */

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.cta-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 38px;
  padding: 0 22px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #6366f1, #7c3aed);
  border-radius: 999px;
  text-decoration: none;
  transition: transform 0.15s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.cta-btn:hover {
  background: linear-gradient(135deg, #4f46e5, #6d28d9);
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

/* ===== Hamburger ===== */

.hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  width: 36px;
  height: 36px;
  padding: 6px;
  background: none;
  border: none;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.2s;
}

.hamburger:hover {
  background: rgba(0, 0, 0, 0.04);
}

.hamburger span {
  display: block;
  width: 100%;
  height: 2px;
  background: #334155;
  border-radius: 2px;
  transform-origin: center;
  transition: transform 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94),
              opacity 0.15s ease;
}

.hamburger.open span:nth-child(1) {
  transform: translateY(7px) rotate(45deg);
}

.hamburger.open span:nth-child(2) {
  opacity: 0;
  transform: scaleX(0);
}

.hamburger.open span:nth-child(3) {
  transform: translateY(-7px) rotate(-45deg);
}

/* ===== Mobile Overlay ===== */

.mobile-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.88);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.mobile-slide-enter-active {
  transition: opacity 0.3s ease;
}
.mobile-slide-leave-active {
  transition: opacity 0.2s ease;
}
.mobile-slide-enter-from,
.mobile-slide-leave-to {
  opacity: 0;
}

.mobile-overlay-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  animation: overlay-up 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes overlay-up {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

.mobile-link {
  display: block;
  padding: 14px 40px;
  font-size: 22px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  border-radius: 8px;
  transition: color 0.2s, background 0.2s;
  letter-spacing: 0.02em;
}

.mobile-link:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.mobile-cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 16px;
  padding: 12px 40px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #6366f1, #7c3aed);
  border-radius: 999px;
  text-decoration: none;
  transition: box-shadow 0.2s;
}

.mobile-cta:hover {
  box-shadow: 0 4px 24px rgba(99, 102, 241, 0.5);
}

/* ===== Main Content ===== */

.main-content {
  padding-top: 64px;
  min-height: calc(100vh - 64px);
}

/* ===== Footer ===== */

.footer {
  background: #0f172a;
  color: #cbd5e1;
  padding: 72px 32px 32px;
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  gap: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.footer-brand-desc {
  margin: 12px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: #94a3b8;
  max-width: 260px;
}

.footer-brand-row {
  display: flex;
  align-items: center;
  gap: 9px;
}

.footer-brand-text {
  font-size: 17px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.02em;
}

.footer-heading {
  margin: 0 0 16px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.footer-link {
  display: block;
  padding: 5px 0;
  font-size: 14px;
  color: #94a3b8;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-link:hover {
  color: #a5b4fc;
}

.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
  max-width: 1200px;
  margin: 52px auto 0;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 13px;
  color: #64748b;
}

/* ===== Responsive ===== */

@media (max-width: 900px) {
  .footer-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .footer-brand {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .desktop-nav {
    display: none;
  }

  .cta-btn {
    display: none;
  }

  .hamburger {
    display: flex;
  }

  .navbar-inner {
    padding: 0 20px;
  }

  .footer {
    padding: 48px 24px 28px;
  }

  .footer-bottom {
    flex-direction: column;
    gap: 8px;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .navbar-inner {
    padding: 0 16px;
  }

  .brand-text {
    font-size: 15px;
  }
}
</style>
