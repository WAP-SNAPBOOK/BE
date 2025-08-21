import { type NextRequest, NextResponse } from "next/server"
import type { SlotsResponse } from "@/lib/types"

export async function GET(request: NextRequest) {
  const searchParams = request.nextUrl.searchParams
  const date = searchParams.get("date")

  if (!date) {
    return NextResponse.json({ error: "Date parameter is required" }, { status: 400 })
  }

  // Mock data - some slots are unavailable to simulate real usage
  const mockSlots = [
    // AM slots
    { time: "09:00", period: "am" as const, available: true },
    { time: "09:30", period: "am" as const, available: true },
    { time: "10:00", period: "am" as const, available: false },
    { time: "10:30", period: "am" as const, available: true },
    { time: "11:00", period: "am" as const, available: true },
    { time: "11:30", period: "am" as const, available: false },

    // PM slots
    { time: "13:00", period: "pm" as const, available: true },
    { time: "13:30", period: "pm" as const, available: true },
    { time: "14:00", period: "pm" as const, available: false },
    { time: "14:30", period: "pm" as const, available: true },
    { time: "15:00", period: "pm" as const, available: true },
    { time: "15:30", period: "pm" as const, available: false },
    { time: "16:00", period: "pm" as const, available: true },
    { time: "16:30", period: "pm" as const, available: true },
    { time: "17:00", period: "pm" as const, available: true },
    { time: "17:30", period: "pm" as const, available: false },
    { time: "18:00", period: "pm" as const, available: true },
    { time: "18:30", period: "pm" as const, available: true },
    { time: "19:00", period: "pm" as const, available: true },
    { time: "19:30", period: "pm" as const, available: true },
  ]

  // Simulate API delay
  await new Promise((resolve) => setTimeout(resolve, 500))

  const response: SlotsResponse = {
    date,
    slots: mockSlots,
  }

  return NextResponse.json(response)
}
