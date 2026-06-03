<template>
  <div class="block-editor">
    <div class="block-list">
      <div v-for="(block, i) in blocks" :key="block.id" class="block-item">
        <div class="block-item-main">
          <div class="block-item-info">
            <n-tag :bordered="false" size="small" type="info">
              {{ getDefinition(block.type)?.name || block.type }}
            </n-tag>
            <span class="block-summary">{{ getBlockSummary(block) }}</span>
          </div>
          <div class="block-item-actions">
            <n-button size="tiny" quaternary @click="openEdit(i)">编辑</n-button>
            <n-button size="tiny" quaternary @click="removeBlock(i)">删除</n-button>
            <n-button size="tiny" quaternary :disabled="i === 0" @click="moveBlock(i, -1)">▲</n-button>
            <n-button size="tiny" quaternary :disabled="i === blocks.length - 1" @click="moveBlock(i, 1)">▼</n-button>
          </div>
        </div>
      </div>
      <div v-if="blocks.length === 0" class="block-empty">暂无区块，点击下方按钮添加</div>
    </div>

    <div class="add-block">
      <n-dropdown placement="top-start" trigger="click" :options="addOptions" @select="onAddSelect">
        <n-button :disabled="isEditing">+ 添加区块</n-button>
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
            :placeholder="`请输入${field.label}`"
          />
          <n-input-number
            v-else-if="field.type === 'number'"
            :value="editingProps[field.key] as number"
            @update:value="(v: number | null) => onPropChange(field.key, v)"
            :placeholder="`请输入${field.label}`"
          />
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

function getDefinition(type: string): BlockDefinition | undefined {
  return props.definitions.find(d => d.type === type)
}

function initEditingProps(source: Record<string, unknown>, def: BlockDefinition): Record<string, unknown> {
  const result: Record<string, unknown> = {}
  for (const field of def.schema.fields) {
    result[field.key] = field.key in source ? source[field.key] : (field.defaultValue ?? '')
  }
  return result
}

function openEdit(index: number) {
  editingIndex.value = index
  selectedAddType.value = null
  const block = props.blocks[index]
  const def = getDefinition(block.type)
  if (def) {
    editingProps.value = initEditingProps(block.props, def)
  }
  showModal.value = true
}

function onAddSelect(type: string) {
  editingIndex.value = null
  selectedAddType.value = type
  const def = getDefinition(type)
  if (def) {
    editingProps.value = initEditingProps({}, def)
  }
  showModal.value = true
}

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

function removeBlock(index: number) {
  const updated = props.blocks.filter((_, i) => i !== index)
  emit('update:blocks', updated)
}

function moveBlock(index: number, direction: -1 | 1) {
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= props.blocks.length) return
  const updated = [...props.blocks]
  const [removed] = updated.splice(index, 1)
  updated.splice(newIndex, 0, removed)
  emit('update:blocks', updated)
}

function onPropChange(key: string, value: unknown) {
  editingProps.value[key] = value
}

function getBlockSummary(block: PageBlock): string {
  const def = getDefinition(block.type)
  if (!def) return block.type
  const textField = def.schema.fields.find(f => f.type === 'text' || f.type === 'url')
  if (textField && block.props[textField.key]) {
    return String(block.props[textField.key])
  }
  return def.name
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

.block-empty {
  text-align: center;
  color: #9ca3af;
  padding: 32px 0;
  font-size: 14px;
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
