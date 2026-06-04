<template>
  <div class="common-table-wrapper" ref="wrapperRef">
    <n-data-table
      :columns="columns"
      :data="data"
      :loading="loading"
      bordered
      :single-line="false"
      :striped="true"
      size="small"
      :max-height="bodyMaxHeight"
      :style="tableStyle"
      :pagination="paginationConfig"
      :scroll-x="scrollX"
      :row-class-name="rowClassName"
    >
      <template #empty>
        <slot name="empty">
          <n-empty description="暂无数据" />
        </slot>
      </template>
    </n-data-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { NDataTable, NEmpty } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

const props = withDefaults(defineProps<{
  columns: DataTableColumns<any>
  data: any[]
  loading?: boolean
  scrollX?: number
  rowClassName?: string | ((row: any, index: number) => string)
  pagination?: {
    page?: number
    pageSize?: number
    showSizePicker?: boolean
    pageSizes?: number[]
    onChange?: (page: number) => void
    onUpdatePageSize?: (size: number) => void
    [key: string]: any
  }
}>(), {
  loading: false,
  scrollX: undefined,
  rowClassName: undefined,
  pagination: undefined,
})

const wrapperRef = ref<HTMLElement | null>(null)
const bodyMaxHeight = ref(600)
const componentMinHeight = ref(680)

function recalcHeight() {
  if (!wrapperRef.value || typeof window === 'undefined') return
  const rect = wrapperRef.value.getBoundingClientRect()
  const available = window.innerHeight - rect.top
  // max-height constrains only the table body, header(40) + pagination(40) are outside
  bodyMaxHeight.value = Math.max(300, Math.round(available - 140))
  // min-height constrains the entire component
  componentMinHeight.value = Math.max(300, Math.round(available - 60))
}

onMounted(() => {
  recalcHeight()
  window.addEventListener('resize', recalcHeight)
})

onUnmounted(() => {
  window.removeEventListener('resize', recalcHeight)
})

const tableStyle = computed(() => ({
  minHeight: `${componentMinHeight.value}px`,
}))

const paginationConfig = computed(() => {
  if (!props.pagination) return undefined
  return {
    page: 1,
    pageSize: 20,
    showSizePicker: true,
    pageSizes: [10, 20, 50, 100],
    ...props.pagination,
  }
})
</script>

<style scoped>
.common-table-wrapper {
  width: 100%;
}

.common-table-wrapper :deep(.n-data-table-tr) {
  transition: background-color 0.15s ease;
}

.common-table-wrapper :deep(.n-data-table-tr:hover) {
  background-color: rgba(99, 102, 241, 0.04) !important;
}

/* Improve pagination appearance */
.common-table-wrapper :deep(.n-data-table-pagination) {
  padding-top: 12px;
  border-top: 1px solid rgba(128, 128, 128, 0.1);
  margin-top: 0;
}
</style>
