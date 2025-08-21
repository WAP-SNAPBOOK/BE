"use client"

import { useEffect, useRef } from "react"
import { useChatStore } from "@/lib/chat-store"
import { format } from "date-fns"
import { ko } from "date-fns/locale"
import { InlineChatCalendar } from "./InlineChatCalendar"

export function MessageList() {
  const messages = useChatStore((state) => state.messages)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  return (
    <div className="h-full overflow-y-auto">
      <div className="p-4 space-y-4 min-h-full">
        {messages.map((message) => (
          <div key={message.id} className={`flex ${message.type === "user" ? "justify-end" : "justify-start"}`}>
            {message.type === "calendar" ? (
              <div className="w-full">
                <div className="bg-muted rounded-lg px-3 py-2 mb-2 max-w-xs">
                  <p className="text-sm leading-relaxed">{message.content}</p>
                  <p className="mt-1 text-xs opacity-70">{format(message.timestamp, "HH:mm", { locale: ko })}</p>
                </div>
                <div className="w-full">
                  <InlineChatCalendar />
                </div>
              </div>
            ) : (
              <div
                className={`max-w-[85%] rounded-lg px-3 py-2 ${
                  message.type === "user"
                    ? "bg-green-600 text-white"
                    : message.type === "system"
                      ? "bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100"
                      : "bg-muted"
                }`}
              >
                <p className="text-sm leading-relaxed">{message.content}</p>
                <p className="mt-1 text-xs opacity-70">{format(message.timestamp, "HH:mm", { locale: ko })}</p>
              </div>
            )}
          </div>
        ))}
        <div ref={messagesEndRef} className="h-4" />
      </div>
    </div>
  )
}
