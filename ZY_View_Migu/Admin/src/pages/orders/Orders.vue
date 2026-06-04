<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>订单管理</h2>
          <p>查看和管理用户订单。</p>
        </div>
        <div class="page-head-actions">
          <n-input v-model:value="searchQuery" placeholder="搜索订单号..." clearable style="width: 220px">
            <template #prefix>
              <n-icon size="14"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></n-icon>
            </template>
          </n-input>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card">
      <n-empty v-if="!loading && !filteredOrders.length" description="暂无订单" />
      <n-data-table v-else :columns="columns" :data="filteredOrders" :loading="loading" :bordered="false" :striped="true" size="small" />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { NCard, NDataTable, NEmpty, NIcon, NInput, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { listOrders, type OrderItem } from '../../api/order'

const message = useMessage()
const orders = ref<OrderItem[]>([])
const loading = ref(false)
const searchQuery = ref('')

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
      return h(NTag, { type, size: 'small', bordered: false }, { default: () => row.status })
    }
  },
  { title: '创建时间', key: 'createdAt', width: 180 }
]

async function load() {
  loading.value = true
  try {
    orders.value = await listOrders()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { load() })
</script>
