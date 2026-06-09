<template>
  <div class="reports-page">
    <div class="page-head fade-in-up">
      <div class="page-head-inner">
        <div>
          <h2>数据报表</h2>
          <p>平台运营数据概览</p>
        </div>
        <div class="page-head-actions">
          <n-button quaternary @click="loadAll" :loading="loading" size="small">
            <template #icon><n-icon size="15"><RefreshIcon /></n-icon></template>
            刷新
          </n-button>
        </div>
      </div>
    </div>

    <n-spin :show="loading">
      <n-grid :cols="4" :x-gap="16" :y-gap="16" class="fade-in-up">
        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-label">总用户</div>
            <div class="stat-value">{{ overview?.totalUsers ?? 0 }}</div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-label">订单总数</div>
            <div class="stat-value">{{ overview?.totalOrders ?? 0 }}</div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-label">活跃订阅</div>
            <div class="stat-value">{{ overview?.totalSubscriptions ?? 0 }}</div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-label">总收入</div>
            <div class="stat-value">¥{{ (overview?.totalRevenue ?? 0).toLocaleString() }}</div>
          </n-card>
        </n-grid-item>
      </n-grid>

      <div class="section-title fade-in-up">最近 30 天收入</div>
      <n-card class="fade-in-up">
        <n-data-table :columns="revenueColumns" :data="revenueData" :bordered="false" :single-line="false" size="small" v-if="revenueData.length" />
        <n-empty v-else description="暂无收入数据" />
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { request } from '../../api/http'
import type { DataTableColumn } from 'naive-ui'

const RefreshIcon = () => h('svg', { xmlns: 'http://www.w3.org/2000/svg', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
  h('polyline', { points: '23 4 23 10 17 10' }),
  h('polyline', { points: '1 20 1 14 7 14' }),
  h('path', { d: 'M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15' }),
])

const loading = ref(false)
const overview = ref<any>(null)
const revenueData = ref<any[]>([])
const usageData = ref<any[]>([])

const revenueColumns: DataTableColumn<any>[] = [
  { title: '日期', key: 'day', width: 120 },
  { title: '收入 (CNY)', key: 'revenue', width: 120, render: (r: any) => `¥${Number(r.revenue).toLocaleString()}` },
  { title: '订单数', key: 'orders', width: 100 },
]

async function loadAll() {
  loading.value = true
  try {
    const [overviewRes, revenueRes] = await Promise.all([
      request<any>('/admin/stats/overview'),
      request<any>('/admin/stats/revenue/daily'),
    ])
    overview.value = overviewRes
    revenueData.value = (revenueRes?.daily || []).map((r: any) => ({ ...r, key: r.day }))
  } catch (e) {
    console.error('Failed to load stats', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.reports-page { padding: 0; }
.page-head { margin-bottom: 24px; }
.page-head-inner { display: flex; justify-content: space-between; align-items: flex-start; }
.page-head-inner h2 { margin: 0; font-size: 22px; font-weight: 700; color: var(--vp-c-text); }
.page-head-inner p { margin: 4px 0 0; font-size: 14px; color: var(--vp-c-text-2); }
.section-title { font-size: 16px; font-weight: 600; margin: 24px 0 12px; color: var(--vp-c-text); }
.stat-card { text-align: center; }
.stat-label { font-size: 13px; color: var(--vp-c-text-2); margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; color: var(--vp-c-text); }
</style>
