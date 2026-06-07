<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>操作日志</h2>
          <p>查看管理员操作日志，包括资源创建、更新、删除等操作记录。</p>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card" content-style="padding: 0;">
      <template #header>
        <n-space align="center" size="small" style="padding: 4px 0;">
          <n-input v-model:value="filterOperator" placeholder="操作人 ID" clearable style="width: 120px" />
          <n-select
            v-model:value="filterResourceType"
            placeholder="资源类型"
            clearable
            style="width: 160px"
            :options="resourceTypeOptions"
          />
          <n-select
            v-model:value="filterOperationType"
            placeholder="操作类型"
            clearable
            style="width: 140px"
            :options="operationTypeOptions"
          />
          <n-date-picker
            v-model:value="dateRange"
            type="daterange"
            clearable
            style="width: 250px"
            placeholder="选择日期范围"
          />
          <n-button type="primary" @click="search" :loading="loading">搜索</n-button>
        </n-space>
      </template>
      <n-empty v-if="!loading && !logs.length" description="暂无操作日志" />
      <CommonTable
        v-else
        :columns="columns"
        :data="logs"
        :loading="loading"
        :pagination="paginationReactive"
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NCard,
  NDatePicker,
  NEmpty,
  NInput,
  NSelect,
  NSpace,
  NTag,
  useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import { listOperationLogs, type OperationLog, type OperationType, type ResourceType } from '../../api/system'
import { formatDate } from '../../utils/format'

const message = useMessage()
const logs = ref<OperationLog[]>([])
const loading = ref(false)
const filterOperator = ref('')
const filterResourceType = ref<ResourceType | null>(null)
const filterOperationType = ref<OperationType | null>(null)
const dateRange = ref<[number, number] | null>(null)

const resourceTypeOptions = [
  { label: 'CMS 页面', value: 'CMS_PAGE' },
  { label: '套餐分组', value: 'PRODUCT_PLAN_GROUP' },
  { label: '套餐', value: 'PRODUCT_PLAN' },
  { label: '价格', value: 'PRODUCT_PRICE' },
  { label: '功能', value: 'PRODUCT_FEATURE' },
  { label: '订单', value: 'ORDER' },
  { label: '文件', value: 'ASSET_FILE' },
  { label: '用户', value: 'ADMIN_USER' },
  { label: '角色', value: 'ADMIN_ROLE' },
  { label: '系统设置', value: 'SYSTEM_SETTING' },
  { label: '其他', value: 'OTHER' }
]

const operationTypeOptions = [
  { label: '创建', value: 'CREATE' },
  { label: '更新', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
  { label: '查询', value: 'QUERY' },
  { label: '登录', value: 'LOGIN' },
  { label: '导出', value: 'EXPORT' },
  { label: '导入', value: 'IMPORT' },
  { label: '其他', value: 'OTHER' }
]

const paginationReactive = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => {
    paginationReactive.page = page
    loadData()
  },
  onUpdatePageSize: (size: number) => {
    paginationReactive.pageSize = size
    paginationReactive.page = 1
    loadData()
  }
})

function getOperationTypeLabel(type: string): string {
  const map: Record<string, string> = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    QUERY: '查询',
    LOGIN: '登录',
    EXPORT: '导出',
    IMPORT: '导入',
    OTHER: '其他'
  }
  return map[type] || type
}

function getResourceTypeLabel(type: string): string {
  const map: Record<string, string> = {
    CMS_PAGE: 'CMS 页面',
    PRODUCT_PLAN_GROUP: '套餐分组',
    PRODUCT_PLAN: '套餐',
    PRODUCT_PRICE: '价格',
    PRODUCT_FEATURE: '功能',
    ORDER: '订单',
    ASSET_FILE: '文件',
    ADMIN_USER: '用户',
    ADMIN_ROLE: '角色',
    SYSTEM_SETTING: '系统设置',
    OTHER: '其他'
  }
  return map[type] || type
}

const columns: DataTableColumns<OperationLog> = [
  {
    title: 'ID',
    key: 'id',
    width: 70,
    render(row) {
      return h(NTag, { size: 'tiny', bordered: false }, { default: () => row.id })
    }
  },
  { title: '操作人', key: 'operatorName', width: 120 },
  {
    title: '操作类型',
    key: 'operationType',
    width: 100,
    render(row) {
      const typeMap: Record<string, string> = {
        CREATE: 'success',
        UPDATE: 'warning',
        DELETE: 'error',
        QUERY: 'default',
        LOGIN: 'info',
        EXPORT: 'info',
        IMPORT: 'info',
        OTHER: 'default'
      }
      return h(
        NTag,
        { size: 'small', bordered: false, type: typeMap[row.operationType] as any },
        { default: () => getOperationTypeLabel(row.operationType) }
      )
    }
  },
  {
    title: '资源类型',
    key: 'resourceType',
    width: 120,
    render(row) {
      return h(NTag, { size: 'small', bordered: false, type: 'default' }, {
        default: () => getResourceTypeLabel(row.resourceType)
      })
    }
  },
  { title: '资源 ID', key: 'resourceId', width: 100, ellipsis: { tooltip: true } },
  { title: '详情', key: 'detail', ellipsis: { tooltip: true } },
  { title: 'IP 地址', key: 'ipAddress', width: 140 },
  {
    title: '状态',
    key: 'success',
    width: 80,
    render(row) {
      return row.success
        ? h(NTag, { size: 'small', type: 'success', bordered: false }, { default: () => '成功' })
        : h(NTag, { size: 'small', type: 'error', bordered: false }, { default: () => '失败' })
    }
  },
  {
    title: '时间',
    key: 'createdAt',
    width: 180,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.createdAt))
    }
  }
]

function search() {
  paginationReactive.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    let startTime: string | undefined
    let endTime: string | undefined
    if (dateRange.value) {
      startTime = new Date(dateRange.value[0]).toISOString()
      endTime = new Date(dateRange.value[1]).toISOString()
    }
    const resp = await listOperationLogs({
      page: paginationReactive.page,
      pageSize: paginationReactive.pageSize,
      operatorId: filterOperator.value ? Number(filterOperator.value) : undefined,
      resourceType: filterResourceType.value || undefined,
      operationType: filterOperationType.value || undefined,
      startTime,
      endTime
    })
    logs.value = resp.items
    paginationReactive.itemCount = resp.total
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.table-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
}
</style>
