"use client"

import type React from "react"
import { useSearchParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { FormFieldRenderer } from "@/components/form-field-renderer"
import { useBookingForm } from "@/hooks/use-booking-form"

export default function BookingPage() {
  const searchParams = useSearchParams()
  const storeId = searchParams.get("storeId") || "1"

  const { storeData, formValues, loading, error, handleValueChange, handleFileUpload, removeFile, submitForm } =
    useBookingForm(storeId)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    await submitForm()
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p>폼을 불러오는 중...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 py-8 flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <Button onClick={() => window.location.reload()}>다시 시도</Button>
        </div>
      </div>
    )
  }

  if (!storeData) {
    return (
      <div className="min-h-screen bg-gray-50 py-8 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p>폼을 불러오는 중...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto px-4">
        <Card>
          <CardHeader>
            <CardTitle className="text-center text-2xl">{storeData.title}</CardTitle>
            <p className="text-center text-gray-600">{storeData.name}</p>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              {storeData.formFields.map((field) => (
                <FormFieldRenderer
                  key={field.fieldId}
                  field={field}
                  value={formValues[field.fieldId]}
                  onValueChange={(value) => handleValueChange(field.fieldId, value)}
                  onFileUpload={(files) => handleFileUpload(field.fieldId, files)}
                  onFileRemove={(index) => removeFile(field.fieldId, index)}
                />
              ))}

              <Button type="submit" className="w-full" size="lg">
                예약 신청하기
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
