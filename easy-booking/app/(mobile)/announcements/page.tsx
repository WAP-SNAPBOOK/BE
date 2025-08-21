import { ArrowLeft } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import Link from "next/link"

const announcements = [
  {
    id: 1,
    title: "서비스 점검 안내",
    date: "2025-08-20",
    isNew: true,
    content: "시스템 점검으로 인해 일시적으로 서비스가 중단될 예정입니다.",
  },
  {
    id: 2,
    title: "새로운 예약 시간 추가",
    date: "2025-08-18",
    isNew: true,
    content: "오후 8시, 8시 30분 예약 시간이 새롭게 추가되었습니다.",
  },
  {
    id: 3,
    title: "휴무일 안내",
    date: "2025-08-15",
    isNew: false,
    content: "8월 15일은 광복절로 인해 휴무입니다.",
  },
]

export default function AnnouncementsPage() {
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="flex items-center border-b bg-background px-4 py-4 safe-area-top sm:py-3">
        <Link href="/chat">
          <Button variant="ghost" size="icon" className="mr-3 h-10 w-10 sm:mr-2 sm:h-8 sm:w-8">
            <ArrowLeft className="h-5 w-5 sm:h-4 sm:w-4" />
            <span className="sr-only">뒤로가기</span>
          </Button>
        </Link>
        <h1 className="text-lg font-semibold sm:text-xl">공지사항</h1>
      </header>

      {/* Announcements List */}
      <div className="p-4">
        <div className="space-y-4">
          {announcements.map((announcement, index) => (
            <div key={announcement.id}>
              <Link href={`/announcements/${announcement.id}`}>
                <div className="space-y-3 rounded-lg border p-4 transition-colors hover:bg-muted/50 active:bg-muted sm:space-y-2 sm:p-4">
                  <div className="flex items-start justify-between gap-3">
                    <h3 className="font-medium leading-tight text-base sm:text-sm">{announcement.title}</h3>
                    {announcement.isNew && (
                      <Badge variant="destructive" className="shrink-0 text-xs">
                        NEW
                      </Badge>
                    )}
                  </div>
                  <p className="text-sm text-muted-foreground">{announcement.date}</p>
                  <p className="text-sm text-muted-foreground line-clamp-2 leading-relaxed">{announcement.content}</p>
                </div>
              </Link>
              {index < announcements.length - 1 && <Separator className="mt-4" />}
            </div>
          ))}
        </div>

        {/* Empty state for when there are no announcements */}
        {announcements.length === 0 && (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <p className="text-muted-foreground">현재 공지사항이 없습니다.</p>
          </div>
        )}
      </div>
    </div>
  )
}
