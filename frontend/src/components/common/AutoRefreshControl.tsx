import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { RefreshCw, Clock } from "lucide-react"
import type { PollingInterval } from "@/hooks/use-realtime-executions"

interface AutoRefreshControlProps {
  pollingInterval: PollingInterval
  onIntervalChange: (interval: PollingInterval) => void
  onManualRefresh?: () => void
  isRefreshing?: boolean
}

const intervalOptions: Array<{ value: PollingInterval; label: string }> = [
  { value: 30, label: "30 seconds" },
  { value: 60, label: "1 minute" },
  { value: 300, label: "5 minutes" },
  { value: false, label: "Disabled" },
]

export function AutoRefreshControl({
  pollingInterval,
  onIntervalChange,
  onManualRefresh,
  isRefreshing = false,
}: AutoRefreshControlProps) {
  const currentLabel = intervalOptions.find((opt) => opt.value === pollingInterval)?.label || "Disabled"

  return (
    <div className="flex items-center space-x-2">
      {onManualRefresh && (
        <Button
          variant="outline"
          size="sm"
          onClick={onManualRefresh}
          disabled={isRefreshing}
          title="Refresh now"
        >
          <RefreshCw className={`h-4 w-4 ${isRefreshing ? "animate-spin" : ""}`} />
        </Button>
      )}
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="outline" size="sm" title="Auto-refresh settings">
            <Clock className="h-4 w-4 mr-2" />
            {currentLabel}
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          {intervalOptions.map((option) => (
            <DropdownMenuItem
              key={option.value === false ? "disabled" : option.value}
              onClick={() => onIntervalChange(option.value)}
              className={pollingInterval === option.value ? "bg-primary-50" : ""}
            >
              {option.label}
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  )
}

