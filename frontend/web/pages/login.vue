<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h1>登录</h1>
        <p>登录到 ZHANGYUAN 平台</p>
      </div>
      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label>邮箱</label>
          <input v-model="email" type="email" placeholder="your@email.com" required class="form-input" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;" required class="form-input" />
        </div>
        <p v-if="error" class="form-error">{{ error }}</p>
        <button type="submit" class="btn-primary auth-btn" :disabled="auth.loading.value">
          {{ auth.loading.value ? '登录中...' : '登录' }}
        </button>
      </form>
      <p class="auth-footer">
        还没有账号？<NuxtLink to="/register">立即注册</NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default' })

const email = ref('')
const password = ref('')
const error = ref('')
const auth = useSaasAuth()

async function handleLogin() {
  error.value = ''
  try {
    await auth.login(email.value, password.value)
    navigateTo('/dashboard')
  } catch (e: any) {
    error.value = e?.data?.message || e?.message || '登录失败'
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}
.auth-card {
  background: var(--card-bg, #fff);
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  padding: 40px;
  width: 100%;
  max-width: 420px;
}
.auth-header { text-align: center; margin-bottom: 32px; }
.auth-header h1 { font-size: 24px; font-weight: 700; margin: 0 0 8px; }
.auth-header p { color: #64748b; font-size: 14px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px; color: #334155; }
.form-input {
  width: 100%; padding: 10px 14px; border: 1px solid #e2e8f0; border-radius: 8px;
  font-size: 14px; outline: none; transition: border-color 0.2s; box-sizing: border-box;
}
.form-input:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.1); }
.form-error { color: #ef4444; font-size: 13px; margin-bottom: 12px; }
.auth-btn { width: 100%; padding: 12px; font-size: 15px; }
.auth-footer { text-align: center; margin-top: 24px; font-size: 14px; color: #64748b; }
.auth-footer a { color: #6366f1; font-weight: 600; text-decoration: none; }
</style>
