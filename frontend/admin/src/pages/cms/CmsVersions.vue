<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <n-button text @click="router.push('/cms/pages')">
            <template #icon>
              <n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg></n-icon>
            </template>
            返回页面管理
          </n-button>
          <h2>版本历史 — {{ page?.slug }}</h2>
          <p v-if="page">{{ page.translations.find(t => t.locale === locale)?.title || page.slug }} ({{ locale }})</p>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card">
      <n-empty v-if="!loading && !versions.length" description="暂无版本记录" />
      <n-data-table v-else :columns="columns" :data="versions" :loading="loading" :bordered="false" size="small" />
    </n-card>

    <n-modal v-model:show="showPreview" preset="card" title="版本预览" style="width: min(720px, 92vw)">
      <pre class="preview-json">{{ previewContent }}</pre>
    </n-modal>

    <n-modal v-model:show="showRollback" preset="card" title="回滚版本" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="备注">
          <n-input v-model:value="rollbackRemark" type="textarea" placeholder="回滚原因" />
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showRollback = false">取消</n-button>
          <n-button type="primary" :loading="rollingBack" @click="confirmRollback">确认回滚</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NDataTable, NEmpty, NForm, NFormItem, NIcon, NInput, NModal, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { getPage, type CmsPageDetail } from '../../api/cms'
import { getVersions, getPreviewContent, rollbackVersion } from '../../api/block'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const pageId = Number(route.params.id)
const locale = ref((route.query.locale as string) || 'zh-CN')

const page = ref<CmsPageDetail | null>(null)
const versions = ref<any[]>([])
const loading = ref(false)
const showPreview = ref(false)
const showRollback = ref(false)
const previewContent = ref('')
const rollbackVersionId = ref<number | null>(null)
const rollbackRemark = ref('')
const rollingBack = ref(false)

const publishedVersionId = computed(() => {
  if (!page.value) return null
  const trans = page.value.translations.find(t => t.locale === locale.value)
  return trans?.publishedVersionId ?? null
})

const columns: DataTableColumns<any> = [
  { title: '版本号', key: 'versionNo', width: 100 },
  {
    title: '状态', key: 'status', width: 100,
    render(row) {
      const isPublished = publishedVersionId.value === row.id
      if (isPublished) {
        return h(NTag, { type: 'success', size: 'small', bordered: false }, { default: () => '已发布' })
      }
      return h(NTag, { type: 'default', size: 'small', bordered: false }, { default: () => '历史版本' })
    }
  },
  { title: '创建时间', key: 'createdAt', width: 180 },
  {
    title: '操作', key: 'actions', width: 180,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, onClick: () => openPreview(row.id) }, { default: () => '预览' }),
          h(NButton, { size: 'small', quaternary: true, type: 'warning', onClick: () => openRollback(row.id) }, { default: () => '回滚' })
        ]
      })
    }
  }
]

async function load() {
  loading.value = true
  try {
    const [p, v] = await Promise.all([getPage(pageId), getVersions(pageId, locale.value)])
    page.value = p
    versions.value = v
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function openPreview(versionId: number) {
  try {
    previewContent.value = JSON.stringify(await getPreviewContent(pageId, locale.value, versionId), null, 2)
    showPreview.value = true
  } catch (error) {
    message.error(error instanceof Error ? error.message : '预览加载失败')
  }
}

function openRollback(versionId: number) {
  rollbackVersionId.value = versionId
  rollbackRemark.value = ''
  showRollback.value = true
}

async function confirmRollback() {
  if (!rollbackVersionId.value) return
  rollingBack.value = true
  try {
    await rollbackVersion(pageId, locale.value, rollbackVersionId.value, rollbackRemark.value)
    message.success('回滚成功')
    showRollback.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '回滚失败')
  } finally {
    rollingBack.value = false
  }
}

onMounted(() => { load() })
</script>

<style scoped>
.preview-json {
  background: #1e293b;
  color: #e2e8f0;
  padding: 16px;
  border-radius: 8px;
  font-size: 13px;
  overflow: auto;
  max-height: 60vh;
}
</style>
