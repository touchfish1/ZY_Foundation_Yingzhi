<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>订单详情</h2>
          <p>订单号: {{ orderNo }}</p>
        </div>
        <div class="page-head-actions">
          <n-button size="small" @click="$router.back()">返回</n-button>
        </div>
      </div>
    </div>

    <n-spin :show="loading">
      <n-grid :cols="2" :x-gap="16" :y-gap="16">
        <n-grid-item :span="2">
      <n-card title="基本信息" :bordered="false" class="detail-card">
        <n-descriptions label-placement="left" bordered :column="2">
          <n-descriptions-item label="订单号">
            {{ detail?.orderNo ?? '-' }}
          </n-descriptions-item>
          <n-descriptions-item label="金额">
            <span style="color: #f59e0b; font-weight: 600;">
              <n-number-animation v-if="detail?.amount" :from="0" :to="+detail.amount" show-separator />
              <span v-else>-</span>
            </span>
          </n-descriptions-item>
          <n-descriptions-item label="币种">
            {{ detail?.currency ?? '-' }}
          </n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag v-if="detail?.status" :type="statusType(detail.status)" size="small" bordered>
              {{ detail.status }}
            </n-tag>
            <span v-else>-</span>
          </n-descriptions-item>
          <n-descriptions-item label="创建时间">
            {{ formatDate(detail?.createdAt) }}
          </n-descriptions-item>
          <n-descriptions-item label="支付时间">
            {{ formatDate(detail?.paidAt) }}
          </n-descriptions-item>
        </n-descriptions>
      </n-card>
        </n-grid-item>

        <n-grid-item :span="2" v-if="detail?.planName">
          <n-card title="套餐信息" :bordered="false" class="detail-card">
            <n-descriptions label-placement="left" bordered :column="2">
              <n-descriptions-item label="套餐名称">
                {{ detail.planName }}
              </n-descriptions-item>
              <n-descriptions-item label="套餐编码">
                {{ detail.planCode ?? '-' }}
              </n-descriptions-item>
              <n-descriptions-item label="计费周期">
                {{ detail.billingCycle ?? '-' }}
              </n-descriptions-item>
            </n-descriptions>
          </n-card>
        </n-grid-item>

        <n-grid-item :span="2" v-if="detail?.userId">
          <n-card title="用户信息" :bordered="false" class="detail-card">
            <n-descriptions label-placement="left" bordered :column="2">
              <n-descriptions-item label="用户ID">
                {{ detail.userId }}
              </n-descriptions-item>
            </n-descriptions>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  NButton, NCard, NDescriptions, NDescriptionsItem, NGrid, NGridItem,
  NNumberAnimation, NSpin, NTag, useMessage
} from 'naive-ui'
import { getOrder } from '../../api/order'
import type { OrderItem } from '../../api/order'
import { formatDate } from '../../utils/format'

const route = useRoute()
const message = useMessage()
const orderNo = route.params.orderNo as string
const loading = ref(false)
const detail = ref<any>(null)

function statusType(status: string) {
  if (status === 'paid' || status === 'success' || status === 'completed') return 'success'
  if (status === 'pending') return 'warning'
  if (status === 'cancelled' || status === 'failed') return 'error'
  return 'default'
}

async function load() {
  loading.value = true
  try {
    const data = await getOrder(orderNo)
    // Parse snapshot_json for plan info
    if (data?.snapshotJson) {
      try {
        const snapshot = JSON.parse(data.snapshotJson)
        data.planName = snapshot.plan?.name ?? '-'
        data.planCode = snapshot.plan?.code ?? '-'
        data.billingCycle = snapshot.price?.billingCycle ?? '-'
      } catch { /* invalid JSON, ignore */ }
    }
    detail.value = data
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { load() })
</script>

<style scoped>
.detail-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
}
</style>
