<template>
  <div class="layout-root">
    <AnimCursorGlow />
    <AnimWaves />
    <AnimTrail :count="10" :size="4" color="rgba(168,50,43,0.4)" />

    <header class="nav-pill">
      <AnimMagnetic :as="'span'" :strength="0.4" :radius="150">
        <NuxtLink to="/" class="brand">
          <span class="crest">◇</span>
          卡米API
        </NuxtLink>
      </AnimMagnetic>
      <nav class="links" aria-label="Primary navigation">
        <NuxtLink to="/" class="link nav-link-hover">主页</NuxtLink>
        <a class="link nav-link-hover" href="#">控制台</a>
        <a class="link nav-link-hover" href="#models">模型广场</a>
        <NuxtLink to="/" class="link nav-link-hover">定价</NuxtLink>
        <a class="link nav-link-hover" href="#ranking">排行榜</a>
        <a class="link nav-link-hover" href="#docs">文档</a>
        <a class="link nav-link-hover" href="#about">关于</a>
      </nav>
      <AnimRipple :duration="500" color="rgba(255,255,255,0.3)">
        <div class="nav-actions">
          <a href="#" class="login">登录</a>
        </div>
      </AnimRipple>
    </header>

    <main class="layout-content">
      <slot />
    </main>

    <footer class="footer">
      <span>{{ site.siteName }} © {{ new Date().getFullYear() }}</span>
      <span>{{ site.footerText }}</span>
      <span>{{ site.icpFiling }}</span>
    </footer>
  </div>
</template>

<script setup lang="ts">
const site = await useSiteSettings()
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
  background: #f4f0e8;
}

.nav-pill {
  position: fixed;
  top: 14px;
  left: 50%;
  z-index: 50;
  width: min(810px, calc(100% - 32px));
  height: 48px;
  display: flex;
  align-items: center;
  gap: 26px;
  padding: 0 10px 0 18px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 14px 40px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(18px);
  transform: translateX(-50%);
}

.brand,
.link,
.login {
  color: inherit;
  text-decoration: none;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
  font-family: ui-sans-serif, system-ui, sans-serif;
  font-size: 15px;
  font-weight: 900;
}

.crest {
  font-family: Georgia, serif;
  font-size: 22px;
  line-height: 1;
}

.links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 22px;
  flex: 1;
  font-family: ui-sans-serif, system-ui, sans-serif;
  color: #555;
  font-size: 13px;
  font-weight: 700;
}

.link.active,
.link:hover {
  color: #000;
}

.nav-link-hover {
  transition: transform 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94),
              color 0.2s ease;
  will-change: transform;
  display: inline-block;
}

.nav-link-hover:hover {
  transform: translateY(-1px) scale(1.04);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}

.login {
  padding: 9px 18px;
  color: #fff;
  border-radius: 999px;
  background: #050505;
  font: 800 13px/1 ui-sans-serif, system-ui, sans-serif;
}

.layout-content {
  padding-top: 76px;
}

.footer {
  min-height: 56px;
  display: flex;
  align-items: center;
  gap: 34px;
  padding: 0 9vw;
  color: #b9b9b9;
  background: #050505;
  font-family: "Courier New", monospace;
  letter-spacing: 0.32em;
  font-size: 12px;
}

@media (max-width: 1000px) {
  .links { display: none; }
  .nav-pill { justify-content: space-between; }
}

@media (max-width: 640px) {
  .footer { flex-direction: column; align-items: flex-start; justify-content: center; }
}

@media (max-width: 768px) {
  .nav-pill {
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px 16px;
  }
  .nav-pill .links {
    order: 3;
    width: 100%;
    overflow-x: auto;
    gap: 12px;
    padding: 8px 0;
  }
}
</style>
