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

    <div v-if="selectedDiff.length === 2" style="margin-bottom: 12px;">
      <n-button size="small" type="primary" @click="openDiff">对比选中版本</n-button>
      <n-button size="small" quaternary style="margin-left: 8px;" @click="selectedDiff = []">取消选择</n-button>
      <span style="margin-left: 8px; font-size: 13px; color: #6b7280;">已选 2 个版本</span>
    </div>

    <n-card :bordered="false" class="table-card">
      <n-empty v-if="!loading && !versions.length" description="暂无版本记录" />
      <CommonTable
        v-else
        :columns="columns"
        :data="versions"
        :loading="loading"
        :pagination="paginationReactive"
      />
    </n-card>

    <n-modal v-model:show="showPreview" preset="card" title="版本预览" style="width: min(720px, 92vw)">
      <pre class="preview-json">{{ previewContent }}</pre>
    </n-modal>

    <n-modal v-model:show="showDiff" preset="card" title="版本对比" style="width: min(90vw, 1100px)" :mask-closable="false">
      <div v-if="diffLoading" style="text-align:center;padding:40px"><n-spin size="large" /></div>
      <div v-else class="diff-container">
        <div class="diff-panel">
          <h4 style="margin:0 0 8px;">版本 {{ diffLeftVersion }}</h4>
          <pre class="diff-json">{{ diffLeft }}</pre>
        </div>
        <div class="diff-divider" />
        <div class="diff-panel">
          <h4 style="margin:0 0 8px;">版本 {{ diffRightVersion }}</h4>
          <pre class="diff-json">{{ diffRight }}</pre>
        </div>
      </div>
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
import { computed, h, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NEmpty, NForm, NFormItem, NIcon, NInput, NModal, NSpace, NSpin, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { getPage, type CmsPageDetail } from '../../api/cms'
import { getVersions, getPreviewContent, rollbackVersion, type VersionInfo } from '../../api/block'
import CommonTable from '../../components/CommonTable.vue'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const pageId = Number(route.params.id)
const locale = ref((route.query.locale as string) || 'zh-CN')

const page = ref<CmsPageDetail | null>(null)
const versions = ref<VersionInfo[]>([])
const loading = ref(false)
const showPreview = ref(false)
const showRollback = ref(false)
const previewContent = ref('')
const rollbackVersionId = ref<number | null>(null)
const rollbackRemark = ref('')
const rollingBack = ref(false)

const paginationReactive = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    paginationReactive.page = page
    loadVersions()
  },
  onUpdatePageSize: (size: number) => {
    paginationReactive.pageSize = size
    paginationReactive.page = 1
    loadVersions()
  }
})

const selectedDiff = ref<number[]>([])
const showDiff = ref(false)
const diffLoading = ref(false)
const diffLeft = ref('')
const diffRight = ref('')
const diffLeftVersion = ref(0)
const diffRightVersion = ref(0)

const publishedVersionId = computed(() => {
  if (!page.value) return null
  const trans = page.value.translations.find(t => t.locale === locale.value)
  return trans?.publishedVersionId ?? null
})

const columns: DataTableColumns<VersionInfo> = [
  {
    title: '选择', key: 'select', width: 60,
    render(row) {
      const checked = selectedDiff.value.includes(row.versionId)
      const disabled = !checked && selectedDiff.value.length >= 2
      return h('input', {
        type: 'checkbox',
        checked,
        disabled,
        style: 'cursor:pointer;width:16px;height:16px',
        onChange: () => toggleSelect(row.versionId)
      })
    }
  },
  { title: '版本号', key: 'version', width: 100 },
  {
    title: '状态', key: 'status', width: 100,
    render(row) {
      const isPublished = publishedVersionId.value === row.versionId
      if (isPublished) {
        return h(NTag, { type: 'success', size: 'small', bordered: false }, { default: () => '已发布' })
      }
      return h(NTag, { type: 'default', size: 'small', bordered: false }, { default: () => '历史版本' })
    }
  },
  { title: '创建时间', key: 'publishedAt', width: 180 },
  {
    title: '操作', key: 'actions', width: 180,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, onClick: () => openPreview(row.versionId) }, { default: () => '预览' }),
          h(NButton, { size: 'small', quaternary: true, type: 'warning', onClick: () => openRollback(row.versionId) }, { default: () => '回滚' })
        ]
      })
    }
  }
]

async function load() {
  loading.value = true
  try {
    const [p, v] = await Promise.all([
      getPage(pageId),
      getVersions(pageId, locale.value, paginationReactive.page, paginationReactive.pageSize)
    ])
    page.value = p
    versions.value = v.items
    paginationReactive.itemCount = v.total
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadVersions() {
  loading.value = true
  try {
    const v = await getVersions(pageId, locale.value, paginationReactive.page, paginationReactive.pageSize)
    versions.value = v.items
    paginationReactive.itemCount = v.total
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

function toggleSelect(versionId: number) {
  const idx = selectedDiff.value.indexOf(versionId)
  if (idx >= 0) {
    selectedDiff.value.splice(idx, 1)
  } else if (selectedDiff.value.length < 2) {
    selectedDiff.value.push(versionId)
  }
}

async function openDiff() {
  if (selectedDiff.value.length !== 2) return
  diffLoading.value = true
  showDiff.value = true
  try {
    const [a, b] = selectedDiff.value
    const [left, right] = await Promise.all([
      getPreviewContent(pageId, locale.value, a),
      getPreviewContent(pageId, locale.value, b)
    ])
    diffLeftVersion.value = versions.value.find(v => v.versionId === a)?.version ?? a
    diffRightVersion.value = versions.value.find(v => v.versionId === b)?.version ?? b
    diffLeft.value = JSON.stringify(left, null, 2)
    diffRight.value = JSON.stringify(right, null, 2)
  } catch (error) {
    message.error('加载版本内容失败')
    showDiff.value = false
  } finally {
    diffLoading.value = false
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

.diff-container {
  display: flex;
  gap: 0;
  min-height: 400px;
}

.diff-panel {
  flex: 1;
  min-width: 0;
}

.diff-divider {
  width: 1px;
  background: #e5e7eb;
  margin: 0 12px;
}

.diff-json {
  background: #1e293b;
  color: #e2e8f0;
  padding: 12px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.5;
  overflow: auto;
  max-height: 60vh;
  white-space: pre;
}
</style>
