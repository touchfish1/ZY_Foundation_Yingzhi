<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>角色管理</h2>
          <p>管理系统角色。</p>
        </div>
        <div class="page-head-actions">
          <n-button type="primary" @click="openCreate">新建角色</n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card">
      <n-empty v-if="!loading && !roles.length" description="暂无角色" />
      <n-data-table v-else :columns="columns" :data="roles" :loading="loading" :bordered="false" :striped="true" size="small" />
    </n-card>

    <n-modal v-model:show="showCreate" preset="card" title="新建角色" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="编码">
          <n-input v-model:value="form.code" placeholder="editor" />
        </n-form-item>
        <n-form-item label="名称">
          <n-input v-model:value="form.name" placeholder="编辑者" />
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showCreate = false">取消</n-button>
          <n-button type="primary" :loading="creating" @click="submitCreate">创建</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
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
  { title: 'ID', key: 'id', width: 70 },
  { title: '编码', key: 'code' },
  { title: '名称', key: 'name' },
  { title: '创建时间', key: 'createdAt', width: 180 }
]

async function load() {
  loading.value = true
  try {
    roles.value = await listRoles()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.code = ''; form.name = ''
  showCreate.value = true
}

async function submitCreate() {
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

onMounted(() => { load() })
</script>
