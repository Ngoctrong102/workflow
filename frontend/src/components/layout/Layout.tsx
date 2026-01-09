import type { ReactNode } from 'react'
import { useLocation } from 'react-router-dom'
import { Navigation } from '@/components/common/Navigation'
import { Header } from '@/components/layout/Header'
import { Breadcrumbs } from '@/components/common/Breadcrumbs'
import { SkipToContent } from '@/components/common/SkipToContent'
import { useUIStore } from '@/store/ui-store'
import { cn } from '@/lib/utils'
import type { InAppNotification } from '@/components/notifications/InAppNotificationList'

interface LayoutProps {
  children: ReactNode
  fullWidth?: boolean
}

export function Layout({ children, fullWidth }: LayoutProps) {
  const { sidebarOpen } = useUIStore()
  const location = useLocation()

  // Auto-detect fullWidth for WorkflowBuilder routes
  const isWorkflowBuilder = location.pathname.includes('/workflows/') && 
    (location.pathname.includes('/workflows/new') || location.pathname.match(/\/workflows\/[^/]+$/))
  const shouldUseFullWidth = fullWidth !== undefined ? fullWidth : isWorkflowBuilder

  // Mock notifications - replace with actual notifications from context/hooks
  const notifications: InAppNotification[] = []

  return (
    <div className={cn("min-h-screen bg-secondary-50", shouldUseFullWidth && "full-width-layout")}>
      <SkipToContent />
      <Navigation />
      <Header notifications={notifications} />
      <div
        className={cn(
          "transition-all duration-300 ease-in-out",
          sidebarOpen ? "lg:pl-64" : "lg:pl-16"
        )}
      >
        <div className={cn("transition-all duration-300 ease-in-out", shouldUseFullWidth ? "h-screen pt-16" : "pt-16")}>
          {!shouldUseFullWidth && <Breadcrumbs />}
          <main id="main-content" className={cn(
            shouldUseFullWidth 
              ? "w-full h-full" 
              : "container mx-auto px-4 py-3 max-w-7xl"
          )} role="main" aria-label="Main content">
            {children}
          </main>
        </div>
      </div>
    </div>
  )
}

