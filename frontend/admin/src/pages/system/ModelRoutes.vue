<template>
  <div>
    <n-card title="模型路由" :bordered="true" class="table-card" style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
      <template #header-extra>
        <n-space align="center" size="small">
          <n-button type="primary" @click="openCreate">新建路由</n-button>
        </n-space>
      </template>
      <CommonTable
        :columns="columns"
        :data="routes"
        :loading="loading"
      />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" :title="isEditing ? '编辑路由' : '新建路由'" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="模型名称">
          <n-input v-model:value="form.modelName" placeholder="例如: gpt-4o" />
        </n-form-item>
        <n-form-item label="提供商">
          <n-select v-model:value="form.provider" :options="providerOptions" placeholder="选择提供商" />
        </n-form-item>
        <n-form-item label="提供商模型名称">
          <n-input v-model:value="form.providerModelName" placeholder="例如: gpt-4o-2024-08-06" />
        </n-form-item>
        <n-form-item label="模型类型">
          <n-select v-model:value="form.modelType" :options="modelTypeOptions" placeholder="选择模型类型" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="form.status" :options="statusOptions" placeholder="选择状态" />
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="submitForm">保存</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NForm, NFormItem, NInput, NModal, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import { listModelRoutes, createModelRoute, updateModelRoute, deleteModelRoute, type ModelRouteConfig } from '../../api/ai'
import { useConfirm } from '../../composables/useConfirm'

const message = useMessage()
const { confirm } = useConfirm()

const routes = ref<ModelRouteConfig[]>([])
const loading = ref(false)
const showModal = ref(false)
const saving = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  modelName: '',
  provider: '',
  providerModelName: '',
  modelType: 'chat',
  status: 'active'
})

const providerOptions: SelectOption[] = [
  { label: 'OpenAI', value: 'openai' },
  { label: 'Anthropic', value: 'anthropic' },
  { label: 'Google', value: 'google' },
  { label: 'Azure', value: 'azure' },
  { label: 'DeepSeek', value: 'deepseek' },
  { label: 'Qwen', value: 'qwen' },
  { label: '自定义', value: 'custom' }
]

const modelTypeOptions: SelectOption[] = [
  { label: '对话 (chat)', value: 'chat' },
  { label: '嵌入 (embedding)', value: 'embedding' },
  { label: '图像 (image)', value: 'image' },
  { label: '音频 (audio)', value: 'audio' }
]

const statusOptions: SelectOption[] = [
  { label: '启用', value: 'active' },
  { label: '禁用', value: 'inactive' }
]

const columns: DataTableColumns<ModelRouteConfig> = [
  { title: 'ID', key: 'id', width: 70, render(row) { return h(NTag, { size: 'tiny', bordered: false }, { default: () => row.id }) } },
  { title: '模型名称', key: 'modelName' },
  { title: '提供商', key: 'provider', width: 100 },
  { title: '提供商模型名称', key: 'providerModelName' },
  { title: '模型类型', key: 'modelType', width: 100 },
  {
    title: '状态', key: 'status', width: 80,
    render(row) {
      return h(NTag, { type: row.status === 'active' ? 'success' : 'warning', size: 'small', bordered: false },
        { default: () => row.status === 'active' ? '启用' : '禁用' })
    }
  },
  {
    title: '操作', key: 'actions', width: 180,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', quaternary: true, onClick: () => openEdit(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'error', quaternary: true, onClick: () => handleDelete(row.id) }, { default: () => '删除' })
        ]
      })
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    routes.value = await listModelRoutes()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.modelName = ''
  form.provider = ''
  form.providerModelName = ''
  form.modelType = 'chat'
  form.status = 'active'
}

function openCreate() {
  isEditing.value = false
  editingId.value = null
  resetForm()
  showModal.value = true
}

function openEdit(row: ModelRouteConfig) {
  isEditing.value = true
  editingId.value = row.id
  form.modelName = row.modelName
  form.provider = row.provider
  form.providerModelName = row.providerModelName
  form.modelType = row.modelType
  form.status = row.status
  showModal.value = true
}

async function submitForm() {
  saving.value = true
  try {
    if (isEditing.value && editingId.value) {
      await updateModelRoute(editingId.value, { ...form })
      message.success('路由已更新')
    } else {
      await createModelRoute({ ...form })
      message.success('路由已创建')
    }
    showModal.value = false
    await loadData()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  const confirmed = await confirm({ content: '确定要删除此模型路由吗？' })
  if (!confirmed) return
  try {
    await deleteModelRoute(id)
    message.success('路由已删除')
    await loadData()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => { loadData() })
</script>
