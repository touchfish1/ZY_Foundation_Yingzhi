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
  // TODO: call API to regenerate
  alert('API Key 已重新生成')
}
</script>

<style scoped>
.dashboard-page { padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 20px; font-weight: 700; margin: 0; }
.table-card { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 16px; text-align: left; font-size: 14px; }
.data-table th { background: #f8fafc; font-weight: 600; color: #475569; font-size: 12px; text-transform: uppercase; }
.data-table tr:not(:last-child) td { border-bottom: 1px solid #f1f5f9; }
.mono { font-family: monospace; font-size: 13px; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.badge-success { background: #dcfce7; color: #16a34a; }
.btn-sm { padding: 6px 12px; border-radius: 6px; border: 1px solid #e2e8f0; background: #fff; cursor: pointer; font-size: 12px; margin-right: 8px; }
.btn-sm:hover { background: #f1f5f9; }
.btn-danger { color: #dc2626; border-color: #fecaca; }
.btn-danger:hover { background: #fef2f2; }
</style>
