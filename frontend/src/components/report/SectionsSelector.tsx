import { Label } from "@/components/ui/label"
import { Checkbox } from "@/components/ui/checkbox"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription } from "@/components/ui/card"
import { CheckSquare, Square } from "lucide-react"
import type { ReportSection } from "@/types/workflow-report"

interface SectionsSelectorProps {
  value: ReportSection[]
  onChange: (sections: ReportSection[]) => void
  error?: string
}

const AVAILABLE_SECTIONS: Array<{
  id: ReportSection
  label: string
  description: string
}> = [
  {
    id: "execution_summary",
    label: "Execution Summary",
    description: "Overview of workflow executions including total count, success rate, and trends",
  },
  {
    id: "notification_summary",
    label: "Notification Summary",
    description: "Summary of notifications sent, delivered, and failed across all channels",
  },
  {
    id: "performance_metrics",
    label: "Performance Metrics",
    description: "Detailed performance metrics including execution times and throughput",
  },
  {
    id: "error_analysis",
    label: "Error Analysis",
    description: "Error breakdown by type, node, and channel with error rates",
  },
  {
    id: "custom_metrics",
    label: "Custom Metrics",
    description: "Custom metrics and KPIs defined for this workflow",
  },
]

export function SectionsSelector({ value, onChange, error }: SectionsSelectorProps) {
  const allSelected = value.length === AVAILABLE_SECTIONS.length
  const someSelected = value.length > 0 && value.length < AVAILABLE_SECTIONS.length

  const handleSelectAll = () => {
    if (allSelected) {
      onChange([])
    } else {
      onChange(AVAILABLE_SECTIONS.map((s) => s.id))
    }
  }

  const handleToggle = (sectionId: ReportSection) => {
    if (value.includes(sectionId)) {
      onChange(value.filter((id) => id !== sectionId))
    } else {
      onChange([...value, sectionId])
    }
  }

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <Label>Report Sections *</Label>
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={handleSelectAll}
          className="h-auto py-1"
        >
          {allSelected ? (
            <>
              <Square className="h-4 w-4 mr-1" />
              Deselect All
            </>
          ) : (
            <>
              <CheckSquare className="h-4 w-4 mr-1" />
              Select All
            </>
          )}
        </Button>
      </div>
      <div className="grid gap-3">
        {AVAILABLE_SECTIONS.map((section) => {
          const isSelected = value.includes(section.id)
          return (
            <Card
              key={section.id}
              className={`cursor-pointer transition-colors ${
                isSelected ? "border-primary-500 bg-primary-50" : "border-secondary-200"
              }`}
              onClick={() => handleToggle(section.id)}
            >
              <CardContent className="p-3">
                <div className="flex items-start gap-3">
                  <Checkbox
                    checked={isSelected}
                    onCheckedChange={() => handleToggle(section.id)}
                    className="mt-0.5"
                  />
                  <div className="flex-1">
                    <Label className="font-medium cursor-pointer">{section.label}</Label>
                    <CardDescription className="text-xs mt-0.5">
                      {section.description}
                    </CardDescription>
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>
      {error && <p className="text-sm text-error-600">{error}</p>}
      <p className="text-xs text-secondary-500">
        Select at least one section to include in the report
      </p>
    </div>
  )
}

