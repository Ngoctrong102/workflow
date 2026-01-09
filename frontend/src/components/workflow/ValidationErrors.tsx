import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { AlertCircle, CheckCircle2, XCircle, X } from "lucide-react"
import type { ValidationError } from "@/utils/workflow-validation"
import { cn } from "@/lib/utils"

interface ValidationErrorsProps {
  errors: ValidationError[]
  onNodeClick?: (nodeId: string) => void
  onClose?: () => void
}

export function ValidationErrors({ errors, onNodeClick, onClose }: ValidationErrorsProps) {
  const errorCount = errors.filter((e) => e.type === "error").length
  const warningCount = errors.filter((e) => e.type === "warning").length

  if (errors.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <CheckCircle2 className="h-5 w-5 text-success-600" />
            <span>Validation</span>
          </CardTitle>
          <CardDescription>Workflow is valid</CardDescription>
        </CardHeader>
        <CardContent>
          <Alert variant="default" className="border-success-500 bg-success-50">
            <CheckCircle2 className="h-4 w-4 text-success-600" />
            <AlertTitle className="text-success-900">All checks passed</AlertTitle>
            <AlertDescription className="text-success-700">
              Your workflow is ready to be saved and activated.
            </AlertDescription>
          </Alert>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="border-error-200 shadow-xl">
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <AlertCircle className="h-5 w-5 text-error-600" />
            <CardTitle className="text-base font-semibold">Validation Errors</CardTitle>
            <Badge variant="destructive" className="text-xs">{errorCount}</Badge>
            {warningCount > 0 && (
              <Badge variant="outline" className="border-warning-500 text-warning-700 text-xs">{warningCount}</Badge>
            )}
          </div>
          {onClose && (
            <Button
              variant="ghost"
              size="sm"
              className="h-6 w-6 p-0 cursor-pointer"
              onClick={onClose}
              title="Close validation errors"
            >
              <X className="h-3.5 w-3.5" />
            </Button>
          )}
        </div>
        <CardDescription className="text-xs mt-1">
          Please fix the following issues before saving
        </CardDescription>
      </CardHeader>
      <CardContent className="pt-0 pb-3">
        <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
          {errors.map((error, index) => (
            <Alert
              key={index}
              variant={error.type === "error" ? "destructive" : "warning"}
              className={cn(
                "py-2 px-3 text-sm",
                error.nodeId && onNodeClick && "cursor-pointer transition-colors hover:opacity-90"
              )}
              onClick={() => error.nodeId && onNodeClick?.(error.nodeId)}
            >
              <div className="flex items-start space-x-2">
                {error.type === "error" ? (
                  <XCircle className="h-4 w-4 mt-0.5 flex-shrink-0" />
                ) : (
                  <AlertCircle className="h-4 w-4 mt-0.5 flex-shrink-0" />
                )}
                <div className="flex-1 min-w-0">
                  <AlertTitle className="text-xs font-medium mb-0.5">
                    {error.nodeId ? (
                      <span className="truncate">Node: {error.nodeId}</span>
                    ) : (
                      "Workflow"
                    )}
                  </AlertTitle>
                  <AlertDescription className="text-xs">{error.message}</AlertDescription>
                </div>
              </div>
            </Alert>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}

