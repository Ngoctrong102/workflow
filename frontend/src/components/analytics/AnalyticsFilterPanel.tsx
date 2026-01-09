import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Button } from "@/components/ui/button"
import { X, Filter } from "lucide-react"
import { useWorkflows } from "@/hooks/use-workflows"
import { useChannels } from "@/hooks/use-channels"
import { format, subDays, subMonths } from "date-fns"
import { cn } from "@/lib/utils"

interface AnalyticsFilterPanelProps {
  dateRange: {
    start: string
    end: string
  }
  onDateRangeChange: (field: "start" | "end", value: string) => void
  granularity?: "hourly" | "daily" | "weekly" | "monthly"
  onGranularityChange?: (granularity: "hourly" | "daily" | "weekly" | "monthly") => void
  selectedWorkflow?: string
  onWorkflowChange: (workflowId: string | undefined) => void
  selectedChannel?: string
  onChannelChange: (channelId: string | undefined) => void
  selectedStatus?: string
  onStatusChange: (status: string | undefined) => void
  onReset: () => void
}

const datePresets = [
  { label: "Today", getValue: () => ({ start: format(new Date(), "yyyy-MM-dd"), end: format(new Date(), "yyyy-MM-dd") }) },
  { label: "Last 7 days", getValue: () => ({ start: format(subDays(new Date(), 7), "yyyy-MM-dd"), end: format(new Date(), "yyyy-MM-dd") }) },
  { label: "Last 30 days", getValue: () => ({ start: format(subDays(new Date(), 30), "yyyy-MM-dd"), end: format(new Date(), "yyyy-MM-dd") }) },
  { label: "Last 90 days", getValue: () => ({ start: format(subDays(new Date(), 90), "yyyy-MM-dd"), end: format(new Date(), "yyyy-MM-dd") }) },
  { label: "This month", getValue: () => ({ start: format(new Date(new Date().getFullYear(), new Date().getMonth(), 1), "yyyy-MM-dd"), end: format(new Date(), "yyyy-MM-dd") }) },
  { label: "Last month", getValue: () => {
    const lastMonth = subMonths(new Date(), 1)
    return { start: format(new Date(lastMonth.getFullYear(), lastMonth.getMonth(), 1), "yyyy-MM-dd"), end: format(new Date(lastMonth.getFullYear(), lastMonth.getMonth() + 1, 0), "yyyy-MM-dd") }
  }},
]

export function AnalyticsFilterPanel({
  dateRange,
  onDateRangeChange,
  granularity = "daily",
  onGranularityChange,
  selectedWorkflow,
  onWorkflowChange,
  selectedChannel,
  onChannelChange,
  selectedStatus,
  onStatusChange,
  onReset,
}: AnalyticsFilterPanelProps) {
  const { data: workflowsData } = useWorkflows({ limit: 100 })
  const { data: channelsData } = useChannels({ limit: 100 })

  const workflows = workflowsData?.data || []
  const channels = channelsData?.data || []

  const handlePresetClick = (preset: typeof datePresets[0]) => {
    const { start, end } = preset.getValue()
    onDateRangeChange("start", start)
    onDateRangeChange("end", end)
  }

  const hasFilters = selectedWorkflow || selectedChannel || selectedStatus

  return (
    <Card className="sticky top-6 border-slate-200 shadow-sm">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Filter className="h-4 w-4 text-slate-500" />
            <div>
              <CardTitle className="text-base">Filters</CardTitle>
              <CardDescription className="text-xs mt-0.5">Filter analytics data</CardDescription>
            </div>
          </div>
          {hasFilters && (
            <Button 
              variant="ghost" 
              size="sm" 
              onClick={onReset}
              className="h-7 px-2 text-xs hover:bg-slate-100 transition-colors duration-200"
            >
              <X className="h-3.5 w-3.5 mr-1.5" />
              Reset
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent className="space-y-5">
        {/* Date Range */}
        <div className="space-y-3">
          <Label className="text-sm font-medium text-slate-700">Date Range</Label>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <Input
                type="date"
                value={dateRange.start}
                onChange={(e) => onDateRangeChange("start", e.target.value)}
                className="h-9 text-sm transition-colors duration-200 focus:border-primary-500"
              />
            </div>
            <div>
              <Input
                type="date"
                value={dateRange.end}
                onChange={(e) => onDateRangeChange("end", e.target.value)}
                className="h-9 text-sm transition-colors duration-200 focus:border-primary-500"
              />
            </div>
          </div>
          <div className="flex flex-wrap gap-2">
            {datePresets.map((preset) => {
              const isActive = 
                dateRange.start === preset.getValue().start && 
                dateRange.end === preset.getValue().end
              
              return (
                <Button
                  key={preset.label}
                  variant={isActive ? "default" : "outline"}
                  size="sm"
                  onClick={() => handlePresetClick(preset)}
                  className={cn(
                    "h-7 px-2.5 text-xs transition-all duration-200",
                    isActive 
                      ? "bg-primary-600 text-white hover:bg-primary-700 shadow-sm" 
                      : "hover:bg-slate-50 hover:border-slate-300"
                  )}
                >
                  {preset.label}
                </Button>
              )
            })}
          </div>
        </div>

        {/* Workflow Filter */}
        <div className="space-y-2">
          <Label htmlFor="workflow-filter" className="text-sm font-medium text-slate-700">Workflow</Label>
          <Select
            value={selectedWorkflow || "all"}
            onValueChange={(value) => onWorkflowChange(value === "all" ? undefined : value)}
          >
            <SelectTrigger 
              id="workflow-filter" 
              className="h-9 text-sm transition-colors duration-200 focus:border-primary-500"
            >
              <SelectValue placeholder="All workflows" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Workflows</SelectItem>
              {workflows.map((workflow) => (
                <SelectItem key={workflow.id} value={workflow.id}>
                  {workflow.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* Channel Filter */}
        <div className="space-y-2">
          <Label htmlFor="channel-filter" className="text-sm font-medium text-slate-700">Channel</Label>
          <Select
            value={selectedChannel || "all"}
            onValueChange={(value) => onChannelChange(value === "all" ? undefined : value)}
          >
            <SelectTrigger 
              id="channel-filter" 
              className="h-9 text-sm transition-colors duration-200 focus:border-primary-500"
            >
              <SelectValue placeholder="All channels" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Channels</SelectItem>
              {channels.map((channel) => (
                <SelectItem key={channel.id} value={channel.id}>
                  {channel.name} ({channel.type})
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* Status Filter */}
        <div className="space-y-2">
          <Label htmlFor="status-filter" className="text-sm font-medium text-slate-700">Status</Label>
          <Select
            value={selectedStatus || "all"}
            onValueChange={(value) => onStatusChange(value === "all" ? undefined : value)}
          >
            <SelectTrigger 
              id="status-filter" 
              className="h-9 text-sm transition-colors duration-200 focus:border-primary-500"
            >
              <SelectValue placeholder="All statuses" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Statuses</SelectItem>
              <SelectItem value="success">Success</SelectItem>
              <SelectItem value="failed">Failed</SelectItem>
              <SelectItem value="pending">Pending</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Granularity Selector */}
        {onGranularityChange && (
          <div className="space-y-2">
            <Label htmlFor="granularity-selector" className="text-sm font-medium text-slate-700">Granularity</Label>
            <Select
              value={granularity}
              onValueChange={(value) => onGranularityChange(value as "hourly" | "daily" | "weekly" | "monthly")}
            >
              <SelectTrigger 
                id="granularity-selector" 
                className="h-9 text-sm transition-colors duration-200 focus:border-primary-500"
              >
                <SelectValue placeholder="Select granularity" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="hourly">Hourly</SelectItem>
                <SelectItem value="daily">Daily</SelectItem>
                <SelectItem value="weekly">Weekly</SelectItem>
                <SelectItem value="monthly">Monthly</SelectItem>
              </SelectContent>
            </Select>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
