<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>用户管理</h2>
          <p>管理系统管理员账号。</p>
        </div>
        <div class="page-head-actions">
          <n-input v-model:value="searchQuery" placeholder="搜索用户名..." clearable style="width: 200px">
            <template #prefix>
              <n-icon size="14"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></n-icon>
            </template>
          </n-input>
          <n-button type="primary" @click="openCreate">新建用户</n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="false" class="table-card">
      <n-data-table :columns="columns" :data="filteredUsers" :loading="loading" :bordered="false" :striped="true" size="small" />
    </n-card>

    <n-modal v-model:show="showCreate" preset="card" title="新建用户" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="用户名"><n-input v-model:value="createForm.username" placeholder="请输入用户名" /></n-form-item>
        <n-form-item label="密码"><n-input v-model:value="createForm.password" type="password" placeholder="请输入密码" /></n-form-item>
        <n-form-item label="昵称"><n-input v-model:value="createForm.nickname" placeholder="选填" /></n-form-item>
        <n-form-item label="邮箱"><n-input v-model:value="createForm.email" placeholder="选填" /></n-form-item>
        <n-form-item label="角色">
          <div>
            <n-button size="tiny" quaternary @click="toggleSelectAllCreateRoles" style="margin-bottom: 8px">
              {{ allCreateRolesSelected ? '取消全选' : '全选' }}
            </n-button>
            <n-checkbox-group v-model:value="createSelectedRoleIds">
              <n-space item-style="display: flex;">
                <n-checkbox v-for="role in allRoles" :key="role.id" :value="role.id" :label="`${role.name} (${role.code})`" />
              </n-space>
            </n-checkbox-group>
          </div>
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showCreate = false">取消</n-button>
          <n-button type="primary" :loading="creating" @click="submitCreate">创建</n-button>
        </n-space>
      </n-form>
    </n-modal>

    <n-modal v-model:show="showEdit" preset="card" title="编辑用户" class="modal-card" :mask-closable="false">
      <n-form label-placement="top">
        <n-form-item label="昵称"><n-input v-model:value="editForm.nickname" /></n-form-item>
        <n-form-item label="邮箱"><n-input v-model:value="editForm.email" /></n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="editForm.status" :options="statusOptions" />
        </n-form-item>
        <n-form-item label="角色">
          <div>
            <n-button size="tiny" quaternary @click="toggleSelectAllRoles" style="margin-bottom: 8px">
              {{ allRolesSelected ? '取消全选' : '全选' }}
            </n-button>
            <n-checkbox-group v-model:value="editSelectedRoleIds">
              <n-space item-style="display: flex;">
                <n-checkbox v-for="role in allRoles" :key="role.id" :value="role.id" :label="`${role.name} (${role.code})`" />
              </n-space>
            </n-checkbox-group>
          </div>
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
import { NButton, NCard, NCheckbox, NCheckboxGroup, NDataTable, NForm, NFormItem, NIcon, NInput, NModal, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { listUsers, createUser, updateUser, deleteUser, listRoles, getUserRoles, setUserRoles, type UserInfo, type RoleInfo } from '../../api/system'
import { useConfirm } from '../../composables/useConfirm'

const message = useMessage()
const { confirm } = useConfirm()
const users = ref<UserInfo[]>([])
const loading = ref(false)
const searchQuery = ref('')

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const q = searchQuery.value.toLowerCase()
  return users.value.filter(u => u.username.toLowerCase().includes(q) || (u.nickname && u.nickname.toLowerCase().includes(q)))
})

const showCreate = ref(false)
const showEdit = ref(false)
const creating = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)

const createForm = reactive({ username: '', password: '', nickname: '', email: '' })
const editForm = reactive({ nickname: '', email: '', status: 'enabled' })
const statusOptions: SelectOption[] = [
  { label: '启用', value: 'enabled' },
  { label: '禁用', value: 'disabled' }
]

const allRoles = ref<RoleInfo[]>([])
const editSelectedRoleIds = ref<number[]>([])
const createSelectedRoleIds = ref<number[]>([])

const allRolesSelected = computed(() => allRoles.value.length > 0 && editSelectedRoleIds.value.length === allRoles.value.length)
const allCreateRolesSelected = computed(() => allRoles.value.length > 0 && createSelectedRoleIds.value.length === allRoles.value.length)

function toggleSelectAllRoles() {
  editSelectedRoleIds.value = allRolesSelected.value ? [] : allRoles.value.map(r => r.id)
}

function toggleSelectAllCreateRoles() {
  createSelectedRoleIds.value = allCreateRolesSelected.value ? [] : allRoles.value.map(r => r.id)
}

const columns: DataTableColumns<UserInfo> = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '用户名', key: 'username' },
  { title: '昵称', key: 'nickname' },
  { title: '邮箱', key: 'email' },
  { title: '状态', key: 'status', width: 90, render(row) {
      return h(NTag, { type: row.status === 'enabled' ? 'success' : 'default', size: 'small', bordered: false }, { default: () => row.status === 'enabled' ? '启用' : '禁用' })
    }
  },
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
    users.value = await listUsers()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function openCreate() {
  createForm.username = ''; createForm.password = ''; createForm.nickname = ''; createForm.email = ''
  createSelectedRoleIds.value = []
  showCreate.value = true
  try {
    allRoles.value = await listRoles()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载角色列表失败')
  }
}

async function submitCreate() {
  creating.value = true
  try {
    const user = await createUser(createForm)
    if (createSelectedRoleIds.value.length > 0) {
      await setUserRoles(user.id, createSelectedRoleIds.value)
    }
    message.success('用户已创建')
    showCreate.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  } finally {
    creating.value = false
  }
}

async function openEdit(user: UserInfo) {
  editingId.value = user.id
  editForm.nickname = user.nickname
  editForm.email = user.email
  editForm.status = user.status
  editSelectedRoleIds.value = []
  showEdit.value = true
  try {
    const [roles, userRoleIds] = await Promise.all([
      listRoles(),
      getUserRoles(user.id)
    ])
    allRoles.value = roles
    editSelectedRoleIds.value = userRoleIds
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载角色信息失败')
  }
}

async function submitEdit() {
  if (!editingId.value) return
  saving.value = true
  try {
    await updateUser(editingId.value, editForm)
    await setUserRoles(editingId.value, editSelectedRoleIds.value)
    message.success('用户已更新')
    showEdit.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '更新失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  const ok = await confirm({ content: '确定要删除此用户？' })
  if (!ok) return
  try {
    await deleteUser(id)
    message.success('用户已删除')
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => { load() })
</script>
