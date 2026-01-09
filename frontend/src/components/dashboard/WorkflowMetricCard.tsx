import { Card, CardContent } from "@/components/ui/card"
import { TrendingUp, TrendingDown, ArrowRight } from "lucide-react"
import { cn } from "@/lib/utils"

interface WorkflowMetricCardProps {
  label: string
  value: string | number
  trend?: number
  icon?: React.ReactNode
  variant?: "default" | "success" | "error" | "warning"
  onClick?: () => void
  isLoading?: boolean
}

export function WorkflowMetricCard({
  label,
  value,
  trend,
  icon,
  variant = "default",
  onClick,
  isLoading = false,
}: WorkflowMetricCardProps) {
  const variantStyles = {
    default: "bg-white border-secondary-200 hover:border-primary-300",
    success: "bg-success-50 border-success-200 hover:border-success-300",
    error: "bg-error-50 border-error-200 hover:border-error-300",
    warning: "bg-warning-50 border-warning-200 hover:border-warning-300",
  }

  const valueStyles = {
    default: "text-slate-900",
    success: "text-success-700",
    error: "text-error-700",
    warning: "text-warning-700",
  }

  if (isLoading) {
    return (
      <Card className={cn("border transition-all duration-200", variantStyles[variant])}>
        <CardContent className="p-4">
          <div className="space-y-2">
            <div className="h-4 w-24 bg-secondary-200 rounded animate-pulse" />
            <div className="h-8 w-32 bg-secondary-200 rounded animate-pulse" />
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card
      className={cn(
        "border transition-all duration-200",
        variantStyles[variant],
        onClick && "cursor-pointer hover:shadow-md"
      )}
      onClick={onClick}
    >
      <CardContent className="p-4">
        <div className="flex items-start justify-between">
          <div className="space-y-1 flex-1">
            <p className="text-sm font-medium text-slate-600">{label}</p>
            <div className="flex items-baseline gap-2">
              <p className={cn("text-2xl font-bold", valueStyles[variant])}>
                {typeof value === "number" ? value.toLocaleString() : value}
              </p>
              {trend !== undefined && trend !== 0 && (
                <div
                  className={cn(
                    "flex items-center gap-1 text-xs font-medium",
                    trend >= 0 ? "text-success-600" : "text-error-600"
                  )}
                >
                  {trend >= 0 ? (
                    <TrendingUp className="h-3 w-3" />
                  ) : (
                    <TrendingDown className="h-3 w-3" />
                  )}
                  <span>{Math.abs(trend).toFixed(1)}%</span>
                </div>
              )}
            </div>
          </div>
          <div className="flex items-center gap-2">
            {icon && (
              <div
                className={cn(
                  "p-2 rounded-lg",
                  variant === "default"
                    ? "bg-slate-100"
                    : variant === "success"
                    ? "bg-success-100"
                    : variant === "error"
                    ? "bg-error-100"
                    : "bg-warning-100"
                )}
              >
                {icon}
              </div>
            )}
            {onClick && (
              <ArrowRight className="h-4 w-4 text-secondary-400" />
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

