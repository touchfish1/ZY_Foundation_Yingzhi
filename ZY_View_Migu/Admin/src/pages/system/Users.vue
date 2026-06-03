<template>
  <div class="page-head">
    <div>
      <h2>用户管理</h2>
      <p>管理系统管理员账号。</p>
    </div>
    <n-space>
      <n-input v-model:value="searchQuery" placeholder="搜索用户名..." clearable style="width: 200px" />
      <n-button type="primary" @click="openCreate">新建用户</n-button>
    </n-space>
  </div>

  <n-card>
    <n-data-table :columns="columns" :data="filteredUsers" :loading="loading" :bordered="false" />
  </n-card>

  <n-modal v-model:show="showCreate" preset="card" title="新建用户" class="modal-card">
    <n-form>
      <n-form-item label="用户名"><n-input v-model:value="createForm.username" /></n-form-item>
      <n-form-item label="密码"><n-input v-model:value="createForm.password" type="password" /></n-form-item>
      <n-form-item label="昵称"><n-input v-model:value="createForm.nickname" /></n-form-item>
      <n-form-item label="邮箱"><n-input v-model:value="createForm.email" /></n-form-item>
      <n-button type="primary" :loading="creating" @click="submitCreate">创建</n-button>
    </n-form>
  </n-modal>

  <n-modal v-model:show="showEdit" preset="card" title="编辑用户" class="modal-card">
    <n-form>
      <n-form-item label="昵称"><n-input v-model:value="editForm.nickname" /></n-form-item>
      <n-form-item label="邮箱"><n-input v-model:value="editForm.email" /></n-form-item>
      <n-form-item label="状态">
        <n-select v-model:value="editForm.status" :options="statusOptions" />
      </n-form-item>
      <n-button type="primary" :loading="saving" @click="submitEdit">保存</n-button>
    </n-form>
  </n-modal>
</template>

<script setup lang="ts">
// 用户管理页面：管理系统管理员账号，支持新建/编辑/删除/搜索
import { computed, h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NModal, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { listUsers, createUser, updateUser, deleteUser, type UserInfo } from '../../api/system'
import { useConfirm } from '../../composables/useConfirm'

const message = useMessage()
const { confirm } = useConfirm()
const users = ref<UserInfo[]>([])
const loading = ref(false)
const searchQuery = ref('')
// 根据用户名或昵称过滤用户列表
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

const columns: DataTableColumns<UserInfo> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户名', key: 'username' },
  { title: '昵称', key: 'nickname' },
  { title: '邮箱', key: 'email' },
  { title: '状态', key: 'status', width: 100, render(row) { return h(NTag, { type: row.status === 'enabled' ? 'success' : 'default' }, { default: () => row.status }) } },
  { title: '创建时间', key: 'createdAt', width: 180 },
  {
    title: '操作', key: 'actions', width: 160,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, { default: () => '删除' })
        ]
      })
    }
  }
]

// 加载用户列表
async function load() {
  console.log('[Users] load')
  loading.value = true
  try {
    users.value = await listUsers()
    console.log('[Users] loaded', users.value.length, 'users')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

// 打开新建用户模态框并清空表单
function openCreate() {
  console.log('[Users] openCreate')
  createForm.username = ''; createForm.password = ''; createForm.nickname = ''; createForm.email = ''
  showCreate.value = true
}

// 提交新建用户表单
async function submitCreate() {
  console.log('[Users] submitCreate')
  creating.value = true
  try {
    await createUser(createForm)
    message.success('用户已创建')
    showCreate.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  } finally {
    creating.value = false
  }
}

// 打开编辑用户模态框，填充当前值
function openEdit(user: UserInfo) {
  console.log('[Users] openEdit', { id: user.id })
  editingId.value = user.id
  editForm.nickname = user.nickname
  editForm.email = user.email
  editForm.status = user.status
  showEdit.value = true
}

// 提交编辑用户表单
async function submitEdit() {
  if (!editingId.value) return
  console.log('[Users] submitEdit', { id: editingId.value })
  saving.value = true
  try {
    await updateUser(editingId.value, editForm)
    message.success('用户已更新')
    showEdit.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '更新失败')
  } finally {
    saving.value = false
  }
}

// 删除用户：确认对话框 -> 调用 API -> 刷新列表
async function handleDelete(id: number) {
  console.log('[Users] handleDelete', { id })
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

onMounted(() => { console.log('[Users] mounted'); load() })
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
