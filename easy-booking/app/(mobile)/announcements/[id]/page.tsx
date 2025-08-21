import { ArrowLeft } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import Link from "next/link"
import { notFound } from "next/navigation"

const announcements = [
  {
    id: 1,
    title: "서비스 점검 안내",
    date: "2025-08-20",
    isNew: true,
    content: "시스템 점검으로 인해 일시적으로 서비스가 중단될 예정입니다.",
    fullContent: `안녕하세요, EasyBooking 사용자 여러분.

시스템 안정성 향상을 위한 정기 점검을 실시할 예정입니다.

**점검 일시:** 2025년 8월 25일 (일) 오전 2시 ~ 오전 6시 (4시간)

**점검 내용:**
- 서버 시스템 업그레이드
- 데이터베이스 최적화
- 보안 패치 적용

점검 시간 동안에는 서비스 이용이 일시적으로 중단됩니다. 이용에 불편을 드려 죄송합니다.

감사합니다.`,
  },
  {
    id: 2,
    title: "새로운 예약 시간 추가",
    date: "2025-08-18",
    isNew: true,
    content: "오후 8시, 8시 30분 예약 시간이 새롭게 추가되었습니다.",
    fullContent: `안녕하세요!

많은 분들의 요청에 따라 새로운 예약 시간대를 추가하였습니다.

**추가된 시간:**
- 오후 8시 (20:00)
- 오후 8시 30분 (20:30)

이제 더욱 편리한 시간에 예약하실 수 있습니다.

앞으로도 더 나은 서비스를 위해 노력하겠습니다.

감사합니다.`,
  },
  {
    id: 3,
    title: "휴무일 안내",
    date: "2025-08-15",
    isNew: false,
    content: "8월 15일은 광복절로 인해 휴무입니다.",
    fullContent: `안녕하세요.

8월 15일 광복절 휴무 안내드립니다.

**휴무일:** 2025년 8월 15일 (목)

해당 날짜에는 예약 서비스가 제공되지 않습니다.
8월 16일 (금)부터 정상 운영됩니다.

이용에 참고 부탁드립니다.

감사합니다.`,
  },
]

interface AnnouncementDetailPageProps {
  params: {
    id: string
  }
}

export default function AnnouncementDetailPage({ params }: AnnouncementDetailPageProps) {
  const announcement = announcements.find((item) => item.id === Number.parseInt(params.id))

  if (!announcement) {
    notFound()
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="flex items-center border-b bg-background px-4 py-3 safe-area-top">
        <Link href="/announcements">
          <Button variant="ghost" size="icon" className="mr-2 h-8 w-8">
            <ArrowLeft className="h-4 w-4" />
            <span className="sr-only">뒤로가기</span>
          </Button>
        </Link>
        <h1 className="text-lg font-semibold">공지사항</h1>
      </header>

      {/* Content */}
      <div className="p-4">
        <Card>
          <CardHeader className="pb-4">
            <div className="flex items-start justify-between">
              <h2 className="text-xl font-semibold leading-tight">{announcement.title}</h2>
              {announcement.isNew && (
                <Badge variant="destructive" className="ml-2 text-xs">
                  NEW
                </Badge>
              )}
            </div>
            <p className="text-sm text-muted-foreground">{announcement.date}</p>
          </CardHeader>
          <CardContent>
            <div className="whitespace-pre-line text-sm leading-relaxed">{announcement.fullContent}</div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
