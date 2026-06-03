<template>
  <div class="page-head">
    <div>
      <h2>支付记录</h2>
      <p>查看支付交易记录。</p>
    </div>
    <n-input v-model:value="searchQuery" placeholder="搜索支付单号..." clearable style="width: 240px" />
  </div>

  <n-card>
    <n-empty v-if="!loading && !filteredPayments.length" description="暂无支付记录" />
    <n-data-table v-else :columns="columns" :data="filteredPayments" :loading="loading" :bordered="false" />
  </n-card>
</template>

<script setup lang="ts">
// 支付记录页面：查看支付交易记录，支持按支付单号搜索
import { computed, h, onMounted, ref } from 'vue'
import { NCard, NDataTable, NEmpty, NInput, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { listPayments, type PaymentItem } from '../../api/payment'

const message = useMessage()
const payments = ref<PaymentItem[]>([])
const loading = ref(false)
const searchQuery = ref('')
// 根据搜索关键词过滤支付记录
const filteredPayments = computed(() => {
  if (!searchQuery.value) return payments.value
  const q = searchQuery.value.toLowerCase()
  return payments.value.filter(p => p.paymentNo.toLowerCase().includes(q))
})

const columns: DataTableColumns<PaymentItem> = [
  { title: '支付单号', key: 'paymentNo', width: 200 },
  { title: '订单号', key: 'orderNo', width: 200 },
  { title: '渠道', key: 'channel', width: 100 },
  { title: '金额', key: 'amount', width: 100 },
  { title: '状态', key: 'status', width: 100, render(row) {
      const type = row.status === 'success' || row.status === 'paid' ? 'success' : row.status === 'pending' ? 'warning' : row.status === 'cancelled' || row.status === 'failed' ? 'error' : 'default'
      return h(NTag, { type }, { default: () => row.status })
    }
  },
  { title: '创建时间', key: 'createdAt', width: 180 }
]

// 加载支付记录列表
async function load() {
  console.log('[Transactions] load')
  loading.value = true
  try {
    payments.value = await listPayments()
    console.log('[Transactions] loaded', payments.value.length, 'payments')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { console.log('[Transactions] mounted'); load() })
</script>

<style scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-head h2 { margin: 0 0 6px; }
.page-head p { margin: 0; color: #64748b; }
</style>
