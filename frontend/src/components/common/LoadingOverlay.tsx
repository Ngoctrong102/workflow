import { Spinner } from "@/components/ui/spinner"
import { cn } from "@/lib/utils"

interface LoadingOverlayProps {
  isLoading: boolean
  text?: string
  className?: string
  children: React.ReactNode
}

export function LoadingOverlay({ 
  isLoading, 
  text, 
  className,
  children 
}: LoadingOverlayProps) {
  return (
    <div className={cn("relative", className)}>
      {children}
      {isLoading && (
        <div className="absolute inset-0 z-10 flex items-center justify-center bg-background/80 backdrop-blur-sm rounded-md">
          <div className="flex flex-col items-center gap-2">
            <Spinner size="md" />
            {text && (
              <p className="text-sm text-secondary-600 animate-pulse">{text}</p>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

