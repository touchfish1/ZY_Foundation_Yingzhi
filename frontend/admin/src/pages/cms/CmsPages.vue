<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>页面管理</h2>
          <p>创建多语言页面，保存草稿并发布到前台。</p>
        </div>
        <div class="page-head-actions">
          <n-button quaternary @click="load" :loading="loading">
            <template #icon><n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></n-icon></template>
          </n-button>
          <n-button type="primary" @click="showCreate = true">新建页面</n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="true" class="table-card" style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
      <n-input
        v-model:value="keyword"
        placeholder="搜索页面路径或标题..."
        clearable
        style="max-width: 320px; margin-bottom: 12px;"
      >
        <template #prefix>
          <n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></n-icon>
        </template>
      </n-input>
      <n-empty v-if="!loading && !pages.length" description="暂无页面">
        <template #extra>
          <n-button size="small" @click="showCreate = true">新建第一个页面</n-button>
        </template>
      </n-empty>
      <CommonTable
        v-else
        :columns="columns"
        :data="pages"
        :loading="loading"
        :pagination="paginationReactive"
      />
    </n-card>

    <n-modal v-model:show="showCreate" preset="card" title="新建页面" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="Slug（路径）">
          <n-input v-model:value="form.slug" placeholder="/plans" />
        </n-form-item>
        <n-form-item label="标题">
          <n-input v-model:value="form.title" placeholder="套餐价格" />
        </n-form-item>
        <n-form-item label="默认语言">
          <n-select v-model:value="form.defaultLocale" :options="localeOptions" />
        </n-form-item>
        <n-form-item label="页面类型">
          <n-select v-model:value="form.pageType" :options="pageTypeOptions" />
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showCreate = false">取消</n-button>
          <n-button type="primary" :loading="creating" @click="create">创建</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NEmpty, NForm, NFormItem, NIcon, NInput, NModal, NSelect, NSpace, NSwitch, NTag, useMessage } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { createPage, deletePage, duplicatePage, listPages, updatePage } from '../../api/cms'
import type { CmsPageListItem } from '../../api/cms'
import { useConfirm } from '../../composables/useConfirm'
import { formatDate } from '../../utils/format'

const router = useRouter()
const message = useMessage()
const { confirm } = useConfirm()
const loading = ref(false)
const creating = ref(false)
const showCreate = ref(false)
const pages = ref<CmsPageListItem[]>([])
const keyword = ref('')
let searchTimer: ReturnType<typeof setTimeout> | null = null
const form = reactive({ slug: '/plans', title: '套餐价格', defaultLocale: 'zh-CN', pageType: 'custom' })
const localeOptions: SelectOption[] = [
  { label: '中文 (zh-CN)', value: 'zh-CN' },
  { label: '英文 (en-US)', value: 'en-US' },
  { label: '日文 (ja-JP)', value: 'ja-JP' }
]

const pageTypeOptions: SelectOption[] = [
  { label: '自定义页面', value: 'custom' },
  { label: '博客文章', value: 'blog' },
  { label: '文档文章', value: 'doc' }
]

const paginationReactive = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => {
    paginationReactive.page = page
    load()
  },
  onUpdatePageSize: (size: number) => {
    paginationReactive.pageSize = size
    paginationReactive.page = 1
    load()
  }
})

watch(keyword, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    paginationReactive.page = 1
    load()
  }, 300)
})

const columns: DataTableColumns<CmsPageListItem> = [
  {
    title: 'ID', key: 'id', width: 80,
    render(row) {
      return h(NTag, { size: 'small', bordered: false }, { default: () => String(row.id) })
    }
  },
  { title: '路径', key: 'slug', ellipsis: { tooltip: true } },
  {
    title: '翻译语言', key: 'translations', width: 180,
    render(row) {
      if (!row.translations?.length) return h('span', { style: 'color: #94a3b8;' }, '—')
      return h(NSpace, { size: 4 }, {
        default: () => row.translations!.map((loc: string) =>
          h(NTag, { size: 'small', bordered: false }, { default: () => loc })
        )
      })
    }
  },
  { title: '默认语言', key: 'defaultLocale', width: 110 },
  {
    title: '状态', key: 'status', width: 120,
    render(row) {
      return h(NSpace, { align: 'center', size: 'small' }, {
        default: () => [
          h(NSwitch, {
            value: row.status === 'enabled',
            loading: loading.value,
            onUpdateValue: () => handleToggleStatus(row)
          }),
          h(NTag, {
            type: row.status === 'enabled' ? 'success' : 'default',
            size: 'small',
            bordered: false
          }, { default: () => row.status === 'enabled' ? '启用' : '禁用' })
        ]
      })
    }
  },
  {
    title: '更新时间', key: 'updatedAt', width: 170,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.updatedAt))
    }
  },
  {
    title: '操作', key: 'actions', width: 360, fixed: 'right' as const,
    render(row) {
      const buttons = [
        h(NButton, { size: 'small', type: 'primary', ghost: true, onClick: () => router.push(`/cms/pages/${row.id}/edit`) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', tertiary: true, onClick: () => router.push(`/cms/pages/${row.id}/versions?locale=${row.defaultLocale}`) }, { default: () => '版本' }),
        h(NButton, { size: 'small', quaternary: true, onClick: () => handleCopyUrl(row) }, { default: () => '复制URL' }),
        h(NButton, { size: 'small', quaternary: true, onClick: () => handleDuplicate(row) }, { default: () => '复制页面' })
      ]
      if (row.status === 'enabled') {
        buttons.push(h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => handleDelete(row) }, { default: () => '删除' }))
      } else {
        buttons.push(h(NButton, { size: 'small', type: 'success', ghost: true, onClick: () => handleEnable(row) }, { default: () => '启用' }))
      }
      return h(NSpace, null, { default: () => buttons })
    }
  }
]

async function load() {
  loading.value = true
  try {
    const resp = await listPages(paginationReactive.page, paginationReactive.pageSize, keyword.value || undefined)
    pages.value = resp.items
    paginationReactive.itemCount = resp.total
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function create() {
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

async function handleDelete(row: CmsPageListItem) {
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

async function handleEnable(row: CmsPageListItem) {
  try {
    await updatePage(row.id, { status: 'enabled' })
    message.success('页面已启用')
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '启用失败')
  }
}

async function handleToggleStatus(row: CmsPageListItem) {
  const newStatus = row.status === 'enabled' ? 'disabled' : 'enabled'
  try {
    await updatePage(row.id, { status: newStatus })
    message.success(newStatus === 'enabled' ? '页面已启用' : '页面已禁用')
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '操作失败')
  }
}

async function handleCopyUrl(row: CmsPageListItem) {
  try {
    await navigator.clipboard.writeText(`/${row.slug}`)
    message.success('URL 已复制')
  } catch {
    message.error('复制失败')
  }
}

async function handleDuplicate(row: CmsPageListItem) {
  try {
    await duplicatePage(row.id)
    message.success('页面已复制')
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '复制失败')
  }
}

onMounted(() => { load() })
</script>
