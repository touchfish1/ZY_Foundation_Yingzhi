<template>
  <div class="page-head">
    <div>
      <h2>套餐管理</h2>
      <p>维护真实套餐、价格和权益，供 CMS pricing 区块绑定展示。</p>
    </div>
    <n-space>
      <n-button @click="load" :loading="loading">刷新</n-button>
      <n-button @click="showPlan = true" :disabled="!selectedGroupId">新增套餐</n-button>
      <n-button type="primary" @click="showGroup = true">新增套餐组</n-button>
    </n-space>
  </div>

  <n-grid :cols="24" :x-gap="20">
    <n-grid-item :span="7">
      <n-card title="套餐组">
        <n-empty v-if="!loading && !groups.length" description="暂无套餐组，请先新增套餐组" />
        <n-list v-else clickable hoverable>
          <n-list-item v-for="group in groups" :key="group.id" @click="selectedGroupId = group.id">
            <div class="group-item" :class="{ active: selectedGroupId === group.id }">
              <strong>{{ group.name }}</strong>
              <span>{{ group.code }}</span>
            </div>
          </n-list-item>
        </n-list>
      </n-card>
    </n-grid-item>

    <n-grid-item :span="17">
      <n-card :title="selectedGroup?.name || '请选择套餐组'">
        <div v-if="selectedGroup" class="plans">
          <n-empty v-if="!selectedGroup.plans.length" description="该套餐组暂无套餐，请点击新增套餐" />
          <article v-for="plan in selectedGroup.plans" :key="plan.id" class="plan-card">
            <div class="plan-top">
              <div>
                <strong>{{ plan.name }}</strong>
                <p>{{ plan.description }}</p>
              </div>
              <n-tag v-if="plan.badge" type="info">{{ plan.badge }}</n-tag>
            </div>
            <div class="price-row">
              <span v-for="price in plan.prices" :key="price.id">{{ price.currency }} {{ price.amount }} / {{ price.billingCycle }}</span>
            </div>
            <ul>
              <li v-for="feature in plan.features" :key="feature.id">{{ feature.featureName }} {{ feature.featureValue }}</li>
            </ul>
            <n-space>
              <n-button size="small" @click="openPrice(plan.id)">添加价格</n-button>
              <n-button size="small" @click="openFeature(plan.id)">添加权益</n-button>
            </n-space>
          </article>
        </div>
        <n-empty v-else description="请选择或新增一个套餐组" />
      </n-card>
    </n-grid-item>
  </n-grid>

  <n-modal v-model:show="showGroup" preset="card" title="新增套餐组" class="modal-card">
    <n-form>
      <n-form-item label="编码"><n-input v-model:value="groupForm.code" /></n-form-item>
      <n-form-item label="名称"><n-input v-model:value="groupForm.name" /></n-form-item>
      <n-form-item label="描述"><n-input v-model:value="groupForm.description" type="textarea" /></n-form-item>
      <n-button type="primary" @click="submitGroup">创建</n-button>
    </n-form>
  </n-modal>

  <n-modal v-model:show="showPlan" preset="card" title="新增套餐" class="modal-card">
    <n-form>
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
// 套餐管理页面：左侧套餐组列表 + 右侧套餐详情，支持创建套餐组/套餐/价格/权益
import { computed, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NEmpty, NForm, NFormItem, NGrid, NGridItem, NInput, NInputNumber, NList, NListItem, NModal, NSpace, NTag, useMessage } from 'naive-ui'
import { createFeature, createPlan, createPlanGroup, createPrice, listPlanGroups, type PlanGroup } from '../../api/product'

const message = useMessage()
const groups = ref<PlanGroup[]>([])
const loading = ref(false)
const selectedGroupId = ref<number | null>(null)
const selectedPlanId = ref<number | null>(null)
const selectedGroup = computed(() => groups.value.find(group => group.id === selectedGroupId.value))

const showGroup = ref(false)
const showPlan = ref(false)
const showPrice = ref(false)
const showFeature = ref(false)

const groupForm = reactive({ code: 'api_plans', name: 'API 套餐', description: '适合不同规模团队的 API 服务套餐', sortOrder: 10 })
const planForm = reactive({ code: 'starter', name: '入门版', description: '适合快速开始', badge: 'Starter', sortOrder: 10 })
const priceForm = reactive({ currency: 'CNY', billingCycle: 'monthly', amount: 29 })
const featureForm = reactive({ featureName: '每日 1,000 次调用', featureValue: '', included: true, sortOrder: 10 })

// 加载套餐组列表，保持当前选中组不变
async function load() {
  console.log('[PlanGroups] load')
  loading.value = true
  try {
    groups.value = await listPlanGroups()
    if (!groups.value.some(group => group.id === selectedGroupId.value)) {
      selectedGroupId.value = groups.value[0]?.id || null
    }
    console.log('[PlanGroups] loaded', groups.value.length, 'groups')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '套餐组加载失败')
  } finally {
    loading.value = false
  }
}

// 新增套餐组：校验 -> 调用 API -> 刷新列表
async function submitGroup() {
  console.log('[PlanGroups] submitGroup', groupForm)
  if (!groupForm.code || !groupForm.name) {
    message.warning('请填写编码和名称')
    return
  }
  try {
    await createPlanGroup(groupForm)
    message.success('套餐组已创建')
    showGroup.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  }
}

// 新增套餐：校验 -> 调用 API -> 刷新列表
async function submitPlan() {
  console.log('[PlanGroups] submitPlan', planForm)
  if (!selectedGroupId.value) {
    message.warning('请先选择套餐组')
    return
  }
  if (!planForm.code || !planForm.name) {
    message.warning('请填写编码和名称')
    return
  }
  try {
    await createPlan({ ...planForm, groupId: selectedGroupId.value })
    message.success('套餐已创建')
    showPlan.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  }
}

// 打开添加价格模态框
function openPrice(planId: number) {
  console.log('[PlanGroups] openPrice', { planId })
  selectedPlanId.value = planId
  showPrice.value = true
}

// 打开添加权益模态框
function openFeature(planId: number) {
  console.log('[PlanGroups] openFeature', { planId })
  selectedPlanId.value = planId
  showFeature.value = true
}

// 添加价格：校验 -> 调用 API -> 刷新列表
async function submitPrice() {
  console.log('[PlanGroups] submitPrice', priceForm)
  if (!selectedPlanId.value) {
    message.warning('请先选择套餐')
    return
  }
  if (!priceForm.amount || priceForm.amount <= 0) {
    message.warning('请输入有效金额')
    return
  }
  await createPrice({ ...priceForm, planId: selectedPlanId.value })
  message.success('价格已添加')
  showPrice.value = false
  await load()
}

// 添加权益：校验 -> 调用 API -> 刷新列表
async function submitFeature() {
  console.log('[PlanGroups] submitFeature', featureForm)
  if (!selectedPlanId.value) {
    message.warning('请先选择套餐')
    return
  }
  if (!featureForm.featureName) {
    message.warning('请填写权益名称')
    return
  }
  await createFeature({ ...featureForm, planId: selectedPlanId.value })
  message.success('权益已添加')
  showFeature.value = false
  await load()
}

onMounted(() => { console.log('[PlanGroups] mounted'); load() })
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

.page-head p,
.group-item span,
.plan-card p {
  color: #64748b;
}

.group-item {
  padding: 10px;
  border-radius: 12px;
}

.group-item.active {
  background: #eef6ff;
}

.group-item strong,
.group-item span {
  display: block;
}

.plans {
  display: grid;
  gap: 16px;
}

.plan-card {
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
}

.plan-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.price-row {
  display: flex;
  gap: 10px;
  margin: 10px 0;
  font-weight: 800;
}

.modal-card {
  width: min(520px, 92vw);
}

.full {
  width: 100%;
}
</style>
