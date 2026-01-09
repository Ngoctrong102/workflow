import { memo, useMemo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { CheckCircle2, XCircle, AlertCircle } from "lucide-react"
import type { WaitState } from "@/types/execution"

interface AggregatedEventDataProps {
  waitState: WaitState
}

export const AggregatedEventData = memo(function AggregatedEventData({ waitState }: AggregatedEventDataProps) {
  const { enabledEvents, receivedEvents, eventData, status } = waitState

  const isCompleted = status === "completed"
  const isTimeout = status === "timeout"
  const allEventsReceived = useMemo(() => {
    return enabledEvents.every((event) => receivedEvents.includes(event))
  }, [enabledEvents, receivedEvents])

  const aggregatedData = useMemo(() => {
    const data: Record<string, unknown> = {}
    
    if (eventData?.apiResponse) {
      data.api_response = eventData.apiResponse
    }
    
    if (eventData?.kafkaEvent) {
      data.kafka_event = eventData.kafkaEvent
    }
    
    return data
  }, [eventData])

  if (!isCompleted && !allEventsReceived && !isTimeout) {
    // Still waiting, show partial data if available
    if (Object.keys(aggregatedData).length === 0) {
      return null
    }
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-base">Aggregated Event Data</CardTitle>
            <CardDescription>Combined data from all received events</CardDescription>
          </div>
          {isCompleted && (
            <Badge variant="default" className="bg-success-600">
              <CheckCircle2 className="h-3 w-3 mr-1" />
              Complete
            </Badge>
          )}
          {isTimeout && (
            <Badge variant="destructive">
              <XCircle className="h-3 w-3 mr-1" />
              Timeout
            </Badge>
          )}
          {!isCompleted && !isTimeout && allEventsReceived && (
            <Badge variant="secondary">
              <AlertCircle className="h-3 w-3 mr-1" />
              Partial
            </Badge>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {Object.keys(aggregatedData).length === 0 ? (
          <div className="text-center py-8 text-secondary-500 text-sm">
            <p>No event data received yet</p>
          </div>
        ) : (
          <Tabs defaultValue="aggregated" className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="aggregated">Aggregated</TabsTrigger>
              <TabsTrigger value="api">API Response</TabsTrigger>
              <TabsTrigger value="kafka">Kafka Event</TabsTrigger>
            </TabsList>
            
            <TabsContent value="aggregated" className="mt-4">
              <div className="space-y-2">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-sm font-medium">Combined Data:</span>
                  <Badge variant="outline">
                    {Object.keys(aggregatedData).length} event(s)
                  </Badge>
                </div>
                <pre className="bg-secondary-100 p-4 rounded-lg overflow-x-auto text-sm max-h-96">
                  {JSON.stringify(aggregatedData, null, 2)}
                </pre>
              </div>
            </TabsContent>
            
            <TabsContent value="api" className="mt-4">
              {eventData?.apiResponse ? (
                <div className="space-y-2">
                  <div className="flex items-center gap-2 mb-2">
                    <CheckCircle2 className="h-4 w-4 text-success-600" />
                    <span className="text-sm font-medium">API Response Received</span>
                  </div>
                  <pre className="bg-secondary-100 p-4 rounded-lg overflow-x-auto text-sm max-h-96">
                    {JSON.stringify(eventData.apiResponse, null, 2)}
                  </pre>
                </div>
              ) : (
                <div className="text-center py-8 text-secondary-500 text-sm">
                  <p>API response not received</p>
                </div>
              )}
            </TabsContent>
            
            <TabsContent value="kafka" className="mt-4">
              {eventData?.kafkaEvent ? (
                <div className="space-y-2">
                  <div className="flex items-center gap-2 mb-2">
                    <CheckCircle2 className="h-4 w-4 text-success-600" />
                    <span className="text-sm font-medium">Kafka Event Received</span>
                  </div>
                  <pre className="bg-secondary-100 p-4 rounded-lg overflow-x-auto text-sm max-h-96">
                    {JSON.stringify(eventData.kafkaEvent, null, 2)}
                  </pre>
                </div>
              ) : (
                <div className="text-center py-8 text-secondary-500 text-sm">
                  <p>Kafka event not received</p>
                </div>
              )}
            </TabsContent>
          </Tabs>
        )}
      </CardContent>
    </Card>
  )
})

