<template>
  <div class="page-head">
    <div>
      <h2>页面管理</h2>
      <p>创建多语言页面，保存草稿并发布到前台。</p>
    </div>
    <n-button type="primary" @click="showCreate = true">新建页面</n-button>
  </div>

  <n-card>
    <n-empty v-if="!loading && !pages.length" description="暂无页面">
      <template #extra>
        <n-button size="small" @click="showCreate = true">新建第一个页面</n-button>
      </template>
    </n-empty>
    <n-data-table v-else :columns="columns" :data="pages" :loading="loading" :bordered="false" />
  </n-card>

  <n-modal v-model:show="showCreate" preset="card" title="新建页面" class="modal-card">
    <n-form>
      <n-form-item label="Slug">
        <n-input v-model:value="form.slug" placeholder="/plans" />
      </n-form-item>
      <n-form-item label="标题">
        <n-input v-model:value="form.title" placeholder="套餐价格" />
      </n-form-item>
      <n-form-item label="默认语言">
        <n-select v-model:value="form.defaultLocale" :options="localeOptions" />
      </n-form-item>
      <n-space justify="end">
        <n-button @click="showCreate = false">取消</n-button>
        <n-button type="primary" :loading="creating" @click="create">创建</n-button>
      </n-space>
    </n-form>
  </n-modal>
</template>

<script setup lang="ts">
// 页面管理：列出所有 CMS 页面，支持新建和删除
import { h, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NDataTable, NEmpty, NForm, NFormItem, NInput, NModal, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { createPage, deletePage, listPages, type CmsPageListItem } from '../../api/cms'
import { useConfirm } from '../../composables/useConfirm'

const router = useRouter()
const message = useMessage()
const { confirm } = useConfirm()
const loading = ref(false)
const creating = ref(false)
const showCreate = ref(false)
const pages = ref<CmsPageListItem[]>([])
const form = reactive({ slug: '/plans', title: '套餐价格', defaultLocale: 'zh-CN' })
const localeOptions: SelectOption[] = [
  { label: '中文 (zh-CN)', value: 'zh-CN' },
  { label: '英文 (en-US)', value: 'en-US' },
  { label: '日文 (ja-JP)', value: 'ja-JP' }
]

const columns: DataTableColumns<CmsPageListItem> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '路径', key: 'slug' },
  { title: '默认语言', key: 'defaultLocale', width: 120 },
  {
    title: '状态', key: 'status', width: 100,
    render(row) {
      return h(NTag, {
        type: row.status === 'enabled' ? 'success' : 'default',
        size: 'small'
      }, { default: () => row.status === 'enabled' ? '启用' : '禁用' })
    }
  },
  {
    title: '操作', key: 'actions', width: 280,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', ghost: true, onClick: () => router.push(`/cms/pages/${row.id}/edit`) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', quaternary: true, onClick: () => router.push(`/cms/pages/${row.id}/versions`) }, { default: () => '版本' }),
          h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
        ]
      })
    }
  }
]

// 加载页面列表
async function load() {
  console.log('[CmsPages] load')
  loading.value = true
  try {
    pages.value = await listPages()
    console.log('[CmsPages] loaded', pages.value.length, 'pages')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

// 创建新页面：提交表单 -> 跳转到编辑页
async function create() {
  console.log('[CmsPages] create', form)
  creating.value = true
  try {
    const page = await createPage(form)
    message.success('页面已创建')
    showCreate.value = false
    router.push(`/cms/pages/${page.id}/edit`)
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  } finally {
    creating.value = false
  }
}

// 删除页面：弹出确认对话框 -> 调用删除 API -> 刷新列表
async function handleDelete(row: CmsPageListItem) {
  console.log('[CmsPages] handleDelete', { id: row.id, slug: row.slug })
  const ok = await confirm({ content: `确定要删除页面 "${row.slug}"？此操作不可撤销。` })
  if (!ok) return
  try {
    await deletePage(row.id)
    message.success('页面已删除')
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => { console.log('[CmsPages] mounted'); load() })
</script>

<style scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-head h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
}
.page-head p {
  margin: 0;
  color: #64748b;
}
.modal-card {
  width: min(520px, 92vw);
}
</style>
