<template>
  <div v-if="loading && !loaded" style="display:flex;justify-content:center;padding:80px 0">
    <n-spin size="large" />
  </div>
  <div v-else class="form-page">
    <div class="page-head">
      <div class="page-head-inner">
        <div>
          <n-button text @click="router.push('/cms/block-definitions')">
            <template #icon>
              <n-icon size="16"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg></n-icon>
            </template>
            返回
          </n-button>
          <h2>{{ isNew ? '新建区块定义' : '编辑区块定义' }}</h2>
        </div>
        <div class="page-head-actions">
          <n-button type="primary" :loading="saving" @click="save">保存</n-button>
        </div>
      </div>
    </div>

    <n-card :bordered="true" style="border-radius: 8px; max-width: 720px;">
      <n-form label-placement="top">
        <n-form-item label="类型标识 (type)" required>
          <n-input v-model:value="form.type" placeholder="hero" :disabled="!isNew" />
        </n-form-item>
        <n-form-item label="显示名称 (name)" required>
          <n-input v-model:value="form.name" placeholder="Hero" />
        </n-form-item>
        <n-form-item label="排序序号" required>
          <n-input-number v-model:value="form.sortOrder" :min="1" :max="999" style="width: 100%" />
        </n-form-item>
        <n-form-item label="默认属性 (defaultProps)">
          <n-input v-model:value="form.defaultPropsStr" type="textarea" :rows="3" placeholder='{"key": "value"}' />
        </n-form-item>
      </n-form>

      <n-divider />

      <div class="fields-section">
        <div class="fields-header">
          <h3 style="margin:0">字段定义 (schema.fields)</h3>
          <n-button size="small" quaternary type="primary" @click="addField">+ 添加字段</n-button>
        </div>
        <div v-for="(field, i) in form.fields" :key="i" class="field-row">
          <div class="field-row-header">
            <span class="field-index">#{{ i + 1 }}</span>
            <n-button size="tiny" quaternary circle type="error" @click="removeField(i)">✕</n-button>
          </div>
          <div class="field-row-body">
            <n-input v-model:value="field.key" placeholder="字段 key" size="small" />
            <n-input v-model:value="field.label" placeholder="显示标签" size="small" />
            <n-select v-model:value="field.type" :options="fieldTypeOptions" size="small" style="width:120px" />
            <n-input v-model:value="field.defaultValue" placeholder="默认值" size="small" style="width:100px" />
            <n-checkbox v-model:checked="field.required">必填</n-checkbox>
          </div>
          <div v-if="field.type === 'list'" class="list-fields-section">
            <div class="list-fields-header">
              <span style="font-size:12px;color:#6b7280">子字段 (listFields):</span>
              <n-button size="tiny" quaternary @click="addListField(i)">+ 添加子字段</n-button>
            </div>
            <div v-for="(lf, li) in field.listFields" :key="li" class="list-field-row">
              <n-input v-model:value="lf.key" placeholder="key" size="tiny" style="width:100px" />
              <n-input v-model:value="lf.label" placeholder="label" size="tiny" style="width:120px" />
              <n-button size="tiny" quaternary circle type="error" @click="removeListField(i, li)">✕</n-button>
            </div>
          </div>
        </div>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NCheckbox, NDivider, NForm, NFormItem, NIcon, NInput, NInputNumber, NSelect, NSpin, useMessage } from 'naive-ui'
import type { SelectOption } from 'naive-ui'
import { getBlockDefinitions, createBlockDefinition, updateBlockDefinition } from '../../api/block'

interface FieldForm {
  key: string
  label: string
  type: string
  defaultValue: string
  required: boolean
  listFields: Array<{ key: string; label: string }>
}

const route = useRoute()
const router = useRouter()
const message = useMessage()
const isNew = route.params.id === 'new'
const definitionId = isNew ? null : Number(route.params.id)
const loading = ref(false)
const loaded = ref(false)
const saving = ref(false)

const form = reactive({
  type: '',
  name: '',
  sortOrder: 50,
  defaultPropsStr: '',
  fields: [] as FieldForm[]
})

const fieldTypeOptions: SelectOption[] = [
  { label: '文本', value: 'text' },
  { label: '多行文本', value: 'textarea' },
  { label: 'URL', value: 'url' },
  { label: '数字', value: 'number' },
  { label: '富文本', value: 'rich-text' },
  { label: '列表', value: 'list' }
]

function addField() {
  form.fields.push({ key: '', label: '', type: 'text', defaultValue: '', required: false, listFields: [] })
}

function removeField(index: number) {
  form.fields.splice(index, 1)
}

function addListField(fieldIndex: number) {
  form.fields[fieldIndex].listFields.push({ key: '', label: '' })
}

function removeListField(fieldIndex: number, listIndex: number) {
  form.fields[fieldIndex].listFields.splice(listIndex, 1)
}

async function load() {
  if (isNew) {
    loaded.value = true
    return
  }
  loading.value = true
  try {
    const all = await getBlockDefinitions()
    const def = all.find((d: any) => d.id === definitionId)
    if (!def) {
      message.error('区块定义不存在')
      router.push('/cms/block-definitions')
      return
    }
    form.type = def.type
    form.name = def.name
    form.sortOrder = (def as any).sortOrder ?? 50
    form.defaultPropsStr = JSON.stringify(def.defaultProps ?? {}, null, 2)
    const rawFields = ((def.schema as any)?.fields ?? []) as any[]
    form.fields = rawFields.map((f: any) => ({
      key: f.key ?? '',
      label: f.label ?? '',
      type: f.type ?? 'text',
      defaultValue: f.defaultValue !== undefined ? String(f.defaultValue) : '',
      required: !!f.required,
      listFields: (f.listFields ?? []).map((lf: any) => ({ key: lf.key ?? '', label: lf.label ?? '' }))
    }))
    loaded.value = true
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.type || !form.name) {
    message.warning('请填写类型标识和显示名称')
    return
  }
  saving.value = true
  try {
    let defaultProps: Record<string, unknown> = {}
    if (form.defaultPropsStr.trim()) {
      try {
        defaultProps = JSON.parse(form.defaultPropsStr)
      } catch {
        message.warning('默认属性 JSON 格式错误')
        saving.value = false
        return
      }
    }
    const schema = {
      fields: form.fields.map(f => {
        const field: Record<string, unknown> = {
          key: f.key,
          label: f.label,
          type: f.type
        }
        if (f.defaultValue) field.defaultValue = f.defaultValue
        if (f.required) field.required = true
        if (f.type === 'list' && f.listFields.length > 0) {
          field.listFields = f.listFields.map(lf => ({ key: lf.key, label: lf.label }))
        }
        return field
      })
    }
    if (isNew) {
      await createBlockDefinition({
        type: form.type,
        name: form.name,
        schema,
        defaultProps,
        sortOrder: form.sortOrder
      })
      message.success('区块定义已创建')
    } else {
      await updateBlockDefinition(definitionId!, {
        type: form.type,
        name: form.name,
        schema,
        defaultProps,
        sortOrder: form.sortOrder
      })
      message.success('区块定义已更新')
    }
    router.push('/cms/block-definitions')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => { load() })
</script>

<style scoped>
.form-page {
  max-width: 800px;
}
.fields-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.fields-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.field-row {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}
.field-row-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.field-index {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
}
.field-row-body {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}
.list-fields-section {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #d1d5db;
}
.list-fields-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.list-field-row {
  display: flex;
  gap: 6px;
  align-items: center;
  margin-bottom: 4px;
}
</style>
