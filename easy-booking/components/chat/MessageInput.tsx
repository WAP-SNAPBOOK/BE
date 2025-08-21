"use client"

import type React from "react"

import { useState } from "react"
import { Send } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useChatStore } from "@/lib/chat-store"

export function MessageInput() {
  const [message, setMessage] = useState("")
  const addMessage = useChatStore((state) => state.addMessage)

  const handleSend = () => {
    if (message.trim()) {
      addMessage(message.trim(), "user")
      setMessage("")

      // Simulate assistant response after a delay
      setTimeout(() => {
        addMessage("메시지를 확인했습니다. 추가 도움이 필요하시면 언제든 말씀해 주세요!", "assistant")
      }, 1000)
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="flex items-center gap-3 p-4 sm:gap-2 sm:p-4">
      <Input
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        onKeyPress={handleKeyPress}
        placeholder="메시지를 입력하세요..."
        className="flex-1 h-12 text-base sm:h-10 sm:text-sm"
      />
      <Button
        onClick={handleSend}
        size="icon"
        disabled={!message.trim()}
        className="h-12 w-12 shrink-0 sm:h-10 sm:w-10"
      >
        <Send className="h-5 w-5 sm:h-4 sm:w-4" />
        <span className="sr-only">전송</span>
      </Button>
    </div>
  )
}
