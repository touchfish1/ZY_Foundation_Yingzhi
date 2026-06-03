<template>
  <main class="error-page">
    <div class="error-content">
      <div class="error-code">{{ error?.statusCode || 'Error' }}</div>
      <h1 class="error-title">{{ error?.statusMessage || '页面出错了' }}</h1>
      <p class="error-desc">{{ error?.message || '请稍后重试或返回首页。' }}</p>
      <NuxtLink to="/" class="error-link">返回首页</NuxtLink>
    </div>
  </main>
</template>

<script setup lang="ts">
defineProps<{
  error: any
}>()

const handleError = () => clearError({ redirect: '/' })
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: #fff;
}

.error-content {
  text-align: center;
  max-width: 480px;
}

.error-code {
  font-size: 96px;
  font-weight: 800;
  color: #e2e8f0;
  line-height: 1;
  margin-bottom: 16px;
}

.error-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 12px;
  color: #0a0a0a;
}

.error-desc {
  font-size: 16px;
  color: #64748b;
  margin: 0 0 32px;
  line-height: 1.6;
}

.error-link {
  display: inline-block;
  padding: 12px 32px;
  background: #0a0a0a;
  color: #fff;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s;
}

.error-link:hover {
  background: #334155;
}
</style>
