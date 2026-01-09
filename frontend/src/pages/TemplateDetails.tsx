import { useNavigate, useParams } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { TemplatePreview } from "@/components/template/TemplatePreview"
import { Edit, ArrowLeft, Trash2, Loader2 } from "lucide-react"
import { useTemplate, useDeleteTemplate } from "@/hooks/use-templates"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import type { Template } from "@/types/template"

const channelLabels: Record<Template["channel"], string> = {
  email: "Email",
  sms: "SMS",
  push: "Push",
  "in-app": "In-App",
  slack: "Slack",
  discord: "Discord",
  teams: "Teams",
  webhook: "Webhook",
}

export default function TemplateDetails() {
  const navigate = useNavigate()
  const { id } = useParams()
  const { data: template, isLoading } = useTemplate(id)
  const deleteTemplate = useDeleteTemplate()

  const { confirm } = useConfirmDialog()

  const handleDelete = async () => {
    if (!id) return

    const confirmed = await confirm({
      title: "Delete Template",
      description: "Are you sure you want to delete this template? This action cannot be undone.",
      variant: "destructive",
      confirmText: "Delete",
      cancelText: "Cancel",
    })

    if (confirmed) {
      await deleteTemplate.mutateAsync(id)
      navigate("/templates")
    }
  }

  if (isLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex items-center justify-center h-screen">
          <div className="text-secondary-500">Loading template...</div>
        </div>
      </div>
    )
  }

  if (!template) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardContent className="p-6 text-center">
            <p className="text-secondary-500">Template not found</p>
            <Button
              variant="outline"
              className="mt-4"
              onClick={() => navigate("/templates")}
            >
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Templates
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <Button
            variant="ghost"
            onClick={() => navigate("/templates")}
            className="mb-2"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <h1 className="text-3xl font-bold">{template.name}</h1>
          <p className="text-secondary-600 mt-2">{template.description}</p>
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            onClick={() => navigate(`/templates/${template.id}`)}
          >
            <Edit className="h-4 w-4 mr-2" />
            Edit
          </Button>
          <Button
            variant="outline"
            onClick={handleDelete}
            disabled={deleteTemplate.isPending}
          >
            {deleteTemplate.isPending ? (
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <Trash2 className="h-4 w-4 mr-2" />
            )}
            Delete
          </Button>
        </div>
      </div>

      {/* Template Info */}
      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Template Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <div className="text-sm text-secondary-500">Channel</div>
              <Badge variant="outline" className="mt-1">
                {channelLabels[template.channel]}
              </Badge>
            </div>
            <div>
              <div className="text-sm text-secondary-500">Status</div>
              <Badge
                variant={
                  template.status === "active"
                    ? "default"
                    : template.status === "inactive"
                    ? "secondary"
                    : "outline"
                }
                className="mt-1"
              >
                {template.status || "draft"}
              </Badge>
            </div>
            {template.version && (
              <div>
                <div className="text-sm text-secondary-500">Version</div>
                <div className="mt-1">{template.version}</div>
              </div>
            )}
            {template.createdAt && (
              <div>
                <div className="text-sm text-secondary-500">Created</div>
                <div className="mt-1">
                  {new Date(template.createdAt).toLocaleString()}
                </div>
              </div>
            )}
            {template.updatedAt && (
              <div>
                <div className="text-sm text-secondary-500">Updated</div>
                <div className="mt-1">
                  {new Date(template.updatedAt).toLocaleString()}
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Variables */}
        <Card>
          <CardHeader>
            <CardTitle>Variables</CardTitle>
            <CardDescription>Variables used in this template</CardDescription>
          </CardHeader>
          <CardContent>
            {template.variables && template.variables.length > 0 ? (
              <div className="flex flex-wrap gap-2">
                {template.variables.map((variable) => (
                  <Badge key={variable} variant="secondary">
                    {`{{${variable}}}`}
                  </Badge>
                ))}
              </div>
            ) : (
              <p className="text-sm text-secondary-500">No variables used</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Preview */}
      <TemplatePreview
        channel={template.channel}
        subject={template.subject}
        body={template.body}
      />
    </div>
  )
}

