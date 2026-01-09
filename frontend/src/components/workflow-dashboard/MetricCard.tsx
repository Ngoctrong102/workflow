import { Card, CardContent } from "@/components/ui/card"
import { ArrowUp, ArrowDown, Minus } from "lucide-react"
import { cn } from "@/lib/utils"

interface MetricCardProps {
  label: string
  value: string | number
  trend?: number // Percentage change from previous period
  onClick?: () => void
  className?: string
}

export function MetricCard({ label, value, trend, onClick, className }: MetricCardProps) {
  const getTrendIcon = () => {
    if (trend === undefined || trend === 0) return <Minus className="h-3 w-3 text-secondary-400" />
    if (trend > 0) return <ArrowUp className="h-3 w-3 text-success-600" />
    return <ArrowDown className="h-3 w-3 text-error-600" />
  }

  const getTrendColor = () => {
    if (trend === undefined || trend === 0) return "text-secondary-500"
    if (trend > 0) return "text-success-600"
    return "text-error-600"
  }

  return (
    <Card
      className={cn(
        "cursor-pointer transition-all hover:shadow-md",
        onClick && "hover:border-primary-300",
        className
      )}
      onClick={onClick}
    >
      <CardContent className="p-4">
        <div className="flex items-center justify-between">
          <div className="flex-1">
            <p className="text-xs text-secondary-500 mb-1">{label}</p>
            <p className="text-2xl font-bold text-secondary-900">{value}</p>
            {trend !== undefined && (
              <div className={cn("flex items-center gap-1 mt-2 text-xs", getTrendColor())}>
                {getTrendIcon()}
                <span>{Math.abs(trend * 100).toFixed(1)}%</span>
                <span className="text-secondary-500">vs previous period</span>
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

