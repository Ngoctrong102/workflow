import { useEffect } from "react"
import { useForm, Controller } from "react-hook-form"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { X } from "lucide-react"
import type { FieldDefinition, FieldType } from "@/utils/fieldTypeValidator"
import { useObjectTypes } from "@/hooks/use-object-types"

interface FieldDefinitionEditorProps {
  field?: FieldDefinition
  onSave: (field: FieldDefinition) => void
  onCancel: () => void
  existingFieldNames?: string[]
}

const FIELD_TYPES: FieldType[] = [
  "string",
  "number",
  "boolean",
  "date",
  "datetime",
  "email",
  "phone",
  "url",
  "json",
  "array",
  "object",
]

export function FieldDefinitionEditor({
  field,
  onSave,
  onCancel,
  existingFieldNames = [],
}: FieldDefinitionEditorProps) {
  const { data: objectTypesData } = useObjectTypes()
  const availableObjectTypes = objectTypesData?.data || []

  const {
    register,
    handleSubmit,
    control,
    watch,
    reset,
    formState: { errors },
  } = useForm<FieldDefinition>({
    defaultValues: field || {
      name: "",
      displayName: "",
      type: "string",
      required: false,
      defaultValue: undefined,
      validation: {},
      description: "",
      examples: [],
    },
  })

  const fieldType = watch("type")
  const isObjectType = fieldType === "object"
  const isArrayType = fieldType === "array"

  useEffect(() => {
    if (field) {
      reset(field)
    }
  }, [field, reset])

  const onSubmit = (data: FieldDefinition) => {
    // Validate field name uniqueness
    if (existingFieldNames.includes(data.name) && (!field || field.name !== data.name)) {
      return
    }

    // Clean up validation based on field type
    const cleanedData: FieldDefinition = {
      ...data,
      validation: {
        ...data.validation,
        // Remove validation rules that don't apply to this field type
        ...(fieldType === "string" || fieldType === "email" || fieldType === "phone" || fieldType === "url"
          ? {}
          : { minLength: undefined, maxLength: undefined, pattern: undefined }),
        ...(fieldType === "number"
          ? {}
          : { min: undefined, max: undefined }),
        ...(fieldType === "array"
          ? {}
          : { minItems: undefined, maxItems: undefined }),
      },
    }

    onSave(cleanedData)
  }

  return (
    <Card className="border-primary-200 bg-primary-50/30">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-base">{field ? "Edit Field" : "Add Field"}</CardTitle>
            <CardDescription className="text-xs">
              Define field properties and validation rules
            </CardDescription>
          </div>
          <Button variant="ghost" size="sm" onClick={onCancel} className="cursor-pointer">
            <X className="h-4 w-4" />
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Field Name */}
          <div className="space-y-2">
            <Label htmlFor="name">
              Field Name <span className="text-error-600">*</span>
            </Label>
            <Input
              id="name"
              {...register("name", {
                required: "Field name is required",
                pattern: {
                  value: /^[a-zA-Z_][a-zA-Z0-9_]*$/,
                  message: "Field name must start with letter or underscore and contain only alphanumeric characters and underscores",
                },
                validate: (value) => {
                  if (existingFieldNames.includes(value) && (!field || field.name !== value)) {
                    return "Field name already exists"
                  }
                  return true
                },
              })}
              placeholder="email"
            />
            {errors.name && <p className="text-sm text-error-600">{errors.name.message}</p>}
          </div>

          {/* Display Name */}
          <div className="space-y-2">
            <Label htmlFor="displayName">Display Name</Label>
            <Input
              id="displayName"
              {...register("displayName")}
              placeholder="Email Address"
            />
          </div>

          {/* Field Type */}
          <div className="space-y-2">
            <Label htmlFor="type">
              Field Type <span className="text-error-600">*</span>
            </Label>
            <Controller
              name="type"
              control={control}
              rules={{ required: "Field type is required" }}
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select field type" />
                  </SelectTrigger>
                  <SelectContent>
                    {FIELD_TYPES.map((type) => (
                      <SelectItem key={type} value={type}>
                        {type.charAt(0).toUpperCase() + type.slice(1)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.type && <p className="text-sm text-error-600">{errors.type.message}</p>}
          </div>

          {/* Required */}
          <div className="flex items-center space-x-2">
            <Controller
              name="required"
              control={control}
              render={({ field }) => (
                <Checkbox
                  id="required"
                  checked={field.value}
                  onCheckedChange={field.onChange}
                />
              )}
            />
            <Label htmlFor="required" className="font-normal cursor-pointer">
              Required field
            </Label>
          </div>

          {/* Default Value (type-aware) */}
          {(fieldType === "string" ||
            fieldType === "email" ||
            fieldType === "phone" ||
            fieldType === "url") && (
            <div className="space-y-2">
              <Label htmlFor="defaultValue">Default Value</Label>
              <Input
                id="defaultValue"
                type="text"
                {...register("defaultValue")}
                placeholder="default value"
              />
            </div>
          )}

          {fieldType === "number" && (
            <div className="space-y-2">
              <Label htmlFor="defaultValue">Default Value</Label>
              <Input
                id="defaultValue"
                type="number"
                {...register("defaultValue", { valueAsNumber: true })}
                placeholder="0"
              />
            </div>
          )}

          {fieldType === "boolean" && (
            <div className="space-y-2">
              <Label htmlFor="defaultValue">Default Value</Label>
              <Controller
                name="defaultValue"
                control={control}
                render={({ field }) => (
                  <Select
                    value={field.value === undefined ? "" : String(field.value)}
                    onValueChange={(value) => field.onChange(value === "true" ? true : value === "false" ? false : undefined)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select default value" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="">None</SelectItem>
                      <SelectItem value="true">True</SelectItem>
                      <SelectItem value="false">False</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>
          )}

          {/* Nested Object Type (for object/array types) */}
          {(isObjectType || isArrayType) && (
            <div className="space-y-2">
              <Label htmlFor="objectTypeId">
                {isObjectType ? "Object Type" : "Item Object Type"}
              </Label>
              <Controller
                name="validation.objectTypeId"
                control={control}
                render={({ field }) => (
                  <Select value={field.value || ""} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select object type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="">None</SelectItem>
                      {availableObjectTypes.map((type) => (
                        <SelectItem key={type.id} value={type.id}>
                          {type.displayName || type.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
            </div>
          )}

          {/* Array Item Type */}
          {isArrayType && (
            <div className="space-y-2">
              <Label htmlFor="itemType">Item Type</Label>
              <Controller
                name="validation.itemType"
                control={control}
                render={({ field }) => (
                  <Select value={field.value || ""} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select item type" />
                    </SelectTrigger>
                    <SelectContent>
                      {FIELD_TYPES.filter((t) => t !== "array").map((type) => (
                        <SelectItem key={type} value={type}>
                          {type.charAt(0).toUpperCase() + type.slice(1)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
            </div>
          )}

          {/* Validation Rules (conditional based on field type) */}
          {(fieldType === "string" || fieldType === "email" || fieldType === "phone" || fieldType === "url") && (
            <div className="space-y-4 p-4 border rounded-lg">
              <Label className="text-sm font-semibold">String Validation</Label>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="minLength">Min Length</Label>
                  <Input
                    id="minLength"
                    type="number"
                    {...register("validation.minLength", { valueAsNumber: true })}
                    placeholder="0"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="maxLength">Max Length</Label>
                  <Input
                    id="maxLength"
                    type="number"
                    {...register("validation.maxLength", { valueAsNumber: true })}
                    placeholder="255"
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="pattern">Pattern (Regex)</Label>
                <Input
                  id="pattern"
                  {...register("validation.pattern")}
                  placeholder="^[a-zA-Z0-9]+$"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="enum">Allowed Values (comma-separated)</Label>
                <Input
                  id="enum"
                  {...register("validation.enum")}
                  placeholder="value1,value2,value3"
                  onChange={(e) => {
                    const value = e.target.value
                    const enumValues = value ? value.split(",").map((v) => v.trim()).filter(Boolean) : undefined
                    // @ts-ignore
                    control._formValues.validation = {
                      ...control._formValues.validation,
                      enum: enumValues,
                    }
                  }}
                />
              </div>
            </div>
          )}

          {fieldType === "number" && (
            <div className="space-y-4 p-4 border rounded-lg">
              <Label className="text-sm font-semibold">Number Validation</Label>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="min">Min Value</Label>
                  <Input
                    id="min"
                    type="number"
                    {...register("validation.min", { valueAsNumber: true })}
                    placeholder="0"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="max">Max Value</Label>
                  <Input
                    id="max"
                    type="number"
                    {...register("validation.max", { valueAsNumber: true })}
                    placeholder="100"
                  />
                </div>
              </div>
            </div>
          )}

          {fieldType === "array" && (
            <div className="space-y-4 p-4 border rounded-lg">
              <Label className="text-sm font-semibold">Array Validation</Label>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="minItems">Min Items</Label>
                  <Input
                    id="minItems"
                    type="number"
                    {...register("validation.minItems", { valueAsNumber: true })}
                    placeholder="0"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="maxItems">Max Items</Label>
                  <Input
                    id="maxItems"
                    type="number"
                    {...register("validation.maxItems", { valueAsNumber: true })}
                    placeholder="100"
                  />
                </div>
              </div>
            </div>
          )}

          {/* Description */}
          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              {...register("description")}
              placeholder="Field description"
              rows={3}
            />
          </div>

          {/* Examples */}
          <div className="space-y-2">
            <Label htmlFor="examples">Examples (comma-separated)</Label>
            <Input
              id="examples"
              {...register("examples")}
              placeholder="example1, example2, example3"
              onChange={(e) => {
                const value = e.target.value
                const examples = value ? value.split(",").map((v) => v.trim()).filter(Boolean) : []
                // @ts-ignore
                control._formValues.examples = examples
              }}
            />
          </div>

          {/* Action Buttons */}
          <div className="flex space-x-2 pt-4">
            <Button type="submit" className="flex-1">
              {field ? "Update Field" : "Add Field"}
            </Button>
            <Button type="button" variant="outline" onClick={onCancel} className="flex-1">
              Cancel
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}

