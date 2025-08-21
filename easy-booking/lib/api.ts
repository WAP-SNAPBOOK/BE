import type { TimeSlot } from "./types"

export async function fetchSlots(date: string): Promise<TimeSlot[]> {
  try {
    const response = await fetch(`/api/slots?date=${date}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const data = await response.json()
    return data.slots || []
  } catch (error) {
    console.error("Error fetching slots:", error)
    throw new Error("예약 시간을 불러오는데 실패했습니다.")
  }
}

export const getAvailableSlots = fetchSlots

export async function submitReservation(date: string, time: string): Promise<{ success: boolean; message: string }> {
  try {
    // Simulate API call delay
    await new Promise((resolve) => setTimeout(resolve, 1000))

    // Mock successful response
    return {
      success: true,
      message: "예약이 성공적으로 요청되었습니다.",
    }
  } catch (error) {
    console.error("Error submitting reservation:", error)
    return {
      success: false,
      message: "예약 요청 중 오류가 발생했습니다.",
    }
  }
}
