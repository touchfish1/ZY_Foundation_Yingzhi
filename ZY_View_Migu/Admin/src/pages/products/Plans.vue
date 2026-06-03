<template>
  <div class="page-head">
    <div>
      <h2>套餐列表</h2>
      <p>集中查看和管理所有套餐组下的套餐、价格和权益。</p>
    </div>
    <n-space>
      <n-button @click="load" :loading="loading">刷新</n-button>
      <n-button type="primary" @click="showPlan = true">新增套餐</n-button>
    </n-space>
  </div>

  <n-card>
    <n-data-table :columns="columns" :data="plans" :loading="loading" :pagination="pagination" :bordered="false" />
  </n-card>

  <n-modal v-model:show="showPlan" preset="card" title="新增套餐" class="modal-card">
    <n-form>
      <n-form-item label="所属组">
        <n-select v-model:value="planForm.groupId" :options="groupOptions" filterable />
      </n-form-item>
      <n-form-item label="编码"><n-input v-model:value="planForm.code" /></n-form-item>
      <n-form-item label="名称"><n-input v-model:value="planForm.name" /></n-form-item>
      <n-form-item label="描述"><n-input v-model:value="planForm.description" type="textarea" /></n-form-item>
      <n-form-item label="徽标"><n-input v-model:value="planForm.badge" /></n-form-item>
      <n-button type="primary" @click="submitPlan">创建</n-button>
    </n-form>
  </n-modal>

  <n-modal v-model:show="showPrice" preset="card" title="添加价格" class="modal-card">
    <n-form>
      <n-form-item label="币种"><n-input v-model:value="priceForm.currency" /></n-form-item>
      <n-form-item label="周期"><n-input v-model:value="priceForm.billingCycle" /></n-form-item>
      <n-form-item label="金额"><n-input-number v-model:value="priceForm.amount" class="full" /></n-form-item>
      <n-form-item label="原价（可选）"><n-input-number v-model:value="priceForm.originalAmount" class="full" /></n-form-item>
      <n-button type="primary" @click="submitPrice">添加</n-button>
    </n-form>
  </n-modal>

  <n-modal v-model:show="showFeature" preset="card" title="添加权益" class="modal-card">
    <n-form>
      <n-form-item label="权益名称"><n-input v-model:value="featureForm.featureName" /></n-form-item>
      <n-form-item label="权益值"><n-input v-model:value="featureForm.featureValue" /></n-form-item>
      <n-button type="primary" @click="submitFeature">添加</n-button>
    </n-form>
  </n-modal>
</template>

<script setup lang="ts">
// 套餐列表页面：表格展示所有套餐，支持新增套餐/价格/权益
import { computed, h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NInputNumber, NModal, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import { createFeature, createPlan, createPrice, listPlanGroups, listPlans, type PlanGroup, type Price, type Feature } from '../../api/product'

interface PlanRow {
  id: number
  code: string
  name: string
  description: string
  badge: string
  status: string
  sortOrder: number
  groupName: string
  groupId: number
  prices: Price[]
  features: Feature[]
}

const message = useMessage()
const plans = ref<PlanRow[]>([])
const planGroups = ref<PlanGroup[]>([])
const loading = ref(false)
const showPlan = ref(false)
const showPrice = ref(false)
const showFeature = ref(false)
const selectedPlanId = ref<number | null>(null)

const planForm = reactive({ groupId: 0, code: 'starter', name: '入门版', description: '适合快速开始', badge: 'Starter', sortOrder: 10 })
const priceForm = reactive({ currency: 'CNY', billingCycle: 'monthly', amount: 29, originalAmount: null as number | null })
const featureForm = reactive({ featureName: '', featureValue: '', included: true, sortOrder: 10 })

const groupOptions = computed(() => planGroups.value.map(g => ({ label: g.name, value: g.id })))

const pagination = reactive({ pageSize: 20 })

const columns = [
  { title: '编码', key: 'code', width: 120 },
  { title: '名称', key: 'name', width: 140 },
  {
    title: '徽标',
    key: 'badge',
    width: 100,
    render(row: PlanRow) {
      return row.badge ? h(NTag, { type: 'info', size: 'small' }, { default: () => row.badge }) : null
    }
  },
  { title: '所属组', key: 'groupName', width: 120 },
  {
    title: '价格',
    key: 'prices',
    render(row: PlanRow) {
      const tags = (row.prices || []).map(p => h(NTag, { size: 'small', style: 'margin: 2px' }, { default: () => `${p.currency} ${p.amount} / ${p.billingCycle}` }))
      return h('span', {}, tags)
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row: PlanRow) {
      return h(NTag, { type: row.status === 'ACTIVE' ? 'success' : 'warning', size: 'small' }, { default: () => row.status })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row: PlanRow) {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => openPrice(row.id) }, { default: () => '添加价格' }),
          h(NButton, { size: 'small', onClick: () => openFeature(row.id) }, { default: () => '添加权益' })
        ]
      })
    }
  }
]

// 加载套餐列表和套餐组映射信息
async function load() {
  console.log('[Plans] load')
  loading.value = true
  try {
    const [p, g] = await Promise.all([listPlans(), listPlanGroups()])
    const groupMap = new Map(g.map(grp => [grp.id, grp.name]))
    plans.value = p.map(plan => ({
      ...plan,
      groupName: groupMap.get(plan.groupId) ?? '未知',
      groupId: plan.groupId
    }))
    planGroups.value = g
    console.log('[Plans] loaded', plans.value.length, 'plans')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

// 打开添加价格模态框
function openPrice(planId: number) {
  console.log('[Plans] openPrice', { planId })
  selectedPlanId.value = planId
  showPrice.value = true
}

// 打开添加权益模态框
function openFeature(planId: number) {
  console.log('[Plans] openFeature', { planId })
  selectedPlanId.value = planId
  showFeature.value = true
}

// 提交新增套餐：校验 -> 调用 API -> 刷新列表
async function submitPlan() {
  console.log('[Plans] submitPlan', planForm)
  if (!planForm.groupId) {
    message.warning('请先选择套餐组')
    return
  }
  if (!planForm.code || !planForm.name) {
    message.warning('请填写编码和名称')
    return
  }
  try {
    await createPlan(planForm)
    message.success('套餐已创建')
    showPlan.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  }
}

// 提交添加价格：校验 -> 调用 API -> 刷新列表
async function submitPrice() {
  console.log('[Plans] submitPrice', priceForm)
  if (!selectedPlanId.value) {
    message.warning('请先选择套餐')
    return
  }
  if (!priceForm.amount || priceForm.amount <= 0) {
    message.warning('请输入有效金额')
    return
  }
  try {
    await createPrice({ ...priceForm, planId: selectedPlanId.value })
    message.success('价格已添加')
    showPrice.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '添加失败')
  }
}

// 提交添加权益：校验 -> 调用 API -> 刷新列表
async function submitFeature() {
  console.log('[Plans] submitFeature', featureForm)
  if (!selectedPlanId.value) {
    message.warning('请先选择套餐')
    return
  }
  if (!featureForm.featureName) {
    message.warning('请填写权益名称')
    return
  }
  try {
    await createFeature({ ...featureForm, planId: selectedPlanId.value })
    message.success('权益已添加')
    showFeature.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '添加失败')
  }
}

onMounted(() => { console.log('[Plans] mounted'); load() })
</script>

<style scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-head h2 {
  margin: 0 0 6px;
}

.page-head p {
  color: #64748b;
}

.modal-card {
  width: min(520px, 92vw);
}

.full {
  width: 100%;
}
</style>
