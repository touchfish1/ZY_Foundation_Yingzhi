<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>区块定义管理</h2>
          <p>管理内容区块的类型定义、字段结构和默认属性。</p>
        </div>
        <div class="page-head-actions">
          <n-button quaternary @click="load" :loading="loading">
            <template #icon><n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></n-icon></template>
          </n-button>
          <n-button type="primary" @click="router.push('/cms/block-definitions/new')">新建区块定义</n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="true" class="table-card" style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
      <n-empty v-if="!loading && !items.length" description="暂无区块定义">
        <template #extra>
          <n-button size="small" @click="router.push('/cms/block-definitions/new')">新建第一个区块定义</n-button>
        </template>
      </n-empty>
      <n-data-table v-else :columns="columns" :data="items" :loading="loading" :bordered="false" size="small" />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NEmpty, NIcon, NSpace, NTag, NSwitch, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { getBlockDefinitions, deleteBlockDefinition } from '../../api/block'
import { useConfirm } from '../../composables/useConfirm'

interface BlockDefRow {
  id: number
  type: string
  name: string
  schema: Record<string, unknown>
  defaultProps: Record<string, unknown>
  enabled: boolean
  sortOrder: number
}

const router = useRouter()
const message = useMessage()
const { confirm } = useConfirm()
const loading = ref(false)
const items = ref<BlockDefRow[]>([])

const columns: DataTableColumns<BlockDefRow> = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '类型', key: 'type', width: 130 },
  { title: '名称', key: 'name', width: 130 },
  {
    title: '字段数', key: 'schema', width: 80,
    render(row) {
      const fields = (row.schema as any)?.fields
      return h('span', String(fields?.length ?? 0))
    }
  },
  { title: '排序', key: 'sortOrder', width: 70 },
  {
    title: '状态', key: 'enabled', width: 90,
    render(row) {
      return h(NTag, {
        type: row.enabled ? 'success' : 'default',
        size: 'small',
        bordered: false
      }, { default: () => row.enabled ? '启用' : '禁用' })
    }
  },
  {
    title: '操作', key: 'actions', width: 260, fixed: 'right' as const,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', ghost: true, onClick: () => router.push(`/cms/block-definitions/${row.id}/edit`) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => handleDelete(row) }, { default: () => '删除' })
        ]
      })
    }
  }
]

async function load() {
  loading.value = true
  try {
    items.value = await getBlockDefinitions() as unknown as BlockDefRow[]
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleDelete(row: BlockDefRow) {
  const ok = await confirm({ content: `确定要删除区块定义 "${row.name}"？删除后不可恢复。` })
  if (!ok) return
  try {
    await deleteBlockDefinition(row.id)
    message.success('已删除')
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => { load() })
</script>
