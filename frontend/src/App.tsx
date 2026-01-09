import { useEffect } from 'react'
import { QueryProvider } from '@/providers/query-provider'
import { AppRouter } from '@/router'
import { Toaster } from '@/components/ui/toaster'
import { ErrorBoundary } from '@/components/error/ErrorBoundary'
import { ConfirmDialogProvider } from '@/components/common/ConfirmDialog'
import { TooltipProvider } from '@/components/ui/tooltip'
import { performanceMonitor, logBundleInfo } from '@/utils/performance'

function App() {
  useEffect(() => {
    // Performance monitoring in development
    if (import.meta.env.DEV) {
      performanceMonitor.mark('app-start')
      
      // Log bundle info
      logBundleInfo()
      
      // Measure initial load time
      window.addEventListener('load', () => {
        performanceMonitor.mark('app-loaded')
        const loadTime = performanceMonitor.measure('app-load-time', 'app-start', 'app-loaded')
        if (loadTime) {
          console.log(`[Performance] App loaded in ${loadTime.toFixed(2)}ms`)
        }
      })
    }
  }, [])

  return (
    <ErrorBoundary>
      <QueryProvider>
        <TooltipProvider>
          <ConfirmDialogProvider>
            <AppRouter />
            <Toaster />
          </ConfirmDialogProvider>
        </TooltipProvider>
      </QueryProvider>
    </ErrorBoundary>
  )
}

export default App
