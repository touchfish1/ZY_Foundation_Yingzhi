<template>
  <div v-if="!detail" style="display:flex;justify-content:center;padding:80px 0">
    <n-spin size="large" />
  </div>

  <div v-else class="edit-page">
    <div class="top-bar">
      <div class="top-bar-left">
        <n-button text @click="router.push('/cms/pages')">
          <template #icon>
            <n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg></n-icon>
          </template>
          返回
        </n-button>
        <h2 class="page-title">编辑 {{ detail.slug }}</h2>
      </div>
      <div class="top-bar-center">
        <locale-switcher
          :translations="detail.translations ?? []"
          :current-locale="locale"
          :available-locales="['zh-CN','en-US','ja-JP']"
          @select="onLocaleChange"
          @add-locale="onAddLocale"
        />
      </div>
      <div class="top-bar-right">
        <n-button quaternary size="small" @click="openPreview">
          <template #icon>
            <n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg></n-icon>
          </template>
          预览
        </n-button>
        <n-button quaternary size="small" @click="router.push(`/cms/pages/${pageId}/versions?locale=${locale}`)">
          <template #icon>
            <n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></n-icon>
          </template>
          版本历史
        </n-button>
        <n-button @click="save" :loading="saving">保存草稿</n-button>
        <n-button type="primary" @click="publish" :loading="publishing">发布</n-button>
      </div>
    </div>

    <div class="edit-body">
      <div class="edit-left">
        <n-card title="区块编辑器" size="small" :bordered="false" class="editor-card">
          <BlockFormEditor
            v-model:blocks="blocks"
            :definitions="blockDefinitions"
          />
        </n-card>
      </div>

      <div class="edit-right">
        <n-card title="基本信息" size="small" :bordered="false" class="editor-card">
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
            <n-form-item label="SEO 关键词">
              <n-input v-model:value="seoKeywords" placeholder="关键词以逗号分隔" />
            </n-form-item>
          </n-form>
        </n-card>

        <n-modal v-model:show="showPublishModal" preset="card" title="发布页面" class="modal-card" :mask-closable="false">
          <n-form label-placement="top">
            <n-form-item label="发布备注">
              <n-input v-model:value="publishRemark" type="textarea" placeholder="本次发布的说明" />
            </n-form-item>
            <n-space justify="end" style="margin-top: 8px;">
              <n-button @click="showPublishModal = false">取消</n-button>
              <n-button type="primary" :loading="publishing" @click="confirmPublish">确认发布</n-button>
            </n-space>
          </n-form>
        </n-modal>

        <n-card title="区块预览" size="small" :bordered="false" class="editor-card">
          <BlockPreviewPane
            :blocks="blocks"
            :definitions="blockDefinitions"
          />
        </n-card>

        <n-card title="页面信息" size="small" :bordered="false" class="info-card editor-card">
          <div class="info-row"><span class="info-label">状态</span><n-tag :type="detail.status === 'enabled' ? 'success' : 'default'" size="small" :bordered="false">{{ detail.status }}</n-tag></div>
          <div class="info-row"><span class="info-label">默认语言</span><span>{{ detail.defaultLocale }}</span></div>
          <div class="info-row"><span class="info-label">页面类型</span><span>{{ pageTypeLabel(detail.pageType) }}</span></div>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NIcon, NInput, NSpin, NTag, useMessage } from 'naive-ui'
import { getPage, publishPage, saveDraft } from '../../api/cms'
import { getBlockDefinitions, getDraftContent } from '../../api/block'
import LocaleSwitcher from '../../components/page-editor/LocaleSwitcher.vue'
import BlockFormEditor from '../../components/page-editor/BlockFormEditor.vue'
import BlockPreviewPane from '../../components/page-editor/BlockPreviewPane.vue'
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
  pageType: string
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
const seoKeywords = ref('')
const blockDefinitions = ref<BlockDefinition[]>([])
const blocks = ref<PageBlock[]>([])
const saving = ref(false)
const publishing = ref(false)
const showPublishModal = ref(false)
const publishRemark = ref('')
const wasSaved = ref(true) // tracks whether current state matches last save

const previewBaseUrl = import.meta.env.DEV
  ? 'http://localhost:3000'
  : ''

function openPreview() {
  const slug = detail.value?.slug || '/'
  const url = `${previewBaseUrl}${slug}${locale.value === 'zh-CN' ? '' : `?locale=${locale.value}`}`
  window.open(url, '_blank')
}

const pageTypeLabels: Record<string, string> = {
  custom: '自定义页面',
  blog: '博客文章',
  doc: '文档文章'
}

function pageTypeLabel(type: string): string {
  return pageTypeLabels[type] || type
}

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
  // Use locale from query if provided (e.g., coming back from versions page), else first translation
  const queryLocale = route.query.locale as string | undefined
  const translation = queryLocale
    ? detail.value!.translations.find(t => t.locale === queryLocale)
    : detail.value!.translations[0]
  if (translation) {
    locale.value = translation.locale
    title.value = translation.title
    seoTitle.value = translation.seoTitle || title.value
    seoDescription.value = translation.seoDescription || ''
    seoKeywords.value = translation.seoKeywords || ''
    await loadDraft(locale.value)
  } else if (queryLocale) {
    // Translation doesn't exist yet — user wants to create it
    locale.value = queryLocale
    title.value = ''
    seoTitle.value = ''
    seoDescription.value = ''
    seoKeywords.value = ''
    blocks.value = []
  }
}

function onAddLocale(newLocale: string) {
  // Switch to the new locale — blocks start empty, first saveDraft will create the translation
  locale.value = newLocale
  title.value = ''
  seoTitle.value = ''
  seoDescription.value = ''
  seoKeywords.value = ''
  blocks.value = []
  message.info(`已切换到 ${newLocale}，保存草稿后将创建该语言翻译`)
}

// Track unsaved changes
watch([title, seoTitle, seoDescription, seoKeywords, blocks], () => {
  wasSaved.value = false
}, { deep: true })

onBeforeRouteLeave((_to, _from, next) => {
  if (wasSaved.value) return next()
  const ok = window.confirm('有未保存的更改，确定要离开吗？')
  if (ok) next()
})

// Browser beforeunload (tab close / refresh)
onMounted(() => {
  window.addEventListener('beforeunload', (e) => {
    if (!wasSaved.value) {
      e.preventDefault()
      e.returnValue = ''
    }
  })
})

// Mark as saved after save or publish
function markSaved() {
  wasSaved.value = true
}

async function onLocaleChange(newLocale: string) {
  locale.value = newLocale
  const translation = detail.value!.translations.find(t => t.locale === newLocale)
  if (translation) {
    title.value = translation.title
    seoTitle.value = translation.seoTitle || title.value
    seoDescription.value = translation.seoDescription || ''
    seoKeywords.value = translation.seoKeywords || ''
    await loadDraft(newLocale)
  } else {
    // New locale — create via first saveDraft
    title.value = ''
    seoTitle.value = ''
    seoDescription.value = ''
    seoKeywords.value = ''
    blocks.value = []
  }
}

async function save() {
  saving.value = true
  try {
    await saveDraft(pageId, locale.value, {
      title: title.value, seoTitle: seoTitle.value, seoDescription: seoDescription.value, seoKeywords: seoKeywords.value || '',
      content: { layout: 'default', blocks: blocks.value },
      remark: 'admin draft'
    })
    message.success('草稿已保存')
    markSaved()
    await loadPage()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function publish() {
  publishRemark.value = ''
  showPublishModal.value = true
}

async function confirmPublish() {
  publishing.value = true
  try {
    const result = await publishPage(pageId, locale.value, publishRemark.value || 'admin publish')
    showPublishModal.value = false
    const trans = result.translations.find(t => t.locale === locale.value)
    const versionId = trans?.publishedVersionId
    message.success(versionId ? `页面已发布 (版本 #${versionId})` : '页面已发布')
    markSaved()
    await loadPage()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '发布失败')
  } finally {
    publishing.value = false
  }
}

onMounted(() => { load() })
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
  gap: 8px;
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
  align-items: center;
}

.edit-body {
  display: grid;
  grid-template-columns: 1fr 340px;
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

.editor-card {
  border-radius: 12px;
}

.info-card {
  margin-top: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
}

.info-row + .info-row {
  border-top: 1px solid var(--n-border-color);
}

.info-label {
  color: #6b7280;
}
</style>
