<template>
  <div class="page-head">
    <div>
      <n-button text @click="router.push('/cms/pages')">← 返回页面管理</n-button>
      <h2>版本历史 — {{ page?.slug }}</h2>
      <p v-if="page">{{ page.title }} ({{ locale }})</p>
    </div>
  </div>

  <n-card>
    <n-empty v-if="!loading && !versions.length" description="暂无版本记录" />
    <n-data-table v-else :columns="columns" :data="versions" :loading="loading" />
  </n-card>

  <n-modal v-model:show="showPreview" preset="card" title="版本预览" style="width: min(720px, 92vw)">
    <pre class="preview-json">{{ previewContent }}</pre>
  </n-modal>

  <n-modal v-model:show="showRollback" preset="card" title="回滚版本" class="modal-card">
    <n-form>
      <n-form-item label="备注">
        <n-input v-model:value="rollbackRemark" type="textarea" placeholder="回滚原因" />
      </n-form-item>
      <n-space>
        <n-button @click="showRollback = false">取消</n-button>
        <n-button type="primary" :loading="rollingBack" @click="confirmRollback">确认回滚</n-button>
      </n-space>
    </n-form>
  </n-modal>
</template>

<script setup lang="ts">
// 版本历史：查看指定页面的所有历史版本，支持预览和回滚
import { h, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NDataTable, NEmpty, NForm, NFormItem, NInput, NModal, NSpace, useMessage } from 'naive-ui'
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
const previewContent = ref('')

const showRollback = ref(false)
const rollbackVersionId = ref<number>(0)
const rollbackRemark = ref('')
const rollingBack = ref(false)

const columns: DataTableColumns<any> = [
  { title: '版本号', key: 'versionNo', width: 100 },
  { title: '创建时间', key: 'createdAt', width: 180 },
  { title: '备注', key: 'remark' },
  {
    title: '操作', key: 'actions', width: 200,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => openPreview(row.id) }, { default: () => '预览' }),
          h(NButton, { size: 'small', type: 'warning', onClick: () => openRollback(row.id) }, { default: () => '回滚' })
        ]
      })
    }
  }
]

// 加载页面信息和版本历史列表
async function load() {
  console.log('[CmsVersions] load', { pageId, locale: locale.value })
  loading.value = true
  try {
    page.value = await getPage(pageId)
    versions.value = await getVersions(pageId, locale.value)
    console.log('[CmsVersions] loaded', versions.value.length, 'versions')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

// 打开版本预览模态框，加载并显示版本内容 JSON
async function openPreview(versionId: number) {
  console.log('[CmsVersions] openPreview', { versionId })
  try {
    const content = await getPreviewContent(pageId, locale.value, versionId)
    previewContent.value = JSON.stringify(content, null, 2)
    showPreview.value = true
  } catch (error) {
    message.error(error instanceof Error ? error.message : '预览加载失败')
  }
}

// 打开回滚确认模态框
function openRollback(versionId: number) {
  console.log('[CmsVersions] openRollback', { versionId })
  rollbackVersionId.value = versionId
  rollbackRemark.value = ''
  showRollback.value = true
}

// 确认回滚：调用 API 后跳转回编辑页
async function confirmRollback() {
  console.log('[CmsVersions] confirmRollback', { versionId: rollbackVersionId.value })
  rollingBack.value = true
  try {
    await rollbackVersion(pageId, locale.value, rollbackVersionId.value, rollbackRemark.value || 'rollback')
    message.success('回滚成功')
    showRollback.value = false
    router.push(`/cms/pages/${pageId}/edit`)
  } catch (error) {
    message.error(error instanceof Error ? error.message : '回滚失败')
  } finally {
    rollingBack.value = false
  }
}

onMounted(() => { console.log('[CmsVersions] mounted', { pageId }); load() })
</script>

<style scoped>
.page-head {
  margin-bottom: 20px;
}
.page-head h2 {
  margin: 8px 0 4px;
}
.page-head p {
  margin: 0;
  color: #64748b;
}
.preview-json {
  background: #1e293b;
  color: #e2e8f0;
  padding: 16px;
  border-radius: 8px;
  font-size: 13px;
  overflow: auto;
  max-height: 60vh;
}
.modal-card {
  width: min(520px, 92vw);
}
</style>
