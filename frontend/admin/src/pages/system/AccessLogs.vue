<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>访问日志</h2>
          <p>查看系统 HTTP 访问日志，记录所有 API 请求的方法、路径、状态和耗时。</p>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card" content-style="padding: 0;">
      <template #header>
        <n-space align="center" size="small" style="padding: 4px 0;">
          <n-select
            v-model:value="filterMethod"
            placeholder="请求方法"
            clearable
            style="width: 120px"
            :options="methodOptions"
          />
          <n-input v-model:value="filterPath" placeholder="请求路径" clearable style="width: 200px" />
          <n-input v-model:value="filterStatus" placeholder="状态码" clearable style="width: 100px" />
          <n-input v-model:value="filterUserId" placeholder="用户 ID" clearable style="width: 120px" />
          <n-date-picker
            v-model:value="dateRange"
            type="daterange"
            clearable
            style="width: 250px"
            placeholder="选择日期范围"
          />
          <n-button type="primary" @click="search" :loading="loading">搜索</n-button>
        </n-space>
      </template>
      <n-empty v-if="!loading && !logs.length" description="暂无访问日志" />
      <CommonTable
        v-else
        :columns="columns"
        :data="logs"
        :loading="loading"
        :pagination="paginationReactive"
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NCard,
  NDatePicker,
  NEmpty,
  NInput,
  NSelect,
  NSpace,
  NTag,
  useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import { listAccessLogs, type AccessLog } from '../../api/system'
import { formatDate } from '../../utils/format'

const message = useMessage()
const logs = ref<AccessLog[]>([])
const loading = ref(false)
const filterMethod = ref<string | null>(null)
const filterPath = ref('')
const filterStatus = ref('')
const filterUserId = ref('')
const dateRange = ref<[number, number] | null>(null)

const methodOptions = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'PATCH', value: 'PATCH' }
]

const paginationReactive = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => {
    paginationReactive.page = page
    loadData()
  },
  onUpdatePageSize: (size: number) => {
    paginationReactive.pageSize = size
    paginationReactive.page = 1
    loadData()
  }
})

function getMethodType(method: string): string {
  const map: Record<string, string> = {
    GET: 'info',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'error',
    PATCH: 'warning'
  }
  return map[method] || 'default'
}

function getStatusType(status: number): string {
  if (status >= 200 && status < 300) return 'success'
  if (status >= 300 && status < 400) return 'warning'
  if (status >= 400 && status < 500) return 'error'
  if (status >= 500) return 'error'
  return 'default'
}

function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`
  return `${(ms / 1000).toFixed(2)}s`
}

const columns: DataTableColumns<AccessLog> = [
  {
    title: 'ID',
    key: 'id',
    width: 70,
    render(row) {
      return h(NTag, { size: 'tiny', bordered: false }, { default: () => row.id })
    }
  },
  {
    title: '方法',
    key: 'requestMethod',
    width: 90,
    render(row) {
      return h(
        NTag,
        { size: 'small', bordered: false, type: getMethodType(row.requestMethod) as any },
        { default: () => row.requestMethod }
      )
    }
  },
  { title: '路径', key: 'requestPath', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'responseStatus',
    width: 90,
    render(row) {
      return h(
        NTag,
        { size: 'small', bordered: false, type: getStatusType(row.responseStatus) as any },
        { default: () => String(row.responseStatus) }
      )
    }
  },
  {
    title: '用户',
    key: 'username',
    width: 120,
    render(row) {
      return row.username || h('span', { style: 'color: #94a3b8;' }, '-')
    }
  },
  { title: 'IP 地址', key: 'ipAddress', width: 140 },
  {
    title: '耗时',
    key: 'durationMs',
    width: 100,
    render(row) {
      const isSlow = row.durationMs > 1000
      return h(
        'span',
        { style: `font-size: 13px; color: ${isSlow ? '#dc2626' : '#64748b'};` },
        formatDuration(row.durationMs)
      )
    }
  },
  {
    title: '时间',
    key: 'createdAt',
    width: 180,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.createdAt))
    }
  }
]

function search() {
  paginationReactive.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    let startTime: string | undefined
    let endTime: string | undefined
    if (dateRange.value) {
      startTime = new Date(dateRange.value[0]).toISOString()
      endTime = new Date(dateRange.value[1]).toISOString()
    }
    const resp = await listAccessLogs({
      page: paginationReactive.page,
      pageSize: paginationReactive.pageSize,
      method: filterMethod.value || undefined,
      path: filterPath.value || undefined,
      status: filterStatus.value ? Number(filterStatus.value) : undefined,
      userId: filterUserId.value ? Number(filterUserId.value) : undefined,
      startTime,
      endTime
    })
    logs.value = resp.items
    paginationReactive.itemCount = resp.total
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.table-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
}
</style>
