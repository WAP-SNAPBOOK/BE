"use client"

import { Calendar, Bell } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useChatStore } from "@/lib/chat-store" // added chat store import
import Link from "next/link"

export function ChatActionBar() {
  const { showCalendar } = useChatStore() // use chat store instead of state

  return (
    <div className="border-t bg-background p-4 sm:p-4">
      <div className="flex gap-3 sm:gap-2">
        <Button
          onClick={showCalendar} // show calendar in chat instead of sheet
          variant="outline"
          className="flex-1 h-12 text-sm font-medium sm:h-10 bg-transparent"
        >
          <Calendar className="mr-2 h-5 w-5 sm:mr-2 sm:h-4 sm:w-4" />
          예약 날짜 조회
        </Button>
        <Link href="/announcements" className="flex-1">
          <Button variant="outline" className="w-full h-12 text-sm font-medium bg-transparent sm:h-10">
            <Bell className="mr-2 h-5 w-5 sm:mr-2 sm:h-4 sm:w-4" />
            공지사항
          </Button>
        </Link>
      </div>
    </div>
  )
}
