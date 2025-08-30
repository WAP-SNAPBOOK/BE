export enum FieldType {
  TEXT = "TEXT",
  NUMBER = "NUMBER",
  RADIO = "RADIO",
  CHECKBOX = "CHECKBOX",
  FILE = "FILE",
  DATETIME_LOCAL = "DATETIME_LOCAL",
  TEXTAREA = "TEXTAREA",
}

export interface FormField {
  fieldId: string // Changed id to fieldId for better clarity
  label: string
  type: FieldType // Changed from string to FieldType enum
  required: boolean
  placeholder?: string
  options?: string[]
  min?: number
  max?: number
  accept?: string
}

export interface StoreConfig {
  id: string
  name: string
  title: string
  formFields: FormField[]
}

export interface FileInfo {
  name: string
  size: number
  type: string
  lastModified: number
}
