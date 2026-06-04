<template>
  <div class="dashboard">
    <div class="page-head">
      <div>
        <h2>仪表盘</h2>
        <p>内容管理系统概览</p>
      </div>
      <div class="page-head-actions">
        <n-button quaternary @click="loadStats" :loading="loading">
          <template #icon>
            <n-icon size="16">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
            </n-icon>
          </template>
          刷新
        </n-button>
      </div>
    </div>

    <div class="stat-grid">
      <n-card class="stat-card" :segmented="{ content: true }">
        <template #header>
          <div class="stat-content">
            <div class="stat-icon" style="background: rgba(99,102,241,0.12); color: #6366f1">
              <n-icon size="22">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
              </n-icon>
            </div>
            <div class="stat-body">
              <div class="stat-label">页面总数</div>
              <div class="stat-value">{{ stats.pages }}</div>
            </div>
          </div>
        </template>
        <div class="stat-footer">
          <span>已发布 {{ stats.published }} 个页面</span>
          <n-tag size="tiny" type="success" :bordered="false">live</n-tag>
        </div>
      </n-card>

      <n-card class="stat-card" :segmented="{ content: true }">
        <template #header>
          <div class="stat-content">
            <div class="stat-icon" style="background: rgba(16,185,129,0.12); color: #10b981">
              <n-icon size="22">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
              </n-icon>
            </div>
            <div class="stat-body">
              <div class="stat-label">套餐组</div>
              <div class="stat-value">{{ stats.planGroups }}</div>
            </div>
          </div>
        </template>
        <div class="stat-footer">
          <span>当前活跃的套餐分组</span>
        </div>
      </n-card>

      <n-card class="stat-card" :segmented="{ content: true }">
        <template #header>
          <div class="stat-content">
            <div class="stat-icon" style="background: rgba(245,158,11,0.12); color: #f59e0b">
              <n-icon size="22">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
              </n-icon>
            </div>
            <div class="stat-body">
              <div class="stat-label">订单数</div>
              <div class="stat-value">{{ stats.orders }}</div>
            </div>
          </div>
        </template>
        <div class="stat-footer">
          <span>全部订单</span>
        </div>
      </n-card>

      <n-card class="stat-card" :segmented="{ content: true }">
        <template #header>
          <div class="stat-content">
            <div class="stat-icon" style="background: rgba(139,92,246,0.12); color: #8b5cf6">
              <n-icon size="22">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
              </n-icon>
            </div>
            <div class="stat-body">
              <div class="stat-label">发布率</div>
              <div class="stat-value">{{ stats.pages > 0 ? Math.round(stats.published / stats.pages * 100) : 0 }}%</div>
            </div>
          </div>
        </template>
        <div class="stat-footer">
          <span>{{ stats.published }} / {{ stats.pages }} 已发布</span>
        </div>
      </n-card>
    </div>

    <n-grid :cols="2" :x-gap="20" :y-gap="20">
      <n-grid-item>
        <n-card title="快捷操作" class="action-card">
          <n-space vertical>
            <n-space>
              <n-button type="primary" @click="$router.push('/cms/pages')">
                <template #icon>
                  <n-icon size="16">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/></svg>
                  </n-icon>
                </template>
                页面管理
              </n-button>
              <n-button @click="$router.push('/cms/pages')">
                <template #icon>
                  <n-icon size="16">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                  </n-icon>
                </template>
                新建页面
              </n-button>
            </n-space>
            <n-space>
              <n-button @click="$router.push('/products/plan-groups')">
                <template #icon>
                  <n-icon size="16">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
                  </n-icon>
                </template>
                套餐管理
              </n-button>
              <n-button @click="$router.push('/system/settings')">
                <template #icon>
                  <n-icon size="16">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
                  </n-icon>
                </template>
                系统设置
              </n-button>
            </n-space>
          </n-space>
        </n-card>
      </n-grid-item>

      <n-grid-item>
        <n-card title="系统信息" class="action-card">
          <div class="info-list">
            <div class="info-row"><span class="info-label">运行环境</span><n-tag :type="isDev ? 'warning' : 'success'" size="small" :bordered="false">{{ isDev ? '开发环境' : '生产环境' }}</n-tag></div>
            <div class="info-row"><span class="info-label">前端版本</span><span>0.1.0</span></div>
            <div class="info-row"><span class="info-label">当前时间</span><span>{{ currentTime }}</span></div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { NButton, NCard, NGrid, NGridItem, NIcon, NSpace, NTag, useMessage } from 'naive-ui'
import { listPages } from '../api/cms'
import { listPlanGroups } from '../api/product'
import { listOrders } from '../api/order'

const message = useMessage()
const loading = ref(false)

const stats = reactive({ pages: 0, published: 0, planGroups: 0, orders: 0 })

const isDev = import.meta.env.DEV
const currentTime = ref(new Date().toLocaleString('zh-CN'))
let timer: ReturnType<typeof setInterval> | null = null

async function loadStats() {
  loading.value = true
  try {
    const [pages, groups, orders] = await Promise.all([
      listPages().catch(() => []),
      listPlanGroups().catch(() => []),
      listOrders().catch(() => [])
    ])
    stats.pages = Array.isArray(pages) ? pages.length : 0
    stats.published = Array.isArray(pages) ? pages.filter((p: any) => p.status === 'published' || p.status === 'enabled').length : 0
    stats.planGroups = Array.isArray(groups) ? groups.length : 0
    stats.orders = Array.isArray(orders) ? orders.length : 0
  } catch {
    message.error('部分数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
  timer = setInterval(() => {
    currentTime.value = new Date().toLocaleString('zh-CN')
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.stat-card {
  border-radius: 14px;
}

.stat-card :deep(.n-card-header) {
  padding-bottom: 0;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 2px;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  line-height: 1.2;
}

.stat-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #94a3b8;
  padding-top: 2px;
}

.action-card {
  border-radius: 14px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.info-label {
  color: #64748b;
}
</style>
