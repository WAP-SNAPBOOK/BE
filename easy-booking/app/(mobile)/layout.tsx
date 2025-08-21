import type { ReactNode } from "react"

interface MobileLayoutProps {
  children: ReactNode
}

export default function MobileLayout({ children }: MobileLayoutProps) {
  return (
    <div className="min-h-screen bg-background">
      <div className="mx-auto min-h-screen w-full max-w-md bg-background">{children}</div>
    </div>
  )
}
