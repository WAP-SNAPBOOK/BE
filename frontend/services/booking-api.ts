import type { StoreConfig } from "@/types/booking"
import { FALLBACK_CONFIGS } from "@/constants/fallback-configs"

export class BookingApiService {
  private static async fetchWithTimeout(url: string, options: RequestInit = {}) {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000)

    try {
      const response = await fetch(url, {
        ...options,
        signal: controller.signal,
      })
      clearTimeout(timeoutId)
      return response
    } catch (error) {
      clearTimeout(timeoutId)
      throw error
    }
  }

  static async getStoreConfig(storeId: string): Promise<StoreConfig> {
    console.log("[v0] API Request - Store Config")
    console.log("[v0] Store ID:", storeId)

    const proxyUrl = `/api/v1/form/${storeId}`
    console.log("[v0] Using proxy URL:", proxyUrl)

    try {
      const response = await this.fetchWithTimeout(proxyUrl, {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      })

      console.log("[v0] Response Status:", response.status)
      console.log("[v0] Response OK:", response.ok)

      if (!response.ok) {
        console.log("[v0] Request failed with status:", response.status)
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const contentType = response.headers.get("content-type")
      console.log("[v0] Response Content-Type:", contentType)

      if (!contentType || !contentType.includes("application/json")) {
        console.log("[v0] Non-JSON response received")
        const responseText = await response.text()
        console.log("[v0] Response body preview:", responseText.substring(0, 200))

        try {
          const jsonData = JSON.parse(responseText)
          console.log("[v0] Successfully parsed JSON despite wrong content-type")
          const convertedResponse = this.convertSpringBootResponse(jsonData)
          return convertedResponse
        } catch (parseError) {
          console.log("[v0] Failed to parse as JSON:", parseError)
          throw new Error("Server returned non-JSON response")
        }
      }

      const springBootResponse = await response.json()
      console.log("[v0] Spring Boot Response:", springBootResponse)

      const convertedResponse = this.convertSpringBootResponse(springBootResponse)
      console.log("[v0] Converted Response:", convertedResponse)

      return convertedResponse
    } catch (error) {
      console.log("[v0] API Request Error:", error)
      if (typeof window === "undefined" || process.env.NODE_ENV === "production") {
        console.error("Failed to fetch store config:", error)
      }
      console.log("[v0] Using fallback config due to error")
      return this.getFallbackConfig(storeId)
    }
  }

  private static convertSpringBootResponse(response: any): StoreConfig {
    return {
      id: response.id,
      name: response.name,
      title: response.title,
      formFields: response.formFields.map((field: any) => ({
        fieldId: field.fieldId,
        label: field.label,
        type: field.type, // FieldType enum from Spring Boot
        required: field.required || false,
        placeholder: field.placeholder,
        options: field.options,
        min: field.min,
        max: field.max,
        accept: field.accept,
      })),
    }
  }

  static async submitBooking(formData: FormData): Promise<void> {
    console.log("[v0] API Request - Submit Booking")

    // Log form data contents
    const formDataEntries: Record<string, any> = {}
    for (const [key, value] of formData.entries()) {
      if (value instanceof File) {
        formDataEntries[key] = `File: ${value.name} (${value.size} bytes)`
      } else {
        formDataEntries[key] = value
      }
    }
    console.log("[v0] Form Data:", formDataEntries)

    const isDevelopment = typeof window !== "undefined" && window.location.hostname === "localhost"

    if (isDevelopment) {
      console.log("[v0] Simulating API call in development mode")
      // Simulate API call in development
      await new Promise((resolve) => setTimeout(resolve, 1000))
      console.log("[v0] Development booking submission completed")
      return
    }

    console.log("[v0] Sending booking request to /api/bookings")
    const response = await fetch("/api/bookings", {
      method: "POST",
      body: formData,
    })

    console.log("[v0] Booking Response Status:", response.status)
    console.log("[v0] Booking Response OK:", response.ok)

    if (!response.ok) {
      console.log("[v0] Booking submission failed")
      throw new Error("예약 신청에 실패했습니다.")
    }

    console.log("[v0] Booking submission successful")
  }

  private static getFallbackConfig(storeId: string): StoreConfig {
    return FALLBACK_CONFIGS[storeId] || FALLBACK_CONFIGS["1"]
  }
}
