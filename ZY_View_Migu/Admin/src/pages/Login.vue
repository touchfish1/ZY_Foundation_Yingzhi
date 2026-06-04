<template>
  <main class="login-page">
    <section class="panel">
      <div class="brand-section">
        <div class="brand-icon">
          <n-icon size="48" color="#6366f1">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
          </n-icon>
        </div>
        <p class="eyebrow">Project ZHANGYUAN</p>
        <h1>运营内容管理中心</h1>
        <p class="sub">管理多语言页面、区块内容、套餐展示和发布版本。</p>
      </div>

      <n-card class="card" :bordered="false">
        <n-h3 style="margin-bottom:20px;font-weight:700;">登录后台</n-h3>
        <n-form @submit.prevent="submit">
          <n-form-item label="账号">
            <n-input v-model:value="username" placeholder="admin" size="large" />
          </n-form-item>
          <n-form-item label="密码">
            <n-input v-model:value="password" type="password" placeholder="admin123" size="large" @keyup.enter="submit" />
          </n-form-item>
          <n-button type="primary" block :loading="loading" @click="submit" size="large" style="margin-top:8px;">
            {{ loading ? '登录中...' : '登录' }}
          </n-button>
        </n-form>
      </n-card>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NH3, NIcon, NInput, useMessage } from 'naive-ui'
import { login } from '../api/auth'

const router = useRouter()
const message = useMessage()
const username = ref('admin')
const password = ref('admin123')
const loading = ref(false)

async function submit() {
  if (!username.value || !password.value) {
    message.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await login(username.value, password.value)
    message.success('登录成功')
    router.push('/')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  background: radial-gradient(circle at 20% 20%, rgba(99, 102, 241, 0.15), transparent 40%), radial-gradient(circle at 80% 80%, rgba(16, 185, 129, 0.1), transparent 40%), linear-gradient(135deg, #eef4ff, #f8fafc 45%, #eefdf8);
}

.panel {
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 48px;
  align-items: center;
}

.brand-section {
  max-width: 520px;
}

.brand-icon {
  margin-bottom: 20px;
}

.eyebrow {
  color: #6366f1;
  font-weight: 800;
  letter-spacing: 0.08em;
  margin: 0 0 16px;
  font-size: 13px;
}

h1 {
  margin: 0 0 16px;
  font-size: clamp(36px, 5vw, 64px);
  line-height: 1.1;
  font-weight: 800;
}

.sub {
  color: #64748b;
  font-size: 16px;
  line-height: 1.6;
}

.card {
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
  border-radius: 16px;
}

@media (max-width: 760px) {
  .panel {
    grid-template-columns: 1fr;
  }
}
</style>
