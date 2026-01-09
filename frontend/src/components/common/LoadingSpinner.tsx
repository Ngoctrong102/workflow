import { Spinner } from "@/components/ui/spinner"
import { cn } from "@/lib/utils"

interface LoadingSpinnerProps {
  size?: "sm" | "md" | "lg"
  className?: string
  text?: string
  fullScreen?: boolean
}

export function LoadingSpinner({ 
  size = "md", 
  className,
  text,
  fullScreen = false 
}: LoadingSpinnerProps) {
  const content = (
    <div className={cn("flex flex-col items-center justify-center gap-2", className)}>
      <Spinner size={size} />
      {text && (
        <p className="text-sm text-secondary-600 animate-pulse">{text}</p>
      )}
    </div>
  )

  if (fullScreen) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm">
        {content}
      </div>
    )
  }

  return content
}

