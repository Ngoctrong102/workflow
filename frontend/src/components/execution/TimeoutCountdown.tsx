import { useState, useEffect, useMemo, memo } from "react"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Clock, AlertTriangle } from "lucide-react"
import { formatDistanceToNow } from "date-fns"

interface TimeoutCountdownProps {
  expiresAt: string
  onTimeout?: () => void
}

export const TimeoutCountdown = memo(function TimeoutCountdown({ expiresAt, onTimeout }: TimeoutCountdownProps) {
  const [timeRemaining, setTimeRemaining] = useState<number>(0)
  const [isExpired, setIsExpired] = useState(false)

  useEffect(() => {
    const updateCountdown = () => {
      const now = new Date().getTime()
      const expiry = new Date(expiresAt).getTime()
      const remaining = expiry - now

      if (remaining <= 0) {
        setIsExpired(true)
        setTimeRemaining(0)
        if (onTimeout) {
          onTimeout()
        }
      } else {
        setIsExpired(false)
        setTimeRemaining(remaining)
      }
    }

    updateCountdown()
    const interval = setInterval(updateCountdown, 1000)

    return () => clearInterval(interval)
  }, [expiresAt, onTimeout])

  const formatTime = (ms: number): string => {
    const seconds = Math.floor(ms / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (days > 0) return `${days}d ${hours % 24}h ${minutes % 60}m`
    if (hours > 0) return `${hours}h ${minutes % 60}m ${seconds % 60}s`
    if (minutes > 0) return `${minutes}m ${seconds % 60}s`
    return `${seconds}s`
  }

  const formattedTime = useMemo(() => formatTime(timeRemaining), [timeRemaining])
  const isWarning = useMemo(() => timeRemaining < 60000, [timeRemaining]) // Less than 1 minute
  const isCritical = useMemo(() => timeRemaining < 10000, [timeRemaining]) // Less than 10 seconds

  if (isExpired) {
    return (
      <Alert variant="destructive">
        <AlertTriangle className="h-4 w-4" />
        <AlertDescription>
          Timeout expired at {new Date(expiresAt).toLocaleString()}
        </AlertDescription>
      </Alert>
    )
  }

  return (
    <div className="space-y-2" role="timer" aria-live="polite" aria-atomic="true">
      <div className="flex items-center gap-2">
        <Clock 
          className={`h-4 w-4 ${isCritical ? "text-error-600" : isWarning ? "text-warning-600" : "text-secondary-500"}`}
          aria-hidden="true"
        />
        <Badge
          variant={isCritical ? "destructive" : isWarning ? "default" : "secondary"}
          className="font-mono"
          aria-label={`${formattedTime} remaining until timeout`}
        >
          {formattedTime} remaining
        </Badge>
      </div>
      {isWarning && (
        <p className="text-xs text-warning-600" role="alert">
          Timeout will expire {formatDistanceToNow(new Date(expiresAt), { addSuffix: true })}
        </p>
      )}
    </div>
  )
})

