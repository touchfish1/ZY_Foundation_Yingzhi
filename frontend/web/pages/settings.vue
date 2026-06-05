<template>
  <div class="settings-page">
    <h2>个人设置</h2>

    <div class="settings-card">
      <h3>基本信息</h3>
      <div class="form-group">
        <label>邮箱</label>
        <div class="form-value">{{ user?.email }}</div>
      </div>
      <div class="form-group">
        <label>昵称</label>
        <div class="form-value">{{ user?.nickname }}</div>
      </div>
    </div>

    <div class="settings-card">
      <h3>API Key</h3>
      <div class="api-key-display">
        <code class="key-text">{{ user?.apiKey || '无' }}</code>
        <button class="btn-sm" @click="copyKey">复制</button>
      </div>
    </div>

    <div class="settings-card">
      <h3>账户余额</h3>
      <div class="balance-display">
        <span class="balance-amount">¥{{ balance }}</span>
      </div>
    </div>

    <div class="settings-card">
      <h3>修改密码</h3>
      <div class="form-group">
        <label>旧密码</label>
        <input type="password" v-model="oldPassword" class="form-input" placeholder="输入旧密码" />
      </div>
      <div class="form-group">
        <label>新密码</label>
        <input type="password" v-model="newPassword" class="form-input" placeholder="输入新密码（至少6位）" />
      </div>
      <div class="form-group">
        <label>确认新密码</label>
        <input type="password" v-model="confirmPassword" class="form-input" placeholder="再次输入新密码" />
      </div>
      <p v-if="passwordError" class="form-error">{{ passwordError }}</p>
      <p v-if="passwordSuccess" class="form-success">{{ passwordSuccess }}</p>
      <button class="btn-primary" @click="changePassword" :disabled="changing">
        {{ changing ? '修改中...' : '修改密码' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const config = useRuntimeConfig()
const auth = useSaasAuth()
const { user } = auth
const balance = ref(0)

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const changing = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')

onMounted(async () => {
  if (!user.value) await auth.fetchProfile()
  if (user.value?.id) {
    try {
      const res = await $fetch(`${config.public.apiBase}/api/balance/${user.value.id}`) as any
      balance.value = res.balance || 0
    } catch (e) { console.error('Failed to load balance') }
  }
})

async function copyKey() {
  if (!user.value?.apiKey) return
  await navigator.clipboard.writeText(user.value.apiKey)
  alert('API Key 已复制')
}

async function changePassword() {
  passwordError.value = ''
  passwordSuccess.value = ''
  if (!oldPassword.value) {
    passwordError.value = '请输入旧密码'
    return
  }
  if (!newPassword.value || newPassword.value.length < 6) {
    passwordError.value = '新密码至少6位'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }
  changing.value = true
  try {
    const token = localStorage.getItem('saas_token')
    await $fetch(`${config.public.apiBase}/api/auth/password`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token}` },
      body: { oldPassword: oldPassword.value, newPassword: newPassword.value }
    }) as any
    passwordSuccess.value = '密码修改成功'
    oldPassword.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
  } catch (e: any) {
    passwordError.value = e?.data?.message || e?.message || '密码修改失败'
  } finally { changing.value = false }
}
</script>

<style scoped>
.settings-page { max-width: 720px; margin: 0 auto; padding: 40px 24px; }
.settings-page h2 { font-size: 24px; font-weight: 700; margin: 0 0 32px; }
.settings-card {
  background: #fff; border-radius: 12px; padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06); margin-bottom: 20px;
}
.settings-card h3 { font-size: 16px; font-weight: 600; margin: 0 0 16px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 12px; color: #64748b; margin-bottom: 4px; }
.form-value { font-size: 14px; color: #1e293b; }
.form-input {
  width: 100%; padding: 8px 12px; border: 1px solid #e2e8f0; border-radius: 6px;
  font-size: 14px; outline: none; box-sizing: border-box;
}
.form-input:focus { border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.form-error { color: #dc2626; font-size: 13px; margin: 0 0 12px; }
.form-success { color: #16a34a; font-size: 13px; margin: 0 0 12px; }
.api-key-display { display: flex; align-items: center; gap: 12px; }
.key-text {
  flex: 1; padding: 8px 12px; background: #f1f5f9; border-radius: 6px;
  font-size: 12px; word-break: break-all; font-family: monospace;
}
.btn-sm { padding: 6px 16px; border-radius: 6px; border: 1px solid #e2e8f0; background: #fff; cursor: pointer; font-size: 13px; }
.btn-sm:hover { background: #f1f5f9; }
.btn-primary {
  padding: 8px 24px; border-radius: 6px; border: none; background: #6366f1;
  color: #fff; font-size: 14px; cursor: pointer;
}
.btn-primary:hover { background: #4f46e5; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.balance-display { padding: 8px 0; }
.balance-amount { font-size: 28px; font-weight: 700; color: #6366f1; }
</style>
