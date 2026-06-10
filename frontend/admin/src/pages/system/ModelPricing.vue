<template>
  <div>
    <n-card title="模型定价" :bordered="true" class="table-card" style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
      <template #header-extra>
        <n-space align="center" size="small">
          <n-button type="primary" @click="openCreate">新建定价</n-button>
        </n-space>
      </template>
      <CommonTable
        :columns="columns"
        :data="pricings"
        :loading="loading"
      />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" :title="isEditing ? '编辑定价' : '新建定价'" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="模型名称">
          <n-input v-model:value="form.modelName" placeholder="例如: gpt-4o" />
        </n-form-item>
        <n-form-item label="输入价格">
          <n-input-number v-model:value="form.inputPrice" :min="0" :precision="8" placeholder="0" style="width: 200px" />
        </n-form-item>
        <n-form-item label="输出价格">
          <n-input-number v-model:value="form.outputPrice" :min="0" :precision="8" placeholder="0" style="width: 200px" />
        </n-form-item>
        <n-form-item label="货币">
          <n-select v-model:value="form.currency" :options="currencyOptions" />
        </n-form-item>
        <n-form-item label="生效时间">
          <n-date-picker v-model:value="form.effectiveFromTs" type="datetime" placeholder="选择生效时间" style="width: 100%" />
        </n-form-item>
        <n-form-item label="失效时间（可选）">
          <n-date-picker v-model:value="form.effectiveToTs" type="datetime" placeholder="选择失效时间" style="width: 100%" clearable />
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="submitForm">保存</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDatePicker, NForm, NFormItem, NInput, NInputNumber, NModal, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import { listModelPricings, createModelPricing, updateModelPricing, type ModelPricingConfig } from '../../api/ai'
import { formatDate } from '../../utils/format'

const message = useMessage()

const pricings = ref<ModelPricingConfig[]>([])
const loading = ref(false)
const showModal = ref(false)
const saving = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  modelName: '',
  inputPrice: 0,
  outputPrice: 0,
  currency: 'CNY',
  effectiveFromTs: Date.now(),
  effectiveToTs: null as number | null
})

const currencyOptions: SelectOption[] = [
  { label: 'CNY (人民币)', value: 'CNY' },
  { label: 'USD (美元)', value: 'USD' },
  { label: 'JPY (日元)', value: 'JPY' }
]

const columns: DataTableColumns<ModelPricingConfig> = [
  { title: 'ID', key: 'id', width: 70, render(row) { return h(NTag, { size: 'tiny', bordered: false }, { default: () => row.id }) } },
  { title: '模型名称', key: 'modelName' },
  { title: '输入价格', key: 'inputPrice', width: 120,
    render(row) { return row.inputPrice.toFixed(8) }
  },
  { title: '输出价格', key: 'outputPrice', width: 120,
    render(row) { return row.outputPrice.toFixed(8) }
  },
  { title: '货币', key: 'currency', width: 80 },
  { title: '生效时间', key: 'effectiveFrom', width: 170,
    render(row) { return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.effectiveFrom)) }
  },
  {
    title: '失效时间', key: 'effectiveTo', width: 170,
    render(row) { return h('span', { style: 'color: #64748b; font-size: 13px;' }, row.effectiveTo ? formatDate(row.effectiveTo) : '-') }
  },
  {
    title: '操作', key: 'actions', width: 100,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', quaternary: true, onClick: () => openEdit(row) }, { default: () => '编辑' })
        ]
      })
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    pricings.value = await listModelPricings()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.modelName = ''
  form.inputPrice = 0
  form.outputPrice = 0
  form.currency = 'CNY'
  form.effectiveFromTs = Date.now()
  form.effectiveToTs = null
}

function openCreate() {
  isEditing.value = false
  editingId.value = null
  resetForm()
  showModal.value = true
}

function openEdit(row: ModelPricingConfig) {
  isEditing.value = true
  editingId.value = row.id
  form.modelName = row.modelName
  form.inputPrice = row.inputPrice
  form.outputPrice = row.outputPrice
  form.currency = row.currency
  form.effectiveFromTs = new Date(row.effectiveFrom).getTime()
  form.effectiveToTs = row.effectiveTo ? new Date(row.effectiveTo).getTime() : null
  showModal.value = true
}

async function submitForm() {
  saving.value = true
  try {
    const payload = {
      modelName: form.modelName,
      inputPrice: form.inputPrice,
      outputPrice: form.outputPrice,
      currency: form.currency,
      effectiveFrom: new Date(form.effectiveFromTs).toISOString(),
      effectiveTo: form.effectiveToTs ? new Date(form.effectiveToTs).toISOString() : null
    }
    if (isEditing.value && editingId.value) {
      await updateModelPricing(editingId.value, payload)
      message.success('定价已更新')
    } else {
      await createModelPricing(payload)
      message.success('定价已创建')
    }
    showModal.value = false
    await loadData()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => { loadData() })
</script>
