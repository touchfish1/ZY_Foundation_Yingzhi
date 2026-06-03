<template>
  <div class="edit-page">
    <div class="top-bar">
      <div class="top-bar-left">
        <n-button text @click="router.push('/cms/pages')">
          ← 返回页面管理
        </n-button>
        <h2 class="page-title">编辑页面 {{ detail?.slug }}</h2>
      </div>
      <div class="top-bar-center">
        <LocaleSwitcher
          :translations="detail?.translations ?? []"
          :current-locale="locale"
          @select="onLocaleChange"
        />
      </div>
      <div class="top-bar-right">
        <n-button @click="save" :loading="saving">保存草稿</n-button>
        <n-button type="primary" @click="publish" :loading="publishing">发布</n-button>
      </div>
    </div>

    <div v-if="detail" class="edit-body">
      <div class="edit-left">
        <n-card title="区块编辑器" size="small">
          <BlockFormEditor
            v-model:blocks="blocks"
            :definitions="blockDefinitions"
          />
        </n-card>
      </div>

      <div class="edit-right">
        <n-card title="基本信息" size="small">
          <n-form label-placement="top">
            <n-form-item label="标题">
              <n-input v-model:value="title" placeholder="页面标题" />
            </n-form-item>
            <n-form-item label="SEO 标题">
              <n-input v-model:value="seoTitle" placeholder="SEO 标题" />
            </n-form-item>
            <n-form-item label="SEO 描述">
              <n-input v-model:value="seoDescription" type="textarea" placeholder="SEO 描述" />
            </n-form-item>
          </n-form>
        </n-card>

        <n-card title="页面信息" size="small" class="info-card">
          <div class="info-row"><span class="info-label">状态</span><span>{{ detail.status }}</span></div>
          <div class="info-row"><span class="info-label">默认语言</span><span>{{ detail.defaultLocale }}</span></div>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NInput, useMessage } from 'naive-ui'
import { getPage, publishPage, saveDraft } from '../../api/cms'
import { getBlockDefinitions, getDraftContent } from '../../api/block'
import LocaleSwitcher from '../../components/page-editor/LocaleSwitcher.vue'
import BlockFormEditor from '../../components/page-editor/BlockFormEditor.vue'
import type { BlockDefinition, PageBlock } from '../../types/block'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const pageId = Number(route.params.id)

const detail = ref<{
  id: number
  slug: string
  defaultLocale: string
  status: string
  translations: Array<{
    locale: string
    title: string
    seoTitle?: string
    seoDescription?: string
    seoKeywords?: string
    draftVersionId?: number
    publishedVersionId?: number
    content?: unknown
    status: string
  }>
} | null>(null)

const locale = ref('zh-CN')
const title = ref('')
const seoTitle = ref('')
const seoDescription = ref('')
const blockDefinitions = ref<BlockDefinition[]>([])
const blocks = ref<PageBlock[]>([])
const saving = ref(false)
const publishing = ref(false)

async function loadPage() {
  detail.value = await getPage(pageId)
}

async function loadDefinitions() {
  try {
    blockDefinitions.value = await getBlockDefinitions()
  } catch {
    blockDefinitions.value = []
  }
}

async function loadDraft(localeCode: string) {
  try {
    const draft = await getDraftContent(pageId, localeCode)
    blocks.value = draft.blocks || []
  } catch {
    blocks.value = []
  }
}

async function load() {
  await loadPage()
  await loadDefinitions()
  const translation = detail.value!.translations[0]
  if (translation) {
    locale.value = translation.locale
    title.value = translation.title
    seoTitle.value = translation.seoTitle || title.value
    seoDescription.value = translation.seoDescription || ''
    await loadDraft(locale.value)
  }
}

async function onLocaleChange(newLocale: string) {
  locale.value = newLocale
  const translation = detail.value!.translations.find(t => t.locale === newLocale)
  if (translation) {
    title.value = translation.title
    seoTitle.value = translation.seoTitle || title.value
    seoDescription.value = translation.seoDescription || ''
  }
  await loadDraft(newLocale)
}

async function save() {
  saving.value = true
  try {
    await saveDraft(pageId, locale.value, {
      title: title.value,
      seoTitle: seoTitle.value,
      seoDescription: seoDescription.value,
      seoKeywords: 'API,套餐,CMS',
      content: {
        layout: 'default',
        blocks: blocks.value
      },
      remark: 'admin draft'
    })
    message.success('草稿已保存')
    await loadPage()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function publish() {
  publishing.value = true
  try {
    const result = await publishPage(pageId, locale.value, 'admin publish')
    const trans = result.translations.find(t => t.locale === locale.value)
    const versionId = trans?.publishedVersionId
    if (versionId) {
      message.success(`页面已发布 (版本 #${versionId})`)
    } else {
      message.success('页面已发布')
    }
    await loadPage()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '发布失败')
  } finally {
    publishing.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.edit-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
  flex-wrap: wrap;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.top-bar-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.top-bar-right {
  display: flex;
  gap: 8px;
}

.edit-body {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
  flex: 1;
  min-height: 0;
}

.edit-left {
  min-height: 0;
}

.edit-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  margin-top: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 13px;
}

.info-label {
  color: #6b7280;
}
</style>
