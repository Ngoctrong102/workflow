import React from "react"
import { toast } from "@/hooks/use-toast"
import { getUserFriendlyErrorMessage, isRetryableError, type ApiException } from "@/utils/error-handler"
import { RefreshCw } from "lucide-react"
import { ToastAction, type ToastActionElement } from "@/components/ui/toast"

/**
 * Show a success toast notification
 */
export function showSuccessToast(title: string, description?: string) {
  toast({
    variant: "success",
    title,
    description,
  })
}

/**
 * Show an error toast notification with optional retry action
 */
export function showErrorToast(
  error: unknown,
  title?: string,
  onRetry?: () => void | Promise<void>
) {
  const apiError = error as ApiException
  const message = getUserFriendlyErrorMessage(apiError)
  const canRetry = onRetry && isRetryableError(apiError)

  toast({
    variant: "destructive",
    title: title || "Error",
    description: message,
    action: canRetry
      ? (React.createElement(
          ToastAction,
          {
            altText: "Retry",
            onClick: async () => {
              if (onRetry) {
                await onRetry()
              }
            },
          },
          React.createElement(RefreshCw, { className: "h-4 w-4 mr-2" }),
          "Retry"
        ) as unknown as ToastActionElement)
      : undefined,
  })
}

/**
 * Show a warning toast notification
 */
export function showWarningToast(title: string, description?: string) {
  toast({
    variant: "warning",
    title,
    description,
  })
}

/**
 * Show an info toast notification
 */
export function showInfoToast(title: string, description?: string) {
  toast({
    title,
    description,
  })
}

