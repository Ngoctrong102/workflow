import { useState } from "react"
import { Textarea } from "@/components/ui/textarea"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { CheckCircle2, XCircle, Loader2, Info } from "lucide-react"
import { cn } from "@/lib/utils"

interface AnalystQueryEditorProps {
  value: string
  onChange: (value: string) => void
  onValidate?: (query: string) => Promise<{ valid: boolean; error: string | null }>
  error?: string
  disabled?: boolean
}

export function AnalystQueryEditor({
  value,
  onChange,
  onValidate,
  error,
  disabled = false,
}: AnalystQueryEditorProps) {
  const [isValidating, setIsValidating] = useState(false)
  const [validationResult, setValidationResult] = useState<{
    valid: boolean
    error: string | null
  } | null>(null)

  const handleValidate = async () => {
    if (!onValidate || !value.trim()) return

    setIsValidating(true)
    setValidationResult(null)

    try {
      const result = await onValidate(value)
      setValidationResult(result)
    } catch (err) {
      setValidationResult({
        valid: false,
        error: err instanceof Error ? err.message : "Validation failed",
      })
    } finally {
      setIsValidating(false)
    }
  }

  const queryHints = [
    { param: ":workflow_id", description: "Automatically replaced with workflow ID" },
    { param: ":start_date", description: "Automatically replaced with period start date (TIMESTAMP)" },
    { param: ":end_date", description: "Automatically replaced with period end date (TIMESTAMP)" },
  ]

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <Label htmlFor="analyst-query">Analyst Query *</Label>
          {onValidate && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleValidate}
              disabled={isValidating || !value.trim() || disabled}
            >
              {isValidating ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Validating...
                </>
              ) : (
                "Validate Query"
              )}
            </Button>
          )}
        </div>
        <Textarea
          id="analyst-query"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder="SELECT COUNT(*) as total_executions, AVG(duration) as avg_duration FROM executions WHERE workflow_id = :workflow_id AND started_at BETWEEN :start_date AND :end_date"
          className={cn(
            "font-mono text-sm min-h-[200px]",
            error && "border-error-500",
            validationResult && !validationResult.valid && "border-error-500"
          )}
          disabled={disabled}
        />
        {error && <p className="text-sm text-error-600">{error}</p>}
      </div>

      {/* Query Parameter Hints */}
      <Alert>
        <Info className="h-4 w-4" />
        <AlertDescription>
          <div className="space-y-1">
            <p className="font-medium text-sm">Available Query Parameters:</p>
            <ul className="list-disc list-inside space-y-1 text-xs">
              {queryHints.map((hint) => (
                <li key={hint.param}>
                  <code className="bg-secondary-100 px-1 rounded">{hint.param}</code> - {hint.description}
                </li>
              ))}
            </ul>
          </div>
        </AlertDescription>
      </Alert>

      {/* Validation Result */}
      {validationResult && (
        <Alert variant={validationResult.valid ? "default" : "destructive"}>
          {validationResult.valid ? (
            <CheckCircle2 className="h-4 w-4" />
          ) : (
            <XCircle className="h-4 w-4" />
          )}
          <AlertDescription>
            {validationResult.valid ? (
              <p className="text-success-700">Query is valid and ready to use.</p>
            ) : (
              <p className="text-error-700">
                {validationResult.error || "Query validation failed. Please check the syntax."}
              </p>
            )}
          </AlertDescription>
        </Alert>
      )}
    </div>
  )
}

