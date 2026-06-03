<template>
  <div class="dashboard">
    <h2 class="page-title">仪表盘</h2>
    <p class="page-subtitle">CMS 系统概览</p>

    <n-grid :cols="4" :x-gap="16" :y-gap="16" class="stats-grid">
      <n-grid-item>
        <n-card class="stat-card">
          <n-statistic label="页面总数" :value="stats.pages" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card">
          <n-statistic label="已发布" :value="stats.published">
            <template #suffix>
              <n-tag type="success" size="tiny">live</n-tag>
            </template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card">
          <n-statistic label="套餐组" :value="stats.planGroups" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card">
          <n-statistic label="订单数" :value="stats.orders" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="快捷操作" class="quick-actions">
      <n-space>
        <n-button type="primary" @click="router.push('/cms/pages')">页面管理</n-button>
        <n-button @click="router.push('/cms/pages')">新建页面</n-button>
        <n-button @click="router.push('/products/plan-groups')">套餐管理</n-button>
        <n-button @click="router.push('/system/settings')">系统设置</n-button>
      </n-space>
    </n-card>
  </div>
</template>

<script setup lang="ts">
// 仪表盘页面：展示 CMS 系统概览统计数据
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NGrid, NGridItem, NTag, NSpace, NStatistic, useMessage } from 'naive-ui'
import { listPages } from '@/api/cms'
import { listPlanGroups } from '@/api/product'
import { listOrders } from '@/api/order'

const router = useRouter()
const message = useMessage()
const stats = reactive({ pages: 0, published: 0, planGroups: 0, orders: 0 })

// 并行加载页面数、套餐组数、订单数等统计
async function loadStats() {
  console.log('[Dashboard] loadStats')
  try {
    const [pages, groups, orders] = await Promise.all([
      listPages().catch(() => []),
      listPlanGroups().catch(() => []),
      listOrders().catch(() => [])
    ])
    stats.pages = Array.isArray(pages) ? pages.length : 0
    stats.published = Array.isArray(pages) ? pages.filter((p: any) => p.status === 'published').length : 0
    stats.planGroups = Array.isArray(groups) ? groups.length : 0
    stats.orders = Array.isArray(orders) ? orders.length : 0
    console.log('[Dashboard] stats:', { ...stats })
  } catch {
    message.error('部分数据加载失败')
  }
}

onMounted(() => { console.log('[Dashboard] mounted'); loadStats() })
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}
.page-title {
  margin: 0 0 4px;
  font-size: 24px;
  font-weight: 700;
}
.page-subtitle {
  margin: 0 0 24px;
  color: #64748b;
}
.stats-grid {
  margin-bottom: 24px;
}
.stat-card {
  text-align: center;
}
.quick-actions {
  margin-top: 8px;
}
</style>
