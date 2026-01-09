import { useNavigate, useParams } from "react-router-dom"
import { TemplateEditor as TemplateEditorComponent } from "@/components/template/TemplateEditor"
import { useTemplate, useCreateTemplate, useUpdateTemplate } from "@/hooks/use-templates"
import type { Template } from "@/types/template"

export default function TemplateEditorPage() {
  const navigate = useNavigate()
  const { id } = useParams()

  const isEditMode = id !== "new" && id !== undefined
  const { data: template, isLoading: isLoadingTemplate } = useTemplate(id)
  const createTemplate = useCreateTemplate()
  const updateTemplate = useUpdateTemplate()

  const handleSave = async (templateData: Omit<Template, "id" | "createdAt" | "updatedAt">) => {
    try {
      if (isEditMode && id) {
        await updateTemplate.mutateAsync({
          id,
          ...templateData,
        })
      } else {
        await createTemplate.mutateAsync(templateData)
      }
      navigate("/templates")
    } catch (error) {
      // Error handling is done in the mutation hooks
      console.error("Failed to save template:", error)
    }
  }

  const handleCancel = () => {
    navigate("/templates")
  }

  if (isLoadingTemplate && isEditMode) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex items-center justify-center h-screen">
          <div className="text-secondary-500">Loading template...</div>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold">
          {isEditMode ? "Edit Template" : "Create Template"}
        </h1>
        <p className="text-secondary-600 mt-2">
          {isEditMode
            ? "Update your notification template"
            : "Create a new notification template"}
        </p>
      </div>
      <TemplateEditorComponent
        template={template}
        onSave={handleSave}
        onCancel={handleCancel}
      />
    </div>
  )
}

