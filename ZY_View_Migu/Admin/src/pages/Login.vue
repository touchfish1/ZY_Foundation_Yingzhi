<template>
  <main class="login-page">
    <section class="panel">
      <div>
        <p class="eyebrow">Project ZHANGYUAN</p>
        <h1>运营内容管理中心</h1>
        <p class="sub">管理多语言页面、区块内容、套餐展示和发布版本。</p>
      </div>
      <n-card class="card" title="登录后台">
        <n-form @submit.prevent="submit">
          <n-form-item label="账号">
            <n-input v-model:value="username" placeholder="admin" />
          </n-form-item>
          <n-form-item label="密码">
            <n-input v-model:value="password" type="password" placeholder="admin123" />
          </n-form-item>
          <n-button type="primary" block :loading="loading" @click="submit">登录</n-button>
        </n-form>
      </n-card>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NInput, useMessage } from 'naive-ui'
import { login } from '../api/auth'

const router = useRouter()
const message = useMessage()
const username = ref('admin')
const password = ref('admin123')
const loading = ref(false)

async function submit() {
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
  background:
    radial-gradient(circle at 20% 20%, rgba(37, 99, 235, 0.2), transparent 32%),
    linear-gradient(135deg, #eef4ff, #f8fafc 45%, #eefdf8);
}

.panel {
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 48px;
  align-items: center;
}

.eyebrow {
  color: #2563eb;
  font-weight: 800;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  font-size: clamp(38px, 6vw, 72px);
  line-height: 1;
}

.sub {
  max-width: 520px;
  color: #64748b;
  font-size: 18px;
}

.card {
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
}

@media (max-width: 760px) {
  .panel {
    grid-template-columns: 1fr;
  }
}
</style>
