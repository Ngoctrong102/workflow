import { useMemo, memo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { CheckCircle2, Clock } from "lucide-react"
import { Progress } from "@/components/ui/progress"
import type { WaitState } from "@/types/execution"

interface EventReceptionStatusProps {
  waitState: WaitState
}

export const EventReceptionStatus = memo(function EventReceptionStatus({ waitState }: EventReceptionStatusProps) {
  const { enabledEvents, receivedEvents, eventData } = waitState

  const getEventStatus = useMemo(() => {
    return (eventType: string) => {
      const isReceived = receivedEvents.includes(eventType)
      const isEnabled = enabledEvents.includes(eventType)
      
      if (!isEnabled) return null
      
      return {
        received: isReceived,
        icon: isReceived ? (
          <CheckCircle2 className="h-4 w-4 text-success-600" />
        ) : (
          <Clock className="h-4 w-4 text-warning-600 animate-pulse" />
        ),
        label: eventType === "api_response" ? "API Response" : "Kafka Event",
      }
    }
  }, [enabledEvents, receivedEvents])

  const apiStatus = useMemo(() => getEventStatus("api_response"), [getEventStatus])
  const kafkaStatus = useMemo(() => getEventStatus("kafka_event"), [getEventStatus])

  const totalEvents = useMemo(() => enabledEvents.length, [enabledEvents.length])
  const receivedCount = useMemo(() => receivedEvents.length, [receivedEvents.length])
  const progress = useMemo(() => totalEvents > 0 ? (receivedCount / totalEvents) * 100 : 0, [totalEvents, receivedCount])

  if (enabledEvents.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Event Reception Status</CardTitle>
          <CardDescription>Progress of event reception</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-secondary-500 text-sm">
            <p>No events configured</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Event Reception Status</CardTitle>
        <CardDescription>Progress of event reception</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <div className="flex items-center justify-between text-sm">
            <span className="text-secondary-600">Progress</span>
            <span className="font-medium" aria-live="polite">
              {receivedCount} / {totalEvents} events received
            </span>
          </div>
          <Progress 
            value={progress} 
            className="h-2"
            aria-label={`Event reception progress: ${receivedCount} of ${totalEvents} events received`}
            role="progressbar"
            aria-valuenow={receivedCount}
            aria-valuemin={0}
            aria-valuemax={totalEvents}
          />
        </div>

        <div className="space-y-3">
          {apiStatus && (
            <div className="flex items-center justify-between p-3 border rounded-lg">
              <div className="flex items-center gap-3">
                {apiStatus.icon}
                <div>
                  <p className="font-medium text-sm">{apiStatus.label}</p>
                  <p className="text-xs text-secondary-500">
                    {apiStatus.received ? "Received" : "Waiting..."}
                  </p>
                </div>
              </div>
              <Badge variant={apiStatus.received ? "default" : "secondary"}>
                {apiStatus.received ? "Received" : "Pending"}
              </Badge>
            </div>
          )}

          {kafkaStatus && (
            <div className="flex items-center justify-between p-3 border rounded-lg">
              <div className="flex items-center gap-3">
                {kafkaStatus.icon}
                <div>
                  <p className="font-medium text-sm">{kafkaStatus.label}</p>
                  <p className="text-xs text-secondary-500">
                    {kafkaStatus.received ? "Received" : "Waiting..."}
                  </p>
                </div>
              </div>
              <Badge variant={kafkaStatus.received ? "default" : "secondary"}>
                {kafkaStatus.received ? "Received" : "Pending"}
              </Badge>
            </div>
          )}
        </div>

        {eventData && (eventData.apiResponse || eventData.kafkaEvent) && (
          <div className="space-y-2 pt-2 border-t">
            <p className="text-sm font-medium">Received Event Data</p>
            {eventData.apiResponse && (
              <div className="space-y-1">
                <p className="text-xs text-secondary-500">API Response:</p>
                <pre className="text-xs bg-secondary-50 p-2 rounded overflow-auto max-h-32">
                  {JSON.stringify(eventData.apiResponse, null, 2)}
                </pre>
              </div>
            )}
            {eventData.kafkaEvent && (
              <div className="space-y-1">
                <p className="text-xs text-secondary-500">Kafka Event:</p>
                <pre className="text-xs bg-secondary-50 p-2 rounded overflow-auto max-h-32">
                  {JSON.stringify(eventData.kafkaEvent, null, 2)}
                </pre>
              </div>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  )
})

