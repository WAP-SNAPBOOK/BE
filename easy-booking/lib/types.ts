export interface TimeSlot {
  time: string
  period: "오전" | "오후" // Updated to Korean periods to match API
  available: boolean
}

export interface SlotsResponse {
  date: string
  slots: TimeSlot[]
}

export interface Message {
  id: string
  content: string
  type: "user" | "system" | "assistant" | "calendar" // Added calendar message type
  timestamp: Date
}
