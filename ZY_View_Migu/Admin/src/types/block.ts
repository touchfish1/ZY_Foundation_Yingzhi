export interface FieldDef {
  key: string
  label: string
  type: 'text' | 'textarea' | 'url' | 'number' | 'rich-text' | 'list'
  required?: boolean
  defaultValue?: unknown
}

export interface BlockDefinition {
  type: string
  name: string
  schema: { fields: FieldDef[] }
  defaultProps: Record<string, unknown>
}

export interface PageBlock {
  id: string
  type: string
  props: Record<string, unknown>
}
