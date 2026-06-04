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
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showCreate = false">取消</n-button>
          <n-button type="primary" :loading="creating" @click="create">创建</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NEmpty, NForm, NFormItem, NIcon, NInput, NModal, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { createPage, deletePage, listPages, type CmsPageListItem } from '../../api/cms'
import { useConfirm } from '../../composables/useConfirm'
import { formatDate } from '../../utils/format'

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

const paginationReactive = reactive({
  page: 1,
  pageSize: 20,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => { paginationReactive.page = page },
  onUpdatePageSize: (size: number) => {
    paginationReactive.pageSize = size
    paginationReactive.page = 1
  }
})

const columns: DataTableColumns<CmsPageListItem> = [
  {
    title: 'ID', key: 'id', width: 80,
    render(row) {
      return h(NTag, { size: 'small', bordered: false }, { default: () => String(row.id) })
    }
  },
  { title: '路径', key: 'slug', ellipsis: { tooltip: true } },
  { title: '默认语言', key: 'defaultLocale', width: 110 },
  {
    title: '状态', key: 'status', width: 90,
    render(row) {
      return h(NTag, {
        type: row.status === 'enabled' ? 'success' : 'default',
        size: 'small',
        bordered: false
      }, { default: () => row.status === 'enabled' ? '启用' : '禁用' })
    }
  },
  {
    title: '更新时间', key: 'updatedAt', width: 170,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.updatedAt))
    }
  },
  {
    title: '操作', key: 'actions', width: 280, fixed: 'right' as const,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', ghost: true, onClick: () => router.push(`/cms/pages/${row.id}/edit`) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', tertiary: true, onClick: () => router.push(`/cms/pages/${row.id}/versions`) }, { default: () => '版本' }),
          h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => handleDelete(row) }, { default: () => '删除' })
        ]
      })
    }
  }
]

async function load() {
  loading.value = true
  try {
    pages.value = await listPages()
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

onMounted(() => { load() })
</script>
