import { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { useForm, useFieldArray } from "react-hook-form"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Plus, Save, X } from "lucide-react"
import { useObjectType, useCreateObjectType, useUpdateObjectType } from "@/hooks/use-object-types"
import { FieldList } from "@/components/object-type/FieldList"
import { FieldDefinitionEditor } from "@/components/object-type/FieldDefinitionEditor"
import { ObjectTypePreview } from "@/components/object-type/ObjectTypePreview"
import { ObjectStructureView } from "@/components/object-type/ObjectStructureView"
import { JSONFieldEditor } from "@/components/object-type/JSONFieldEditor"
import type { CreateObjectTypeRequest, UpdateObjectTypeRequest } from "@/types/objectTypeTypes"
import type { FieldDefinition } from "@/utils/fieldTypeValidator"

export default function ObjectTypeEditor() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const isEditMode = !!id

  const { data: objectType, isLoading: isLoadingObjectType } = useObjectType(id)
  const createObjectType = useCreateObjectType()
  const updateObjectType = useUpdateObjectType()

  const [editingFieldIndex, setEditingFieldIndex] = useState<number | null>(null)
  const [showFieldEditor, setShowFieldEditor] = useState(false)
  const [editMode, setEditMode] = useState<"visual" | "json">("visual")

  const {
    register,
    handleSubmit,
    control,
    reset,
    watch,
    formState: { errors, isDirty },
  } = useForm<CreateObjectTypeRequest>({
    defaultValues: {
      name: "",
      displayName: "",
      description: "",
      fields: [],
      tags: [],
    },
  })

  const {
    fields: formFields,
    append: appendField,
    update: updateField,
    remove: removeField,
    move: moveField,
    replace: replaceFields,
  } = useFieldArray({
    control,
    name: "fields",
  })

  // Load object type data when editing
  useEffect(() => {
    if (objectType && isEditMode) {
      reset({
        name: objectType.name,
        displayName: objectType.displayName || "",
        description: objectType.description || "",
        fields: objectType.fields || [],
        tags: objectType.tags || [],
      })
    }
  }, [objectType, isEditMode, reset])

  const onSubmit = async (data: CreateObjectTypeRequest) => {
    try {
      if (isEditMode && id) {
        await updateObjectType.mutateAsync({
          id,
          data: data as Omit<UpdateObjectTypeRequest, "id">,
        })
      } else {
        await createObjectType.mutateAsync(data)
      }
      navigate("/object-types")
    } catch (error) {
      // Error handling is done in the mutation hooks
      console.error("Failed to save object type:", error)
    }
  }

  const handleAddField = () => {
    setEditingFieldIndex(null)
    setShowFieldEditor(true)
  }

  const handleEditField = (field: FieldDefinition, index: number) => {
    setEditingFieldIndex(index)
    setShowFieldEditor(true)
  }

  const handleSaveField = (field: FieldDefinition) => {
    if (editingFieldIndex !== null) {
      updateField(editingFieldIndex, field)
    } else {
      appendField(field)
    }
    setShowFieldEditor(false)
    setEditingFieldIndex(null)
  }

  const handleCancelField = () => {
    setShowFieldEditor(false)
    setEditingFieldIndex(null)
  }

  const handleDeleteField = (index: number) => {
    removeField(index)
  }

  const handleReorderFields = (fromIndex: number, toIndex: number) => {
    moveField(fromIndex, toIndex)
  }

  const existingFieldNames = formFields.map((f) => f.name)

  if (isLoadingObjectType && isEditMode) {
    return (
      <div className="space-y-3">
        <div className="flex items-center space-x-2">
          <Button variant="ghost" size="sm" onClick={() => navigate("/object-types")}>
            <ArrowLeft className="h-4 w-4 mr-1" />
            Back
          </Button>
        </div>
        <Card>
          <CardContent className="p-6">
            <p className="text-secondary-500">Loading object type...</p>
          </CardContent>
        </Card>
      </div>
    )
  }

  // Watch form values for preview
  const watchedName = watch("name") || ""
  const watchedDisplayName = watch("displayName") || ""
  const watchedDescription = watch("description") || ""
  const watchedTagsValue = watch("tags")
  const watchedTags = Array.isArray(watchedTagsValue) 
    ? watchedTagsValue 
    : typeof watchedTagsValue === 'string' 
    ? watchedTagsValue.split(',').map(t => t.trim()).filter(Boolean)
    : []

  return (
    <div className="space-y-4 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <Button variant="ghost" size="sm" onClick={() => navigate("/object-types")} className="cursor-pointer">
            <ArrowLeft className="h-4 w-4 mr-1" />
            Back
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-secondary-900">
              {isEditMode ? "Edit Object Type" : "Create Object Type"}
            </h1>
            <p className="text-secondary-600 mt-1 text-sm">
              {isEditMode
                ? "Update object type definition and fields"
                : "Define a new object type with fields"}
            </p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          {/* Main Content - 2 columns */}
          <div className="lg:col-span-2 space-y-4">
            {/* Basic Information */}
            <Card className="border-secondary-200">
              <CardHeader>
                <CardTitle className="text-base">Basic Information</CardTitle>
                <CardDescription>Define the object type metadata</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="name">
                      Name <span className="text-error-600">*</span>
                    </Label>
                    <Input
                      id="name"
                      {...register("name", {
                        required: "Name is required",
                        pattern: {
                          value: /^[a-z][a-z0-9-]*$/,
                          message: "Name must start with lowercase letter and contain only lowercase letters, numbers, and hyphens",
                        },
                      })}
                      placeholder="user"
                      disabled={isEditMode}
                      className="transition-colors"
                    />
                    {errors.name && (
                      <p className="text-sm text-error-600">{errors.name.message}</p>
                    )}
                    <p className="text-xs text-secondary-500">
                      Used as identifier in code (lowercase, no spaces)
                    </p>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="displayName">Display Name</Label>
                    <Input
                      id="displayName"
                      {...register("displayName")}
                      placeholder="User"
                      className="transition-colors"
                    />
                    <p className="text-xs text-secondary-500">
                      Human-readable name shown in UI
                    </p>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    {...register("description")}
                    placeholder="Describe what this object type represents..."
                    rows={3}
                    className="transition-colors"
                  />
                  <p className="text-xs text-secondary-500">
                    Help others understand the purpose of this object type
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="tags">Tags</Label>
                  <Input
                    id="tags"
                    {...register("tags")}
                    placeholder="user, customer, profile"
                    onChange={(e) => {
                      const value = e.target.value
                      const tags = value ? value.split(",").map((t) => t.trim()).filter(Boolean) : []
                      // @ts-ignore
                      control._formValues.tags = tags
                    }}
                    className="transition-colors"
                  />
                  <p className="text-xs text-secondary-500">
                    Separate multiple tags with commas for better organization
                  </p>
                </div>
              </CardContent>
            </Card>

            {/* Fields Section */}
            <Card className="border-secondary-200">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="text-base">Fields</CardTitle>
                    <CardDescription>
                      Define the structure of your object type
                    </CardDescription>
                  </div>
                  <div className="flex items-center gap-2">
                    {/* Mode Toggle */}
                    <div className="flex items-center gap-1 bg-secondary-100 rounded-lg p-1">
                      <Button
                        type="button"
                        variant={editMode === "visual" ? "default" : "ghost"}
                        size="sm"
                        onClick={() => {
                          setEditMode("visual")
                          setShowFieldEditor(false)
                        }}
                        className="cursor-pointer h-7 text-xs"
                      >
                        Visual
                      </Button>
                      <Button
                        type="button"
                        variant={editMode === "json" ? "default" : "ghost"}
                        size="sm"
                        onClick={() => {
                          setEditMode("json")
                          setShowFieldEditor(false)
                        }}
                        className="cursor-pointer h-7 text-xs"
                      >
                        JSON
                      </Button>
                    </div>
                    {editMode === "visual" && !showFieldEditor && (
                      <Button
                        type="button"
                        variant="default"
                        size="sm"
                        onClick={handleAddField}
                        className="cursor-pointer"
                      >
                        <Plus className="h-4 w-4 mr-1" />
                        Add Field
                      </Button>
                    )}
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {editMode === "json" ? (
                  /* JSON Editor Mode */
                  <JSONFieldEditor
                    fields={formFields}
                    onChange={(newFields) => {
                      replaceFields(newFields)
                    }}
                  />
                ) : showFieldEditor ? (
                  /* Field Editor (when adding/editing) */
                  <FieldDefinitionEditor
                    field={editingFieldIndex !== null ? formFields[editingFieldIndex] : undefined}
                    onSave={handleSaveField}
                    onCancel={handleCancelField}
                    existingFieldNames={existingFieldNames}
                  />
                ) : (
                  /* Object Structure View */
                  <ObjectStructureView
                    fields={formFields}
                    onAddField={handleAddField}
                    onEditField={handleEditField}
                    onDeleteField={handleDeleteField}
                    objectName={watchedName || "object"}
                  />
                )}
              </CardContent>
            </Card>

            {/* Action Buttons */}
            <div className="flex space-x-2 pt-2">
              <Button
                type="submit"
                disabled={!isDirty || createObjectType.isPending || updateObjectType.isPending}
                className="flex-1 cursor-pointer"
              >
                <Save className="h-4 w-4 mr-2" />
                {createObjectType.isPending || updateObjectType.isPending
                  ? "Saving..."
                  : isEditMode
                  ? "Update Object Type"
                  : "Create Object Type"}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate("/object-types")}
                className="flex-1 cursor-pointer"
              >
                Cancel
              </Button>
            </div>
          </div>

          {/* Preview Sidebar - 1 column */}
          <div className="lg:col-span-1">
            <div className="sticky top-4">
              <ObjectTypePreview
                name={watchedName || ""}
                displayName={watchedDisplayName}
                description={watchedDescription}
                tags={watchedTags}
                fields={formFields}
              />
            </div>
          </div>
        </div>
      </form>
    </div>
  )
}

