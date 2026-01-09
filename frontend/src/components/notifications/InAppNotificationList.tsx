import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Bell, CheckCircle2, AlertCircle, Info, XCircle, X } from "lucide-react"
import { formatDistanceToNow } from "date-fns"
import { cn } from "@/lib/utils"

export interface InAppNotification {
  id: string
  title: string
  message: string
  type: "info" | "success" | "warning" | "error"
  read: boolean
  createdAt: string
  expiresAt?: string
  actionUrl?: string
  actionLabel?: string
}

interface InAppNotificationListProps {
  notifications?: InAppNotification[]
  onMarkAsRead?: (id: string) => void
  onMarkAllAsRead?: () => void
  onDismiss?: (id: string) => void
  onAction?: (notification: InAppNotification) => void
}

const typeConfig = {
  info: {
    icon: Info,
    color: "text-primary-600",
    bgColor: "bg-primary-50",
    borderColor: "border-primary-200",
  },
  success: {
    icon: CheckCircle2,
    color: "text-success-600",
    bgColor: "bg-success-50",
    borderColor: "border-success-200",
  },
  warning: {
    icon: AlertCircle,
    color: "text-warning-600",
    bgColor: "bg-warning-50",
    borderColor: "border-warning-200",
  },
  error: {
    icon: XCircle,
    color: "text-error-600",
    bgColor: "bg-error-50",
    borderColor: "border-error-200",
  },
}

export function InAppNotificationList({
  notifications = [],
  onMarkAsRead,
  onMarkAllAsRead,
  onDismiss,
  onAction,
}: InAppNotificationListProps) {
  const [filteredNotifications, setFilteredNotifications] = useState<InAppNotification[]>([])

  useEffect(() => {
    const now = new Date()
    const filtered = notifications.filter((notification) => {
      if (notification.expiresAt) {
        return new Date(notification.expiresAt) > now
      }
      return true
    })
    setFilteredNotifications(filtered)
  }, [notifications])

  const unreadCount = filteredNotifications.filter((n) => !n.read).length

  if (filteredNotifications.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Bell className="h-5 w-5" />
            <span>Notifications</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-secondary-500">
            <Bell className="h-12 w-12 mx-auto mb-4 text-secondary-300" />
            <p>No notifications</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Bell className="h-5 w-5" />
            <CardTitle>Notifications</CardTitle>
            {unreadCount > 0 && (
              <Badge variant="default" className="bg-primary-600">
                {unreadCount} new
              </Badge>
            )}
          </div>
          {onMarkAllAsRead && unreadCount > 0 && (
            <Button variant="ghost" size="sm" onClick={onMarkAllAsRead}>
              Mark all as read
            </Button>
          )}
        </div>
        <CardDescription>
          {filteredNotifications.length} notification{filteredNotifications.length !== 1 ? "s" : ""}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {filteredNotifications.map((notification) => {
            const config = typeConfig[notification.type]
            const Icon = config.icon
            const timeAgo = formatDistanceToNow(new Date(notification.createdAt), { addSuffix: true })

            return (
              <div
                key={notification.id}
                className={cn(
                  "p-4 rounded-lg border-2 transition-all",
                  notification.read
                    ? "bg-white border-secondary-200"
                    : cn(config.bgColor, config.borderColor, "border-2")
                )}
              >
                <div className="flex items-start space-x-3">
                  <div className={cn("flex-shrink-0 mt-0.5", config.color)}>
                    <Icon className="h-5 w-5" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <h4
                          className={cn(
                            "font-semibold text-sm",
                            notification.read ? "text-secondary-700" : "text-secondary-900"
                          )}
                        >
                          {notification.title}
                        </h4>
                        <p
                          className={cn(
                            "text-sm mt-1",
                            notification.read ? "text-secondary-500" : "text-secondary-700"
                          )}
                        >
                          {notification.message}
                        </p>
                        <div className="flex items-center space-x-2 mt-2">
                          <span className="text-xs text-secondary-400">{timeAgo}</span>
                          {notification.expiresAt && (
                            <>
                              <span className="text-secondary-300">•</span>
                              <span className="text-xs text-secondary-400">
                                Expires {formatDistanceToNow(new Date(notification.expiresAt), { addSuffix: true })}
                              </span>
                            </>
                          )}
                        </div>
                        {notification.actionUrl && notification.actionLabel && (
                          <Button
                            variant="outline"
                            size="sm"
                            className="mt-3"
                            onClick={() => onAction?.(notification)}
                          >
                            {notification.actionLabel}
                          </Button>
                        )}
                      </div>
                      <div className="flex items-center space-x-2 ml-2">
                        {!notification.read && (
                          <Badge variant="default" className="bg-primary-600 text-xs">
                            New
                          </Badge>
                        )}
                        {onDismiss && (
                          <Button
                            variant="ghost"
                            size="sm"
                            className="h-6 w-6 p-0"
                            onClick={() => onDismiss(notification.id)}
                          >
                            <X className="h-4 w-4" />
                          </Button>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
                {!notification.read && onMarkAsRead && (
                  <div className="mt-3 pt-3 border-t border-secondary-200">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onMarkAsRead(notification.id)}
                      className="text-xs"
                    >
                      Mark as read
                    </Button>
                  </div>
                )}
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}

