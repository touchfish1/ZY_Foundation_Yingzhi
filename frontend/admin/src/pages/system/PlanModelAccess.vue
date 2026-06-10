<template>
  <div>
    <n-card title="模型访问权限" :bordered="true" class="table-card" style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
      <template #header-extra>
        <n-space align="center" size="small">
          <n-button type="primary" @click="openCreate">新建规则</n-button>
        </n-space>
      </template>
      <CommonTable
        :columns="columns"
        :data="accessRules"
        :loading="loading"
      />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" title="新建访问规则" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="套餐编码">
          <n-input v-model:value="form.planCode" placeholder="例如: plan_basic" />
        </n-form-item>
        <n-form-item label="模型名称">
          <n-input v-model:value="form.modelName" placeholder="例如: gpt-4o" />
        </n-form-item>
        <n-form-item label="最大并发数">
          <n-input-number v-model:value="form.maxConcurrency" :min="0" :precision="0" placeholder="5" style="width: 200px" />
        </n-form-item>
        <n-form-item label="最大 RPM（0 表示不限）">
          <n-input-number v-model:value="form.maxRpm" :min="0" :precision="0" placeholder="0" style="width: 200px" />
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="submitForm">创建</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NForm, NFormItem, NInput, NInputNumber, NModal, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import { listPlanModelAccesses, createPlanModelAccess, deletePlanModelAccess, type PlanModelAccess } from '../../api/ai'
import { useConfirm } from '../../composables/useConfirm'

const message = useMessage()
const { confirm } = useConfirm()

const accessRules = ref<PlanModelAccess[]>([])
const loading = ref(false)
const showModal = ref(false)
const saving = ref(false)

const form = reactive({
  planCode: '',
  modelName: '',
  maxConcurrency: 5,
  maxRpm: 0
})

const columns: DataTableColumns<PlanModelAccess> = [
  { title: 'ID', key: 'id', width: 70, render(row) { return h(NTag, { size: 'tiny', bordered: false }, { default: () => row.id }) } },
  { title: '套餐编码', key: 'planCode' },
  { title: '模型名称', key: 'modelName' },
  { title: '最大并发数', key: 'maxConcurrency', width: 120 },
  {
    title: '最大 RPM', key: 'maxRpm', width: 120,
    render(row) {
      return row.maxRpm === 0 ? '不限' : row.maxRpm
    }
  },
  {
    title: '操作', key: 'actions', width: 100,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'error', quaternary: true, onClick: () => handleDelete(row.id) }, { default: () => '删除' })
        ]
      })
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    accessRules.value = await listPlanModelAccesses()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.planCode = ''
  form.modelName = ''
  form.maxConcurrency = 5
  form.maxRpm = 0
}

function openCreate() {
  resetForm()
  showModal.value = true
}

async function submitForm() {
  saving.value = true
  try {
    await createPlanModelAccess({ ...form })
    message.success('访问规则已创建')
    showModal.value = false
    await loadData()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  const confirmed = await confirm({ content: '确定要删除此访问规则吗？' })
  if (!confirmed) return
  try {
    await deletePlanModelAccess(id)
    message.success('访问规则已删除')
    await loadData()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => { loadData() })
</script>
