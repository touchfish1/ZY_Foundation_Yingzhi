<template>
  <div class="page-head">
    <div>
      <h2>编辑页面 {{ detail?.slug }}</h2>
      <p>当前先提供 JSON 编辑器，后续替换为区块表单编辑器。</p>
    </div>
    <div class="actions">
      <n-button @click="save" :loading="saving">保存草稿</n-button>
      <n-button type="primary" @click="publish" :loading="publishing">发布</n-button>
    </div>
  </div>

  <n-card v-if="detail">
    <n-form>
      <n-form-item label="语言">
        <n-input v-model:value="locale" />
      </n-form-item>
      <n-form-item label="标题">
        <n-input v-model:value="title" />
      </n-form-item>
      <n-form-item label="SEO 标题">
        <n-input v-model:value="seoTitle" />
      </n-form-item>
      <n-form-item label="SEO 描述">
        <n-input v-model:value="seoDescription" type="textarea" />
      </n-form-item>
      <n-form-item label="页面 JSON">
        <n-input v-model:value="content" type="textarea" :autosize="{ minRows: 14 }" />
      </n-form-item>
    </n-form>
  </n-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NInput, useMessage } from 'naive-ui'
import { getPage, publishPage, saveDraft, type CmsPageDetail } from '../../api/cms'

const route = useRoute()
const message = useMessage()
const pageId = Number(route.params.id)
const detail = ref<CmsPageDetail | null>(null)
const locale = ref('zh-CN')
const title = ref('套餐价格')
const seoTitle = ref('套餐价格 - ZHANGYUAN')
const seoDescription = ref('选择适合你的 API 套餐')
const content = ref(JSON.stringify({
  layout: 'default',
  blocks: [
    { id: 'hero_001', type: 'hero', props: { title: '选择适合你的套餐', subtitle: '稳定、高性能的 API 服务' } },
    { id: 'pricing_001', type: 'pricing', props: { planGroupCode: 'api_plans', defaultBillingCycle: 'monthly' } }
  ]
}, null, 2))
const saving = ref(false)
const publishing = ref(false)

async function load() {
  detail.value = await getPage(pageId)
  const translation = detail.value.translations[0]
  if (translation) {
    locale.value = translation.locale
    title.value = translation.title
    seoTitle.value = translation.seoTitle || title.value
    seoDescription.value = translation.seoDescription || ''
  }
}

async function save() {
  saving.value = true
  try {
    await saveDraft(pageId, locale.value, {
      title: title.value,
      seoTitle: seoTitle.value,
      seoDescription: seoDescription.value,
      seoKeywords: 'API,套餐,CMS',
      content: JSON.parse(content.value),
      remark: 'admin draft'
    })
    message.success('草稿已保存')
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function publish() {
  publishing.value = true
  try {
    await publishPage(pageId, locale.value, 'admin publish')
    message.success('页面已发布')
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '发布失败')
  } finally {
    publishing.value = false
  }
}

onMounted(load)
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
  margin: 0;
  color: #64748b;
}

.actions {
  display: flex;
  gap: 12px;
}
</style>
