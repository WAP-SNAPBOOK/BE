"use client"

import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import type { TimeSlot } from "@/lib/types"

interface TimeSlotGridProps {
  slots: TimeSlot[]
  selectedTime: string | null
  onTimeSelect: (time: string) => void
  isLoading: boolean
}

export function TimeSlotGrid({ slots, selectedTime, onTimeSelect, isLoading }: TimeSlotGridProps) {
  const amSlots = slots.filter((slot) => slot.period === "am")
  const pmSlots = slots.filter((slot) => slot.period === "pm")

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div>
          <h3 className="mb-3 text-sm font-medium">오전</h3>
          <div className="grid grid-cols-3 gap-3 sm:grid-cols-4 sm:gap-2">
            {Array.from({ length: 8 }).map((_, i) => (
              <Skeleton key={i} className="h-12 sm:h-10" />
            ))}
          </div>
        </div>
        <div>
          <h3 className="mb-3 text-sm font-medium">오후</h3>
          <div className="grid grid-cols-3 gap-3 sm:grid-cols-4 sm:gap-2">
            {Array.from({ length: 8 }).map((_, i) => (
              <Skeleton key={i} className="h-12 sm:h-10" />
            ))}
          </div>
        </div>
      </div>
    )
  }

  const renderTimeSlots = (timeSlots: TimeSlot[], title: string) => (
    <div>
      <h3 className="mb-3 text-sm font-medium">{title}</h3>
      <div className="grid grid-cols-3 gap-3 sm:grid-cols-4 sm:gap-2">
        {timeSlots.map((slot) => {
          const isSelected = selectedTime === slot.time
          const isAvailable = slot.available

          return (
            <Button
              key={slot.time}
              variant={isSelected ? "default" : "outline"}
              size="sm"
              onClick={() => isAvailable && onTimeSelect(slot.time)}
              disabled={!isAvailable}
              className={`h-12 text-sm font-medium sm:h-10 sm:text-xs ${
                isSelected
                  ? "bg-green-600 text-white hover:bg-green-700"
                  : isAvailable
                    ? "border-border bg-background hover:bg-muted active:bg-muted/80"
                    : "cursor-not-allowed bg-muted text-muted-foreground"
              }`}
            >
              {slot.time}
            </Button>
          )
        })}
      </div>
    </div>
  )

  return (
    <div className="space-y-6">
      {amSlots.length > 0 && renderTimeSlots(amSlots, "오전")}
      {pmSlots.length > 0 && renderTimeSlots(pmSlots, "오후")}
    </div>
  )
}
