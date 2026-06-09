<template>
  <div class="block-editor">
    <div class="block-list">
      <div
        v-for="(block, i) in blocks"
        :key="block.id"
        class="block-item"
        :class="{ 'drag-over': dragOverIndex === i }"
        draggable="true"
        @dragstart="onDragStart(i)"
        @dragover.prevent="onDragOver(i)"
        @dragleave="onDragLeave"
        @drop="onDrop(i)"
        @dragend="onDragEnd"
      >
        <div class="block-item-main">
          <div class="block-drag-handle">⠿</div>
          <div class="block-item-info">
            <n-tag :bordered="false" size="small" :type="blockTypeTagType(block.type)">
              {{ getDefinition(block.type)?.name || block.type }}
            </n-tag>
            <span class="block-summary">{{ getBlockSummary(block) }}</span>
          </div>
          <div class="block-item-actions">
            <n-button size="tiny" quaternary round @click="openEdit(i)">编辑</n-button>
            <n-button size="tiny" quaternary round @click="duplicateBlock(i)">复制</n-button>
            <n-button size="tiny" quaternary round type="error" @click="removeBlock(i)">删除</n-button>
            <n-button size="tiny" quaternary round :disabled="i === 0" @click="moveBlock(i, -1)">▲</n-button>
            <n-button size="tiny" quaternary round :disabled="i === blocks.length - 1" @click="moveBlock(i, 1)">▼</n-button>
          </div>
        </div>
      </div>
      <div v-if="blocks.length === 0" class="block-empty">
        <div class="block-empty-icon">◇</div>
        <p>暂无区块</p>
        <p class="block-empty-hint">点击下方按钮添加区块</p>
      </div>
    </div>

    <div class="add-block">
      <n-dropdown placement="top-start" trigger="click" :options="addOptions" @select="onAddSelect">
        <n-button type="primary" ghost :disabled="isEditing">+ 添加区块</n-button>
      </n-dropdown>
    </div>

    <n-modal v-model:show="showModal" preset="card" :title="editTitle" class="editor-modal" :mask-closable="false">
      <n-form v-if="currentDef" label-placement="top">
        <n-form-item
          v-for="field in currentDef.schema.fields"
          :key="field.key"
          :label="field.label"
          :required="field.required"
        >
          <n-input
            v-if="['text', 'url'].includes(field.type)"
            :value="editingProps[field.key] as string"
            @update:value="(v: string) => onPropChange(field.key, v)"
            :placeholder="`请输入${field.label}`"
          />
          <n-input
            v-else-if="['textarea', 'rich-text'].includes(field.type)"
            :value="editingProps[field.key] as string"
            @update:value="(v: string) => onPropChange(field.key, v)"
            type="textarea"
            :rows="4"
            :placeholder="`请输入${field.label}`"
          />
          <n-input-number
            v-else-if="field.type === 'number'"
            :value="editingProps[field.key] as number"
            @update:value="(v: number | null) => onPropChange(field.key, v)"
            :placeholder="`请输入${field.label}`"
            style="width: 100%"
          />
          <div v-else-if="field.type === 'list'" class="list-field">
            <div v-for="(item, li) in (editingProps[field.key] as any[] || [])" :key="li" class="list-item">
              <n-input
                v-if="field.listFields"
                v-for="lf in field.listFields"
                :key="lf.key"
                :value="item[lf.key]"
                @update:value="(v: string) => updateListItem(field.key, li, lf.key, v)"
                :placeholder="lf.label"
                style="margin-bottom: 4px"
              />
              <n-button size="tiny" quaternary type="error" @click="removeListItem(field.key, li)">移除</n-button>
            </div>
            <n-button size="small" quaternary @click="addListItem(field.key)">+ 添加一项</n-button>
          </div>
          <n-input
            v-else
            :value="editingProps[field.key] as string"
            @update:value="(v: string) => onPropChange(field.key, v)"
            :placeholder="`请输入${field.label}`"
          />
        </n-form-item>
      </n-form>
      <div class="modal-actions">
        <n-button @click="cancelEdit">取消</n-button>
        <n-button type="primary" @click="confirmEdit">确定</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
// 区块表单编辑器：管理页面中的区块列表，支持添加/编辑/排序/复制/删除区块
import { computed, ref } from 'vue'
import { NButton, NForm, NFormItem, NInput, NInputNumber, NModal, NTag, NDropdown } from 'naive-ui'
import type { BlockDefinition, PageBlock } from '../../types/block'

const props = defineProps<{
  blocks: PageBlock[]
  definitions: BlockDefinition[]
}>()

const emit = defineEmits<{
  'update:blocks': [blocks: PageBlock[]]
}>()

const dragIndex = ref<number | null>(null)
const dragOverIndex = ref<number | null>(null)
const editingIndex = ref<number | null>(null)
const editingProps = ref<Record<string, unknown>>({})
const selectedAddType = ref<string | null>(null)
const showModal = ref(false)

const isEditing = computed(() => showModal.value)

const addOptions = computed(() =>
  props.definitions.map(def => ({
    label: def.name,
    key: def.type
  }))
)

const currentDef = computed(() => {
  if (editingIndex.value !== null) {
    return getDefinition(props.blocks[editingIndex.value].type)
  }
  if (selectedAddType.value) {
    return getDefinition(selectedAddType.value)
  }
  return null
})

const editTitle = computed(() => {
  if (editingIndex.value !== null) {
    return `编辑区块: ${currentDef.value?.name || ''}`
  }
  if (selectedAddType.value) {
    return `添加区块: ${currentDef.value?.name || ''}`
  }
  return ''
})

// 根据区块类型获取对应的定义
function getDefinition(type: string): BlockDefinition | undefined {
  return props.definitions.find(d => d.type === type)
}

// 初始化编辑属性：从数据源或默认值填充所有字段
function initEditingProps(source: Record<string, unknown>, def: BlockDefinition): Record<string, unknown> {
  const result: Record<string, unknown> = {}
  for (const field of def.schema.fields) {
    result[field.key] = field.key in source ? source[field.key] : (field.defaultValue ?? '')
  }
  return result
}

// 打开编辑区块模态框
function openEdit(index: number) {
  console.log('[BlockFormEditor] openEdit', { index, type: props.blocks[index].type })
  editingIndex.value = index
  selectedAddType.value = null
  const block = props.blocks[index]
  const def = getDefinition(block.type)
  if (def) {
    editingProps.value = initEditingProps(block.props, def)
  }
  showModal.value = true
}

// 从下拉菜单选择区块类型进行添加
function onAddSelect(type: string) {
  editingIndex.value = null
  selectedAddType.value = type
  const def = getDefinition(type)
  if (def) {
    editingProps.value = initEditingProps({}, def)
  }
  showModal.value = true
}

// 确认编辑或添加区块
function confirmEdit() {
  if (editingIndex.value !== null) {
    const updated = [...props.blocks]
    updated[editingIndex.value] = { ...updated[editingIndex.value], props: { ...editingProps.value } }
    emit('update:blocks', updated)
  } else if (selectedAddType.value) {
    const def = getDefinition(selectedAddType.value)
    if (def) {
      const newBlock: PageBlock = {
        id: `block_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
        type: selectedAddType.value,
        props: { ...editingProps.value }
      }
      emit('update:blocks', [...props.blocks, newBlock])
    }
  }
  cancelEdit()
}

function cancelEdit() {
  editingIndex.value = null
  selectedAddType.value = null
  editingProps.value = {}
  showModal.value = false
}

// 删除指定索引的区块
function removeBlock(index: number) {
  const updated = props.blocks.filter((_, i) => i !== index)
  emit('update:blocks', updated)
}

// 上移或下移区块
function moveBlock(index: number, direction: -1 | 1) {
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= props.blocks.length) return
  const updated = [...props.blocks]
  const [removed] = updated.splice(index, 1)
  updated.splice(newIndex, 0, removed)
  emit('update:blocks', updated)
}

// 拖拽排序：开始拖拽
function onDragStart(index: number) {
  dragIndex.value = index
}

// 拖拽排序：拖过其他区块时高亮
function onDragOver(index: number) {
  if (dragIndex.value === null || dragIndex.value === index) return
  dragOverIndex.value = index
}

// 拖拽排序：离开高亮区域
function onDragLeave() {
  dragOverIndex.value = null
}

// 拖拽排序：放下区块
function onDrop(targetIndex: number) {
  if (dragIndex.value === null || dragIndex.value === targetIndex) {
    onDragEnd()
    return
  }
  const updated = [...props.blocks]
  const [moved] = updated.splice(dragIndex.value, 1)
  updated.splice(targetIndex, 0, moved)
  emit('update:blocks', updated)
  onDragEnd()
}

// 拖拽排序：结束拖拽
function onDragEnd() {
  dragIndex.value = null
  dragOverIndex.value = null
}

// 更新编辑中区块的指定属性值
function onPropChange(key: string, value: unknown) {
  editingProps.value[key] = value
}

// 生成区块摘要文字（用于列表显示）
function getBlockSummary(block: PageBlock): string {
  const def = getDefinition(block.type)
  if (!def) return block.type
  const textField = def.schema.fields.find(f => f.type === 'text' || f.type === 'url')
  if (textField && block.props[textField.key]) {
    return String(block.props[textField.key])
  }
  return def.name
}

// 根据区块类型返回对应的标签颜色
function blockTypeTagType(type: string) {
  const tagTypes: Record<string, 'info' | 'success' | 'warning' | 'primary'> = {
    hero: 'info',
    pricing: 'success',
    faq: 'warning',
    cta: 'primary',
    'rich-text': 'info',
    'feature-grid': 'success',
    'image-banner': 'warning',
    stats: 'primary'
  }
  return tagTypes[type] || 'info'
}

// 复制指定区块并插入到其后
function duplicateBlock(index: number) {
  const block = props.blocks[index]
  const newBlock: PageBlock = {
    ...block,
    id: `block_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    props: { ...block.props }
  }
  const updated = [...props.blocks]
  updated.splice(index + 1, 0, newBlock)
  emit('update:blocks', updated)
}

// 更新列表字段中的某项子字段值
function updateListItem(fieldKey: string, itemIndex: number, subKey: string, value: string) {
  const items = [...((editingProps.value[fieldKey] as any[]) || [])]
  if (!items[itemIndex]) items[itemIndex] = {}
  items[itemIndex] = { ...items[itemIndex], [subKey]: value }
  editingProps.value[fieldKey] = items
}

// 向列表字段中添加一个新项
function addListItem(fieldKey: string) {
  const items = [...((editingProps.value[fieldKey] as any[]) || [])]
  items.push({})
  editingProps.value[fieldKey] = items
}

// 从列表字段中移除指定项
function removeListItem(fieldKey: string, index: number) {
  const items = [...((editingProps.value[fieldKey] as any[]) || [])]
  items.splice(index, 1)
  editingProps.value[fieldKey] = items
}
</script>

<style scoped>
.block-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.block-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 480px;
  overflow-y: auto;
}

.block-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 12px;
  background: #fafafa;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.block-item.drag-over {
  border-color: #2080f0;
  box-shadow: 0 0 0 2px rgba(32, 128, 240, 0.15);
  background: #f0f7ff;
}

.block-item-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.block-item-info {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.block-summary {
  font-size: 13px;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.block-item-actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}

.block-drag-handle {
  cursor: grab;
  color: #9ca3af;
  font-size: 18px;
  line-height: 1;
  user-select: none;
  flex-shrink: 0;
}
.block-empty {
  text-align: center;
  color: #9ca3af;
  padding: 32px 0;
  font-size: 14px;
}
.block-empty-icon {
  font-size: 36px;
  line-height: 1;
  margin-bottom: 8px;
  color: #d1d5db;
}
.block-empty p {
  margin: 4px 0;
}
.block-empty-hint {
  font-size: 12px;
  color: #bfc0c2;
}
.list-field {
  width: 100%;
}
.list-item {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  margin-bottom: 8px;
}

.add-block {
  display: flex;
  justify-content: center;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
