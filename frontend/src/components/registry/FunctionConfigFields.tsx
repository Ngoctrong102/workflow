import { useEffect } from "react"
import { useForm } from "react-hook-form"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { MvelInputEditor } from "./MvelInputEditor"
import { cn } from "@/lib/utils"
import type { SchemaDefinition } from "./SchemaEditor"

export interface FunctionConfig {
  expression?: string
  outputField?: string
}

interface FunctionConfigFieldsProps {
  config: FunctionConfig
  onChange: (config: FunctionConfig) => void
  inputSchema?: SchemaDefinition[]
}

export function FunctionConfigFields({ config, onChange, inputSchema = [] }: FunctionConfigFieldsProps) {
  const { register, watch, setValue, reset, formState: { errors } } = useForm<FunctionConfig>({
    defaultValues: {
      expression: config.expression || "",
      outputField: config.outputField || "result",
    },
  })

  // Update form values when config prop changes (e.g., when loading from backend)
  useEffect(() => {
    reset({
      expression: config.expression || "",
      outputField: config.outputField || "result",
    })
  }, [config, reset])

  const watchedValues = watch()

  const handleExpressionChange = (value: string) => {
    setValue("expression", value)
    onChange({
      ...watchedValues,
      expression: value,
    })
  }

  const handleOutputFieldChange = (value: string) => {
    setValue("outputField", value)
    onChange({
      ...watchedValues,
      outputField: value,
    })
  }

  return (
    <div className="space-y-6">
      {/* Expression */}
      <MvelInputEditor
        value={watchedValues.expression || ""}
        onChange={handleExpressionChange}
        inputSchema={inputSchema}
        label="Expression"
        required
        type="textarea"
        rows={8}
        placeholder='concat(@{user.firstName}, " ", @{user.lastName})'
        description="MVEL expression. Type @{ to see available input fields."
        errors={errors.expression?.message as string}
      />

      {/* Output Field Name */}
      <MvelInputEditor
        value={watchedValues.outputField || "result"}
        onChange={handleOutputFieldChange}
        inputSchema={inputSchema}
        label="Output Field Name"
        placeholder="result"
        description="Name of the output field where the expression result will be stored (default: 'result')."
      />
    </div>
  )
}

