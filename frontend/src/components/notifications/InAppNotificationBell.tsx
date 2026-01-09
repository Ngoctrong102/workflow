import { useState } from "react"
import { Bell } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { InAppNotificationList, type InAppNotification } from "./InAppNotificationList"

interface InAppNotificationBellProps {
  notifications?: InAppNotification[]
  onMarkAsRead?: (id: string) => void
  onMarkAllAsRead?: () => void
  onDismiss?: (id: string) => void
  onAction?: (notification: InAppNotification) => void
}

export function InAppNotificationBell({
  notifications = [],
  onMarkAsRead,
  onMarkAllAsRead,
  onDismiss,
  onAction,
}: InAppNotificationBellProps) {
  const [open, setOpen] = useState(false)

  const unreadCount = notifications.filter((n) => !n.read).length

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="ghost" size="sm" className="relative">
          <Bell className="h-5 w-5" />
          {unreadCount > 0 && (
            <Badge
              variant="default"
              className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 bg-error-600 text-xs"
            >
              {unreadCount > 9 ? "9+" : unreadCount}
            </Badge>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-96 p-0" align="end">
        <InAppNotificationList
          notifications={notifications}
          onMarkAsRead={onMarkAsRead}
          onMarkAllAsRead={onMarkAllAsRead}
          onDismiss={onDismiss}
          onAction={onAction}
        />
      </PopoverContent>
    </Popover>
  )
}

