"use client"
import type { FormField } from "@/types/booking"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Button } from "@/components/ui/button"
import { Upload, X } from "lucide-react"

interface FormFieldRendererProps {
  field: FormField
  value: any
  onValueChange: (value: any) => void
  onFileUpload?: (files: FileList | null) => void
  onFileRemove?: (index: number) => void
}

export function FormFieldRenderer({ field, value, onValueChange, onFileUpload, onFileRemove }: FormFieldRendererProps) {
  const renderField = () => {
    const fieldValue = value || (field.type === "NUMBER" ? 0 : field.type === "FILE" ? [] : "")

    switch (field.type) {
      case "TEXT":
        return (
          <Input
            id={field.fieldId}
            type="text"
            value={fieldValue}
            onChange={(e) => onValueChange(e.target.value)}
            placeholder={field.placeholder}
            required={field.required}
          />
        )

      case "NUMBER":
        return (
          <Input
            id={field.fieldId}
            type="number"
            min={field.min || 0}
            max={field.max}
            value={fieldValue}
            onChange={(e) => onValueChange(Number.parseInt(e.target.value) || 0)}
            required={field.required}
          />
        )

      case "RADIO":
        return (
          <RadioGroup value={fieldValue} onValueChange={onValueChange} className="space-y-2 mt-4">
            {field.options?.map((option: string) => (
              <div key={option} className="flex items-center space-x-3">
                <RadioGroupItem value={option} id={`${field.fieldId}_${option}`} />
                <Label htmlFor={`${field.fieldId}_${option}`}>{option}</Label>
              </div>
            ))}
          </RadioGroup>
        )

      case "FILE":
        return (
          <div className="space-y-3">
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-gray-400 transition-colors">
              <input
                type="file"
                id={field.fieldId}
                multiple
                accept={field.accept}
                onChange={(e) => onFileUpload?.(e.target.files)}
                className="hidden"
              />
              <label htmlFor={field.fieldId} className="cursor-pointer">
                <Upload className="h-8 w-8 mx-auto mb-2 text-gray-400" />
                <p className="text-sm text-gray-600">클릭하여 파일을 선택하거나 드래그하여 업로드</p>
                <p className="text-xs text-gray-500 mt-1">이미지 파일만 업로드 가능합니다</p>
              </label>
            </div>

            {fieldValue.length > 0 && (
              <div className="space-y-2">
                <p className="text-sm font-medium">업로드된 파일:</p>
                {fieldValue.map((fileInfo: any, index: number) => (
                  <div key={index} className="flex items-center justify-between p-2 bg-gray-50 rounded">
                    <span className="text-sm truncate">{fileInfo.name}</span>
                    <Button variant="ghost" size="sm" onClick={() => onFileRemove?.(index)}>
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )

      case "DATETIME_LOCAL":
        return (
          <Input
            id={field.fieldId}
            type="datetime-local"
            value={fieldValue}
            onChange={(e) => onValueChange(e.target.value)}
            required={field.required}
          />
        )

      case "TEXTAREA":
        return (
          <textarea
            id={field.fieldId}
            value={fieldValue}
            onChange={(e) => onValueChange(e.target.value)}
            placeholder={field.placeholder}
            required={field.required}
            className="w-full p-2 border border-gray-300 rounded-md"
            rows={4}
          />
        )

      default:
        return null
    }
  }

  return (
    <div className="space-y-2">
      <Label htmlFor={field.fieldId} className="text-sm font-medium">
        {field.label} {field.required && <span className="text-red-500">*</span>}
      </Label>
      {renderField()}
    </div>
  )
}
