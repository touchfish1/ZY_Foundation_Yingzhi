<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>套餐列表</h2>
          <p>集中查看和管理所有套餐组下的套餐、价格和权益。</p>
        </div>
        <div class="page-head-actions">
          <n-button quaternary @click="load" :loading="loading">
            <template #icon><n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></n-icon></template>
          </n-button>
          <n-button type="primary" @click="showPlan = true">新增套餐</n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card">
      <n-data-table :columns="columns" :data="plans" :loading="loading" :pagination="pagination" :bordered="false" :striped="true" size="small" />
    </n-card>

    <n-modal v-model:show="showPlan" preset="card" title="新增套餐" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="所属组">
          <n-select v-model:value="planForm.groupId" :options="groupOptions" filterable placeholder="请选择套餐组" />
        </n-form-item>
        <n-form-item label="编码"><n-input v-model:value="planForm.code" placeholder="starter" /></n-form-item>
        <n-form-item label="名称"><n-input v-model:value="planForm.name" placeholder="入门版" /></n-form-item>
        <n-form-item label="描述"><n-input v-model:value="planForm.description" type="textarea" placeholder="适合快速开始" /></n-form-item>
        <n-form-item label="徽标"><n-input v-model:value="planForm.badge" placeholder="Starter" /></n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showPlan = false">取消</n-button>
          <n-button type="primary" @click="submitPlan">创建</n-button>
        </n-space>
      </n-form>
    </n-modal>

    <n-modal v-model:show="showPrice" preset="card" title="添加价格" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="币种"><n-input v-model:value="priceForm.currency" placeholder="CNY" /></n-form-item>
        <n-form-item label="周期"><n-input v-model:value="priceForm.billingCycle" placeholder="monthly" /></n-form-item>
        <n-form-item label="金额"><n-input-number v-model:value="priceForm.amount" class="full" placeholder="29" /></n-form-item>
        <n-form-item label="原价（可选）"><n-input-number v-model:value="priceForm.originalAmount" class="full" /></n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showPrice = false">取消</n-button>
          <n-button type="primary" @click="submitPrice">添加</n-button>
        </n-space>
      </n-form>
    </n-modal>

    <n-modal v-model:show="showFeature" preset="card" title="添加权益" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="权益名称"><n-input v-model:value="featureForm.featureName" placeholder="每日调用次数" /></n-form-item>
        <n-form-item label="权益值"><n-input v-model:value="featureForm.featureValue" placeholder="1,000" /></n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showFeature = false">取消</n-button>
          <n-button type="primary" @click="submitFeature">添加</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDataTable, NForm, NFormItem, NIcon, NInput, NInputNumber, NModal, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import { createFeature, createPlan, createPrice, listPlanGroups, listPlans, type PlanGroup } from '../../api/product'

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
  prices: Array<{ id: number; currency: string; billingCycle: string; amount: string; originalAmount?: string; status: string }>
  features: Array<{ id: number; featureName: string; featureValue: string; included: boolean; sortOrder: number }>
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
  { title: '编码', key: 'code', width: 110 },
  { title: '名称', key: 'name' },
  {
    title: '徽标', key: 'badge', width: 90,
    render(row: PlanRow) {
      return row.badge ? h(NTag, { type: 'info', size: 'small', bordered: false }, { default: () => row.badge }) : null
    }
  },
  { title: '所属组', key: 'groupName', width: 110 },
  {
    title: '价格', key: 'prices',
    render(row: PlanRow) {
      const tags = (row.prices || []).map(p => h(NTag, { size: 'tiny', style: 'margin: 2px', bordered: false }, { default: () => `${p.currency} ${p.amount} / ${p.billingCycle}` }))
      return h('span', {}, tags)
    }
  },
  { title: '状态', key: 'status', width: 80,
    render(row: PlanRow) {
      return h(NTag, { type: row.status === 'ACTIVE' ? 'success' : 'warning', size: 'small', bordered: false }, { default: () => row.status })
    }
  },
  {
    title: '操作', key: 'actions', width: 180,
    render(row: PlanRow) {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, onClick: () => openPrice(row.id) }, { default: () => '添加价格' }),
          h(NButton, { size: 'small', quaternary: true, onClick: () => openFeature(row.id) }, { default: () => '添加权益' })
        ]
      })
    }
  }
]

async function load() {
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
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openPrice(planId: number) {
  selectedPlanId.value = planId
  showPrice.value = true
}

function openFeature(planId: number) {
  selectedPlanId.value = planId
  showFeature.value = true
}

async function submitPlan() {
  if (!planForm.groupId) { message.warning('请先选择套餐组'); return }
  if (!planForm.code || !planForm.name) { message.warning('请填写编码和名称'); return }
  try {
    await createPlan(planForm)
    message.success('套餐已创建')
    showPlan.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  }
}

async function submitPrice() {
  if (!selectedPlanId.value) { message.warning('请先选择套餐'); return }
  if (!priceForm.amount || priceForm.amount <= 0) { message.warning('请输入有效金额'); return }
  try {
    await createPrice({ ...priceForm, planId: selectedPlanId.value })
    message.success('价格已添加')
    showPrice.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '添加失败')
  }
}

async function submitFeature() {
  if (!selectedPlanId.value) { message.warning('请先选择套餐'); return }
  if (!featureForm.featureName) { message.warning('请填写权益名称'); return }
  try {
    await createFeature({ ...featureForm, planId: selectedPlanId.value })
    message.success('权益已添加')
    showFeature.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '添加失败')
  }
}

onMounted(() => { load() })
</script>
