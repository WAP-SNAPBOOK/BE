"use client"

import { useState, useEffect } from "react"
import type { StoreConfig } from "@/types/booking"
import { BookingApiService } from "@/services/booking-api"
import { FormUtils } from "@/utils/form-utils"
import { toast } from "@/hooks/use-toast"

export function useBookingForm(storeId: string) {
  const [storeData, setStoreData] = useState<StoreConfig | null>(null)
  const [formValues, setFormValues] = useState<Record<string, any>>({})
  const [uploadedFiles, setUploadedFiles] = useState<Record<string, File[]>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const loadStoreConfig = async () => {
      setLoading(true)
      setError(null)

      try {
        const config = await BookingApiService.getStoreConfig(storeId)
        setStoreData(config)

        // Initialize form state
        const initialValues = FormUtils.initializeFormValues(config.formFields)
        const initialFiles = FormUtils.initializeFileStorage(config.formFields)

        setFormValues(initialValues)
        setUploadedFiles(initialFiles)
      } catch (err) {
        if (process.env.NODE_ENV === "production") {
          setError("폼 설정을 불러오는데 실패했습니다.")
        }
        console.error("Error loading store config:", err)
      } finally {
        setLoading(false)
      }
    }

    loadStoreConfig()
  }, [storeId])

  const handleValueChange = (fieldId: string, value: any) => {
    setFormValues((prev) => ({ ...prev, [fieldId]: value }))
  }

  const handleFileUpload = (fieldId: string, files: FileList | null) => {
    if (!files || files.length === 0) return

    const newFiles = Array.from(files)
    setUploadedFiles((prev) => ({
      ...prev,
      [fieldId]: [...(prev[fieldId] || []), ...newFiles],
    }))

    const fileInfos = newFiles.map(FormUtils.createFileInfo)
    setFormValues((prev) => ({
      ...prev,
      [fieldId]: [...(prev[fieldId] || []), ...fileInfos],
    }))
  }

  const removeFile = (fieldId: string, index: number) => {
    setUploadedFiles((prev) => ({
      ...prev,
      [fieldId]: prev[fieldId]?.filter((_, i) => i !== index) || [],
    }))

    setFormValues((prev) => ({
      ...prev,
      [fieldId]: prev[fieldId]?.filter((_: any, i: number) => i !== index) || [],
    }))
  }

  const resetForm = () => {
    if (!storeData) return

    const initialValues = FormUtils.initializeFormValues(storeData.formFields)
    const initialFiles = FormUtils.initializeFileStorage(storeData.formFields)

    setFormValues(initialValues)
    setUploadedFiles(initialFiles)
  }

  const submitForm = async () => {
    if (!storeData) return false

    const missingFields = FormUtils.validateRequiredFields(storeData.formFields, formValues)

    if (missingFields.length > 0) {
      toast({
        title: "필수 항목을 입력해주세요",
        description: `다음 항목들이 필요합니다: ${missingFields.map((f) => f.label).join(", ")}`,
        variant: "destructive",
      })
      return false
    }

    try {
      const formData = FormUtils.createFormData(storeData.id, formValues, uploadedFiles)
      await BookingApiService.submitBooking(formData)

      const isDevelopment = process.env.NODE_ENV === "development"
      toast({
        title: isDevelopment ? "예약 신청 완료! (개발 모드)" : "예약 신청 완료!",
        description: isDevelopment
          ? "서버가 연결되면 실제 예약이 처리됩니다."
          : "예약 신청이 성공적으로 접수되었습니다.",
      })

      resetForm()
      return true
    } catch (error) {
      toast({
        title: "예약 신청 실패",
        description: "예약 신청 중 오류가 발생했습니다. 다시 시도해주세요.",
        variant: "destructive",
      })
      return false
    }
  }

  return {
    storeData,
    formValues,
    uploadedFiles,
    loading,
    error,
    handleValueChange,
    handleFileUpload,
    removeFile,
    submitForm,
  }
}
