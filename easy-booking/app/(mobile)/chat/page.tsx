import { Bell } from "lucide-react"
import { Button } from "@/components/ui/button"
import { MessageList } from "@/components/chat/MessageList"
import { MessageInput } from "@/components/chat/MessageInput"
import { ChatActionBar } from "@/components/chat/ChatActionBar"
import { Toaster } from "@/components/ui/toaster"
import Link from "next/link"

export default function ChatPage() {
  return (
    <div className="flex min-h-screen flex-col">
      {/* Header */}
      <header className="flex items-center justify-between border-b bg-background px-4 py-4 safe-area-top sm:py-3 shrink-0">
        <h1 className="text-lg font-semibold sm:text-xl">EasyBooking</h1>
        <Link href="/announcements">
          <Button variant="ghost" size="icon" className="h-10 w-10 sm:h-8 sm:w-8">
            <Bell className="h-5 w-5 sm:h-4 sm:w-4" />
            <span className="sr-only">공지사항</span>
          </Button>
        </Link>
      </header>

      {/* Chat Messages */}
      <div className="flex-1 min-h-0">
        <MessageList />
      </div>

      {/* Action Bar */}
      <div className="shrink-0">
        <ChatActionBar />
      </div>

      {/* Message Input */}
      <div className="border-t bg-background safe-area-bottom shrink-0">
        <MessageInput />
      </div>

      <Toaster />
    </div>
  )
}
