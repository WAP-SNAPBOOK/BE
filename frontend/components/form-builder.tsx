"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Badge } from "@/components/ui/badge"
import { Copy, Wand2, Plus, Settings, Trash2 } from "lucide-react"
import { DynamicForm } from "./dynamic-form"
import { FormFieldEditor } from "./form-field-editor"
import { toast } from "@/hooks/use-toast"

export interface FormField {
  id: string
  label: string
  type: "text" | "phone" | "date" | "time" | "datetime" | "file" | "radio" | "select" | "number" | "textarea"
  options?: string[]
  required?: boolean
  placeholder?: string
  description?: string
}

export function FormBuilder() {
  const [inputText, setInputText] = useState(`ෆ 네일 메루 예약양식 ෆ
성함
연락처
원하시는 관리(손 or 발)
원하시는 날짜와 시간대
제거 유무 (자샵 or 타샵)
연장, 랩핑 (원하시는 개수)
디자인 사진 첨부`)

  const [formFields, setFormFields] = useState<FormField[]>([])
  const [isGenerating, setIsGenerating] = useState(false)
  const [editingField, setEditingField] = useState<FormField | null>(null)

  const parseTextToFields = (text: string): FormField[] => {
    const lines = text.split("\n").filter((line) => line.trim() && !line.includes("ෆ"))
    const fields: FormField[] = []

    let hasPhotoField = formFields.some(
      (field) =>
        field.type === "file" ||
        field.label.includes("사진") ||
        field.label.includes("이미지") ||
        field.label.includes("첨부"),
    )

    let hasTreatmentField = false
    let hasRemovalField = false

    lines.forEach((line, index) => {
      const cleanLine = line.trim()
      const id = `field_${index + 1}`

      // 성함/이름 처리
      if (cleanLine.includes("성함") || cleanLine.includes("이름")) {
        fields.push({
          id,
          label: "성함",
          type: "text",
          required: true,
          placeholder: "성함을 입력해주세요",
        })
      }
      // 연락처/전화번호 처리
      else if (cleanLine.includes("연락처") || cleanLine.includes("전화") || cleanLine.includes("phone")) {
        fields.push({
          id,
          label: "연락처",
          type: "phone",
          required: true,
          placeholder: "010-0000-0000",
        })
      } else if ((cleanLine.includes("관리") || cleanLine.includes("시술")) && !hasTreatmentField) {
        // 선택 옵션이 있는 경우
        if (cleanLine.includes("(") && cleanLine.includes(")")) {
          const optionMatch = cleanLine.match(/\$\$([^)]+)\$\$/)
          if (optionMatch) {
            const optionsText = optionMatch[1]
            // "or" 또는 "또는"으로 구분된 명확한 옵션들만 라디오 버튼으로 처리
            if (optionsText.includes(" or ") || optionsText.includes(" 또는 ")) {
              const options = optionsText.split(/\s+or\s+|\s+또는\s+/).map((opt) => opt.trim())
              fields.push({
                id,
                label: cleanLine.split("(")[0].trim(),
                type: "radio",
                options,
                required: true,
              })
            } else {
              // 복잡한 설명이 포함된 경우 textarea로 처리
              fields.push({
                id,
                label: cleanLine.split("(")[0].trim(),
                type: "textarea",
                required: true,
                placeholder: "원하시는 시술 내용을 자세히 적어주세요",
              })
            }
          }
        } else {
          fields.push({
            id,
            label: cleanLine,
            type: "textarea",
            required: true,
            placeholder: "원하시는 관리 내용을 자세히 적어주세요",
          })
        }

        // 사진 첨부 필드 추가 (중복 방지)
        if (!hasPhotoField) {
          fields.push({
            id: `${id}_photo`,
            label: "시술 참고 사진",
            type: "file",
            description: "원하시는 스타일이나 디자인 사진을 첨부해주세요",
          })
          hasPhotoField = true
        }

        hasTreatmentField = true
      } else if ((cleanLine.includes("관리") || cleanLine.includes("시술")) && hasTreatmentField) {
        // Skip creating additional treatment fields
        return
      }
      // 제거 유무 처리 - 먼저 필요 여부를 묻고, 필요하면 자샵/타샵 선택
      else if (cleanLine.includes("제거") && !hasRemovalField) {
        fields.push({
          id,
          label: "제거가 필요하신가요?",
          type: "radio",
          options: ["예", "아니오"],
          required: true,
        })
        fields.push({
          id: `${id}_shop`,
          label: "제거 샵 선택",
          type: "radio",
          options: ["자샵", "타샵"],
          required: false,
          description: "제거가 필요한 경우에만 선택해주세요",
        })
        hasRemovalField = true
      } else if (cleanLine.includes("제거") && hasRemovalField) {
        return
      }
      // 연장, 랩핑 처리 (함께 있는 경우 각각 분리)
      else if (cleanLine.includes("연장") && cleanLine.includes("랩핑")) {
        fields.push({
          id: `${id}_extension`,
          label: "연장 개수",
          type: "number",
          placeholder: "0",
        })
        fields.push({
          id: `${id}_wrapping`,
          label: "랩핑 개수",
          type: "number",
          placeholder: "0",
        })
      }
      // 연장만 있는 경우
      else if (cleanLine.includes("연장")) {
        fields.push({
          id,
          label: "연장 개수",
          type: "number",
          placeholder: "0",
        })
      }
      // 랩핑만 있는 경우
      else if (cleanLine.includes("랩핑")) {
        fields.push({
          id,
          label: "랩핑 개수",
          type: "number",
          placeholder: "0",
        })
      }
      // 날짜/시간 처리
      else if (cleanLine.includes("날짜") && cleanLine.includes("시간")) {
        fields.push({
          id,
          label: "예약 날짜 및 시간",
          type: "datetime",
          required: true,
        })
      } else if (cleanLine.includes("날짜")) {
        fields.push({
          id,
          label: "예약 날짜",
          type: "date",
          required: true,
        })
      } else if (cleanLine.includes("시간")) {
        fields.push({
          id,
          label: "예약 시간",
          type: "time",
          required: true,
        })
      } else if (
        (cleanLine.includes("사진") || cleanLine.includes("이미지") || cleanLine.includes("첨부")) &&
        hasPhotoField
      ) {
        // Skip creating this field since photo field already exists
        return
      }
      // 사진/이미지/첨부 처리 (사진 필드가 없을 때만 생성)
      else if (
        (cleanLine.includes("사진") || cleanLine.includes("이미지") || cleanLine.includes("첨부")) &&
        !hasPhotoField
      ) {
        fields.push({
          id,
          label: cleanLine,
          type: "file",
          description: "이미지 파일을 첨부해주세요",
        })
        hasPhotoField = true
      }
      // 선택 옵션 처리
      else if (
        cleanLine.includes("(") &&
        cleanLine.includes(")") &&
        (cleanLine.includes("or") || cleanLine.includes("또는"))
      ) {
        const optionMatch = cleanLine.match(/\$\$([^)]+)\$\$/)
        if (optionMatch) {
          const options = optionMatch[1].split(/\s+or\s+|\s+또는\s+/).map((opt) => opt.trim())
          fields.push({
            id,
            label: cleanLine.split("(")[0].trim(),
            type: "radio",
            options,
            required: true,
          })
        }
      }
      // 개수/수량/숫자 처리
      else if (cleanLine.includes("개수") || cleanLine.includes("수량") || cleanLine.includes("숫자")) {
        fields.push({
          id,
          label: cleanLine,
          type: "number",
          placeholder: "0",
        })
      }
      // 기본 텍스트 필드
      else {
        fields.push({
          id,
          label: cleanLine,
          type: "text",
          placeholder: `${cleanLine}을(를) 입력해주세요`,
        })
      }
    })

    const hasPhoneField =
      fields.some(
        (field) =>
          field.type === "phone" ||
          field.label.includes("연락처") ||
          field.label.includes("전화번호") ||
          field.label.includes("휴대전화"),
      ) ||
      formFields.some(
        (field) =>
          field.type === "phone" ||
          field.label.includes("연락처") ||
          field.label.includes("전화번호") ||
          field.label.includes("휴대전화"),
      )

    if (!hasPhoneField) {
      // Find the index of the name field
      const nameFieldIndex = fields.findIndex((field) => field.label.includes("성함") || field.label.includes("이름"))

      const phoneField: FormField = {
        id: `field_phone_${Date.now()}`,
        label: "연락처",
        type: "phone",
        required: true,
        placeholder: "010-0000-0000",
      }

      if (nameFieldIndex !== -1) {
        // Insert phone field right after name field
        fields.splice(nameFieldIndex + 1, 0, phoneField)
      } else {
        // If no name field, add phone field at the beginning
        fields.unshift(phoneField)
      }
    }

    return fields
  }

  const handleGenerateForm = async () => {
    setIsGenerating(true)

    // 실제 AI 파싱 시뮬레이션
    await new Promise((resolve) => setTimeout(resolve, 1000))

    const fields = parseTextToFields(inputText)
    setFormFields(fields)
    setIsGenerating(false)

    toast({
      title: "양식 생성 완료!",
      description: `${fields.length}개의 필드가 생성되었습니다.`,
    })
  }

  const addField = () => {
    const newField: FormField = {
      id: `field_${Date.now()}`,
      label: "새 필드",
      type: "text",
      placeholder: "내용을 입력해주세요",
    }
    setFormFields([...formFields, newField])
  }

  const updateField = (updatedField: FormField) => {
    setFormFields((fields) => fields.map((field) => (field.id === updatedField.id ? updatedField : field)))
    setEditingField(null)
  }

  const deleteField = (fieldId: string) => {
    setFormFields((fields) => fields.filter((field) => field.id !== fieldId))
  }

  const copyFormCode = () => {
    const code = `// 생성된 양식 코드
const formFields = ${JSON.stringify(formFields, null, 2)};`

    navigator.clipboard.writeText(code)
    toast({
      title: "코드 복사 완료!",
      description: "양식 코드가 클립보드에 복사되었습니다.",
    })
  }

  return (
    <div className="grid lg:grid-cols-2 gap-8">
      {/* 입력 패널 */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Wand2 className="h-5 w-5" />
            양식 텍스트 입력
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <Textarea
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            placeholder="예약 양식 텍스트를 입력하세요..."
            className="min-h-[300px] font-mono"
          />

          <div className="flex gap-2">
            <Button onClick={handleGenerateForm} disabled={isGenerating || !inputText.trim()} className="flex-1">
              {isGenerating ? "생성 중..." : "양식 생성하기"}
            </Button>

            {formFields.length > 0 && (
              <Button variant="outline" onClick={copyFormCode}>
                <Copy className="h-4 w-4 mr-2" />
                코드 복사
              </Button>
            )}
          </div>

          <div className="text-sm text-gray-600">
            <p className="font-medium mb-2">자동 인식 키워드:</p>
            <div className="flex flex-wrap gap-1">
              <Badge variant="secondary">성함/이름</Badge>
              <Badge variant="secondary">연락처/전화</Badge>
              <Badge variant="secondary">관리/시술</Badge>
              <Badge variant="secondary">날짜/시간</Badge>
              <Badge variant="secondary">제거 유무</Badge>
              <Badge variant="secondary">연장/랩핑</Badge>
              <Badge variant="secondary">사진/첨부</Badge>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 미리보기 패널 */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <span>양식 미리보기</span>
            {formFields.length > 0 && (
              <Button variant="outline" size="sm" onClick={addField}>
                <Plus className="h-4 w-4 mr-2" />
                필드 추가
              </Button>
            )}
          </CardTitle>
        </CardHeader>
        <CardContent>
          {formFields.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              <Wand2 className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>텍스트를 입력하고 '양식 생성하기'를 클릭하세요</p>
            </div>
          ) : (
            <div className="space-y-6">
              {/* 필드 편집 목록 */}
              <div className="space-y-2">
                <h3 className="font-medium text-sm text-gray-700">필드 관리</h3>
                <div className="space-y-2 max-h-40 overflow-y-auto">
                  {formFields.map((field) => (
                    <div key={field.id} className="flex items-center justify-between p-2 bg-gray-50 rounded">
                      <span className="text-sm">{field.label}</span>
                      <div className="flex gap-1">
                        <Button variant="ghost" size="sm" onClick={() => setEditingField(field)}>
                          <Settings className="h-3 w-3" />
                        </Button>
                        <Button variant="ghost" size="sm" onClick={() => deleteField(field.id)}>
                          <Trash2 className="h-3 w-3" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <Separator />

              {/* 실제 양식 미리보기 */}
              <DynamicForm fields={formFields} />
            </div>
          )}
        </CardContent>
      </Card>

      {/* 필드 편집 모달 */}
      {editingField && (
        <FormFieldEditor field={editingField} onSave={updateField} onCancel={() => setEditingField(null)} />
      )}
    </div>
  )
}
