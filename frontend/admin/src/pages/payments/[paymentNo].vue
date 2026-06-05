<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>支付详情</h2>
          <p>支付单号: {{ paymentNo }}</p>
        </div>
        <div class="page-head-actions">
          <n-button size="small" @click="$router.back()">返回</n-button>
        </div>
      </div>
    </div>

    <n-spin :show="loading">
      <n-card title="支付信息" :bordered="false" class="detail-card">
        <n-descriptions label-placement="left" bordered :column="2">
          <n-descriptions-item label="支付单号">
            {{ detail?.paymentNo ?? '-' }}
          </n-descriptions-item>
          <n-descriptions-item label="订单号">
            {{ detail?.orderNo ?? '-' }}
          </n-descriptions-item>
          <n-descriptions-item label="支付渠道">
            {{ detail?.channel ?? '-' }}
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
          <n-descriptions-item label="完成时间">
            {{ formatDate(detail?.paidAt) }}
          </n-descriptions-item>
        </n-descriptions>
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  NButton, NCard, NDescriptions, NDescriptionsItem,
  NNumberAnimation, NSpin, NTag, useMessage
} from 'naive-ui'
import { getPayment } from '../../api/payment'
import type { PaymentItem } from '../../api/payment'
import { formatDate } from '../../utils/format'

const route = useRoute()
const message = useMessage()
const paymentNo = route.params.paymentNo as string
const loading = ref(false)
const detail = ref<any>(null)

function statusType(status: string) {
  if (status === 'success' || status === 'paid' || status === 'SUCCESS') return 'success'
  if (status === 'pending' || status === 'PENDING') return 'warning'
  if (status === 'failed' || status === 'FAILED' || status === 'cancelled') return 'error'
  return 'default'
}

async function load() {
  loading.value = true
  try {
    detail.value = await getPayment(paymentNo)
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
