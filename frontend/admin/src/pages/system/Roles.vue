<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>角色管理</h2>
          <p>管理系统角色。</p>
        </div>
        <div class="page-head-actions">
          <n-input v-model:value="searchQuery" placeholder="搜索编码/名称..." clearable style="width: 200px">
            <template #prefix>
              <n-icon size="14"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></n-icon>
            </template>
          </n-input>
          <n-button type="primary" @click="openCreate">新建角色</n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card">
      <n-data-table :columns="columns" :data="filteredRoles" :loading="loading" :bordered="false" :striped="true" size="small" />
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

    <n-modal v-model:show="showEdit" preset="card" title="编辑角色" class="modal-card" style="width: 720px" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="编码">
          <n-input v-model:value="editForm.code" />
        </n-form-item>
        <n-form-item label="名称">
          <n-input v-model:value="editForm.name" />
        </n-form-item>
        <n-divider />
        <n-form-item label="权限分配">
          <n-spin v-if="permissionsLoading" />
          <n-transfer
            v-else-if="permissionTransferOptions.length > 0"
            v-model:value="selectedPermissionIds"
            :options="permissionTransferOptions"
            filterable
            :render-label="renderPermissionLabel"
            style="width: 100%"
          />
          <n-empty v-else description="暂无权限数据" size="small" />
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
import { NButton, NCard, NDataTable, NDivider, NEmpty, NForm, NFormItem, NIcon, NInput, NModal, NSpace, NSpin, NTransfer, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { listRoles, createRole, updateRole, deleteRole, getRolePermissions, setRolePermissions, type RoleInfo } from '../../api/system'
import { listPermissions, type PermissionInfo } from '../../api/permission'
import { useConfirm } from '../../composables/useConfirm'

const message = useMessage()
const { confirm } = useConfirm()
const roles = ref<RoleInfo[]>([])
const loading = ref(false)
const searchQuery = ref('')

const filteredRoles = computed(() => {
  if (!searchQuery.value) return roles.value
  const q = searchQuery.value.toLowerCase()
  return roles.value.filter(r => r.code.toLowerCase().includes(q) || r.name.toLowerCase().includes(q))
})

const showCreate = ref(false)
const showEdit = ref(false)
const creating = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({ code: '', name: '' })
const editForm = reactive({ code: '', name: '' })

// Permission assignment state
const allPermissions = ref<PermissionInfo[]>([])
const selectedPermissionIds = ref<number[]>([])
const permissionsLoading = ref(false)

// Transfer options: sorted by module, then code
const permissionTransferOptions = computed(() => {
  return [...allPermissions.value]
    .sort((a, b) => a.module.localeCompare(b.module) || a.code.localeCompare(b.code))
    .map(p => ({
      label: `${p.code} - ${p.name}`,
      value: p.id
    }))
})

function renderPermissionLabel(option: { label: string; value: number | string }) {
  const perm = allPermissions.value.find(p => p.id === option.value)
  if (!perm) return option.label
  return `${perm.module} / ${option.label}`
}

const columns: DataTableColumns<RoleInfo> = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '编码', key: 'code' },
  { title: '名称', key: 'name' },
  { title: '创建时间', key: 'createdAt', width: 180 },
  {
    title: '操作', key: 'actions', width: 150,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, onClick: () => openEdit(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row.id) }, { default: () => '删除' })
        ]
      })
    }
  }
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

async function openEdit(role: RoleInfo) {
  editingId.value = role.id
  editForm.code = role.code
  editForm.name = role.name
  selectedPermissionIds.value = []
  showEdit.value = true

  permissionsLoading.value = true
  try {
    const [permissions, rolePermissionIds] = await Promise.all([
      listPermissions(),
      getRolePermissions(role.id)
    ])
    allPermissions.value = permissions
    selectedPermissionIds.value = rolePermissionIds
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载权限失败')
  } finally {
    permissionsLoading.value = false
  }
}

async function submitEdit() {
  if (!editingId.value) return
  saving.value = true
  try {
    await updateRole(editingId.value, editForm)
    await setRolePermissions(editingId.value, selectedPermissionIds.value)
    message.success('角色已更新')
    showEdit.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '更新失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  const ok = await confirm({ content: '确定要删除此角色？' })
  if (!ok) return
  try {
    await deleteRole(id)
    message.success('角色已删除')
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => { load() })
</script>
