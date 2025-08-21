"use client"

import { useState, useEffect } from "react"
import {
  format,
  addMonths,
  subMonths,
  startOfMonth,
  endOfMonth,
  eachDayOfInterval,
  isSameMonth,
  isToday,
  isSameDay,
} from "date-fns"
import { ko } from "date-fns/locale"
import { ChevronLeft, ChevronRight, Clock } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { useChatStore } from "@/lib/chat-store"
import { getAvailableSlots } from "@/lib/api"
import type { TimeSlot } from "@/lib/types"

const DAYS_OF_WEEK = ["일", "월", "화", "수", "목", "금", "토"]

export function InlineChatCalendar() {
  const [currentDate, setCurrentDate] = useState(new Date())
  const [selectedDate, setSelectedDate] = useState<Date | null>(null)
  const [selectedTime, setSelectedTime] = useState<string | null>(null)
  const [selectedPeriod, setSelectedPeriod] = useState<string | null>(null)
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>([])
  const [isLoading, setIsLoading] = useState(false)

  const { addReservationRequest } = useChatStore()

  const monthStart = startOfMonth(currentDate)
  const monthEnd = endOfMonth(currentDate)
  const calendarDays = eachDayOfInterval({ start: monthStart, end: monthEnd })

  useEffect(() => {
    if (selectedDate) {
      loadTimeSlots(selectedDate)
    }
  }, [selectedDate])

  const loadTimeSlots = async (date: Date) => {
    setIsLoading(true)
    try {
      const slots = await getAvailableSlots(format(date, "yyyy-MM-dd"))
      setTimeSlots(slots)
    } catch (error) {
      console.error("Failed to load time slots:", error)
      setTimeSlots([])
    } finally {
      setIsLoading(false)
    }
  }

  const handleDateSelect = (date: Date) => {
    setSelectedDate(date)
    setSelectedTime(null)
    setSelectedPeriod(null)
  }

  const handleTimeSelect = (time: string, period: string) => {
    setSelectedTime(time)
    setSelectedPeriod(period)

    if (selectedDate) {
      const dateStr = format(selectedDate, "yyyy년 MM월 dd일", { locale: ko })
      addReservationRequest(dateStr, time, period)
    }
  }

  const goToPreviousMonth = () => {
    setCurrentDate(subMonths(currentDate, 1))
  }

  const goToNextMonth = () => {
    setCurrentDate(addMonths(currentDate, 1))
  }

  const amSlots = timeSlots.filter((slot) => slot.period === "오전")
  const pmSlots = timeSlots.filter((slot) => slot.period === "오후")

  return (
    <Card className="w-full max-w-sm mx-auto p-3 bg-muted/30">
      {/* Calendar Header */}
      <div className="flex items-center justify-between mb-3">
        <Button variant="ghost" size="sm" onClick={goToPreviousMonth} className="h-8 w-8 p-0">
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <h3 className="text-sm font-medium">{format(currentDate, "yyyy년 MM월", { locale: ko })}</h3>
        <Button variant="ghost" size="sm" onClick={goToNextMonth} className="h-8 w-8 p-0">
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>

      {/* Days of Week */}
      <div className="grid grid-cols-7 gap-1 mb-2">
        {DAYS_OF_WEEK.map((day) => (
          <div key={day} className="text-center text-xs font-medium text-muted-foreground p-1">
            {day}
          </div>
        ))}
      </div>

      {/* Calendar Grid */}
      <div className="grid grid-cols-7 gap-1 mb-4">
        {calendarDays.map((date) => {
          const isCurrentMonth = isSameMonth(date, currentDate)
          const isTodayDate = isToday(date)
          const isSelected = selectedDate && isSameDay(date, selectedDate)

          return (
            <Button
              key={date.toISOString()}
              variant={isSelected ? "default" : "ghost"}
              size="sm"
              onClick={() => handleDateSelect(date)}
              disabled={!isCurrentMonth}
              className={`
                h-8 w-8 p-0 text-xs
                ${!isCurrentMonth ? "text-muted-foreground/30" : ""}
                ${isTodayDate ? "ring-1 ring-primary" : ""}
                ${isSelected ? "bg-green-600 hover:bg-green-700 text-white" : ""}
              `}
            >
              {format(date, "d")}
            </Button>
          )
        })}
      </div>

      {/* Time Slots */}
      {selectedDate && (
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-sm font-medium">
            <Clock className="h-4 w-4" />
            {format(selectedDate, "MM월 dd일", { locale: ko })} 시간 선택
          </div>

          {isLoading ? (
            <div className="space-y-2">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
            </div>
          ) : (
            <div className="space-y-3 max-h-40 overflow-y-auto">
              {/* AM Slots */}
              {amSlots.length > 0 && (
                <div>
                  <div className="text-xs font-medium text-muted-foreground mb-2">오전</div>
                  <div className="grid grid-cols-3 gap-2">
                    {amSlots.map((slot) => (
                      <Button
                        key={`am-${slot.time}`}
                        variant="outline"
                        size="sm"
                        onClick={() => handleTimeSelect(slot.time, slot.period)}
                        disabled={!slot.available}
                        className="h-8 text-xs"
                      >
                        {slot.time}
                      </Button>
                    ))}
                  </div>
                </div>
              )}

              {/* PM Slots */}
              {pmSlots.length > 0 && (
                <div>
                  <div className="text-xs font-medium text-muted-foreground mb-2">오후</div>
                  <div className="grid grid-cols-3 gap-2">
                    {pmSlots.map((slot) => (
                      <Button
                        key={`pm-${slot.time}`}
                        variant="outline"
                        size="sm"
                        onClick={() => handleTimeSelect(slot.time, slot.period)}
                        disabled={!slot.available}
                        className="h-8 text-xs"
                      >
                        {slot.time}
                      </Button>
                    ))}
                  </div>
                </div>
              )}

              {amSlots.length === 0 && pmSlots.length === 0 && (
                <div className="text-center text-sm text-muted-foreground py-4">
                  선택한 날짜에 예약 가능한 시간이 없습니다.
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </Card>
  )
}
