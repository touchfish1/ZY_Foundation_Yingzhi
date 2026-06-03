<template>
  <div class="page-head">
    <div>
      <h2>系统设置</h2>
      <p>管理站点基础配置项。</p>
    </div>
    <n-button @click="load" :loading="loading">刷新</n-button>
  </div>

  <n-card>
    <n-empty v-if="!loading && !settings.length" description="暂无系统设置" />
    <div v-else class="settings-list">
      <div v-for="setting in settings" :key="setting.key" class="setting-item">
        <div class="setting-info">
          <strong>{{ setting.key }}</strong>
          <p class="setting-value">{{ setting.value }}</p>
        </div>
        <n-button size="small" @click="openEdit(setting)">编辑</n-button>
      </div>
    </div>
  </n-card>

  <n-modal v-model:show="showEdit" preset="card" title="编辑设置" class="modal-card">
    <n-form>
      <n-form-item :label="editing?.key">
        <n-input v-model:value="editValue" type="textarea" :rows="4" />
      </n-form-item>
      <n-button type="primary" :loading="saving" @click="save">保存</n-button>
    </n-form>
  </n-modal>
</template>

<script setup lang="ts">
// 系统设置页面：查看和编辑站点配置项
import { onMounted, ref } from 'vue'
import { NButton, NCard, NEmpty, NForm, NFormItem, NInput, NModal, useMessage } from 'naive-ui'
import { listSettings, updateSetting, type Setting } from '../../api/settings'

const message = useMessage()
const settings = ref<Setting[]>([])
const loading = ref(false)
const showEdit = ref(false)
const editing = ref<Setting | null>(null)
const editValue = ref('')
const saving = ref(false)

// 加载系统设置列表
async function load() {
  console.log('[Settings] load')
  loading.value = true
  try {
    settings.value = await listSettings()
    console.log('[Settings] loaded', settings.value.length, 'settings')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

// 打开编辑设置模态框，填充当前值
function openEdit(setting: Setting) {
  console.log('[Settings] openEdit', { key: setting.key })
  editing.value = setting
  editValue.value = setting.value
  showEdit.value = true
}

// 保存设置项：校验 -> 调用 API -> 刷新列表
async function save() {
  console.log('[Settings] save')
  if (!editing.value) return
  if (!editValue.value) {
    message.warning('设置值不能为空')
    return
  }
  saving.value = true
  try {
    await updateSetting(editing.value.key, editValue.value)
    message.success('设置已更新')
    showEdit.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => { console.log('[Settings] mounted'); load() })
</script>

<style scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-head h2 { margin: 0 0 6px; }
.page-head p { margin: 0; color: #64748b; }
.settings-list { display: flex; flex-direction: column; gap: 8px; }
.setting-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; border: 1px solid #e5e7eb; border-radius: 12px;
}
.setting-info strong { display: block; margin-bottom: 4px; }
.setting-value { margin: 0; font-size: 13px; color: #64748b; word-break: break-all; }
.modal-card { width: min(520px, 92vw); }
</style>
