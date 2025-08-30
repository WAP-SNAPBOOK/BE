"use client"

import type React from "react"
import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Checkbox } from "@/components/ui/checkbox"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Upload, Plus, Trash2, Edit3, Save, X } from "lucide-react"
import { toast } from "@/hooks/use-toast"

interface FormField {
  id: string
  label: string
  type: "text" | "tel" | "date" | "time" | "radio" | "checkbox" | "number" | "file"
  required: boolean
  options?: string[]
}

export default function FormEditorPage() {
  const [fields, setFields] = useState<FormField[]>([
    { id: "name", label: "성함", type: "text", required: true },
    { id: "contact", label: "연락처", type: "tel", required: true },
    { id: "appointmentDate", label: "예약 날짜", type: "date", required: true },
    { id: "appointmentTime", label: "예약 시간", type: "time", required: true },
    { id: "treatmentType", label: "원하는 시술 항목", type: "radio", required: true, options: ["네일", "패디"] },
    { id: "designPhotos", label: "디자인 사진 첨부", type: "file", required: false },
    { id: "removal", label: "제거 유무", type: "radio", required: false, options: ["필요없음", "자샵", "타샵"] },
    {
      id: "wrapping",
      label: "래핑 개수",
      type: "number",
      required: false,
    },
    {
      id: "extension",
      label: "연장 개수",
      type: "number",
      required: false,
    },
  ])

  const [editingField, setEditingField] = useState<string | null>(null)
  const [tempFieldData, setTempFieldData] = useState<FormField | null>(null)
  const [formData, setFormData] = useState<Record<string, any>>({})
  const [previewFormData, setPreviewFormData] = useState<Record<string, any>>({})
  const [tempOptions, setTempOptions] = useState<string[]>([])

  const handleFieldUpdate = (fieldId: string, updates: Partial<FormField>) => {
    if (tempFieldData) {
      setTempFieldData({ ...tempFieldData, ...updates })
    }
  }

  const saveFieldChanges = () => {
    if (tempFieldData) {
      setFields((prev) => prev.map((field) => (field.id === tempFieldData.id ? tempFieldData : field)))
      setEditingField(null)
      setTempFieldData(null)
    }
  }

  const cancelFieldChanges = () => {
    setEditingField(null)
    setTempFieldData(null)
  }

  const startEditing = (field: FormField) => {
    setEditingField(field.id)
    setTempFieldData({ ...field })
    if (field.type === "radio" || field.type === "checkbox") {
      setTempOptions(field.options || [""])
    }
  }

  const addField = () => {
    const newField: FormField = {
      id: `field_${Date.now()}`,
      label: "새 필드",
      type: "text",
      required: false,
    }
    setFields((prev) => [...prev, newField])
    startEditing(newField)
  }

  const deleteField = (fieldId: string) => {
    setFields((prev) => prev.filter((field) => field.id !== fieldId))
  }

  const handleInputChange = (fieldId: string, value: any) => {
    setFormData((prev) => ({
      ...prev,
      [fieldId]: value,
    }))
  }

  const handlePreviewInputChange = (fieldId: string, value: any) => {
    setPreviewFormData((prev) => ({
      ...prev,
      [fieldId]: value,
    }))
  }

  const addOption = () => {
    setTempOptions([...tempOptions, ""])
  }

  const updateOption = (index: number, value: string) => {
    const newOptions = [...tempOptions]
    newOptions[index] = value
    setTempOptions(newOptions)
    if (tempFieldData) {
      const updatedOptions = newOptions.filter(Boolean)
      setTempFieldData({ ...tempFieldData, options: updatedOptions })
      handleFieldUpdate(tempFieldData.id, { options: updatedOptions })
    }
  }

  const removeOption = (index: number) => {
    const newOptions = tempOptions.filter((_, i) => i !== index)
    setTempOptions(newOptions)
    if (tempFieldData) {
      const updatedOptions = newOptions.filter(Boolean)
      setTempFieldData({ ...tempFieldData, options: updatedOptions })
      handleFieldUpdate(tempFieldData.id, { options: updatedOptions })
    }
  }

  const renderField = (field: FormField) => {
    const isEditing = editingField === field.id
    const displayField = isEditing && tempFieldData ? tempFieldData : field

    return (
      <div key={field.id} className="space-y-2 p-4 border rounded-lg relative group">
        <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity flex gap-1">
          {!isEditing && (
            <>
              <Button variant="ghost" size="sm" onClick={() => startEditing(field)}>
                <Edit3 className="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => deleteField(field.id)}
                className="text-red-500 hover:text-red-700"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </>
          )}
        </div>

        {isEditing ? (
          <div className="space-y-3 bg-blue-50 p-3 rounded">
            <div>
              <Label className="text-xs">필드명</Label>
              <Input
                value={displayField.label}
                onChange={(e) => handleFieldUpdate(field.id, { label: e.target.value })}
                className="text-sm"
              />
            </div>
            <div>
              <Label className="text-xs">필드 타입</Label>
              <Select
                value={displayField.type}
                onValueChange={(value) => handleFieldUpdate(field.id, { type: value as FormField["type"] })}
              >
                <SelectTrigger className="text-sm">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="text">텍스트</SelectItem>
                  <SelectItem value="tel">전화번호</SelectItem>
                  <SelectItem value="date">날짜</SelectItem>
                  <SelectItem value="time">시간</SelectItem>
                  <SelectItem value="radio">라디오 버튼</SelectItem>
                  <SelectItem value="checkbox">체크박스</SelectItem>
                  <SelectItem value="number">숫자</SelectItem>
                  <SelectItem value="file">파일 첨부</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {(displayField.type === "radio" || displayField.type === "checkbox") && (
              <div>
                <Label className="text-xs">옵션</Label>
                <div className="space-y-2">
                  {tempOptions.map((option, index) => (
                    <div key={index} className="flex gap-2">
                      <Input
                        value={option}
                        onChange={(e) => updateOption(index, e.target.value)}
                        placeholder={`옵션 ${index + 1}`}
                        className="text-sm flex-1"
                      />
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={() => removeOption(index)}
                        className="text-red-500 hover:text-red-700"
                      >
                        <Trash2 className="h-3 w-3" />
                      </Button>
                    </div>
                  ))}
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={addOption}
                    className="w-full border-dashed bg-transparent"
                  >
                    <Plus className="h-3 w-3 mr-1" />
                    옵션 추가
                  </Button>
                </div>
              </div>
            )}
            <div className="flex items-center space-x-2">
              <Checkbox
                checked={displayField.required}
                onCheckedChange={(checked) => handleFieldUpdate(field.id, { required: !!checked })}
              />
              <Label className="text-xs">필수 항목</Label>
            </div>
            <div className="flex gap-2 pt-2">
              <Button size="sm" onClick={saveFieldChanges} className="flex-1">
                <Save className="h-3 w-3 mr-1" />
                저장
              </Button>
              <Button size="sm" variant="outline" onClick={cancelFieldChanges} className="flex-1 bg-transparent">
                <X className="h-3 w-3 mr-1" />
                취소
              </Button>
            </div>
          </div>
        ) : (
          <div>
            <Label className="text-sm font-medium">
              {field.label} {field.required && <span className="text-red-500">*</span>}
            </Label>

            {field.type === "text" && (
              <Input
                value={formData[field.id] || ""}
                onChange={(e) => handleInputChange(field.id, e.target.value)}
                placeholder={`${field.label}을 입력해주세요`}
              />
            )}

            {field.type === "tel" && (
              <Input
                type="tel"
                value={formData[field.id] || ""}
                onChange={(e) => handleInputChange(field.id, e.target.value)}
                placeholder="010-0000-0000"
              />
            )}

            {field.type === "date" && (
              <Input
                type="date"
                value={formData[field.id] || ""}
                onChange={(e) => handleInputChange(field.id, e.target.value)}
              />
            )}

            {field.type === "time" && (
              <Input
                type="time"
                value={formData[field.id] || ""}
                onChange={(e) => handleInputChange(field.id, e.target.value)}
              />
            )}

            {field.type === "number" && (
              <Input
                type="number"
                min="0"
                value={formData[field.id] || ""}
                onChange={(e) => handleInputChange(field.id, Number.parseInt(e.target.value) || 0)}
                placeholder="개수를 입력해주세요"
              />
            )}

            {field.type === "radio" && field.options && (
              <RadioGroup
                value={formData[field.id] || ""}
                onValueChange={(value) => handleInputChange(field.id, value)}
                className="space-y-2 mt-4"
              >
                {field.options.map((option) => (
                  <div key={option} className="flex items-center space-x-3">
                    <RadioGroupItem value={option} id={`${field.id}_${option}`} />
                    <Label htmlFor={`${field.id}_${option}`}>{option}</Label>
                  </div>
                ))}
              </RadioGroup>
            )}

            {field.type === "checkbox" && (
              <div className="flex items-center space-x-2">
                <Checkbox
                  checked={formData[field.id] || false}
                  onCheckedChange={(checked) => handleInputChange(field.id, checked)}
                />
              </div>
            )}

            {field.type === "file" && (
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-gray-400 transition-colors">
                <Upload className="h-8 w-8 mx-auto mb-2 text-gray-400" />
                <p className="text-sm text-gray-600">클릭하여 파일을 선택하거나 드래그하여 업로드</p>
              </div>
            )}
          </div>
        )}
      </div>
    )
  }

  const renderPreviewField = (field: FormField) => {
    const displayField = editingField === field.id && tempFieldData ? tempFieldData : field
    const displayOptions =
      editingField === field.id && (displayField.type === "radio" || displayField.type === "checkbox")
        ? tempOptions.filter(Boolean)
        : displayField.options

    return (
      <div key={field.id} className="space-y-2">
        <Label className="text-sm font-medium">
          {displayField.label} {displayField.required && <span className="text-red-500">*</span>}
        </Label>

        {displayField.type === "text" && (
          <Input
            value={previewFormData[field.id] || ""}
            onChange={(e) => handlePreviewInputChange(field.id, e.target.value)}
            placeholder={`${displayField.label}을 입력해주세요`}
          />
        )}

        {displayField.type === "tel" && (
          <Input
            type="tel"
            value={previewFormData[field.id] || ""}
            onChange={(e) => handlePreviewInputChange(field.id, e.target.value)}
            placeholder="010-0000-0000"
          />
        )}

        {displayField.type === "date" && (
          <Input
            type="date"
            value={previewFormData[field.id] || ""}
            onChange={(e) => handlePreviewInputChange(field.id, e.target.value)}
          />
        )}

        {displayField.type === "time" && (
          <Input
            type="time"
            value={previewFormData[field.id] || ""}
            onChange={(e) => handlePreviewInputChange(field.id, e.target.value)}
          />
        )}

        {displayField.type === "number" && (
          <Input
            type="number"
            min="0"
            value={previewFormData[field.id] || ""}
            onChange={(e) => handlePreviewInputChange(field.id, Number.parseInt(e.target.value) || 0)}
            placeholder="개수를 입력해주세요"
          />
        )}

        {displayField.type === "radio" && displayOptions && (
          <RadioGroup
            value={previewFormData[field.id] || ""}
            onValueChange={(value) => handlePreviewInputChange(field.id, value)}
            className="space-y-2 mt-4"
          >
            {displayOptions.map((option) => (
              <div key={option} className="flex items-center space-x-3">
                <RadioGroupItem value={option} id={`preview_${field.id}_${option}`} />
                <Label htmlFor={`preview_${field.id}_${option}`}>{option}</Label>
              </div>
            ))}
          </RadioGroup>
        )}

        {displayField.type === "checkbox" && (
          <div className="flex items-center space-x-2">
            <Checkbox
              checked={previewFormData[field.id] || false}
              onCheckedChange={(checked) => handlePreviewInputChange(field.id, checked)}
            />
          </div>
        )}

        {displayField.type === "file" && (
          <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-gray-400 transition-colors">
            <Upload className="h-8 w-8 mx-auto mb-2 text-gray-400" />
            <p className="text-sm text-gray-600">클릭하여 파일을 선택하거나 드래그하여 업로드</p>
          </div>
        )}
      </div>
    )
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    toast({
      title: "폼 설정 저장 완료!",
      description: "폼 구성이 성공적으로 저장되었습니다.",
    })
    console.log("Form Fields:", fields)
    console.log("Form Data:", formData)
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Editor Panel */}
          <Card>
            <CardHeader>
              <CardTitle className="text-center text-2xl">폼 편집기</CardTitle>
              <p className="text-center text-sm text-gray-600">필드를 클릭하여 편집하거나 새 필드를 추가하세요</p>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-6">
                {fields.map(renderField)}

                <Button
                  type="button"
                  variant="outline"
                  onClick={addField}
                  className="w-full border-dashed bg-transparent"
                >
                  <Plus className="h-4 w-4 mr-2" />새 필드 추가
                </Button>

                <Button type="submit" className="w-full" size="lg">
                  폼 설정 저장
                </Button>
              </form>
            </CardContent>
          </Card>

          {/* Preview Panel */}
          <Card>
            <CardHeader>
              <CardTitle className="text-center text-2xl">미리보기</CardTitle>
              <p className="text-center text-sm text-gray-600">실제 폼이 어떻게 보이는지 확인하세요</p>
            </CardHeader>
            <CardContent>
              <div className="space-y-6">
                {fields.map(renderPreviewField)}

                <Button className="w-full" size="lg">
                  예약 신청하기
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
