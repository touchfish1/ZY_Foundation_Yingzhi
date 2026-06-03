<template>
  <div class="page-head">
    <div>
      <h2>媒体资源</h2>
      <p>管理上传的图片和文件。</p>
    </div>
  </div>

  <n-card title="上传文件" class="upload-card">
    <n-upload
      :default-upload="false"
      @change="handleUpload"
      accept="image/*,.pdf,.doc,.docx"
    >
      <n-upload-dragger>
        <p>点击或拖拽文件到此区域上传</p>
      </n-upload-dragger>
    </n-upload>
  </n-card>

  <n-card title="已上传文件">
    <n-empty v-if="!loading && !files.length" description="暂无文件" />
    <n-data-table v-else :columns="columns" :data="files" :loading="loading" />
  </n-card>
</template>

<script setup lang="ts">
// 媒体资源页面：上传文件、查看和管理已上传的文件
import { h, onMounted, ref } from 'vue'
import { NButton, NCard, NDataTable, NEmpty, NUpload, NUploadDragger, useMessage } from 'naive-ui'
import type { UploadFileInfo } from 'naive-ui'
import { uploadFile, listFiles, type AssetFile } from '../../api/asset'

// 格式化文件大小：将字节数转为可读的 B/KB/MB
function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const message = useMessage()
const files = ref<AssetFile[]>([])
const loading = ref(false)

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '文件名', key: 'originalName' },
  { title: '类型', key: 'contentType', width: 140 },
  { title: '大小', key: 'sizeBytes', width: 100, render(row: AssetFile) {
      return formatSize(row.sizeBytes)
    }
  },
  {
    title: '操作', key: 'actions', width: 120,
    render(row: AssetFile) {
      return h(NButton, {
        size: 'small', onClick: () => {
          navigator.clipboard.writeText(row.url)
          message.success('链接已复制')
        }
      }, { default: () => '复制链接' })
    }
  }
]

// 处理上传事件：校验大小限制 -> 上传 -> 刷新列表
async function handleUpload({ file: uploadFileInfo }: { file: UploadFileInfo }) {
  if (!uploadFileInfo.file) return

  console.log('[Assets] handleUpload', { name: uploadFileInfo.file.name, size: uploadFileInfo.file.size })

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

// 加载已上传文件列表
async function load() {
  console.log('[Assets] load')
  loading.value = true
  try {
    files.value = await listFiles()
    console.log('[Assets] loaded', files.value.length, 'files')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { console.log('[Assets] mounted'); load() })
</script>

<style scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-head h2 { margin: 0 0 6px; }
.page-head p { margin: 0; color: #64748b; }
.upload-card { margin-bottom: 20px; }
</style>
