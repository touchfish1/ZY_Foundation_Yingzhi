<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>支付记录</h2>
          <p>查看支付交易记录。</p>
        </div>
        <div class="page-head-actions">
          <n-input v-model:value="searchQuery" placeholder="搜索支付单号..." clearable style="width: 220px">
            <template #prefix>
              <n-icon size="14"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></n-icon>
            </template>
          </n-input>
        </div>
      </div>
    </div>

    <n-card title="支付记录" :bordered="false" class="table-card" content-style="padding: 0;">
      <n-empty v-if="!loading && !filteredPayments.length" description="暂无支付记录" />
      <CommonTable
        v-else
        :columns="columns"
        :data="filteredPayments"
        :loading="loading"
        :pagination="paginationReactive"
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref, watch } from 'vue'
import { NButton, NCard, NEmpty, NIcon, NInput, NNumberAnimation, NTag, useMessage } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import type { DataTableColumns } from 'naive-ui'
import { listPayments, type PaymentItem } from '../../api/payment'
import { formatDate } from '../../utils/format'

const message = useMessage()
const payments = ref<PaymentItem[]>([])
const loading = ref(false)
const searchQuery = ref('')

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

watch(searchQuery, () => {
  paginationReactive.page = 1
})

const filteredPayments = computed(() => {
  if (!searchQuery.value) return payments.value
  const q = searchQuery.value.toLowerCase()
  return payments.value.filter(p => p.paymentNo.toLowerCase().includes(q))
})

const columns: DataTableColumns<PaymentItem> = [
  { title: '支付单号', key: 'paymentNo', width: 200 },
  { title: '订单号', key: 'orderNo', width: 200 },
  { title: '渠道', key: 'channel', width: 100 },
  {
    title: '金额', key: 'amount', width: 120,
    render(row) {
      return h('span', { style: 'color: #f59e0b; font-weight: 600;' },
        h(NNumberAnimation, { from: 0, to: +row.amount, showSeparator: true })
      )
    }
  },
  {
    title: '状态', key: 'status', width: 100,
    render(row) {
      const type = row.status === 'success' || row.status === 'paid' ? 'success' : row.status === 'pending' ? 'warning' : row.status === 'cancelled' || row.status === 'failed' ? 'error' : 'default'
      return h(NTag, { type, size: 'small', bordered: false }, { default: () => row.status })
    }
  },
  {
    title: '创建时间', key: 'createdAt', width: 180,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.createdAt))
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render() {
      return h(NButton, { size: 'small', type: 'primary' }, { default: () => '详情' })
    }
  }
]

async function load() {
  loading.value = true
  try {
    payments.value = await listPayments()
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
