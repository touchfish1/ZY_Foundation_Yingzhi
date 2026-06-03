<template>
  <div class="page-head">
    <div>
      <h2>订单管理</h2>
      <p>查看和管理用户订单。</p>
    </div>
    <n-input v-model:value="searchQuery" placeholder="搜索订单号..." clearable style="width: 240px" />
  </div>

  <n-card>
    <n-empty v-if="!loading && !filteredOrders.length" description="暂无订单" />
    <n-data-table v-else :columns="columns" :data="filteredOrders" :loading="loading" :bordered="false" />
  </n-card>
</template>

<script setup lang="ts">
// 订单管理页面：查看订单列表，支持按订单号搜索
import { computed, h, onMounted, ref } from 'vue'
import { NCard, NDataTable, NEmpty, NInput, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { listOrders, type OrderItem } from '../../api/order'

const message = useMessage()
const orders = ref<OrderItem[]>([])
const loading = ref(false)
const searchQuery = ref('')
// 根据搜索关键词过滤订单列表
const filteredOrders = computed(() => {
  if (!searchQuery.value) return orders.value
  const q = searchQuery.value.toLowerCase()
  return orders.value.filter(o => o.orderNo.toLowerCase().includes(q))
})

const columns: DataTableColumns<OrderItem> = [
  { title: '订单号', key: 'orderNo', width: 200 },
  { title: '金额', key: 'amount', width: 100 },
  { title: '币种', key: 'currency', width: 80 },
  { title: '状态', key: 'status', width: 100, render(row) {
      const type = row.status === 'paid' || row.status === 'success' ? 'success' : row.status === 'pending' ? 'warning' : row.status === 'cancelled' || row.status === 'failed' ? 'error' : 'default'
      return h(NTag, { type }, { default: () => row.status })
    }
  },
  { title: '创建时间', key: 'createdAt', width: 180 }
]

// 加载订单列表
async function load() {
  console.log('[Orders] load')
  loading.value = true
  try {
    orders.value = await listOrders()
    console.log('[Orders] loaded', orders.value.length, 'orders')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { console.log('[Orders] mounted'); load() })
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
