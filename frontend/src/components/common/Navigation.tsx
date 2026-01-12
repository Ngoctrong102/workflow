import { memo } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { Home, Workflow, BarChart3, Play, TestTube, ChevronLeft, ChevronRight, Menu, X, Zap, Settings } from 'lucide-react'
import { cn } from '@/lib/utils'
import { useUIStore } from '@/store/ui-store'
import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'

const navigationItems = [
  { path: '/dashboard', label: 'Dashboard', icon: Home },
  { path: '/workflows', label: 'Workflows', icon: Workflow },
  { path: '/trigger-registry', label: 'Trigger Registry', icon: Zap },
  { path: '/actions', label: 'Action Registry', icon: Settings },
  { path: '/analytics', label: 'Analytics', icon: BarChart3 },
  { path: '/executions', label: 'Executions', icon: Play },
  { path: '/ab-tests', label: 'A/B Tests', icon: TestTube },
]

export const Navigation = memo(function Navigation() {
  const location = useLocation()
  const { sidebarOpen, toggleSidebar, setSidebarOpen } = useUIStore()

  return (
    <>
      {/* Mobile Menu Button */}
      <Button
        variant="ghost"
        size="sm"
        className="fixed top-4 left-4 z-50 lg:hidden h-9 w-9 p-0 bg-white border border-secondary-200 shadow-sm"
        onClick={() => setSidebarOpen(true)}
      >
        <Menu className="h-4 w-4" />
      </Button>

      {/* Mobile Overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/20 z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          "fixed left-0 top-0 z-50 h-screen bg-white border-r border-secondary-200 shadow-sm transition-all duration-300 ease-in-out flex flex-col",
          sidebarOpen ? "w-64" : "w-16",
          "lg:translate-x-0",
          sidebarOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"
        )}
      >
        {/* Sidebar Header */}
        <div className="flex items-center justify-between p-4 border-b border-secondary-200 h-16">
          {sidebarOpen && (
            <h2 className="text-lg font-semibold text-secondary-900">Notification Platform</h2>
          )}
          <div className="flex items-center gap-1 ml-auto">
            <Button
              variant="ghost"
              size="sm"
              className="h-8 w-8 p-0 hidden lg:flex"
              onClick={toggleSidebar}
              title={sidebarOpen ? "Collapse sidebar" : "Expand sidebar"}
            >
              {sidebarOpen ? (
                <ChevronLeft className="h-4 w-4" />
              ) : (
                <ChevronRight className="h-4 w-4" />
              )}
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className="h-8 w-8 p-0 lg:hidden"
              onClick={() => setSidebarOpen(false)}
            >
              <X className="h-4 w-4" />
            </Button>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 overflow-y-auto p-2">
          <TooltipProvider>
            <div className="space-y-1">
              {navigationItems.map((item) => {
                const Icon = item.icon
                let isActive = false
                if (item.path === '/dashboard') {
                  isActive = location.pathname === '/' || location.pathname === '/dashboard'
                } else {
                  isActive = location.pathname === item.path || location.pathname.startsWith(item.path + '/')
                }
                
                const navItem = (
                  <Link
                    to={item.path}
                    className={cn(
                      "flex items-center rounded-md transition-all duration-200 cursor-pointer group relative",
                      sidebarOpen ? "px-3 py-2.5" : "px-2 py-2.5 justify-center",
                      isActive
                        ? "bg-primary-50 text-primary-600"
                        : "text-secondary-600 hover:text-secondary-900 hover:bg-secondary-50"
                    )}
                    onClick={() => {
                      // Close sidebar on mobile after navigation
                      if (window.innerWidth < 1024) {
                        setSidebarOpen(false)
                      }
                    }}
                  >
                    <Icon className={cn("flex-shrink-0", sidebarOpen ? "w-4 h-4 mr-3" : "w-5 h-5")} />
                    {sidebarOpen && (
                      <span className="font-medium text-sm whitespace-nowrap">{item.label}</span>
                    )}
                    {!sidebarOpen && isActive && (
                      <div className="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-6 bg-primary-600 rounded-r-full" />
                    )}
                  </Link>
                )

                if (!sidebarOpen) {
                  return (
                    <Tooltip key={item.path}>
                      <TooltipTrigger asChild>
                        {navItem}
                      </TooltipTrigger>
                      <TooltipContent side="right" className="ml-2">
                        <p>{item.label}</p>
                      </TooltipContent>
                    </Tooltip>
                  )
                }

                return <div key={item.path}>{navItem}</div>
              })}
            </div>
          </TooltipProvider>
        </nav>
      </aside>
    </>
  )
})

