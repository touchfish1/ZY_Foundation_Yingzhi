<template>
  <div class="dashboard">
    <!-- Page header -->
    <div class="page-head fade-in-up">
      <div class="page-head-inner">
        <div>
          <h2>仪表盘</h2>
          <p>内容管理系统概览</p>
        </div>
        <div class="page-head-actions">
          <n-button quaternary @click="loadStats" :loading="loading" size="small">
            <template #icon>
              <n-icon size="15">
                <RefreshIcon />
              </n-icon>
            </template>
            刷新
          </n-button>
        </div>
      </div>
    </div>

    <!-- Stat cards -->
    <div class="stat-grid">
      <div
        v-for="(card, i) in statCards"
        :key="card.label"
        class="stat-card card-hover"
        :class="'fade-in-up fade-in-up-d' + (i + 1)"
      >
        <div class="stat-card-glow" :style="'background:' + card.color"></div>
        <div class="stat-card-body">
          <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
            <n-icon :size="card.iconSize || 22">
              <component :is="card.icon" />
            </n-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value">{{ card.value }}</div>
          </div>
        </div>
        <div class="stat-footer">
          <span>{{ card.footer }}</span>
          <n-tag v-if="card.tag" :type="card.tagType || 'default'" size="tiny" :bordered="false">{{ card.tag }}</n-tag>
        </div>
      </div>
    </div>

    <!-- Bottom grid -->
    <n-grid :cols="2" :x-gap="20" :y-gap="20" class="fade-in-up fade-in-up-d4">
      <!-- Quick actions -->
      <n-grid-item>
        <n-card class="section-card" :segmented="{ content: true }">
          <template #header><span class="section-title">快捷操作</span></template>
          <div class="action-grid">
            <button class="action-item" @click="$router.push('/cms/pages')">
              <span class="action-icon" style="background:rgba(99,102,241,0.12);color:#6366f1">
                <n-icon size="20"><PagesIcon /></n-icon>
              </span>
              <span class="action-label">页面管理</span>
            </button>
            <button class="action-item" @click="$router.push('/cms/pages')">
              <span class="action-icon" style="background:rgba(16,185,129,0.12);color:#10b981">
                <n-icon size="20"><PlusIcon /></n-icon>
              </span>
              <span class="action-label">新建页面</span>
            </button>
            <button class="action-item" @click="$router.push('/assets')">
              <span class="action-icon" style="background:rgba(245,158,11,0.12);color:#f59e0b">
                <n-icon size="20"><ImageIcon /></n-icon>
              </span>
              <span class="action-label">媒体资源</span>
            </button>
            <button class="action-item" @click="$router.push('/products/plan-groups')">
              <span class="action-icon" style="background:rgba(139,92,246,0.12);color:#8b5cf6">
                <n-icon size="20"><LayersIcon /></n-icon>
              </span>
              <span class="action-label">套餐管理</span>
            </button>
            <button class="action-item" @click="$router.push('/orders')">
              <span class="action-icon" style="background:rgba(236,72,153,0.12);color:#ec4899">
                <n-icon size="20"><CartIcon /></n-icon>
              </span>
              <span class="action-label">订单管理</span>
            </button>
            <button class="action-item" @click="$router.push('/system/settings')">
              <span class="action-icon" style="background:rgba(99,102,241,0.12);color:#6366f1">
                <n-icon size="20"><GearIcon /></n-icon>
              </span>
              <span class="action-label">系统设置</span>
            </button>
          </div>
        </n-card>
      </n-grid-item>

      <!-- System info -->
      <n-grid-item>
        <n-card class="section-card" :segmented="{ content: true }">
          <template #header><span class="section-title">系统状态</span></template>
          <div class="info-list">
            <div class="info-row">
              <span class="info-label">运行环境</span>
              <n-tag :type="isDev ? 'warning' : 'success'" size="small" :bordered="false">
                {{ isDev ? '开发环境' : '生产环境' }}
              </n-tag>
            </div>
            <div class="info-row">
              <span class="info-label">服务状态</span>
              <div class="info-status">
                <span class="status-dot"></span>
                <span>运行中</span>
              </div>
            </div>
            <div class="info-row">
              <span class="info-label">前端版本</span>
              <span class="info-mono">0.1.0</span>
            </div>
            <div class="info-row">
              <span class="info-label">当前时间</span>
              <span class="info-mono">{{ currentTime }}</span>
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, onUnmounted, reactive, ref } from 'vue'
import { NButton, NCard, NGrid, NGridItem, NIcon, NTag, useMessage } from 'naive-ui'
import { listPages } from '../api/cms'
import { listPlanGroups } from '../api/product'
import { listOrders } from '../api/order'

/* ── Inline SVG icon components ── */
const RefreshIcon = () => h('svg', { xmlns:'http://www.w3.org/2000/svg', viewBox:'0 0 24 24', fill:'none', stroke:'currentColor', 'stroke-width':'2' }, [
  h('polyline', { points:'23 4 23 10 17 10' }),
  h('polyline', { points:'1 20 1 14 7 14' }),
  h('path', { d:'M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15' })
])
const PagesIcon = () => h('svg', { xmlns:'http://www.w3.org/2000/svg', viewBox:'0 0 24 24', fill:'none', stroke:'currentColor', 'stroke-width':'2' }, [
  h('path', { d:'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z' }),
  h('polyline', { points:'14 2 14 8 20 8' }),
  h('line', { x1:'16', y1:'13', x2:'8', y2:'13' })
])
const PlusIcon = () => h('svg', { xmlns:'http://www.w3.org/2000/svg', viewBox:'0 0 24 24', fill:'none', stroke:'currentColor', 'stroke-width':'2' }, [
  h('line', { x1:'12', y1:'5', x2:'12', y2:'19' }),
  h('line', { x1:'5', y1:'12', x2:'19', y2:'12' })
])
const ImageIcon = () => h('svg', { xmlns:'http://www.w3.org/2000/svg', viewBox:'0 0 24 24', fill:'none', stroke:'currentColor', 'stroke-width':'2' }, [
  h('rect', { x:'3', y:'3', width:'18', height:'18', rx:'2', ry:'2' }),
  h('circle', { cx:'8.5', cy:'8.5', r:'1.5' }),
  h('polyline', { points:'21 15 16 10 5 21' })
])
const LayersIcon = () => h('svg', { xmlns:'http://www.w3.org/2000/svg', viewBox:'0 0 24 24', fill:'none', stroke:'currentColor', 'stroke-width':'2' }, [
  h('path', { d:'M12 2L2 7l10 5 10-5-10-5z' }),
  h('path', { d:'M2 17l10 5 10-5' }),
  h('path', { d:'M2 12l10 5 10-5' })
])
const CartIcon = () => h('svg', { xmlns:'http://www.w3.org/2000/svg', viewBox:'0 0 24 24', fill:'none', stroke:'currentColor', 'stroke-width':'2' }, [
  h('circle', { cx:'9', cy:'21', r:'1' }),
  h('circle', { cx:'20', cy:'21', r:'1' }),
  h('path', { d:'M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6' })
])
const GearIcon = () => h('svg', { xmlns:'http://www.w3.org/2000/svg', viewBox:'0 0 24 24', fill:'none', stroke:'currentColor', 'stroke-width':'2' }, [
  h('circle', { cx:'12', cy:'12', r:'3' }),
  h('path', { d:'M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z' })
])

const message = useMessage()
const loading = ref(false)

const stats = reactive({ pages: 0, published: 0, planGroups: 0, orders: 0 })

const isDev = import.meta.env.DEV
const currentTime = ref(new Date().toLocaleString('zh-CN'))
let timer: ReturnType<typeof setInterval> | null = null

const statCards = computed(() => [
  {
    label: '页面总数',       value: stats.pages,
    icon: PagesIcon,        iconSize: 22,
    color: '#6366f1',       bg: 'rgba(99,102,241,0.12)',
    footer: `已发布 ${stats.published} 个页面`,
    tag: 'live',            tagType: 'success' as const
  },
  {
    label: '套餐组',         value: stats.planGroups,
    icon: LayersIcon,       iconSize: 22,
    color: '#10b981',       bg: 'rgba(16,185,129,0.12)',
    footer: '当前活跃的套餐分组'
  },
  {
    label: '订单数',         value: stats.orders,
    icon: CartIcon,         iconSize: 22,
    color: '#f59e0b',       bg: 'rgba(245,158,11,0.12)',
    footer: '全部订单'
  },
  {
    label: '发布率',         value: stats.pages > 0 ? Math.round(stats.published / stats.pages * 100) + '%' : '0%',
    icon: GearIcon,         iconSize: 22,
    color: '#8b5cf6',       bg: 'rgba(139,92,246,0.12)',
    footer: `${stats.published} / ${stats.pages} 已发布`
  }
])

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

/* ── Stat cards ── */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 18px;
  margin-bottom: 28px;
}

.stat-card {
  position: relative;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e9edf4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

html.dark .stat-card {
  background: #1e293b;
  border-color: #334155;
}

.stat-card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  opacity: 0.8;
}

.stat-card-body {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 22px 12px;
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

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 2px;
}

html.dark .stat-label {
  color: #94a3b8;
}

.stat-value {
  font-size: 26px;
  font-weight: 800;
  line-height: 1.2;
}

.stat-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 22px 14px;
  font-size: 12px;
  color: #94a3b8;
  border-top: 1px solid #f1f5f9;
}

html.dark .stat-footer {
  border-top-color: #1e293b;
}

/* ── Quick actions ── */
.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

@media (max-width: 480px) {
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border: none;
  background: transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
  font-family: inherit;
}

.action-item:hover {
  background: rgba(99, 102, 241, 0.06);
  transform: translateY(-2px);
}

.action-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-label {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
}

html.dark .action-label {
  color: #cbd5e1;
}

/* ── Section card ── */
.section-card {
  border-radius: 14px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
}

/* ── System info ── */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 13px;
  border-bottom: 1px solid #f1f5f9;
}

html.dark .info-row {
  border-bottom-color: #1e293b;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: #64748b;
}

html.dark .info-label {
  color: #94a3b8;
}

.info-mono {
  font-family: ui-monospace, monospace;
  font-size: 12px;
  color: #334155;
}

html.dark .info-mono {
  color: #cbd5e1;
}

.info-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #10b981;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 6px rgba(16,185,129,0.5);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
