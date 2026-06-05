<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>审计日志</h2>
          <p>查看系统操作审计日志。</p>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card" content-style="padding: 0;">
      <template #header>
        <n-space align="center" size="small" style="padding: 4px 0;">
          <n-input v-model:value="filterUser" placeholder="用户 ID" clearable style="width: 120px" />
          <n-input v-model:value="filterAction" placeholder="操作类型" clearable style="width: 140px" />
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
      <n-empty v-if="!loading && !logs.length" description="暂无审计日志" />
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
import { h, onMounted, reactive, ref, watch } from 'vue'
import { NButton, NCard, NDatePicker, NEmpty, NInput, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import { listLogs, type AuditLog } from '../../api/system'
import { formatDate } from '../../utils/format'

const message = useMessage()
const logs = ref<AuditLog[]>([])
const loading = ref(false)
const filterUser = ref('')
const filterAction = ref('')
const dateRange = ref<[number, number] | null>(null)

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

const columns: DataTableColumns<AuditLog> = [
  {
    title: '日志 ID', key: 'id', width: 80,
    render(row) {
      return h(NTag, { size: 'tiny', bordered: false }, { default: () => row.id })
    }
  },
  { title: '用户', key: 'username', width: 120 },
  {
    title: '操作', key: 'action', width: 120,
    render(row) {
      return h(NTag, { size: 'small', bordered: false, type: 'info' }, { default: () => row.action })
    }
  },
  { title: '详情', key: 'detail', ellipsis: { tooltip: true } },
  { title: 'IP 地址', key: 'ipAddress', width: 140 },
  {
    title: '时间', key: 'createdAt', width: 180,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.createdAt))
    }
  }
]

function search() {
  paginationReactive.page = 1
  loadData()
}

watch([filterUser, filterAction], () => {
  paginationReactive.page = 1
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    let startDate: string | undefined
    let endDate: string | undefined
    if (dateRange.value) {
      startDate = new Date(dateRange.value[0]).toISOString()
      endDate = new Date(dateRange.value[1]).toISOString()
    }
    const resp = await listLogs({
      page: paginationReactive.page,
      pageSize: paginationReactive.pageSize,
      userId: filterUser.value ? Number(filterUser.value) : undefined,
      action: filterAction.value || undefined,
      startDate,
      endDate
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
