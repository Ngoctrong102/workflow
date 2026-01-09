import { useState } from 'react'
import { Search, Settings, HelpCircle, LogOut, User } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { InAppNotificationBell } from '@/components/notifications/InAppNotificationBell'
import { LanguageSelector } from '@/components/common/LanguageSelector'
import { useUIStore } from '@/store/ui-store'
import { cn } from '@/lib/utils'
import type { InAppNotification } from '@/components/notifications/InAppNotificationList'

interface HeaderProps {
  notifications?: InAppNotification[]
  onNotificationAction?: (notification: InAppNotification) => void
}

export function Header({ notifications = [], onNotificationAction }: HeaderProps) {
  const { sidebarOpen } = useUIStore()
  const [searchQuery, setSearchQuery] = useState('')

  // Mock user data - replace with actual user data from auth context
  const user = {
    name: 'John Doe',
    email: 'john.doe@example.com',
    avatar: undefined,
  }

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    // Implement search functionality
    console.log('Search:', searchQuery)
  }

  const handleLogout = () => {
    // Implement logout
    console.log('Logout')
  }

  return (
    <header
      className={cn(
        "fixed top-0 right-0 z-40 h-16 bg-white border-b border-secondary-200 shadow-sm transition-all duration-300 ease-in-out",
        sidebarOpen ? "lg:left-64" : "lg:left-16",
        "left-0"
      )}
    >
      <div className="h-full flex items-center justify-between px-3 sm:px-4 gap-2">
        {/* Search Bar */}
        <form onSubmit={handleSearch} className="flex-1 max-w-md hidden md:block">
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
            <Input
              type="search"
              placeholder="Search workflows, templates, channels..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9 h-9 text-sm"
            />
          </div>
        </form>

        {/* Mobile Search Button */}
        <Button
          variant="ghost"
          size="sm"
          className="md:hidden h-9 w-9 p-0"
          onClick={() => {
            // TODO: Open search modal/drawer on mobile
            console.log('Open search')
          }}
          aria-label="Open search"
        >
          <Search className="h-4 w-4" aria-hidden="true" />
        </Button>

        {/* Right Side Actions */}
        <div className="flex items-center gap-1 sm:gap-2">
          {/* Language Selector */}
          <LanguageSelector />
          
          {/* Notifications */}
          <InAppNotificationBell
            notifications={notifications}
            onAction={onNotificationAction}
          />

          {/* User Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="h-9 px-1 sm:px-2 cursor-pointer">
                <Avatar className="h-8 w-8">
                  <AvatarImage src={user.avatar} alt={user.name} />
                  <AvatarFallback className="bg-primary-100 text-primary-600 text-xs">
                    {user.name
                      .split(' ')
                      .map((n) => n[0])
                      .join('')
                      .toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                <span className="ml-2 text-sm font-medium text-secondary-900 hidden lg:inline">
                  {user.name}
                </span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>
                <div className="flex flex-col space-y-1">
                  <p className="text-sm font-medium">{user.name}</p>
                  <p className="text-xs text-secondary-500">{user.email}</p>
                </div>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem className="cursor-pointer">
                <User className="mr-2 h-4 w-4" />
                <span>Profile</span>
              </DropdownMenuItem>
              <DropdownMenuItem className="cursor-pointer">
                <Settings className="mr-2 h-4 w-4" />
                <span>Settings</span>
              </DropdownMenuItem>
              <DropdownMenuItem className="cursor-pointer">
                <HelpCircle className="mr-2 h-4 w-4" />
                <span>Help & Support</span>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem className="cursor-pointer text-error-600 focus:text-error-600 focus:bg-error-50" onClick={handleLogout}>
                <LogOut className="mr-2 h-4 w-4" />
                <span>Log out</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </header>
  )
}

