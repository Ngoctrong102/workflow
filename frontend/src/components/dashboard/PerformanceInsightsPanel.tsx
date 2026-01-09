import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertTriangle, Info, CheckCircle2, X, Lightbulb } from "lucide-react"
import type { PerformanceAlert, PerformanceRecommendation } from "@/types/workflow-dashboard"

interface PerformanceInsightsPanelProps {
  alerts?: PerformanceAlert[]
  recommendations?: PerformanceRecommendation[]
  onDismissAlert?: (alertId: string) => void
  isLoading?: boolean
}

export function PerformanceInsightsPanel({
  alerts = [],
  recommendations = [],
  onDismissAlert,
  isLoading = false,
}: PerformanceInsightsPanelProps) {
  const getSeverityColor = (severity: PerformanceAlert["severity"]) => {
    switch (severity) {
      case "critical":
        return "bg-error-600"
      case "high":
        return "bg-error-500"
      case "medium":
        return "bg-warning-500"
      case "low":
        return "bg-primary-500"
      default:
        return "bg-secondary-500"
    }
  }

  const getAlertIcon = (type: PerformanceAlert["type"]) => {
    switch (type) {
      case "slow_execution":
        return <AlertTriangle className="h-4 w-4" />
      case "high_error_rate":
        return <AlertTriangle className="h-4 w-4" />
      case "low_delivery_rate":
        return <AlertTriangle className="h-4 w-4" />
      case "node_failure":
        return <AlertTriangle className="h-4 w-4" />
      default:
        return <Info className="h-4 w-4" />
    }
  }

  const activeAlerts = alerts.filter((alert) => !alert.dismissed)

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Performance Insights</CardTitle>
          <CardDescription>Alerts and recommendations</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <div className="h-20 bg-secondary-100 rounded animate-pulse" />
            <div className="h-20 bg-secondary-100 rounded animate-pulse" />
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-4">
      {/* Alerts */}
      {activeAlerts.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Performance Alerts</CardTitle>
            <CardDescription>Issues that need attention</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            {activeAlerts.map((alert) => (
              <Alert
                key={alert.id}
                variant={alert.severity === "critical" || alert.severity === "high" ? "destructive" : "default"}
              >
                <div className="flex items-start justify-between">
                  <div className="flex items-start gap-2 flex-1">
                    {getAlertIcon(alert.type)}
                    <div className="flex-1">
                      <AlertDescription>
                        <div className="flex items-center gap-2 mb-1">
                          <span className="font-medium">{alert.message}</span>
                          <Badge
                            variant="outline"
                            className={getSeverityColor(alert.severity)}
                          >
                            {alert.severity}
                          </Badge>
                        </div>
                        <p className="text-xs text-secondary-500">
                          {new Date(alert.timestamp).toLocaleString()}
                        </p>
                      </AlertDescription>
                    </div>
                  </div>
                  {onDismissAlert && (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-6 w-6 p-0"
                      onClick={() => onDismissAlert(alert.id)}
                    >
                      <X className="h-3 w-3" />
                    </Button>
                  )}
                </div>
              </Alert>
            ))}
          </CardContent>
        </Card>
      )}

      {/* Recommendations */}
      {recommendations.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Recommendations</CardTitle>
            <CardDescription>Optimization suggestions</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {recommendations.map((rec) => (
              <div key={rec.id} className="flex items-start gap-3 p-3 border rounded-lg">
                <div className="p-1.5 rounded bg-primary-100">
                  <Lightbulb className="h-4 w-4 text-primary-600" />
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <h4 className="font-medium text-sm">{rec.title}</h4>
                    <Badge variant="outline" className="text-xs">
                      {rec.type}
                    </Badge>
                  </div>
                  <p className="text-sm text-secondary-600">{rec.description}</p>
                  {rec.action && (
                    <Button variant="ghost" size="sm" className="h-auto p-0 mt-1 text-xs">
                      {rec.action}
                    </Button>
                  )}
                  {rec.link && (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-auto p-0 mt-1 text-xs"
                      onClick={() => window.open(rec.link, "_blank")}
                    >
                      Learn more →
                    </Button>
                  )}
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {activeAlerts.length === 0 && recommendations.length === 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Performance Insights</CardTitle>
            <CardDescription>Alerts and recommendations</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="text-center py-8 text-secondary-500">
              <CheckCircle2 className="h-12 w-12 mx-auto mb-2 text-success-400" />
              <p className="text-sm">No alerts or recommendations</p>
              <p className="text-xs mt-1">Your workflow is performing well!</p>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}

