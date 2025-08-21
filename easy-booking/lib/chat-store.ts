import { create } from "zustand"
import { persist } from "zustand/middleware"
import type { Message } from "./types"

interface ChatState {
  messages: Message[]
  addMessage: (content: string, type: Message["type"]) => void
  addReservationRequest: (date: string, time: string, period: string) => void
  showCalendar: () => void
  clearMessages: () => void
}

export const useChatStore = create<ChatState>()(
  persist(
    (set, get) => ({
      messages: [
        {
          id: "1",
          content: "안녕하세요! EasyBooking에 오신 것을 환영합니다.",
          type: "assistant",
          timestamp: new Date(),
        },
        {
          id: "2",
          content: "아래 버튼을 통해 예약 날짜를 조회하거나 공지사항을 확인하실 수 있습니다.",
          type: "assistant",
          timestamp: new Date(),
        },
      ],
      addMessage: (content, type) => {
        const newMessage: Message = {
          id: Date.now().toString(),
          content,
          type,
          timestamp: new Date(),
        }
        set((state) => ({
          messages: [...state.messages, newMessage],
        }))
      },
      addReservationRequest: (date, time, period) => {
        const content = `예약 요청: ${date} ${period} ${time}`
        const newMessage: Message = {
          id: Date.now().toString(),
          content,
          type: "system",
          timestamp: new Date(),
        }
        set((state) => ({
          messages: [...state.messages, newMessage],
        }))
      },
      showCalendar: () => {
        const newMessage: Message = {
          id: Date.now().toString(),
          content: "예약 날짜를 선택해주세요:",
          type: "calendar",
          timestamp: new Date(),
        }
        set((state) => ({
          messages: [...state.messages, newMessage],
        }))
      },
      clearMessages: () => {
        set({ messages: [] })
      },
    }),
    {
      name: "easybooking-chat",
      partialize: (state) => ({ messages: state.messages }),
    },
  ),
)
