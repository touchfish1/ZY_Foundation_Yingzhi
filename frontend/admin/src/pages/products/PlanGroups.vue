<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>套餐管理</h2>
          <p>维护真实套餐、价格和权益，供 CMS pricing 区块绑定展示。</p>
        </div>
        <div class="page-head-actions">
          <n-button quaternary @click="load" :loading="loading">
            <template #icon><n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></n-icon></template>
          </n-button>
          <n-button @click="showPlan = true" :disabled="!selectedGroupId">新增套餐</n-button>
          <n-button type="primary" @click="showGroup = true">新增套餐组</n-button>
        </div>
      </div>
    </div>

    <n-grid :cols="24" :x-gap="20">
      <n-grid-item :span="7">
        <n-card title="套餐组" :bordered="false" class="table-card">
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
        <n-card :title="selectedGroup?.name || '请选择套餐组'" :bordered="false" class="table-card">
          <div v-if="selectedGroup">
            <n-empty v-if="!paginatedPlans.length" description="该套餐组暂无套餐，请点击新增套餐" />
            <div v-else class="plans">
              <article v-for="plan in paginatedPlans" :key="plan.id" class="plan-card">
                <div class="plan-top">
                  <div>
                    <strong>{{ plan.name }}</strong>
                    <p>{{ plan.description }}</p>
                  </div>
                  <n-tag v-if="plan.badge" type="info" size="small" :bordered="false">{{ plan.badge }}</n-tag>
                </div>
                <div class="price-row">
                  <n-tag v-for="price in plan.prices" :key="price.id" size="tiny" :bordered="false" style="margin: 2px">{{ price.currency }} {{ price.amount }} / {{ price.billingCycle }}</n-tag>
                </div>
                <ul class="feature-list">
                  <li v-for="feature in plan.features" :key="feature.id">{{ feature.featureName }} {{ feature.featureValue }}</li>
                </ul>
                <n-space>
                  <n-button size="tiny" quaternary @click="openPrice(plan.id)">添加价格</n-button>
                  <n-button size="tiny" quaternary @click="openFeature(plan.id)">添加权益</n-button>
                </n-space>
              </article>
            </div>
            <div v-if="selectedGroup.plans.length > planPagination.pageSize" class="pagination-bottom">
              <n-pagination
                :page="planPagination.page"
                :page-size="planPagination.pageSize"
                :page-sizes="planPagination.pageSizes"
                :item-count="selectedGroup.plans.length"
                :show-size-picker="planPagination.showSizePicker"
                @update:page="planPagination.page = $event"
                @update:page-size="onUpdatePlanPageSize"
              />
            </div>
          </div>
          <n-empty v-else description="请选择或新增一个套餐组" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-modal v-model:show="showGroup" preset="card" title="新增套餐组" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="编码"><n-input v-model:value="groupForm.code" placeholder="api_plans" /></n-form-item>
        <n-form-item label="名称"><n-input v-model:value="groupForm.name" placeholder="API 套餐" /></n-form-item>
        <n-form-item label="描述"><n-input v-model:value="groupForm.description" type="textarea" placeholder="适合不同规模团队的 API 服务套餐" /></n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showGroup = false">取消</n-button>
          <n-button type="primary" @click="submitGroup">创建</n-button>
        </n-space>
      </n-form>
    </n-modal>

    <n-modal v-model:show="showPlan" preset="card" title="新增套餐" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { NButton, NCard, NEmpty, NForm, NFormItem, NGrid, NGridItem, NIcon, NInput, NInputNumber, NList, NListItem, NModal, NPagination, NSpace, NTag, useMessage } from 'naive-ui'
import { createFeature, createPlan, createPlanGroup, createPrice, listPlanGroups, type PlanGroup } from '../../api/product'

const message = useMessage()
const groups = ref<PlanGroup[]>([])
const loading = ref(false)
const selectedGroupId = ref<number | null>(null)
const selectedPlanId = ref<number | null>(null)
const selectedGroup = computed(() => groups.value.find(group => group.id === selectedGroupId.value))

const planPagination = reactive({
  page: 1,
  pageSize: 20,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
})

const paginatedPlans = computed(() => {
  if (!selectedGroup.value) return []
  const start = (planPagination.page - 1) * planPagination.pageSize
  return selectedGroup.value.plans.slice(start, start + planPagination.pageSize)
})

function onUpdatePlanPageSize(size: number) {
  planPagination.pageSize = size
  planPagination.page = 1
}

watch(selectedGroupId, () => {
  planPagination.page = 1
})

const showGroup = ref(false)
const showPlan = ref(false)
const showPrice = ref(false)
const showFeature = ref(false)

const groupForm = reactive({ code: 'api_plans', name: 'API 套餐', description: '适合不同规模团队的 API 服务套餐', sortOrder: 10 })
const planForm = reactive({ code: 'starter', name: '入门版', description: '适合快速开始', badge: 'Starter', sortOrder: 10 })
const priceForm = reactive({ currency: 'CNY', billingCycle: 'monthly', amount: 29 })
const featureForm = reactive({ featureName: '每日 1,000 次调用', featureValue: '', included: true, sortOrder: 10 })

async function load() {
  loading.value = true
  try {
    groups.value = await listPlanGroups()
    if (!groups.value.some(group => group.id === selectedGroupId.value)) {
      selectedGroupId.value = groups.value[0]?.id || null
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '套餐组加载失败')
  } finally {
    loading.value = false
  }
}

async function submitGroup() {
  if (!groupForm.code || !groupForm.name) { message.warning('请填写编码和名称'); return }
  try {
    await createPlanGroup(groupForm)
    message.success('套餐组已创建')
    showGroup.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  }
}

async function submitPlan() {
  if (!selectedGroupId.value) { message.warning('请先选择套餐组'); return }
  if (!planForm.code || !planForm.name) { message.warning('请填写编码和名称'); return }
  try {
    await createPlan({ ...planForm, groupId: selectedGroupId.value })
    message.success('套餐已创建')
    showPlan.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
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

async function submitPrice() {
  if (!selectedPlanId.value) { message.warning('请先选择套餐'); return }
  if (!priceForm.amount || priceForm.amount <= 0) { message.warning('请输入有效金额'); return }
  await createPrice({ ...priceForm, planId: selectedPlanId.value })
  message.success('价格已添加')
  showPrice.value = false
  await load()
}

async function submitFeature() {
  if (!selectedPlanId.value) { message.warning('请先选择套餐'); return }
  if (!featureForm.featureName) { message.warning('请填写权益名称'); return }
  await createFeature({ ...featureForm, planId: selectedPlanId.value })
  message.success('权益已添加')
  showFeature.value = false
  await load()
}

onMounted(() => { load() })
</script>

<style scoped>
.group-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.2s;
}
.group-item.active {
  background-color: rgba(24, 160, 88, 0.1);
}
.group-item strong {
  font-size: 14px;
}
.group-item span {
  font-size: 12px;
  color: #888;
}

.plans {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plan-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px 20px;
  background: #fff;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.plan-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: #d0d5dd;
}

.plan-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}
.plan-top strong {
  font-size: 16px;
}
.plan-top p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #666;
}

.price-row {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 10px;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0 0 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px 16px;
}
.feature-list li {
  font-size: 13px;
  color: #444;
  position: relative;
  padding-left: 14px;
}
.feature-list li::before {
  content: '·';
  position: absolute;
  left: 0;
  font-weight: bold;
  color: #18a058;
}

.pagination-bottom {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
