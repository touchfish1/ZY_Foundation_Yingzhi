<template>
  <div class="page-head">
    <div>
      <h2>页面管理</h2>
      <p>创建多语言页面，保存草稿并发布到前台。</p>
    </div>
    <n-button type="primary" @click="showCreate = true">新建页面</n-button>
  </div>

  <n-card>
    <n-data-table :columns="columns" :data="pages" :loading="loading" />
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
        <n-input v-model:value="form.defaultLocale" />
      </n-form-item>
      <n-button type="primary" :loading="creating" @click="create">创建</n-button>
    </n-form>
  </n-modal>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NModal, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { createPage, listPages, type CmsPageListItem } from '../../api/cms'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const creating = ref(false)
const showCreate = ref(false)
const pages = ref<CmsPageListItem[]>([])
const form = reactive({ slug: '/plans', title: '套餐价格', defaultLocale: 'zh-CN' })

const columns: DataTableColumns<CmsPageListItem> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: 'Slug', key: 'slug' },
  { title: '默认语言', key: 'defaultLocale', width: 120 },
  { title: '状态', key: 'status', width: 120 },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render(row) {
      return h(NButton, { size: 'small', onClick: () => router.push(`/cms/pages/${row.id}/edit`) }, { default: () => '编辑' })
    }
  }
]

async function load() {
  loading.value = true
  try {
    pages.value = await listPages()
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

onMounted(load)
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
}

.page-head p {
  margin: 0;
  color: #64748b;
}

.modal-card {
  width: min(520px, 92vw);
}
</style>
