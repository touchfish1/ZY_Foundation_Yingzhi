<template>
  <div>
    <n-card title="用户管理" :bordered="true" class="table-card" style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
      <template #header-extra>
        <n-space align="center" size="small">
          <n-input v-model:value="searchQuery" placeholder="搜索用户名..." clearable style="width: 200px">
            <template #prefix>
              <n-icon size="14"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></n-icon>
            </template>
          </n-input>
          <n-button type="primary" @click="openCreate">新建用户</n-button>
        </n-space>
      </template>
      <CommonTable
        :columns="columns"
        :data="users"
        :loading="loading"
        :pagination="paginationReactive"
      />
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

    <n-modal v-model:show="showRecharge" preset="card" title="余额充值" class="modal-card" :mask-closable="false" style="width: 480px;">
      <n-form label-placement="top">
        <n-form-item label="用户">
          <span style="font-weight: 600;">{{ rechargeForm.username }}</span>
          <span style="color: #64748b; margin-left: 8px;">(ID: {{ rechargeForm.userId }})</span>
        </n-form-item>
        <n-form-item label="充值金额（元）">
          <n-input-number v-model:value="rechargeForm.amount" :min="0" :precision="2" placeholder="请输入充值金额" style="width: 200px" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="rechargeForm.remark" placeholder="选填" />
        </n-form-item>
        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showRecharge = false">取消</n-button>
          <n-button type="primary" :loading="recharging" @click="submitRecharge">确认充值</n-button>
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
import { computed, h, onMounted, reactive, ref, watch } from 'vue'
import { NButton, NCard, NCheckbox, NCheckboxGroup, NForm, NFormItem, NIcon, NInput, NInputNumber, NModal, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import CommonTable from '../../components/CommonTable.vue'
import { listUsers, createUser, updateUser, deleteUser, listRoles, getUserRoles, setUserRoles, rechargeBalance, type UserInfo, type RoleInfo } from '../../api/system'
import { useConfirm } from '../../composables/useConfirm'
import { formatDate } from '../../utils/format'

const message = useMessage()
const { confirm } = useConfirm()
const users = ref<UserInfo[]>([])
const loading = ref(false)
const searchQuery = ref('')

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

watch(searchQuery, () => {
  paginationReactive.page = 1
  loadData()
})

const showCreate = ref(false)
const showEdit = ref(false)
const showRecharge = ref(false)
const creating = ref(false)
const saving = ref(false)
const recharging = ref(false)
const editingId = ref<number | null>(null)

const rechargeForm = reactive({ userId: 0, username: '', amount: 0, remark: '' })

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
  { title: 'ID', key: 'id', width: 70, render(row) { return h(NTag, { size: 'tiny', bordered: false }, { default: () => row.id }) } },
  { title: '用户名', key: 'username' },
  { title: '昵称', key: 'nickname' },
  { title: '邮箱', key: 'email' },
  { title: '状态', key: 'status', width: 90, render(row) {
      return h(NTag, { type: row.status === 'enabled' ? 'success' : 'warning', size: 'small', bordered: false }, { default: () => row.status === 'enabled' ? '启用' : '禁用' })
    }
  },
  { title: '创建时间', key: 'createdAt', width: 170,
    render(row) {
      return h('span', { style: 'color: #64748b; font-size: 13px;' }, formatDate(row.createdAt))
    }
  },
  {
    title: '操作', key: 'actions', width: 250,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', quaternary: true, onClick: () => openEdit(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'warning', quaternary: true, onClick: () => openRecharge(row) }, { default: () => '充值' }),
          h(NButton, { size: 'small', type: 'error', quaternary: true, onClick: () => handleDelete(row.id) }, { default: () => '删除' })
        ]
      })
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    const resp = await listUsers(
      paginationReactive.page,
      paginationReactive.pageSize,
      searchQuery.value || undefined
    )
    users.value = resp.items
    paginationReactive.itemCount = resp.total
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
    allRoles.value = (await listRoles(1, 99999)).items
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
    await loadData()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建失败')
  } finally {
    creating.value = false
  }
}

function openRecharge(user: UserInfo) {
  rechargeForm.userId = user.id
  rechargeForm.username = user.username
  rechargeForm.amount = 0
  rechargeForm.remark = ''
  showRecharge.value = true
}

async function submitRecharge() {
  if (rechargeForm.amount <= 0) {
    message.warning('请输入有效的充值金额')
    return
  }
  recharging.value = true
  try {
    await rechargeBalance(rechargeForm.userId, {
      amount: rechargeForm.amount,
      remark: rechargeForm.remark || undefined
    })
    message.success('充值成功')
    showRecharge.value = false
    await loadData()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '充值失败')
  } finally {
    recharging.value = false
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
    const [roleResp, userRoleIds] = await Promise.all([
      listRoles(1, 99999),
      getUserRoles(user.id)
    ])
    allRoles.value = roleResp.items
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
    await loadData()
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
    await loadData()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => { loadData() })
</script>
