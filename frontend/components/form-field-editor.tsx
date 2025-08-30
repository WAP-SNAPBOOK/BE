"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Switch } from "@/components/ui/switch"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import type { FormField } from "./form-builder"

interface FormFieldEditorProps {
  field: FormField
  onSave: (field: FormField) => void
  onCancel: () => void
}

export function FormFieldEditor({ field, onSave, onCancel }: FormFieldEditorProps) {
  const [editedField, setEditedField] = useState<FormField>({ ...field })

  const handleSave = () => {
    onSave(editedField)
  }

  const addOption = () => {
    const newOptions = [...(editedField.options || []), "새 옵션"]
    setEditedField({ ...editedField, options: newOptions })
  }

  const updateOption = (index: number, value: string) => {
    const newOptions = [...(editedField.options || [])]
    newOptions[index] = value
    setEditedField({ ...editedField, options: newOptions })
  }

  const removeOption = (index: number) => {
    const newOptions = [...(editedField.options || [])]
    newOptions.splice(index, 1)
    setEditedField({ ...editedField, options: newOptions })
  }

  const fieldTypes = [
    { value: "text", label: "텍스트" },
    { value: "textarea", label: "긴 텍스트" },
    { value: "phone", label: "전화번호" },
    { value: "number", label: "숫자" },
    { value: "date", label: "날짜" },
    { value: "time", label: "시간" },
    { value: "datetime", label: "날짜 및 시간" },
    { value: "file", label: "파일 업로드" },
    { value: "radio", label: "라디오 버튼" },
    { value: "select", label: "드롭다운" },
  ]

  return (
    <Dialog open={true} onOpenChange={onCancel}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>필드 편집</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div>
            <Label htmlFor="label">필드 이름</Label>
            <Input
              id="label"
              value={editedField.label}
              onChange={(e) => setEditedField({ ...editedField, label: e.target.value })}
            />
          </div>

          <div>
            <Label htmlFor="type">필드 타입</Label>
            <Select
              value={editedField.type}
              onValueChange={(value: any) => setEditedField({ ...editedField, type: value })}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {fieldTypes.map((type) => (
                  <SelectItem key={type.value} value={type.value}>
                    {type.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div>
            <Label htmlFor="placeholder">플레이스홀더</Label>
            <Input
              id="placeholder"
              value={editedField.placeholder || ""}
              onChange={(e) => setEditedField({ ...editedField, placeholder: e.target.value })}
            />
          </div>

          <div>
            <Label htmlFor="description">설명</Label>
            <Textarea
              id="description"
              value={editedField.description || ""}
              onChange={(e) => setEditedField({ ...editedField, description: e.target.value })}
              rows={2}
            />
          </div>

          <div className="flex items-center space-x-2">
            <Switch
              id="required"
              checked={editedField.required || false}
              onCheckedChange={(checked) => setEditedField({ ...editedField, required: checked })}
            />
            <Label htmlFor="required">필수 항목</Label>
          </div>

          {(editedField.type === "radio" || editedField.type === "select") && (
            <div>
              <Label>선택 옵션</Label>
              <div className="space-y-2 mt-2">
                {editedField.options?.map((option, index) => (
                  <div key={index} className="flex gap-2">
                    <Input
                      value={option}
                      onChange={(e) => updateOption(index, e.target.value)}
                      placeholder="옵션 이름"
                    />
                    <Button variant="outline" size="sm" onClick={() => removeOption(index)}>
                      삭제
                    </Button>
                  </div>
                ))}
                <Button variant="outline" size="sm" onClick={addOption}>
                  옵션 추가
                </Button>
              </div>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onCancel}>
            취소
          </Button>
          <Button onClick={handleSave}>저장</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
