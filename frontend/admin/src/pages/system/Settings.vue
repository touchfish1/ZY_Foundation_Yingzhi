<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>系统设置</h2>
          <p>管理站点基础配置项。</p>
        </div>
        <div class="page-head-actions">
          <n-input v-model:value="searchQuery" placeholder="搜索配置项..." clearable style="width: 220px">
            <template #prefix>
              <n-icon size="14"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></n-icon>
            </template>
          </n-input>
          <n-button @click="toggleBatchMode">{{ batchMode ? '退出批量编辑' : '批量编辑' }}</n-button>
          <n-button quaternary @click="load" :loading="loading">
            <template #icon><n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></n-icon></template>
          </n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card">
      <template v-if="batchMode">
        <n-form label-placement="top">
          <n-form-item v-for="s in displaySettings" :key="s.key">
            <template #label>
              <div style="display: flex; align-items: center; gap: 8px;">
                <span>{{ s.key }}</span>
                <n-tag size="tiny" :bordered="false">{{ s.category }}</n-tag>
              </div>
            </template>
            <n-input v-model:value="batchValues[s.key]" type="textarea" :rows="2" />
          </n-form-item>
          <n-space justify="end" style="margin-top: 16px;">
            <n-button @click="cancelBatchEdit">取消</n-button>
            <n-button type="primary" :loading="batchSaving" @click="saveBatch">批量保存</n-button>
          </n-space>
        </n-form>
      </template>
      <template v-else>
        <n-empty v-if="!loading && !displaySettings.length" description="暂无系统设置" />
        <n-data-table v-else :columns="columns" :data="filteredSettings" :loading="loading" :bordered="false" :striped="true" size="small" />
      </template>
    </n-card>

    <n-modal v-model:show="showEdit" preset="card" title="编辑设置" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="配置键">
          <n-input :value="editing?.key" disabled />
        </n-form-item>
        <n-form-item label="配置值">
          <n-input v-model:value="editValue" type="textarea" :rows="5" placeholder="请输入配置值" />
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
import { computed, h, onMounted, ref } from 'vue'
import { NButton, NCard, NDataTable, NEmpty, NForm, NFormItem, NIcon, NInput, NModal, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { listSettings, updateSetting, batchUpdateSettings, type Setting } from '../../api/settings'

interface SettingDisplay extends Setting {
  category: string
}

function getCategory(key: string): string {
  if (key.startsWith('site.')) return '站点设置'
  if (key.startsWith('mail.')) return '邮件设置'
  return '其他设置'
}

const message = useMessage()
const settings = ref<Setting[]>([])
const loading = ref(false)
const searchQuery = ref('')

const displaySettings = computed<SettingDisplay[]>(() =>
  settings.value.map(s => ({ ...s, category: getCategory(s.key) }))
)

const filteredSettings = computed(() => {
  if (!searchQuery.value) return displaySettings.value
  const q = searchQuery.value.toLowerCase()
  return displaySettings.value.filter(s =>
    s.key.toLowerCase().includes(q) || s.value.toLowerCase().includes(q)
  )
})

const batchMode = ref(false)
const batchValues = ref<Record<string, string>>({})
const batchSaving = ref(false)

function toggleBatchMode() {
  if (batchMode.value) {
    batchMode.value = false
  } else {
    const values: Record<string, string> = {}
    settings.value.forEach(s => { values[s.key] = s.value })
    batchValues.value = values
    batchMode.value = true
  }
}

function cancelBatchEdit() {
  batchMode.value = false
}

async function saveBatch() {
  batchSaving.value = true
  try {
    await batchUpdateSettings(batchValues.value)
    message.success('所有设置已保存')
    batchMode.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '批量保存失败')
  } finally {
    batchSaving.value = false
  }
}

const showEdit = ref(false)
const editing = ref<Setting | null>(null)
const editValue = ref('')
const saving = ref(false)

const columns: DataTableColumns<SettingDisplay> = [
  { title: '配置键', key: 'key', width: 220 },
  { title: '配置值', key: 'value', width: 300, ellipsis: { tooltip: true } },
  {
    title: '分类', key: 'category', width: 120,
    render(row) {
      return h(NTag, { size: 'small', bordered: false }, { default: () => row.category })
    }
  },
  { title: '更新时间', key: 'updatedAt', width: 180 },
  {
    title: '操作', key: 'actions', width: 100,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' })
        ]
      })
    }
  }
]

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
