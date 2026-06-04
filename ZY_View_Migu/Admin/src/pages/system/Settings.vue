<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>系统设置</h2>
          <p>管理站点基础配置项。</p>
        </div>
        <div class="page-head-actions">
          <n-button quaternary @click="load" :loading="loading">
            <template #icon><n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></n-icon></template>
          </n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card">
      <n-empty v-if="!loading && !settings.length" description="暂无系统设置" />
      <div v-else class="settings-list">
        <div v-for="setting in settings" :key="setting.key" class="setting-item">
          <div class="setting-info">
            <strong>{{ setting.key }}</strong>
            <p class="setting-value">{{ setting.value }}</p>
          </div>
          <n-button size="small" quaternary type="primary" @click="openEdit(setting)">编辑</n-button>
        </div>
      </div>
    </n-card>

    <n-modal v-model:show="showEdit" preset="card" title="编辑设置" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item :label="editing?.key">
          <n-input v-model:value="editValue" type="textarea" :rows="4" />
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showEdit = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="save">保存</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { NButton, NCard, NEmpty, NForm, NFormItem, NIcon, NInput, NModal, NSpace, useMessage } from 'naive-ui'
import { listSettings, updateSetting, type Setting } from '../../api/settings'

const message = useMessage()
const settings = ref<Setting[]>([])
const loading = ref(false)
const showEdit = ref(false)
const editing = ref<Setting | null>(null)
const editValue = ref('')
const saving = ref(false)

async function load() {
  loading.value = true
  try {
    settings.value = await listSettings()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openEdit(setting: Setting) {
  editing.value = setting
  editValue.value = setting.value
  showEdit.value = true
}

async function save() {
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

onMounted(() => { load() })
</script>
