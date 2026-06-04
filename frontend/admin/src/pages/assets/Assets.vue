<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>媒体资源</h2>
          <p>管理上传的图片和文件。</p>
        </div>
        <div class="page-head-actions">
          <n-button quaternary @click="load" :loading="loading">
            <template #icon><n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></n-icon></template>
          </n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="true" class="table-card" style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 20px;">
      <n-upload
        :default-upload="false"
        @change="handleUpload"
        accept="image/*,.pdf,.doc,.docx"
      >
        <n-upload-dragger>
          <div style="padding: 24px 0;">
            <n-icon size="40" color="#6366f1">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            </n-icon>
            <p style="margin: 8px 0 0; font-weight: 600;">点击或拖拽文件到此区域上传</p>
            <p style="margin: 4px 0 0; font-size: 12px; color: #94a3b8;">支持图片、PDF、DOC 格式，单文件最大 50MB</p>
          </div>
        </n-upload-dragger>
      </n-upload>
    </n-card>

    <n-card :bordered="true" class="table-card" title="已上传文件" style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
      <n-empty v-if="!loading && !files.length" description="暂无文件" />
      <CommonTable
        v-else
        :columns="columns"
        :data="files"
        :loading="loading"
        :pagination="paginationReactive"
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NEmpty, NIcon, NSpace, NTag, NUpload, NUploadDragger, useMessage } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import type { DataTableColumns, UploadFileInfo } from 'naive-ui'
import { uploadFile, listFiles, type AssetFile } from '../../api/asset'

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const paginationReactive = reactive({
  page: 1,
  pageSize: 20,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => { paginationReactive.page = page },
  onUpdatePageSize: (size: number) => {
    paginationReactive.pageSize = size
    paginationReactive.page = 1
  }
})

const message = useMessage()
const files = ref<AssetFile[]>([])
const loading = ref(false)

const columns: DataTableColumns<AssetFile> = [
  {
    title: 'ID', key: 'id', width: 80,
    render(row: AssetFile) {
      return h(NTag, { size: 'small', bordered: false }, { default: () => String(row.id) })
    }
  },
  { title: '文件名', key: 'originalName', ellipsis: { tooltip: true } },
  {
    title: '类型', key: 'contentType', width: 130,
    render(row: AssetFile) {
      const isImage = row.contentType?.startsWith('image/')
      return h(NTag, {
        type: isImage ? 'success' : 'info',
        size: 'small',
        bordered: false
      }, { default: () => row.contentType })
    }
  },
  {
    title: '大小', key: 'sizeBytes', width: 100,
    render(row: AssetFile) {
      return formatSize(row.sizeBytes)
    }
  },
  {
    title: '操作', key: 'actions', width: 130, fixed: 'right' as const,
    render(row: AssetFile) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, {
            size: 'small', type: 'primary', ghost: true, onClick: () => {
              navigator.clipboard.writeText(row.url)
              message.success('链接已复制')
            }
          }, { default: () => '复制链接' })
        ]
      })
    }
  }
]

async function handleUpload({ file: uploadFileInfo }: { file: UploadFileInfo }) {
  if (!uploadFileInfo.file) return
  if (uploadFileInfo.file.size > 50 * 1024 * 1024) {
    message.warning('文件大小不能超过 50MB')
    return
  }
  try {
    const result = await uploadFile(uploadFileInfo.file)
    message.success(`上传成功: ${result.originalName}`)
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '上传失败')
  }
}

async function load() {
  loading.value = true
  try {
    files.value = await listFiles()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { load() })
</script>
