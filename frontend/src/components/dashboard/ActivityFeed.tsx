import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Clock, CheckCircle2, XCircle, AlertCircle } from "lucide-react"
import { formatDistanceToNow } from "date-fns"
import { Link } from "react-router-dom"
import { cn } from "@/lib/utils"

interface ActivityItem {
  id: string
  workflowName: string
  workflowId: string
  status: "success" | "failed" | "running" | "pending"
  timestamp: Date | string
  executionId?: string
}

interface ActivityFeedProps {
  activities: ActivityItem[]
  onActivityClick?: (activity: ActivityItem) => void
}

const statusConfig = {
  success: {
    icon: CheckCircle2,
    color: "success" as const,
    label: "Success",
  },
  failed: {
    icon: XCircle,
    color: "destructive" as const,
    label: "Failed",
  },
  running: {
    icon: Clock,
    color: "warning" as const,
    label: "Running",
  },
  pending: {
    icon: AlertCircle,
    color: "secondary" as const,
    label: "Pending",
  },
}

export function ActivityFeed({ activities, onActivityClick }: ActivityFeedProps) {
  return (
    <Card>
      <CardHeader className="px-4 py-3">
        <CardTitle className="text-base">Recent Activity</CardTitle>
        <CardDescription className="text-xs">Latest workflow executions</CardDescription>
      </CardHeader>
      <CardContent className="px-4 pb-3">
        {activities.length === 0 ? (
          <div className="text-center py-6 text-secondary-500 text-sm">
            No recent activity
          </div>
        ) : (
          <div className="space-y-2">
            {activities.map((activity) => {
              const config = statusConfig[activity.status]
              const Icon = config.icon
              const timeAgo = formatDistanceToNow(
                typeof activity.timestamp === "string"
                  ? new Date(activity.timestamp)
                  : activity.timestamp,
                { addSuffix: true }
              )

              return (
                <div
                  key={activity.id}
                  className="flex items-center justify-between p-2 rounded-md border border-secondary-200 hover:bg-secondary-50 transition-colors duration-200 cursor-pointer"
                  onClick={() => onActivityClick?.(activity)}
                >
                  <div className="flex items-center space-x-2.5 flex-1 min-w-0">
                    <div className={cn(
                      "flex-shrink-0",
                      config.color === "success" && "text-success-600",
                      config.color === "destructive" && "text-error-600",
                      config.color === "warning" && "text-warning-600",
                      config.color === "secondary" && "text-secondary-600"
                    )}>
                      <Icon className="h-4 w-4" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-1.5">
                        <Link
                          to={`/workflows/${activity.workflowId}`}
                          className="font-medium text-sm text-secondary-900 hover:text-primary-600 transition-colors"
                          onClick={(e) => e.stopPropagation()}
                        >
                          {activity.workflowName}
                        </Link>
                        <Badge variant={config.color} className="text-xs px-1.5 py-0">
                          {config.label}
                        </Badge>
                      </div>
                      <div className="text-xs text-secondary-500 mt-0.5">
                        {timeAgo}
                      </div>
                    </div>
                  </div>
                  {activity.executionId && (
                    <Link
                      to={`/executions/${activity.executionId}`}
                      className="text-xs text-primary-600 hover:text-primary-700 transition-colors flex-shrink-0 ml-2"
                      onClick={(e) => e.stopPropagation()}
                    >
                      View
                    </Link>
                  )}
                </div>
              )
            })}
          </div>
        )}
      </CardContent>
    </Card>
  )
}

