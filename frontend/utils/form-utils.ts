import type { FormField, FileInfo } from "@/types/booking"

export class FormUtils {
  static initializeFormValues(fields: FormField[]): Record<string, any> {
    const values: Record<string, any> = {}

    fields.forEach((field) => {
      if (field.type === "number") {
        values[field.fieldId] = 0
      } else if (field.type === "file") {
        values[field.fieldId] = []
      } else {
        values[field.fieldId] = ""
      }
    })

    return values
  }

  static initializeFileStorage(fields: FormField[]): Record<string, File[]> {
    const files: Record<string, File[]> = {}

    fields.forEach((field) => {
      if (field.type === "file") {
        files[field.fieldId] = []
      }
    })

    return files
  }

  static validateRequiredFields(fields: FormField[], values: Record<string, any>): FormField[] {
    const requiredFields = fields.filter((field) => field.required)
    return requiredFields.filter((field) => {
      const value = values[field.fieldId]
      return !value || (Array.isArray(value) && value.length === 0)
    })
  }

  static createFormData(
    storeId: string,
    formValues: Record<string, any>,
    uploadedFiles: Record<string, File[]>,
  ): FormData {
    const formData = new FormData()
    formData.append("storeId", storeId)

    // Add form values (excluding file fields)
    Object.entries(formValues).forEach(([key, value]) => {
      if (!key.includes("file") && !key.includes("photo")) {
        formData.append(key, String(value))
      }
    })

    // Add files
    Object.entries(uploadedFiles).forEach(([fieldId, files]) => {
      files.forEach((file, index) => {
        formData.append(`${fieldId}_${index}`, file)
      })
    })

    return formData
  }

  static createFileInfo(file: File): FileInfo {
    return {
      name: file.name,
      size: file.size,
      type: file.type,
      lastModified: file.lastModified,
    }
  }
}
