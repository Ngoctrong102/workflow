import { useMemo, memo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, Hourglass, AlertCircle, XCircle } from "lucide-react"
import { formatDistanceToNow } from "date-fns"
import { TimeoutCountdown } from "./TimeoutCountdown"
import { EventReceptionStatus } from "./EventReceptionStatus"
import { AggregatedEventData } from "./AggregatedEventData"
import type { WaitState } from "@/types/execution"

interface WaitingStateCardProps {
  waitState: WaitState
}

export const WaitingStateCard = memo(function WaitingStateCard({ waitState }: WaitingStateCardProps) {
  const { nodeLabel, correlationId, status, expiresAt, startedAt, enabledEvents, receivedEvents } = waitState

  const statusBadge = useMemo(() => {
    switch (status) {
      case "waiting":
        return (
          <Badge variant="secondary" className="flex items-center gap-1">
            <Loader2 className="h-3 w-3 animate-spin" />
            Waiting
          </Badge>
        )
      case "completed":
        return <Badge variant="default">Completed</Badge>
      case "timeout":
        return (
          <Badge variant="destructive" className="flex items-center gap-1">
            <AlertCircle className="h-3 w-3" />
            Timeout
          </Badge>
        )
      default:
        return null
    }
  }, [status])

  const missingEvents = useMemo(() => {
    return enabledEvents.filter((e) => !receivedEvents.includes(e))
  }, [enabledEvents, receivedEvents])

  return (
    <Card className="border-primary-200 bg-primary-50/50">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Hourglass className="h-5 w-5 text-primary-600 animate-pulse" />
            <div>
              <CardTitle className="text-base">Waiting for Events</CardTitle>
              <CardDescription>
                Node: {nodeLabel || "Wait for Events"} | Started{" "}
                {formatDistanceToNow(new Date(startedAt), { addSuffix: true })}
              </CardDescription>
            </div>
          </div>
          {statusBadge}
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <p className="text-xs text-secondary-500 mb-1">Correlation ID</p>
            <p className="text-sm font-mono font-medium" aria-label={`Correlation ID: ${correlationId}`}>
              {correlationId}
            </p>
          </div>
          <div>
            <p className="text-xs text-secondary-500 mb-1">Enabled Events</p>
            <p className="text-sm font-medium" aria-label={`${enabledEvents.length} event(s) enabled`}>
              {enabledEvents.length} event(s)
            </p>
          </div>
        </div>

        {status === "waiting" && expiresAt && (
          <div className="pt-2 border-t">
            <p className="text-xs text-secondary-500 mb-2">Timeout</p>
            <TimeoutCountdown expiresAt={expiresAt} />
          </div>
        )}

        {waitState.status === "timeout" && (
          <Alert variant="destructive" className="mt-4">
            <XCircle className="h-4 w-4" />
            <AlertDescription>
              <div className="space-y-1">
                <p className="font-medium">Timeout occurred</p>
                <p className="text-sm">
                  The execution timed out while waiting for events. Missing events:{" "}
                  {missingEvents
                    .map((e) => (e === "api_response" ? "API Response" : "Kafka Event"))
                    .join(", ")}
                </p>
              </div>
            </AlertDescription>
          </Alert>
        )}

        <div className="pt-2 border-t">
          <EventReceptionStatus waitState={waitState} />
        </div>

        {/* Aggregated Event Data */}
        {(status === "completed" || (waitState.eventData && (waitState.eventData.apiResponse || waitState.eventData.kafkaEvent))) && (
          <div className="pt-4 border-t mt-4">
            <AggregatedEventData waitState={waitState} />
          </div>
        )}
      </CardContent>
    </Card>
  )
})

