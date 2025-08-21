"use client"

import { useState } from "react"
import { format, addMonths, subMonths, startOfMonth, endOfMonth, eachDayOfInterval, isSameDay, isToday } from "date-fns"
import { ko } from "date-fns/locale"
import { ChevronLeft, ChevronRight, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Sheet, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet"
import { TimeSlotGrid } from "./TimeSlotGrid"
import { fetchSlots } from "@/lib/api"
import { useChatStore } from "@/lib/chat-store"
import { useToast } from "@/hooks/use-toast"
import type { TimeSlot } from "@/lib/types"

interface ReservationSheetProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function ReservationSheet({ open, onOpenChange }: ReservationSheetProps) {
  const [currentDate, setCurrentDate] = useState(new Date())
  const [selectedDate, setSelectedDate] = useState<Date | null>(null)
  const [selectedTime, setSelectedTime] = useState<string | null>(null)
  const [slots, setSlots] = useState<TimeSlot[]>([])
  const [isLoading, setIsLoading] = useState(false)

  const addReservationRequest = useChatStore((state) => state.addReservationRequest)
  const { toast } = useToast()

  const monthStart = startOfMonth(currentDate)
  const monthEnd = endOfMonth(currentDate)
  const days = eachDayOfInterval({ start: monthStart, end: monthEnd })

  const dayNames = ["일", "월", "화", "수", "목", "금", "토"]

  const handleDateSelect = async (date: Date) => {
    setSelectedDate(date)
    setSelectedTime(null)
    setIsLoading(true)

    try {
      const dateStr = format(date, "yyyy-MM-dd")
      const fetchedSlots = await fetchSlots(dateStr)
      setSlots(fetchedSlots)
    } catch (error) {
      console.error("Failed to fetch slots:", error)
      setSlots([])
      toast({
        title: "오류 발생",
        description: "예약 시간을 불러오는데 실패했습니다. 다시 시도해 주세요.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleTimeSelect = (time: string) => {
    setSelectedTime(selectedTime === time ? null : time)
  }

  const handleReservationRequest = () => {
    if (selectedDate && selectedTime) {
      const dateStr = format(selectedDate, "yyyy.MM.dd", { locale: ko })
      const selectedSlot = slots.find((slot) => slot.time === selectedTime)
      const period = selectedSlot?.period === "am" ? "오전" : "오후"

      addReservationRequest(dateStr, selectedTime, period)

      toast({
        title: "예약 요청 완료",
        description: `${dateStr} ${period} ${selectedTime} 예약 요청이 전송되었습니다.`,
      })

      // Reset and close
      setSelectedDate(null)
      setSelectedTime(null)
      onOpenChange(false)
    }
  }

  const handleCancel = () => {
    setSelectedDate(null)
    setSelectedTime(null)
    onOpenChange(false)
  }

  const goToPreviousMonth = () => {
    setCurrentDate(subMonths(currentDate, 1))
  }

  const goToNextMonth = () => {
    setCurrentDate(addMonths(currentDate, 1))
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent side="bottom" className="h-[95vh] rounded-t-xl safe-area-bottom">
        <SheetHeader className="pb-6">
          <div className="flex items-center justify-between">
            <SheetTitle className="text-lg font-semibold sm:text-xl">날짜와 시간을 선택해 주세요</SheetTitle>
            <Button variant="ghost" size="icon" onClick={handleCancel} className="h-10 w-10 sm:h-8 sm:w-8">
              <X className="h-5 w-5 sm:h-4 sm:w-4" />
            </Button>
          </div>
        </SheetHeader>

        <div className="flex flex-col space-y-6 overflow-y-auto">
          {/* Calendar Header */}
          <div className="flex items-center justify-between px-2">
            <Button variant="ghost" size="icon" onClick={goToPreviousMonth} className="h-10 w-10">
              <ChevronLeft className="h-5 w-5" />
            </Button>
            <h2 className="text-lg font-medium">{format(currentDate, "yyyy.M", { locale: ko })}</h2>
            <Button variant="ghost" size="icon" onClick={goToNextMonth} className="h-10 w-10">
              <ChevronRight className="h-5 w-5" />
            </Button>
          </div>

          {/* Calendar Grid */}
          <div className="grid grid-cols-7 gap-1">
            {/* Day Headers */}
            {dayNames.map((day) => (
              <div key={day} className="p-3 text-center text-sm font-medium text-muted-foreground sm:p-2">
                {day}
              </div>
            ))}

            {/* Calendar Days */}
            {days.map((day) => {
              const isSelected = selectedDate && isSameDay(day, selectedDate)
              const isTodayDate = isToday(day)

              return (
                <button
                  key={day.toISOString()}
                  onClick={() => handleDateSelect(day)}
                  className={`relative p-3 text-sm transition-colors hover:bg-muted active:bg-muted/80 sm:p-2 ${
                    isSelected ? "bg-green-600 text-white hover:bg-green-700" : "hover:bg-muted"
                  }`}
                >
                  {format(day, "d")}
                  {isTodayDate && !isSelected && (
                    <Badge variant="secondary" className="absolute -top-1 -right-1 h-4 px-1 text-xs">
                      오늘
                    </Badge>
                  )}
                </button>
              )
            })}
          </div>

          {/* Time Slots */}
          {selectedDate && (
            <TimeSlotGrid
              slots={slots}
              selectedTime={selectedTime}
              onTimeSelect={handleTimeSelect}
              isLoading={isLoading}
            />
          )}

          {/* Action Buttons */}
          {selectedDate && selectedTime && (
            <div className="flex gap-3 pt-4 pb-6 sm:gap-2">
              <Button variant="outline" onClick={handleCancel} className="flex-1 h-12 bg-transparent sm:h-10">
                취소
              </Button>
              <Button
                onClick={handleReservationRequest}
                className="flex-1 h-12 bg-green-600 hover:bg-green-700 sm:h-10"
              >
                예약 요청 보내기
              </Button>
            </div>
          )}
        </div>
      </SheetContent>
    </Sheet>
  )
}
