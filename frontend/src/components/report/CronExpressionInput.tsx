import { useState } from "react"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Info, CheckCircle2 } from "lucide-react"
import { cn } from "@/lib/utils"

interface CronExpressionInputProps {
  value: string
  onChange: (value: string) => void
  error?: string
  disabled?: boolean
}

const cronPresets = [
  { label: "Daily at 9:00 AM", value: "0 9 * * *" },
  { label: "Daily at 12:00 PM", value: "0 12 * * *" },
  { label: "Daily at 6:00 PM", value: "0 18 * * *" },
  { label: "Every Monday at 9:00 AM", value: "0 9 * * 1" },
  { label: "Every Friday at 5:00 PM", value: "0 17 * * 5" },
  { label: "First day of month at 9:00 AM", value: "0 9 1 * *" },
  { label: "Every 6 hours", value: "0 */6 * * *" },
  { label: "Every 12 hours", value: "0 */12 * * *" },
]

// Basic cron validation (5 fields: minute hour day month weekday)
function validateCronExpression(cron: string): { valid: boolean; error?: string } {
  if (!cron.trim()) {
    return { valid: false, error: "Cron expression is required" }
  }

  const parts = cron.trim().split(/\s+/)
  if (parts.length !== 5) {
    return { valid: false, error: "Cron expression must have 5 fields: minute hour day month weekday" }
  }

  // Basic pattern validation
  const patterns = [
    /^(\*|[0-5]?\d)$/, // minute: 0-59 or *
    /^(\*|[01]?\d|2[0-3])$/, // hour: 0-23 or *
    /^(\*|[12]?\d|3[01])$/, // day: 1-31 or *
    /^(\*|[1-9]|1[0-2])$/, // month: 1-12 or *
    /^(\*|[0-6])$/, // weekday: 0-6 or *
  ]

  for (let i = 0; i < parts.length; i++) {
    if (!patterns[i].test(parts[i]) && !parts[i].includes("/") && !parts[i].includes("-") && !parts[i].includes(",")) {
      return { valid: false, error: `Invalid value in field ${i + 1}: ${parts[i]}` }
    }
  }

  return { valid: true }
}

export function CronExpressionInput({
  value,
  onChange,
  error,
  disabled = false,
}: CronExpressionInputProps) {
  const [validation, setValidation] = useState<{ valid: boolean; error?: string } | null>(null)

  const handleChange = (newValue: string) => {
    onChange(newValue)
    if (newValue.trim()) {
      setValidation(validateCronExpression(newValue))
    } else {
      setValidation(null)
    }
  }

  const handlePresetSelect = (preset: string) => {
    handleChange(preset)
  }

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <Label htmlFor="cron-expression">Schedule Cron Expression *</Label>
          <Select onValueChange={handlePresetSelect} disabled={disabled}>
            <SelectTrigger className="w-[200px]">
              <SelectValue placeholder="Use preset" />
            </SelectTrigger>
            <SelectContent>
              {cronPresets.map((preset) => (
                <SelectItem key={preset.value} value={preset.value}>
                  {preset.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <Input
          id="cron-expression"
          value={value}
          onChange={(e) => handleChange(e.target.value)}
          placeholder="0 9 * * *"
          className={cn(
            "font-mono",
            error && "border-error-500",
            validation && !validation.valid && "border-error-500"
          )}
          disabled={disabled}
        />
        {error && <p className="text-sm text-error-600">{error}</p>}
        {validation && !validation.valid && (
          <p className="text-sm text-error-600">{validation.error}</p>
        )}
      </div>

      <Alert>
        <Info className="h-4 w-4" />
        <AlertDescription>
          <div className="space-y-1">
            <p className="font-medium text-sm">Cron Expression Format:</p>
            <p className="text-xs">
              <code className="bg-secondary-100 px-1 rounded">minute hour day month weekday</code>
            </p>
            <p className="text-xs text-secondary-600">
              Example: <code className="bg-secondary-100 px-1 rounded">0 9 * * *</code> = Daily at 9:00 AM
            </p>
          </div>
        </AlertDescription>
      </Alert>

      {validation && validation.valid && (
        <Alert variant="default" className="border-success-200 bg-success-50">
          <CheckCircle2 className="h-4 w-4 text-success-700" />
          <AlertDescription>
            <p className="text-sm text-success-700">Cron expression is valid</p>
          </AlertDescription>
        </Alert>
      )}
    </div>
  )
}

