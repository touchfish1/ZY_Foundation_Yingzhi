<template>
  <div>
    <n-card title="角色管理" :bordered="true" class="table-card" style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
      <template #header-extra>
        <n-space align="center" size="small">
          <n-button type="primary" @click="openCreate">新建角色</n-button>
        </n-space>
      </template>
      <CommonTable
        :columns="columns"
        :data="roles"
        :loading="loading"
        :pagination="paginationReactive"
      />
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
import { NButton, NCard, NDivider, NEmpty, NForm, NFormItem, NModal, NSpace, NSpin, NTag, NTransfer, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import { listRoles, createRole, updateRole, deleteRole, getRolePermissions, setRolePermissions, type RoleInfo } from '../../api/system'
import { listPermissions, type PermissionInfo } from '../../api/permission'
import { useConfirm } from '../../composables/useConfirm'
import { formatDate } from '../../utils/format'

const message = useMessage()
const { confirm } = useConfirm()
const roles = ref<RoleInfo[]>([])
const loading = ref(false)

const paginationReactive = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => {
    paginationReactive.page = page
    loadData()
  },
  onUpdatePageSize: (size: number) => {
    paginationReactive.pageSize = size
    paginationReactive.page = 1
    loadData()
  }
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
  { title: 'ID', key: 'id', width: 70, render(row) { return h(NTag, { size: 'tiny', bordered: false }, { default: () => row.id }) } },
  { title: '编码', key: 'code' },
  { title: '名称', key: 'name' },
  { title: '创建时间', key: 'createdAt', width: 170,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.createdAt))
    }
  },
  {
    title: '操作', key: 'actions', width: 150,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', quaternary: true, onClick: () => openEdit(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'error', quaternary: true, onClick: () => handleDelete(row.id) }, { default: () => '删除' })
        ]
      })
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    const resp = await listRoles(paginationReactive.page, paginationReactive.pageSize)
    roles.value = resp.items
    paginationReactive.itemCount = resp.total
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
    await loadData()
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
    const [permResp, rolePermissionIds] = await Promise.all([
      listPermissions(1, 99999),
      getRolePermissions(role.id)
    ])
    allPermissions.value = permResp.items
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
    await loadData()
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
    await loadData()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => { loadData() })
</script>
