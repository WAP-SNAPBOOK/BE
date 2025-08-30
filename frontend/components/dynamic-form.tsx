"use client"

import type React from "react"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Upload, X } from "lucide-react"
import type { FormField } from "./form-builder"
import { toast } from "@/hooks/use-toast"

interface DynamicFormProps {
  fields: FormField[]
}

export function DynamicForm({ fields }: DynamicFormProps) {
  const [formData, setFormData] = useState<Record<string, any>>({})
  const [uploadedFiles, setUploadedFiles] = useState<Record<string, File[]>>({})

  const handleInputChange = (fieldId: string, value: any) => {
    setFormData((prev) => ({
      ...prev,
      [fieldId]: value,
    }))
  }

  const handleFileUpload = (fieldId: string, files: FileList | null) => {
    if (files) {
      const fileArray = Array.from(files)
      setUploadedFiles((prev) => ({
        ...prev,
        [fieldId]: fileArray,
      }))
      handleInputChange(fieldId, fileArray)
    }
  }

  const removeFile = (fieldId: string, fileIndex: number) => {
    setUploadedFiles((prev) => {
      const newFiles = [...(prev[fieldId] || [])]
      newFiles.splice(fileIndex, 1)
      return {
        ...prev,
        [fieldId]: newFiles,
      }
    })
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    // 필수 필드 검증
    const missingFields = fields.filter((field) => field.required && !formData[field.id]).map((field) => field.label)

    if (missingFields.length > 0) {
      toast({
        title: "필수 항목을 입력해주세요",
        description: `다음 항목들이 필요합니다: ${missingFields.join(", ")}`,
        variant: "destructive",
      })
      return
    }

    toast({
      title: "예약 신청 완료!",
      description: "예약 신청이 성공적으로 접수되었습니다.",
    })

    console.log("Form Data:", formData)
    console.log("Uploaded Files:", uploadedFiles)
  }

  const shouldShowRemovalShopField = (field: FormField) => {
    if (!field.label.includes("제거 샵 선택")) return true

    // Find the corresponding removal need field
    const removalNeedField = fields.find((f) => f.label.includes("제거가 필요하신가요?"))
    if (!removalNeedField) return true

    // Only show shop selection if user answered "예" to removal need
    return formData[removalNeedField.id] === "예"
  }

  const renderField = (field: FormField) => {
    const value = formData[field.id] || ""

    switch (field.type) {
      case "text":
        return (
          <Input
            value={value}
            onChange={(e) => handleInputChange(field.id, e.target.value)}
            placeholder={field.placeholder}
            required={field.required}
          />
        )

      case "textarea":
        return (
          <Textarea
            value={value}
            onChange={(e) => handleInputChange(field.id, e.target.value)}
            placeholder={field.placeholder}
            required={field.required}
            className="min-h-[100px]"
          />
        )

      case "phone":
        return (
          <Input
            type="tel"
            value={value}
            onChange={(e) => handleInputChange(field.id, e.target.value)}
            placeholder={field.placeholder}
            required={field.required}
          />
        )

      case "number":
        return (
          <Input
            type="number"
            value={value}
            onChange={(e) => handleInputChange(field.id, Number.parseInt(e.target.value) || 0)}
            placeholder={field.placeholder}
            min="0"
            required={field.required}
          />
        )

      case "date":
        return (
          <Input
            type="date"
            value={value}
            onChange={(e) => handleInputChange(field.id, e.target.value)}
            required={field.required}
          />
        )

      case "time":
        return (
          <Input
            type="time"
            value={value}
            onChange={(e) => handleInputChange(field.id, e.target.value)}
            required={field.required}
          />
        )

      case "datetime":
        return (
          <Input
            type="datetime-local"
            value={value}
            onChange={(e) => handleInputChange(field.id, e.target.value)}
            required={field.required}
          />
        )

      case "radio":
        return (
          <RadioGroup
            value={value}
            onValueChange={(value) => handleInputChange(field.id, value)}
            required={field.required}
          >
            {field.options?.map((option) => (
              <div key={option} className="flex items-center space-x-2">
                <RadioGroupItem value={option} id={`${field.id}_${option}`} />
                <Label htmlFor={`${field.id}_${option}`}>{option}</Label>
              </div>
            ))}
          </RadioGroup>
        )

      case "select":
        return (
          <Select value={value} onValueChange={(value) => handleInputChange(field.id, value)} required={field.required}>
            <SelectTrigger>
              <SelectValue placeholder="선택해주세요" />
            </SelectTrigger>
            <SelectContent>
              {field.options?.map((option) => (
                <SelectItem key={option} value={option}>
                  {option}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        )

      case "file":
        return (
          <div className="space-y-3">
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-gray-400 transition-colors">
              <input
                type="file"
                id={field.id}
                multiple
                accept="image/*"
                onChange={(e) => handleFileUpload(field.id, e.target.files)}
                className="hidden"
              />
              <label htmlFor={field.id} className="cursor-pointer">
                <Upload className="h-8 w-8 mx-auto mb-2 text-gray-400" />
                <p className="text-sm text-gray-600">클릭하여 파일을 선택하거나 드래그하여 업로드</p>
                <p className="text-xs text-gray-500 mt-1">이미지 파일만 업로드 가능합니다</p>
              </label>
            </div>

            {uploadedFiles[field.id] && uploadedFiles[field.id].length > 0 && (
              <div className="space-y-2">
                <p className="text-sm font-medium">업로드된 파일:</p>
                {uploadedFiles[field.id].map((file, index) => (
                  <div key={index} className="flex items-center justify-between p-2 bg-gray-50 rounded">
                    <span className="text-sm truncate">{file.name}</span>
                    <Button variant="ghost" size="sm" onClick={() => removeFile(field.id, index)}>
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )

      default:
        return (
          <Input
            value={value}
            onChange={(e) => handleInputChange(field.id, e.target.value)}
            placeholder={field.placeholder}
            required={field.required}
          />
        )
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>예약 신청서</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-6">
          {fields.map((field) => {
            if (!shouldShowRemovalShopField(field)) {
              return null
            }

            return (
              <div key={field.id} className="space-y-2">
                <Label htmlFor={field.id} className="text-sm font-medium">
                  {field.label}
                  {field.required && <span className="text-red-500 ml-1">*</span>}
                </Label>
                {renderField(field)}
                {field.description && <p className="text-xs text-gray-500">{field.description}</p>}
              </div>
            )
          })}

          <Button type="submit" className="w-full" size="lg">
            예약 신청하기
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}
