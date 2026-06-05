<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>用量管理</h2>
          <p>查看用户 API 用量统计和明细。</p>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="filter-card" content-style="padding: 16px;">
      <n-space align="center" size="large">
        <n-space align="center" size="small">
          <span style="font-size: 13px; color: #64748b;">用户 ID：</span>
          <n-input-number v-model:value="userId" placeholder="请输入用户 ID" :min="1" style="width: 160px" />
        </n-space>
        <n-button type="primary" @click="search" :loading="loading">查询</n-button>
      </n-space>
    </n-card>

    <n-grid v-if="hasSummary" :cols="4" :x-gap="16" :y-gap="16" style="margin-top: 16px;">
      <n-grid-item>
        <n-card :bordered="false" class="stat-card">
          <div class="stat-label">总消耗配额</div>
          <div class="stat-value">{{ summary.totalQuota }}</div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :bordered="false" class="stat-card">
          <div class="stat-label">日均消耗</div>
          <div class="stat-value">{{ summary.dailyAverage }}</div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :bordered="false" class="stat-card">
          <div class="stat-label">总调用次数</div>
          <div class="stat-value">{{ summary.totalCalls }}</div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :bordered="false" class="stat-card">
          <div class="stat-label">记录条数</div>
          <div class="stat-value">{{ summary.recordCount }}</div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="用量明细" :bordered="false" class="table-card" content-style="padding: 0;" style="margin-top: 16px;">
      <template #header-extra>
        <n-space align="center" size="small">
          <n-date-picker
            v-model:value="dateRange"
            type="daterange"
            clearable
            style="width: 250px"
            placeholder="选择日期范围"
            @update:value="onDateChange"
          />
        </n-space>
      </template>
      <n-empty v-if="!loading && !records.length" description="暂无用量记录" />
      <CommonTable
        v-else
        :columns="columns"
        :data="records"
        :loading="loading"
        :pagination="paginationReactive"
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDatePicker, NEmpty, NGrid, NGridItem, NInputNumber, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import { getUsageRecords, getUsageSummary, type UsageRecord, type UsageSummary } from '../../api/order'
import { formatDate } from '../../utils/format'

const message = useMessage()
const userId = ref<number | null>(null)
const loading = ref(false)
const records = ref<UsageRecord[]>([])
const summary = ref<UsageSummary>({ totalQuota: 0, dailyAverage: 0, totalCalls: 0, recordCount: 0 })
const hasSummary = ref(false)
const dateRange = ref<[number, number] | null>(null)

const paginationReactive = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => {
    paginationReactive.page = page
    loadRecords()
  },
  onUpdatePageSize: (size: number) => {
    paginationReactive.pageSize = size
    paginationReactive.page = 1
    loadRecords()
  }
})

const columns: DataTableColumns<UsageRecord> = [
  {
    title: '日期', key: 'date', width: 140,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.date))
    }
  },
  {
    title: '模型', key: 'model', width: 200,
    render(row) {
      return h(NTag, { size: 'small', bordered: false }, { default: () => row.model })
    }
  },
  {
    title: '调用次数', key: 'callCount', width: 100,
    render(row) {
      return h('span', { style: 'font-weight: 600;' }, row.callCount)
    }
  },
  {
    title: '消耗配额', key: 'quotaUsed', width: 100,
    render(row) {
      return h('span', { style: 'color: #f59e0b; font-weight: 600;' }, row.quotaUsed)
    }
  }
]

function onDateChange(value: [number, number] | null) {
  dateRange.value = value
  paginationReactive.page = 1
  loadRecords()
}

async function search() {
  if (!userId.value) {
    message.warning('请输入用户 ID')
    return
  }
  paginationReactive.page = 1
  hasSummary.value = false
  await Promise.all([loadSummary(), loadRecords()])
}

async function loadSummary() {
  if (!userId.value) return
  try {
    summary.value = await getUsageSummary(userId.value)
    hasSummary.value = true
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载用量摘要失败')
  }
}

async function loadRecords() {
  if (!userId.value) return
  loading.value = true
  try {
    let startDate: string | undefined
    let endDate: string | undefined
    if (dateRange.value) {
      startDate = new Date(dateRange.value[0]).toISOString()
      endDate = new Date(dateRange.value[1]).toISOString()
    }
    const resp = await getUsageRecords(userId.value, {
      page: paginationReactive.page,
      pageSize: paginationReactive.pageSize,
      startDate,
      endDate
    })
    records.value = resp.items
    paginationReactive.itemCount = resp.total
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载用量记录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.filter-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #6366f1;
}
.table-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
}
</style>
