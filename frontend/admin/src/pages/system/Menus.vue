<template>
  <div>
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <h2>菜单管理</h2>
          <p>管理后台导航菜单和权限绑定。</p>
        </div>
        <div class="page-head-actions">
          <n-button quaternary @click="loadData" :loading="loading" size="small">
            <template #icon>
              <n-icon size="15">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 4 23 10 17 10"/>
                  <polyline points="1 20 1 14 7 14"/>
                  <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
                </svg>
              </n-icon>
            </template>
            刷新
          </n-button>
        </div>
      </div>
    </div>

    <!-- Context Menu Dropdown -->
    <n-dropdown
      trigger="manual"
      placement="bottom-start"
      :show="contextMenuShow"
      :x="contextMenuX"
      :y="contextMenuY"
      :options="contextMenuOptions"
      @select="handleContextMenuSelect"
      @clickoutside="contextMenuShow = false"
    />

    <div class="menu-layout">
      <!-- Left Panel: Tree -->
      <n-card :bordered="false" class="table-card menu-tree-card">
        <template #header>
          <div class="tree-header">
            <span class="tree-title">菜单树</span>
            <n-button size="small" type="primary" @click="openCreateRoot">添加根菜单</n-button>
          </div>
        </template>
        <n-spin :show="loading && treeOptions.length === 0">
          <n-tree
            ref="treeRef"
            :data="treeOptions"
            :draggable="true"
            :block-line="true"
            :selected-keys="selectedKeys"
            :render-label="renderLabel"
            :expand-all="true"
            @update:selected-keys="onSelect"
            @drop="handleDrop"
          />
          <n-empty v-if="!loading && treeOptions.length === 0" description="暂无菜单数据" style="padding: 40px 0" />
        </n-spin>
      </n-card>

      <!-- Right Panel: Edit Form or Empty State -->
      <n-card :bordered="false" class="table-card menu-form-card">
        <template #header>
          <span v-if="selectedNode">编辑菜单：{{ selectedNode.name }}</span>
          <span v-else>菜单详情</span>
        </template>

        <template v-if="selectedNode">
          <n-form label-placement="top" label-width="auto">
            <n-form-item label="名称" path="name">
              <n-input v-model:value="editForm.name" placeholder="菜单名称" />
            </n-form-item>
            <n-form-item label="菜单类型" path="menuType">
              <n-select v-model:value="editForm.menuType" :options="menuTypeOptions" />
            </n-form-item>
            <n-form-item label="路由路径" path="path">
              <n-input
                v-model:value="editForm.path"
                :disabled="editForm.menuType === 'group' || editForm.menuType === 'button'"
                placeholder="如 /system/users"
              />
            </n-form-item>
            <n-form-item label="图标" path="icon">
              <n-input v-model:value="editForm.icon" placeholder="如 People、Settings" />
            </n-form-item>
            <n-form-item label="排序" path="sortOrder">
              <n-input-number v-model:value="editForm.sortOrder" :min="0" />
            </n-form-item>
            <n-form-item label="状态" path="status">
              <n-switch v-model:value="editForm.statusBool">
                <template #checked>启用</template>
                <template #unchecked>禁用</template>
              </n-switch>
            </n-form-item>

            <n-divider />
            <n-form-item label="权限绑定">
              <div v-if="permissionGroups.length === 0" style="color: #94a3b8; font-size: 13px;">暂无权限数据</div>
              <div v-else class="perm-groups">
                <div v-for="group in permissionGroups" :key="group.module" class="perm-group">
                  <div class="perm-group-title">{{ group.module }}</div>
                  <n-checkbox-group v-model:value="editForm.permissionCodes">
                    <n-space size="small" wrap>
                      <n-checkbox
                        v-for="perm in group.permissions"
                        :key="perm.code"
                        :value="perm.code"
                        :label="perm.name"
                      />
                    </n-space>
                  </n-checkbox-group>
                </div>
              </div>
            </n-form-item>

            <n-space justify="end" style="margin-top: 16px;">
              <n-button @click="cancelEdit">取消</n-button>
              <n-button type="primary" :loading="saving" :disabled="!editForm.name" @click="saveEdit">保存</n-button>
            </n-space>
          </n-form>
        </template>

        <template v-else>
          <div class="empty-state">
            <n-empty description="请从左侧菜单树选择一个菜单项进行编辑" />
          </div>
        </template>
      </n-card>
    </div>

    <!-- Create Menu Modal -->
    <n-modal v-model:show="showCreate" preset="card" title="新建菜单" class="modal-card" :mask-closable="false" :style="{ maxWidth: '560px' }">
      <n-form label-placement="top">
        <n-form-item label="名称" path="name">
          <n-input v-model:value="createForm.name" placeholder="菜单名称" />
        </n-form-item>
        <n-form-item label="菜单类型" path="menuType">
          <n-select v-model:value="createForm.menuType" :options="menuTypeOptions" />
        </n-form-item>
        <n-form-item label="路由路径" path="path">
          <n-input
            v-model:value="createForm.path"
            :disabled="createForm.menuType === 'group' || createForm.menuType === 'button'"
            placeholder="如 /system/users"
          />
        </n-form-item>
        <n-form-item label="图标" path="icon">
          <n-input v-model:value="createForm.icon" placeholder="如 People、Settings" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="createForm.sortOrder" :min="0" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-switch v-model:value="createForm.statusBool">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>

        <n-divider />
        <n-form-item label="权限绑定">
          <div v-if="permissionGroups.length === 0" style="color: #94a3b8; font-size: 13px;">暂无权限数据</div>
          <div v-else class="perm-groups">
            <div v-for="group in permissionGroups" :key="group.module" class="perm-group">
              <div class="perm-group-title">{{ group.module }}</div>
              <n-checkbox-group v-model:value="createForm.permissionCodes">
                <n-space size="small" wrap>
                  <n-checkbox
                    v-for="perm in group.permissions"
                    :key="perm.code"
                    :value="perm.code"
                    :label="perm.name"
                  />
                </n-space>
              </n-checkbox-group>
            </div>
          </div>
        </n-form-item>

        <n-space justify="end" style="margin-top: 8px;">
          <n-button @click="showCreate = false">取消</n-button>
          <n-button type="primary" :loading="creating" :disabled="!createForm.name" @click="submitCreate">创建</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  NButton, NCard, NCheckbox, NCheckboxGroup, NDivider, NDropdown, NEmpty, NForm,
  NFormItem, NIcon, NInput, NInputNumber, NModal, NSelect, NSpace, NSpin, NSwitch, NTag, NTree, useMessage
} from 'naive-ui'
import type { DropdownOption, SelectOption, TreeDropInfo, TreeOption } from 'naive-ui'
import { fetchAllMenus, createMenu, updateMenu, deleteMenu, updateMenuSort, type MenuItem } from '../../api/menu'
import { listPermissions, type PermissionInfo } from '../../api/permission'
import { useConfirm } from '../../composables/useConfirm'

// ---------- Setup ----------
const message = useMessage()
const { confirm } = useConfirm()
const treeRef = ref<any>(null)
const loading = ref(false)
const saving = ref(false)
const creating = ref(false)

// ---------- Data ----------
const menuData = ref<MenuItem[]>([])
const allPermissions = ref<PermissionInfo[]>([])
const selectedNode = ref<MenuItem | null>(null)
const selectedKeys = ref<number[]>([])

// ---------- Context Menu ----------
const contextMenuShow = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuNode = ref<MenuItem | null>(null)
const contextMenuOptions: DropdownOption[] = [
  { key: 'add-child', label: '添加子菜单' },
  { key: 'edit', label: '编辑' },
  { key: 'delete', label: '删除' },
  { key: 'add-above', label: '在上面添加' },
  { key: 'add-below', label: '在下面添加' }
]

// ---------- Tree Options ----------
const menuTypeOptions: SelectOption[] = [
  { label: '分组', value: 'group' },
  { label: '页面', value: 'page' },
  { label: '按钮', value: 'button' }
]

const menuTypeColor: Record<string, 'success' | 'info' | 'warning'> = {
  group: 'info',
  page: 'success',
  button: 'warning'
}

const menuTypeLabel: Record<string, string> = {
  group: '分组',
  page: '页面',
  button: '按钮'
}

// ---------- Computed ----------
const treeOptions = computed<TreeOption[]>(() => {
  return buildTreeOptions(menuData.value)
})

const permissionGroups = computed(() => {
  const map = new Map<string, PermissionInfo[]>()
  for (const perm of allPermissions.value) {
    const mod = perm.module || '其他'
    if (!map.has(mod)) map.set(mod, [])
    map.get(mod)!.push(perm)
  }
  const groups: { module: string; permissions: PermissionInfo[] }[] = []
  for (const [module, permissions] of map) {
    groups.push({ module, permissions })
  }
  return groups
})

function buildTreeOptions(items: MenuItem[]): TreeOption[] {
  return items.map(item => ({
    key: item.id,
    label: item.name,
    menuItem: item,
    children: item.children?.length ? buildTreeOptions(item.children) : undefined
  }))
}

// ---------- Render Label ----------
function renderLabel({ option }: { option: TreeOption }) {
  const item = (option as any).menuItem as MenuItem
  if (!item) return option.label as string
  const children: any[] = []
  if (item.icon) {
    children.push(h(NIcon, { size: 16, style: 'margin-right: 6px; vertical-align: -3px;' }, {
      default: () => h('svg', { xmlns: 'http://www.w3.org/2000/svg', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', width: '16', height: '16' }, [
        h('rect', { x: '3', y: '3', width: '18', height: '18', rx: '2', ry: '2' }),
        h('line', { x1: '3', y1: '9', x2: '21', y2: '9' }),
        h('line', { x1: '9', y1: '21', x2: '9', y2: '9' })
      ])
    }))
  }
  children.push(h('span', { style: 'font-size: 13px;' }, item.name))
  children.push(h('span', { style: 'margin-left: 6px;' }, h(NTag, {
    size: 'tiny',
    bordered: false,
    type: menuTypeColor[item.menuType] || 'default',
    style: 'font-size: 11px;'
  }, { default: () => menuTypeLabel[item.menuType] || item.menuType })))

  return h('div', {
    style: 'display: flex; align-items: center; cursor: context-menu;',
    onContextmenu: (e: MouseEvent) => {
      e.preventDefault()
      e.stopPropagation()
      openContextMenu(e, item)
    }
  }, children)
}

// ---------- Context Menu Handlers ----------
function openContextMenu(e: MouseEvent, item: MenuItem) {
  contextMenuNode.value = item
  contextMenuX.value = e.clientX
  contextMenuY.value = e.clientY
  contextMenuShow.value = true
}

function handleContextMenuSelect(key: string) {
  contextMenuShow.value = false
  const node = contextMenuNode.value
  if (!node) return

  switch (key) {
    case 'add-child':
      openCreateChild(node.id)
      break
    case 'edit':
      selectNode(node)
      break
    case 'delete':
      handleDelete(node)
      break
    case 'add-above':
      openCreateSibling(node.parentId, node.sortOrder - 1)
      break
    case 'add-below':
      openCreateSibling(node.parentId, node.sortOrder + 1)
      break
  }
}

// ---------- Selection ----------
function onSelect(keys: number[]) {
  if (keys.length === 0) return
  const id = keys[0]
  const found = findMenuItemById(menuData.value, id)
  if (found) {
    selectNode(found)
  }
}

function selectNode(item: MenuItem) {
  selectedNode.value = item
  selectedKeys.value = [item.id]
  editForm.name = item.name
  editForm.menuType = item.menuType
  editForm.path = item.path || ''
  editForm.icon = item.icon || ''
  editForm.sortOrder = item.sortOrder
  editForm.statusBool = item.status !== 'disabled'
  editForm.permissionCodes = [...(item.permissionCodes || [])]
}

function cancelEdit() {
  selectedNode.value = null
  selectedKeys.value = []
}

// ---------- Tree Utilities ----------
function findMenuItemById(items: MenuItem[], id: number): MenuItem | null {
  for (const item of items) {
    if (item.id === id) return item
    if (item.children?.length) {
      const found = findMenuItemById(item.children, id)
      if (found) return found
    }
  }
  return null
}

function removeFromTree(items: MenuItem[], id: number): MenuItem | null {
  for (let i = 0; i < items.length; i++) {
    if (items[i].id === id) {
      return items.splice(i, 1)[0]
    }
    if (items[i].children?.length) {
      const found = removeFromTree(items[i].children!, id)
      if (found) return found
    }
  }
  return null
}

function insertInTree(items: MenuItem[], node: MenuItem, targetId: number, position: 'before' | 'inside' | 'after'): boolean {
  for (let i = 0; i < items.length; i++) {
    if (items[i].id === targetId) {
      if (position === 'before') {
        items.splice(i, 0, node)
        return true
      } else if (position === 'after') {
        items.splice(i + 1, 0, node)
        return true
      } else if (position === 'inside') {
        if (!items[i].children) items[i].children = []
        items[i].children!.push(node)
        return true
      }
    }
    if (items[i].children?.length) {
      if (insertInTree(items[i].children!, node, targetId, position)) return true
    }
  }
  return false
}

function flattenTreeForSort(items: MenuItem[]): { id: number; sortOrder: number }[] {
  const result: { id: number; sortOrder: number }[] = []
  let order = 1
  function walk(list: MenuItem[]) {
    for (const item of list) {
      result.push({ id: item.id, sortOrder: order++ })
      if (item.children?.length) {
        walk(item.children)
      }
    }
  }
  walk(items)
  return result
}

// ---------- Drag & Drop ----------
async function handleDrop(info: TreeDropInfo) {
  const { node, dragNode, dropPosition } = info
  const nodeId = node.key as number
  const dragId = dragNode.key as number

  // Prevent dropping on self
  if (nodeId === dragId) return

  const dragItem = removeFromTree(menuData.value, dragId)
  if (!dragItem) return

  const inserted = insertInTree(menuData.value, dragItem, nodeId, dropPosition)
  if (!inserted) {
    // If insertion fails (target not found in current tree), reload
    await loadData()
    return
  }

  // Calculate new sort orders from current tree state
  const sortList = flattenTreeForSort(menuData.value)
  try {
    await updateMenuSort(sortList)
    message.success('排序已更新')
  } catch (error: any) {
    message.error(error?.message || '排序更新失败')
  }
  // Always reload to sync with server
  await loadData()
}

// ---------- Create Form ----------
const showCreate = ref(false)
const createForm = reactive({
  name: '',
  menuType: 'page' as string,
  path: '',
  icon: '',
  sortOrder: 1,
  statusBool: true,
  permissionCodes: [] as string[],
  parentId: null as number | null
})

function openCreateRoot() {
  resetCreateForm()
  createForm.parentId = null
  showCreate.value = true
}

function openCreateChild(parentId: number) {
  resetCreateForm()
  createForm.parentId = parentId
  showCreate.value = true
}

function openCreateSibling(parentId: number | null, sortOrder: number) {
  resetCreateForm()
  createForm.parentId = parentId
  createForm.sortOrder = Math.max(0, sortOrder)
  showCreate.value = true
}

function resetCreateForm() {
  createForm.name = ''
  createForm.menuType = 'page'
  createForm.path = ''
  createForm.icon = ''
  createForm.sortOrder = 1
  createForm.statusBool = true
  createForm.permissionCodes = []
  createForm.parentId = null
}

async function submitCreate() {
  if (!createForm.name) {
    message.warning('请输入菜单名称')
    return
  }
  creating.value = true
  try {
    await createMenu({
      parentId: createForm.parentId,
      name: createForm.name,
      path: (createForm.menuType === 'group' || createForm.menuType === 'button') ? undefined : (createForm.path || undefined),
      icon: createForm.icon || undefined,
      menuType: createForm.menuType,
      sortOrder: createForm.sortOrder,
      status: createForm.statusBool ? 'enabled' : 'disabled',
      permissionCodes: createForm.permissionCodes.length > 0 ? createForm.permissionCodes : undefined
    })
    message.success('菜单已创建')
    showCreate.value = false
    await loadData()
  } catch (error: any) {
    message.error(error?.message || '创建失败')
  } finally {
    creating.value = false
  }
}

// ---------- Edit Form ----------
const editForm = reactive({
  name: '',
  menuType: 'page' as string,
  path: '',
  icon: '',
  sortOrder: 0,
  statusBool: true,
  permissionCodes: [] as string[]
})

async function saveEdit() {
  if (!selectedNode.value) return
  if (!editForm.name) {
    message.warning('请输入菜单名称')
    return
  }
  saving.value = true
  try {
    await updateMenu(selectedNode.value.id, {
      name: editForm.name,
      path: (editForm.menuType === 'group' || editForm.menuType === 'button') ? undefined : (editForm.path || undefined),
      icon: editForm.icon || undefined,
      menuType: editForm.menuType,
      sortOrder: editForm.sortOrder,
      status: editForm.statusBool ? 'enabled' : 'disabled',
      permissionCodes: editForm.permissionCodes.length > 0 ? editForm.permissionCodes : undefined
    })
    message.success('菜单已更新')
    await loadData()
  } catch (error: any) {
    message.error(error?.message || '更新失败')
  } finally {
    saving.value = false
  }
}

// ---------- Delete ----------
async function handleDelete(item: MenuItem) {
  const ok = await confirm({ content: `确定要删除菜单「${item.name}」及其所有子菜单吗？` })
  if (!ok) return
  try {
    await deleteMenu(item.id)
    message.success('菜单已删除')
    if (selectedNode.value?.id === item.id) {
      selectedNode.value = null
      selectedKeys.value = []
    }
    await loadData()
  } catch (error: any) {
    message.error(error?.message || '删除失败')
  }
}

// ---------- Load Data ----------
async function loadData() {
  loading.value = true
  try {
    const [menus, permResp] = await Promise.all([
      fetchAllMenus(),
      listPermissions(1, 99999)
    ])
    menuData.value = menus
    allPermissions.value = permResp.items

    // Restore selection after reload
    if (selectedNode.value) {
      const found = findMenuItemById(menus, selectedNode.value.id)
      if (found) {
        selectNode(found)
      } else {
        selectedNode.value = null
        selectedKeys.value = []
      }
    }
  } catch (error: any) {
    message.error(error?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.menu-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.menu-tree-card {
  flex: 0 0 40%;
  min-width: 0;
}

.menu-form-card {
  flex: 1;
  min-width: 0;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-title {
  font-weight: 700;
  font-size: 15px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.perm-groups {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.perm-group {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
}

html.dark .perm-group {
  border-color: #334155;
}

.perm-group-title {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 8px;
  color: #475569;
}

html.dark .perm-group-title {
  color: #cbd5e1;
}
</style>
