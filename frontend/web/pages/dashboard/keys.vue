<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h2>API Keys</h2>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead><tr><th>Key</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr>
            <td class="mono">{{ user?.apiKey || '-' }}</td>
            <td><span class="badge badge-success">active</span></td>
            <td>
              <button class="btn-sm" @click="copyKey">复制</button>
              <button class="btn-sm btn-danger" @click="regenerateKey">重新生成</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const auth = useSaasAuth()
const { user } = auth

async function copyKey() {
  if (!user.value?.apiKey) return
  await navigator.clipboard.writeText(user.value.apiKey)
  alert('API Key 已复制到剪贴板')
}

async function regenerateKey() {
  if (!confirm('确定要重新生成 API Key？旧的 Key 将立即失效。')) return
  try {
    const res = await auth.authFetch<any>('/api/auth/keys/regenerate', { method: 'PUT' })
    if (auth.user.value) {
      auth.user.value.apiKey = res.data.apiKey
      if (import.meta.client) {
        localStorage.setItem('saas_user', JSON.stringify(auth.user.value))
      }
    }
    alert('API Key 已重新生成')
  } catch (e: any) {
    alert(e?.data?.message || '重新生成失败')
  }
}
</script>

<style scoped>
.dashboard-page { padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 20px; font-weight: 700; margin: 0; }
.table-card { background: var(--vp-c-bg); border-radius: 12px; box-shadow: var(--vp-shadow-1); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 16px; text-align: left; font-size: 14px; }
.data-table th { background: var(--vp-c-bg-soft); font-weight: 600; color: var(--vp-c-text-2); font-size: 12px; text-transform: uppercase; }
.data-table tr:not(:last-child) td { border-bottom: 1px solid var(--vp-c-bg-mute); }
.mono { font-family: var(--vp-font-family-mono); font-size: 13px; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.badge-success { background: var(--vp-c-success-soft); color: var(--vp-c-success); }
.btn-sm { padding: 6px 12px; border-radius: 6px; border: 1px solid var(--vp-c-border); background: var(--vp-c-bg); cursor: pointer; font-size: 12px; margin-right: 8px; }
.btn-sm:hover { background: var(--vp-c-bg-mute); }
.btn-danger { color: var(--vp-c-danger); border-color: var(--vp-c-danger-soft); }
.btn-danger:hover { background: var(--vp-c-danger-soft); }
</style>
