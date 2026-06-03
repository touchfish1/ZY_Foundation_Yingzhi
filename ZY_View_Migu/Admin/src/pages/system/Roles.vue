<template>
  <div class="page-head">
    <div>
      <h2>角色管理</h2>
      <p>管理系统角色。</p>
    </div>
    <n-button type="primary" @click="openCreate">新建角色</n-button>
  </div>

  <n-card>
    <n-empty v-if="!loading && !roles.length" description="暂无角色" />
    <n-data-table v-else :columns="columns" :data="roles" :loading="loading" />
  </n-card>

  <n-modal v-model:show="showCreate" preset="card" title="新建角色" class="modal-card">
    <n-form>
      <n-form-item label="编码"><n-input v-model:value="form.code" /></n-form-item>
      <n-form-item label="名称"><n-input v-model:value="form.name" /></n-form-item>
      <n-button type="primary" :loading="creating" @click="submitCreate">创建</n-button>
    </n-form>
  </n-modal>
</template>

<script setup lang="ts">
// 角色管理页面：查看和创建系统角色
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDataTable, NEmpty, NForm, NFormItem, NInput, NModal, NSpace, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { listRoles, createRole, type RoleInfo } from '../../api/system'

const message = useMessage()
const roles = ref<RoleInfo[]>([])
const loading = ref(false)
const showCreate = ref(false)
const creating = ref(false)
const form = reactive({ code: '', name: '' })

const columns: DataTableColumns<RoleInfo> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '编码', key: 'code' },
  { title: '名称', key: 'name' },
  { title: '创建时间', key: 'createdAt', width: 180 }
]

// 加载角色列表
async function load() {
  console.log('[Roles] load')
  loading.value = true
  try {
    roles.value = await listRoles()
    console.log('[Roles] loaded', roles.value.length, 'roles')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

// 打开新建角色模态框并清空表单
function openCreate() {
  console.log('[Roles] openCreate')
  form.code = ''; form.name = ''
  showCreate.value = true
}

// 提交新建角色表单
async function submitCreate() {
  console.log('[Roles] submitCreate')
  creating.value = true
  try {
    await createRole(form)
    message.success('角色已创建')
    showCreate.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  } finally {
    creating.value = false
  }
}

onMounted(() => { console.log('[Roles] mounted'); load() })
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
.modal-card { width: min(520px, 92vw); }
</style>
