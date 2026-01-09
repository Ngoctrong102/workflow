import { memo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { TrendingUp, TrendingDown, ArrowRight } from "lucide-react"
import { cn } from "@/lib/utils"
import { Link } from "react-router-dom"

interface MetricCardProps {
  title: string
  value: string | number
  description?: string
  trend?: {
    value: number
    isPositive: boolean
  }
  onClick?: () => void
  href?: string
  icon?: React.ReactNode
}

export const MetricCard = memo(function MetricCard({
  title,
  value,
  description,
  trend,
  onClick,
  href,
  icon,
}: MetricCardProps) {
  const content = (
    <Card className="hover:shadow-md transition-all duration-200 cursor-pointer hover:border-primary-200" onClick={onClick}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1.5 px-4 pt-3">
        <CardTitle className="text-xs font-medium text-secondary-600">
          {title}
        </CardTitle>
        {icon && <div className="text-secondary-400">{icon}</div>}
      </CardHeader>
      <CardContent className="px-4 pb-3">
        <div className="text-xl font-bold">{value}</div>
        {description && (
          <CardDescription className="mt-0.5 text-xs">{description}</CardDescription>
        )}
        {trend && (
          <div className="flex items-center mt-1.5 text-xs">
            {trend.isPositive ? (
              <TrendingUp className="h-3 w-3 text-success-600 mr-1" />
            ) : (
              <TrendingDown className="h-3 w-3 text-error-600 mr-1" />
            )}
            <span
              className={cn(
                "font-medium",
                trend.isPositive ? "text-success-600" : "text-error-600"
              )}
            >
              {Math.abs(trend.value)}%
            </span>
            <span className="text-secondary-500 ml-1">vs last period</span>
          </div>
        )}
        {href && (
          <div className="flex items-center mt-2 text-xs text-primary-600 hover:text-primary-700 transition-colors">
            <span>View details</span>
            <ArrowRight className="h-3 w-3 ml-1" />
          </div>
        )}
      </CardContent>
    </Card>
  )

  if (href) {
    return <Link to={href}>{content}</Link>
  }

  return content
})

