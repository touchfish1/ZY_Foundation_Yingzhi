<template>
  <div class="layout-root">
    <!-- Navbar -->
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
          <template v-if="auth.isLoggedIn.value">
            <NuxtLink to="/dashboard" class="nav-link">控制台</NuxtLink>
            <NuxtLink to="/pricing" class="nav-link">定价</NuxtLink>
            <NuxtLink to="/docs" class="nav-link">文档</NuxtLink>
          </template>
          <template v-else>
            <NuxtLink to="/" class="nav-link">首页</NuxtLink>
            <NuxtLink to="/#models" class="nav-link nav-hash">模型广场</NuxtLink>
            <NuxtLink to="/#pricing" class="nav-link nav-hash">定价</NuxtLink>
            <NuxtLink to="/docs" class="nav-link">文档</NuxtLink>
            <NuxtLink to="/about" class="nav-link">关于</NuxtLink>
          </template>
        </nav>

        <div class="navbar-right">
          <!-- Dark mode toggle -->
          <ColorSchemeToggle />

          <template v-if="auth.isLoggedIn.value">
            <NuxtLink to="/dashboard" class="nav-link dashboard-link">控制台</NuxtLink>
            <div class="user-menu" @click.stop="userMenuOpen = !userMenuOpen">
              <button class="user-avatar-btn" :title="auth.user.value?.nickname || auth.user.value?.email || '用户'">
                <span class="user-avatar-text">{{ (auth.user.value?.nickname || auth.user.value?.email || 'U').charAt(0).toUpperCase() }}</span>
              </button>
              <Transition name="menu-drop">
                <div v-if="userMenuOpen" class="user-dropdown">
                  <div class="dropdown-header">
                    <span class="dropdown-name">{{ auth.user.value?.nickname || '用户' }}</span>
                    <span class="dropdown-email">{{ auth.user.value?.email }}</span>
                    <span class="dropdown-balance">余额: ¥{{ balance }}</span>
                  </div>
                  <NuxtLink to="/dashboard" class="dropdown-item" @click="userMenuOpen = false">控制台</NuxtLink>
                  <NuxtLink to="/pricing" class="dropdown-item" @click="userMenuOpen = false">定价</NuxtLink>
                  <NuxtLink to="/settings" class="dropdown-item" @click="userMenuOpen = false">设置</NuxtLink>
                  <hr class="dropdown-divider" />
                  <button class="dropdown-item dropdown-logout" @click="handleLogout">退出登录</button>
                </div>
              </Transition>
            </div>
          </template>
          <template v-else>
            <NuxtLink to="/login" class="nav-link nav-login">登录</NuxtLink>
            <NuxtLink to="/register" class="cta-btn">注册</NuxtLink>
          </template>

          <button
            class="hamburger"
            :class="{ open: mobileOpen }"
            aria-label="Toggle menu"
            @click="mobileOpen = !mobileOpen"
          >
            <span /><span /><span />
          </button>
        </div>
      </div>
    </header>

    <!-- Mobile overlay -->
    <Teleport to="body">
      <Transition name="mobile-slide">
        <div v-if="mobileOpen" class="mobile-overlay" @click.self="mobileOpen = false">
          <div class="mobile-overlay-inner">
            <NuxtLink to="/" class="mobile-link" @click="mobileOpen = false">首页</NuxtLink>
            <NuxtLink to="/#models" class="mobile-link" @click="mobileOpen = false">模型广场</NuxtLink>
            <NuxtLink to="/#pricing" class="mobile-link" @click="mobileOpen = false">定价</NuxtLink>
            <NuxtLink to="/docs" class="mobile-link" @click="mobileOpen = false">文档</NuxtLink>
            <NuxtLink to="/about" class="mobile-link" @click="mobileOpen = false">关于</NuxtLink>
            <div class="mobile-color-scheme">
              <ColorSchemeToggle />
              <span class="mobile-theme-label">{{ colorMode === 'dark' ? '暗色模式' : '亮色模式' }}</span>
            </div>
            <template v-if="auth.isLoggedIn.value">
              <NuxtLink to="/dashboard" class="mobile-link" @click="mobileOpen = false">控制台</NuxtLink>
              <NuxtLink to="/pricing" class="mobile-link" @click="mobileOpen = false">定价</NuxtLink>
              <NuxtLink to="/docs" class="mobile-link" @click="mobileOpen = false">文档</NuxtLink>
              <hr class="mobile-divider" />
              <button class="mobile-link mobile-logout" @click="handleLogout">退出登录</button>
            </template>
            <template v-else>
              <NuxtLink to="/login" class="mobile-link" @click="mobileOpen = false">登录</NuxtLink>
              <NuxtLink to="/register" class="mobile-cta" @click="mobileOpen = false">注册</NuxtLink>
            </template>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Main content -->
    <main class="main-content">
      <slot />
    </main>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-grid">
        <div class="footer-col footer-brand">
          <div class="footer-brand-row">
            <svg class="brand-logo" viewBox="0 0 28 28" fill="none">
              <rect width="28" height="28" rx="7" fill="var(--vp-c-brand)" />
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
          <NuxtLink to="/blog" class="footer-link">博客</NuxtLink>
        </div>
        <div class="footer-col">
          <h4 class="footer-heading">法律</h4>
          <a href="#" class="footer-link">隐私</a>
          <a href="#" class="footer-link">条款</a>
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
const auth = useSaasAuth()
const { colorMode, init } = useColorMode()

const balance = ref(0)
const mobileOpen = ref(false)
const isScrolled = ref(false)
const userMenuOpen = ref(false)

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

function handleLogout() {
  userMenuOpen.value = false
  mobileOpen.value = false
  auth.logout()
}

async function fetchBalance() {
  if (!auth.isLoggedIn.value || !auth.user.value?.id) return
  try {
    const config = useRuntimeConfig()
    const res = await $fetch(`${config.public.apiBase}/api/balance/${auth.user.value.id}`) as any
    balance.value = res.balance || 0
  } catch (e) { /* silent */ }
}

function onKeyDown(e: KeyboardEvent) {
  if (e.key === 'Escape') userMenuOpen.value = false
}
function onDocClick() {
  userMenuOpen.value = false
}

onMounted(() => {
  init()
  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('keydown', onKeyDown)
  document.addEventListener('click', onDocClick)
  onScroll()
  fetchBalance()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('keydown', onKeyDown)
  document.removeEventListener('click', onDocClick)
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
  background: var(--vp-c-bg);
  color: var(--vp-c-text);
  font-family: var(--vp-font-family-base);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
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
.navbar-scrolled {
  box-shadow: var(--vp-shadow-nav);
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
  color: var(--vp-c-text);
  letter-spacing: -0.02em;
  transition: color var(--vp-t-color);
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
  color: var(--vp-c-text-2);
  text-decoration: none;
  border-radius: 6px;
  transition: color 0.2s, background 0.2s;
}
.nav-link:hover {
  color: var(--vp-c-brand);
  background: var(--vp-c-brand-dimmer);
}
.nav-link.router-link-exact-active:not(.nav-hash) {
  color: var(--vp-c-brand);
  font-weight: 600;
}

/* ===== Navbar Right ===== */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

/* ===== CTA Button ===== */
.cta-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  padding: 0 20px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: var(--vp-c-brand-gradient);
  border-radius: 8px;
  text-decoration: none;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}
.cta-btn:hover {
  box-shadow: 0 4px 16px rgba(100, 108, 255, 0.4);
  transform: translateY(-1px);
}

.nav-login {
  padding: 6px 16px !important;
  font-size: 14px !important;
}
.dashboard-link {
  display: none;
}

/* ===== User Menu ===== */
.user-menu { position: relative; }
.user-avatar-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid var(--vp-c-border);
  background: var(--vp-c-brand-gradient);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
  padding: 0;
  font-family: inherit;
}
.user-avatar-btn:hover {
  border-color: var(--vp-c-brand);
  box-shadow: 0 0 0 3px var(--vp-c-brand-dimmer);
  transform: scale(1.05);
}
.user-avatar-text {
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}
.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 200px;
  background: var(--vp-c-bg-elavate);
  border: 1px solid var(--vp-c-border);
  border-radius: 12px;
  box-shadow: var(--vp-shadow-3);
  overflow: hidden;
  z-index: 60;
}
.dropdown-header {
  padding: 14px 16px;
  border-bottom: 1px solid var(--vp-c-divider);
}
.dropdown-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--vp-c-text);
}
.dropdown-email {
  display: block;
  font-size: 12px;
  color: var(--vp-c-text-2);
  margin-top: 2px;
}
.dropdown-balance {
  display: block;
  font-size: 13px;
  color: var(--vp-c-brand);
  font-weight: 600;
  margin-top: 6px;
}
.dropdown-item {
  display: block;
  width: 100%;
  padding: 10px 16px;
  font-size: 14px;
  color: var(--vp-c-text);
  text-decoration: none;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s;
}
.dropdown-item:hover {
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-brand);
}
.dropdown-divider {
  margin: 4px 0;
  border: none;
  border-top: 1px solid var(--vp-c-divider);
}
.dropdown-logout { color: var(--vp-c-danger); }
.dropdown-logout:hover {
  background: var(--vp-c-danger-soft) !important;
  color: var(--vp-c-danger) !important;
}

/* ===== User Menu Dropdown Transition ===== */
.menu-drop-enter-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.menu-drop-leave-active { transition: opacity 0.1s ease, transform 0.1s ease; }
.menu-drop-enter-from, .menu-drop-leave-to {
  opacity: 0;
  transform: translateY(-6px);
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
  border: 1px solid var(--vp-c-border);
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.2s;
}
.hamburger:hover { background: var(--vp-c-bg-soft); }
.hamburger span {
  display: block;
  width: 100%;
  height: 2px;
  background: var(--vp-c-text-2);
  border-radius: 2px;
  transform-origin: center;
  transition: transform 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94), opacity 0.15s ease;
}
.hamburger.open span:nth-child(1) { transform: translateY(7px) rotate(45deg); }
.hamburger.open span:nth-child(2) { opacity: 0; transform: scaleX(0); }
.hamburger.open span:nth-child(3) { transform: translateY(-7px) rotate(-45deg); }

/* ===== Mobile Overlay ===== */
.mobile-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}
.mobile-slide-enter-active { transition: opacity 0.3s ease; }
.mobile-slide-leave-active { transition: opacity 0.2s ease; }
.mobile-slide-enter-from, .mobile-slide-leave-to { opacity: 0; }
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
  background: var(--vp-c-brand-gradient);
  border-radius: 8px;
  text-decoration: none;
  transition: box-shadow 0.2s;
}
.mobile-cta:hover { box-shadow: 0 4px 24px rgba(100, 108, 255, 0.5); }
.mobile-logout {
  font-size: 16px !important;
  color: rgba(239, 68, 68, 0.8) !important;
  background: none;
  border: none;
  cursor: pointer;
  font-family: inherit;
  padding: 14px 40px;
}
.mobile-logout:hover {
  color: #ef4444 !important;
  background: rgba(239, 68, 68, 0.1) !important;
}
.mobile-divider {
  width: 60%;
  border: none;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  margin: 8px 0;
}
.mobile-color-scheme {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 40px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}
.mobile-theme-label { font-weight: 500; }

/* ===== Main Content ===== */
.main-content {
  padding-top: var(--vp-nav-height);
  min-height: calc(100vh - var(--vp-nav-height));
}

/* ===== Footer ===== */
.footer {
  background: var(--vp-c-bg-alt);
  color: var(--vp-c-text-2);
  padding: 72px 32px 32px;
  border-top: 1px solid var(--vp-c-divider);
  transition: background var(--vp-t-bg), border-color var(--vp-t-border);
}
.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 40px;
  max-width: 1200px;
  margin: 0 auto;
}
.footer-brand-desc {
  margin: 12px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--vp-c-text-3);
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
  color: var(--vp-c-text);
  letter-spacing: -0.02em;
}
.footer-heading {
  margin: 0 0 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--vp-c-text);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.footer-link {
  display: block;
  padding: 5px 0;
  font-size: 14px;
  color: var(--vp-c-text-3);
  text-decoration: none;
  transition: color 0.2s;
}
.footer-link:hover { color: var(--vp-c-brand); }
.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
  max-width: 1200px;
  margin: 52px auto 0;
  padding-top: 24px;
  border-top: 1px solid var(--vp-c-divider);
  font-size: 13px;
  color: var(--vp-c-text-3);
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .footer-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .footer-brand { grid-column: 1 / -1; }
}
@media (max-width: 768px) {
  .desktop-nav { display: none; }
  .cta-btn,
  .nav-login,
  .dashboard-link,
  .user-menu { display: none; }
  .hamburger { display: flex; }
  .navbar-inner { padding: 0 20px; }
  .footer { padding: 48px 24px 28px; }
  .footer-bottom { flex-direction: column; gap: 8px; text-align: center; }
}
@media (max-width: 480px) {
  .navbar-inner { padding: 0 16px; }
  .brand-text { font-size: 15px; }
}
</style>
