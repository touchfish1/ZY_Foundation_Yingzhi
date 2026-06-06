<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h1>注册</h1>
        <p>创建 ZHANGYUAN 账号</p>
      </div>
      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label>昵称</label>
          <input v-model="nickname" type="text" placeholder="你的昵称" class="form-input" />
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input v-model="email" type="email" placeholder="your@email.com" required class="form-input" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="至少6位" required minlength="6" class="form-input" />
        </div>
        <p v-if="error" class="form-error">{{ error }}</p>
        <button type="submit" class="btn-primary auth-btn" :disabled="auth.loading.value">
          {{ auth.loading.value ? '注册中...' : '注册' }}
        </button>
      </form>
      <p class="auth-footer">
        已有账号？<NuxtLink to="/login">登录</NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default' })

const email = ref('')
const password = ref('')
const nickname = ref('')
const error = ref('')
const auth = useSaasAuth()

async function handleRegister() {
  error.value = ''
  try {
    await auth.register(email.value, password.value, nickname.value || undefined)
    navigateTo('/dashboard')
  } catch (e: any) {
    error.value = e?.data?.message || e?.message || '注册失败'
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
  background: var(--card-bg, var(--vp-c-bg));
  border-radius: 16px;
  box-shadow: var(--vp-shadow-3);
  padding: 40px;
  width: 100%;
  max-width: 420px;
}
.auth-header { text-align: center; margin-bottom: 32px; }
.auth-header h1 { font-size: 24px; font-weight: 700; margin: 0 0 8px; color: var(--vp-c-text); font-family: var(--vp-font-family-display); }
.auth-header p { color: var(--vp-c-text-2); font-size: 14px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px; color: var(--vp-c-text); }
.form-input {
  width: 100%; padding: 10px 14px; border: 1px solid var(--vp-c-border); border-radius: 8px;
  font-size: 14px; outline: none; transition: border-color 0.2s; box-sizing: border-box; color: var(--vp-c-text); background: var(--vp-c-bg);
}
.form-input:focus { border-color: var(--vp-c-brand); box-shadow: 0 0 0 3px var(--vp-c-brand-dimmer); }
.form-error { color: var(--vp-c-danger); font-size: 13px; margin-bottom: 12px; }
.auth-btn { width: 100%; padding: 12px; font-size: 15px; }
.auth-footer { text-align: center; margin-top: 24px; font-size: 14px; color: var(--vp-c-text-2); }
.auth-footer a { color: var(--vp-c-brand); font-weight: 600; text-decoration: none; }
</style>
