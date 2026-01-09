import { AlertCircle } from "lucide-react"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { cn } from "@/lib/utils"
import type { ApiException } from "@/utils/error-handler"
import { getUserFriendlyErrorMessage } from "@/utils/error-handler"

export interface ErrorMessageProps {
  error?: Error | ApiException | string | null
  title?: string
  className?: string
  showDetails?: boolean
}

export function ErrorMessage({
  error,
  title = "Error",
  className,
  showDetails = false,
}: ErrorMessageProps) {
  if (!error) return null

  // Handle string errors
  if (typeof error === "string") {
    return (
      <Alert variant="destructive" className={cn("mb-4", className)}>
        <AlertCircle className="h-4 w-4" />
        <AlertTitle>{title}</AlertTitle>
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    )
  }

  // Handle ApiException
  const apiError = error as ApiException
  const message = getUserFriendlyErrorMessage(apiError)

  return (
    <Alert variant="destructive" className={cn("mb-4", className)}>
      <AlertCircle className="h-4 w-4" />
      <AlertTitle>{title}</AlertTitle>
      <AlertDescription>
        <div className="space-y-2">
          <p>{message}</p>
          {showDetails && apiError.details && Object.keys(apiError.details).length > 0 && (
            <details className="mt-2">
              <summary className="cursor-pointer text-sm font-medium">
                Error Details
              </summary>
              <pre className="mt-2 text-xs overflow-auto max-h-32 p-2 bg-secondary-100 rounded">
                {JSON.stringify(apiError.details, null, 2)}
              </pre>
            </details>
          )}
          {showDetails && apiError.requestId && (
            <p className="text-xs text-secondary-600 mt-2">
              Request ID: {apiError.requestId}
            </p>
          )}
        </div>
      </AlertDescription>
    </Alert>
  )
}

