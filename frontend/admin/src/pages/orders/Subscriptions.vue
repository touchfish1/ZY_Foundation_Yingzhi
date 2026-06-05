<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>订阅管理</h2>
          <p>查看和管理用户订阅。</p>
        </div>
      </div>
    </div>

    <n-card title="订阅列表" :bordered="false" class="table-card" content-style="padding: 0;">
      <n-empty v-if="!loading && !subscriptions.length" description="暂无订阅" />
      <CommonTable
        v-else
        :columns="columns"
        :data="subscriptions"
        :loading="loading"
        :pagination="paginationReactive"
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NCard, NEmpty, NTag, useMessage } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import type { DataTableColumns } from 'naive-ui'
import { listSubscriptions, type SubscriptionItem } from '../../api/order'
import { formatDate } from '../../utils/format'

const message = useMessage()
const subscriptions = ref<SubscriptionItem[]>([])
const loading = ref(false)

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

const columns: DataTableColumns<SubscriptionItem> = [
  { title: '用户ID', key: 'userId', width: 100 },
  { title: '套餐名称', key: 'planName', width: 160 },
  { title: '套餐编码', key: 'planCode', width: 120 },
  {
    title: '状态', key: 'status', width: 100,
    render(row) {
      const type = row.status === 'active' ? 'success' : row.status === 'suspended' ? 'warning' : 'error'
      return h(NTag, { type, size: 'small', bordered: false }, { default: () => row.status })
    }
  },
  {
    title: '开始时间', key: 'startsAt', width: 180,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.startsAt))
    }
  },
  {
    title: '结束时间', key: 'expiresAt', width: 180,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.expiresAt))
    }
  },
  {
    title: '创建时间', key: 'createdAt', width: 180,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.createdAt))
    }
  },
  {
    title: '是否活跃', key: 'active', width: 100,
    render(row) {
      const type = row.active ? 'success' : 'default'
      return h(NTag, { type, size: 'small', bordered: false }, { default: () => row.active ? '是' : '否' })
    }
  }
]

async function load() {
  loading.value = true
  try {
    subscriptions.value = await listSubscriptions()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { load() })
</script>

<style scoped>
.table-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
}
</style>
