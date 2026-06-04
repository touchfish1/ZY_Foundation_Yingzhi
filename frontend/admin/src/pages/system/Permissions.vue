<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>权限管理</h2>
          <p>管理系统权限编码。</p>
        </div>
        <div class="page-head-actions">
          <n-select
            v-model:value="moduleFilter"
            :options="moduleOptions"
            placeholder="筛选模块"
            clearable
            style="width: 160px"
            @update:value="load"
          />
          <n-input v-model:value="searchQuery" placeholder="搜索编码/名称..." clearable style="width: 200px">
            <template #prefix>
              <n-icon size="14"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></n-icon>
            </template>
          </n-input>
          <n-button type="primary" v-permission="'system:permission:create'" @click="openCreate">新建权限</n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card">
      <n-data-table
        :columns="columns"
        :data="filteredPermissions"
        :loading="loading"
        :bordered="false"
        :striped="true"
        size="small"
      />
    </n-card>

    <n-modal v-model:show="showCreate" preset="card" title="新建权限" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="权限编码" :feedback="codeError">
          <n-input v-model:value="createForm.code" placeholder="system:user:create" @keydown="codeError = ''" />
        </n-form-item>
        <n-form-item label="权限名称">
          <n-input v-model:value="createForm.name" placeholder="创建用户" />
        </n-form-item>
        <n-form-item label="所属模块">
          <n-select
            v-model:value="createForm.module"
            :options="moduleOptions"
            placeholder="选择或输入新模块"
            filterable
            tag
          />
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showCreate = false">取消</n-button>
          <n-button type="primary" :loading="creating" @click="submitCreate">创建</n-button>
        </n-space>
      </n-form>
    </n-modal>

    <n-modal v-model:show="showEdit" preset="card" title="编辑权限" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="权限编码" :feedback="codeError">
          <n-input v-model:value="editForm.code" placeholder="system:user:create" @keydown="codeError = ''" />
        </n-form-item>
        <n-form-item label="权限名称">
          <n-input v-model:value="editForm.name" placeholder="创建用户" />
        </n-form-item>
        <n-form-item label="所属模块">
          <n-select
            v-model:value="editForm.module"
            :options="moduleOptions"
            placeholder="选择或输入新模块"
            filterable
            tag
          />
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showEdit = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="submitEdit">保存</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  NButton, NCard, NDataTable, NForm, NFormItem, NIcon, NInput, NModal, NSelect, NSpace, NTag, useMessage
} from 'naive-ui'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import {
  listPermissions,
  listPermissionModules,
  createPermission,
  updatePermission,
  deletePermission,
  type PermissionInfo
} from '../../api/permission'
import { useConfirm } from '../../composables/useConfirm'

const CODE_REGEX = /^[a-z]+:[a-z:]+$/

const message = useMessage()
const { confirm } = useConfirm()

const permissions = ref<PermissionInfo[]>([])
const modules = ref<string[]>([])
const loading = ref(false)
const searchQuery = ref('')
const moduleFilter = ref<string | null>(null)
const codeError = ref('')

const moduleOptions = computed<SelectOption[]>(() =>
  modules.value.map(m => ({ label: m, value: m }))
)

const filteredPermissions = computed(() => {
  let list = permissions.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(p => p.code.toLowerCase().includes(q) || p.name.toLowerCase().includes(q))
  }
  return list
})

const showCreate = ref(false)
const showEdit = ref(false)
const creating = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)

const createForm = reactive({ code: '', name: '', module: '' })
const editForm = reactive({ code: '', name: '', module: '' })

const columns: DataTableColumns<PermissionInfo> = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '权限编码', key: 'code' },
  { title: '权限名称', key: 'name' },
  {
    title: '所属模块', key: 'module', width: 130,
    render(row) {
      return h(NTag, { size: 'small', bordered: false, type: 'info' }, { default: () => row.module })
    }
  },
  { title: '创建时间', key: 'createdAt', width: 180 },
  {
    title: '操作', key: 'actions', width: 150,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, {
            size: 'small', quaternary: true,
            onClick: () => openEdit(row)
          }, { default: () => '编辑' }),
          h(NButton, {
            size: 'small', quaternary: true, type: 'error',
            vPermission: "'system:permission:delete'",
            onClick: () => handleDelete(row.id)
          }, { default: () => '删除' })
        ]
      })
    }
  }
]

async function loadModules() {
  try {
    modules.value = await listPermissionModules()
  } catch (error) {
    // Non-critical, modules will be empty
    console.error('Failed to load permission modules:', error)
  }
}

async function load() {
  loading.value = true
  try {
    permissions.value = await listPermissions(moduleFilter.value ?? undefined)
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function validateCode(code: string): boolean {
  if (!CODE_REGEX.test(code)) {
    codeError.value = '编码格式不正确，需为小写字母和冒号组成，如 system:user:create'
    return false
  }
  codeError.value = ''
  return true
}

function openCreate() {
  createForm.code = ''
  createForm.name = ''
  createForm.module = ''
  codeError.value = ''
  showCreate.value = true
}

async function submitCreate() {
  if (!validateCode(createForm.code)) return
  creating.value = true
  try {
    await createPermission({ ...createForm })
    message.success('权限已创建')
    showCreate.value = false
    await load()
    await loadModules()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  } finally {
    creating.value = false
  }
}

function openEdit(permission: PermissionInfo) {
  editingId.value = permission.id
  editForm.code = permission.code
  editForm.name = permission.name
  editForm.module = permission.module
  codeError.value = ''
  showEdit.value = true
}

async function submitEdit() {
  if (!editingId.value) return
  if (!validateCode(editForm.code)) return
  saving.value = true
  try {
    await updatePermission(editingId.value, { ...editForm })
    message.success('权限已更新')
    showEdit.value = false
    await load()
    await loadModules()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '更新失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  const ok = await confirm({ content: '确定要删除此权限？' })
  if (!ok) return
  try {
    await deletePermission(id)
    message.success('权限已删除')
    await load()
    await loadModules()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => {
  load()
  loadModules()
})
</script>
