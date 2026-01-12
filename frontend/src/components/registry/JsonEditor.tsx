import { useState, useEffect, useCallback, useRef } from "react"
import { Textarea } from "@/components/ui/textarea"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { RefreshCw, AlertCircle } from "lucide-react"
import { cn } from "@/lib/utils"

interface JsonEditorProps {
  value: string | object
  onChange: (value: string | object) => void
  placeholder?: string
  className?: string
  label?: string
  description?: string
  error?: string
}

export function JsonEditor({
  value,
  onChange,
  placeholder = '{"key": "value"} or use ${variable} for template variables',
  className,
  label,
  description,
  error,
}: JsonEditorProps) {
  const [jsonString, setJsonString] = useState<string>("")
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null)
  const formatTimerRef = useRef<NodeJS.Timeout | null>(null)

  // Helper function to extract MVEL expressions from JSON string
  const extractMvelExpressions = (text: string): { placeholders: Map<string, string>, textWithPlaceholders: string } => {
    const placeholders = new Map<string, string>()
    let placeholderIndex = 0
    let textWithPlaceholders = text

    // Match MVEL expressions: @{...}
    const mvelPattern = /@\{[^}]+\}/g
    const matches = text.matchAll(mvelPattern)

    for (const match of matches) {
      const mvelExpr = match[0]
      const placeholder = `__MVEL_PLACEHOLDER_${placeholderIndex++}__`
      placeholders.set(placeholder, mvelExpr)
      textWithPlaceholders = textWithPlaceholders.replace(mvelExpr, `"${placeholder}"`)
    }

    return { placeholders, textWithPlaceholders }
  }

  // Helper function to restore MVEL expressions after formatting
  const restoreMvelExpressions = (formattedText: string, placeholders: Map<string, string>): string => {
    let restored = formattedText
    placeholders.forEach((mvelExpr, placeholder) => {
      // Replace quoted placeholders with unquoted MVEL expressions
      // Handle both quoted and unquoted placeholders
      restored = restored.replace(new RegExp(`"${placeholder}"`, 'g'), mvelExpr)
      restored = restored.replace(new RegExp(placeholder, 'g'), mvelExpr)
    })
    return restored
  }

  // Helper function to format JSON with MVEL expressions (no validation)
  const formatJsonWithMvel = (text: string): string => {
    if (!text.trim()) {
      return ""
    }

    // Check if it's a template variable (starts with ${)
    if (text.trim().startsWith("${") && text.trim().endsWith("}")) {
      return text
    }

    // Check if it's a plain string (not JSON)
    if (!text.trim().startsWith("{") && !text.trim().startsWith("[")) {
      return text
    }

    try {
      // Extract MVEL expressions and replace with placeholders
      const { placeholders, textWithPlaceholders } = extractMvelExpressions(text)

      // Parse JSON with placeholders
      const parsed = JSON.parse(textWithPlaceholders)

      // Format JSON
      const formatted = JSON.stringify(parsed, null, 2)

      // Restore MVEL expressions
      const restored = restoreMvelExpressions(formatted, placeholders)

      return restored
    } catch (e) {
      // If parsing fails, return original text (no validation)
      return text
    }
  }

  // Initialize jsonString from value prop - only update if value actually changed
  useEffect(() => {
    // Clear debounce timer when value prop changes
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current)
      debounceTimerRef.current = null
    }

    if (typeof value === "string") {
      // Format JSON with MVEL expressions
      const formatted = formatJsonWithMvel(value)
      setJsonString(formatted)
    } else if (value && typeof value === "object") {
      try {
        const formatted = JSON.stringify(value, null, 2)
        setJsonString(formatted)
      } catch (e) {
        setJsonString("")
      }
    } else {
      setJsonString("")
    }
  }, [value])

  // Cleanup timers on unmount
  useEffect(() => {
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current)
      }
      if (formatTimerRef.current) {
        clearTimeout(formatTimerRef.current)
      }
    }
  }, [])

  const handleChange = useCallback((newValue: string) => {
    setJsonString(newValue)

    // Clear previous timers
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current)
    }
    if (formatTimerRef.current) {
      clearTimeout(formatTimerRef.current)
    }

    // Auto-format JSON after user stops typing (debounced)
    if (newValue.trim().startsWith("{") || newValue.trim().startsWith("[")) {
      formatTimerRef.current = setTimeout(() => {
        const formatted = formatJsonWithMvel(newValue)
        if (formatted !== newValue) {
          setJsonString(formatted)
        }
      }, 1000) // Format after 1 second of no typing
    }

    // Debounce onChange to avoid lag
    debounceTimerRef.current = setTimeout(() => {
      // Check if there are MVEL expressions
      const hasMvel = /@\{[^}]+\}/.test(newValue)
      if (hasMvel && (newValue.trim().startsWith("{") || newValue.trim().startsWith("["))) {
        // Return formatted string with MVEL expressions
        const formatted = formatJsonWithMvel(newValue)
        onChange(formatted)
      } else if (newValue.trim().startsWith("{") || newValue.trim().startsWith("[")) {
        // No MVEL expressions, try to return parsed object
        try {
          const parsed = JSON.parse(newValue)
          onChange(parsed)
        } catch {
          onChange(newValue)
        }
      } else {
        onChange(newValue)
      }
    }, 300)
  }, [onChange])

  const handleFormat = () => {
    if (!jsonString.trim()) return

    const formatted = formatJsonWithMvel(jsonString)
    setJsonString(formatted)
    
    // Check if there are MVEL expressions
    const hasMvel = /@\{[^}]+\}/.test(formatted)
    if (hasMvel) {
      // Return formatted string with MVEL expressions
      onChange(formatted)
    } else {
      // No MVEL expressions, return parsed object
      try {
        const parsed = JSON.parse(formatted)
        onChange(parsed)
      } catch {
        onChange(formatted)
      }
    }
  }

  return (
    <div className={cn("space-y-2", className)}>
      {label && (
        <Label className="text-sm font-medium">
          {label}
        </Label>
      )}
      {description && (
        <p className="text-xs text-secondary-500">{description}</p>
      )}
      <div className="space-y-2">
        <div className="relative">
          <Textarea
            value={jsonString}
            onChange={(e) => handleChange(e.target.value)}
            placeholder={placeholder}
            className="font-mono text-sm min-h-[120px]"
            rows={8}
          />
          {jsonString.trim() && (
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={handleFormat}
              className="absolute top-2 right-2 cursor-pointer"
              title="Format JSON"
            >
              <RefreshCw className="h-4 w-4" />
            </Button>
          )}
        </div>
        {error && (
          <div className="flex items-center gap-2 text-sm text-error-600">
            <AlertCircle className="h-4 w-4" />
            <span>{error}</span>
          </div>
        )}
        {jsonString.trim() && !error && (
          <p className="text-xs text-secondary-500">
            You can use <code className="px-1 py-0.5 bg-secondary-100 rounded">{"@{expression}"}</code> for MVEL expressions. JSON will be auto-formatted.
          </p>
        )}
      </div>
    </div>
  )
}

