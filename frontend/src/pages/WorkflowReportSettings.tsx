import { useParams, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  ArrowLeft,
  Edit,
  Play,
  Pause,
  Trash2,
  FileText,
  Mail,
  Calendar,
  Settings,
  Download,
  Loader2,
} from "lucide-react"
import { useWorkflow } from "@/hooks/use-workflows"
import {
  useWorkflowReportConfig,
  useDeleteWorkflowReportConfig,
  useUpdateWorkflowReportStatus,
  useGenerateReport,
} from "@/hooks/use-workflow-report"
import { WorkflowReportHistory } from "@/components/report/WorkflowReportHistory"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { format } from "date-fns"

export default function WorkflowReportSettings() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { confirm } = useConfirmDialog()

  const { data: workflow } = useWorkflow(id)
  const { data: config, isLoading } = useWorkflowReportConfig(id)
  const deleteConfig = useDeleteWorkflowReportConfig()
  const updateStatus = useUpdateWorkflowReportStatus()
  const generateReport = useGenerateReport()

  const handleDelete = async () => {
    const confirmed = await confirm({
      title: "Delete Report Configuration",
      description: "Are you sure you want to delete this report configuration? This action cannot be undone.",
      variant: "destructive",
      confirmText: "Delete",
    })

    if (confirmed && id) {
      await deleteConfig.mutateAsync(id)
      navigate(`/workflows/${id}/dashboard`)
    }
  }

  const handleStatusChange = async (status: "active" | "inactive" | "paused") => {
    if (id) {
      await updateStatus.mutateAsync({ workflowId: id, status })
    }
  }

  const handleTestReport = async () => {
    if (id) {
      await generateReport.mutateAsync({
        workflowId: id,
      })
    }
  }

  if (isLoading) {
    return (
      <div className="container mx-auto p-6 space-y-6">
        <Skeleton className="h-10 w-1/3" />
        <Skeleton className="h-64 w-full" />
      </div>
    )
  }

  if (!config) {
    return (
      <div className="container mx-auto p-6">
        <div className="text-center py-12">
          <FileText className="h-12 w-12 mx-auto mb-4 text-secondary-400" />
          <p className="text-secondary-600 mb-4">No report configuration found</p>
          <Button onClick={() => navigate(`/workflows/${id}/report`)}>
            Configure Report
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => navigate(`/workflows/${id}/dashboard`)}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Dashboard
          </Button>
          <div>
            <h1 className="text-3xl font-bold">Report Settings</h1>
            <p className="text-sm text-secondary-500 mt-1">
              {workflow?.name || "Workflow"} - Report configuration and history
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate(`/workflows/${id}/report`)}
          >
            <Edit className="h-4 w-4 mr-2" />
            Edit Configuration
          </Button>
        </div>
      </div>

      <Tabs defaultValue="settings" className="space-y-6">
        <TabsList>
          <TabsTrigger value="settings">Settings</TabsTrigger>
          <TabsTrigger value="history">History</TabsTrigger>
        </TabsList>

        <TabsContent value="settings" className="space-y-6">
          {/* Configuration Overview */}
          <Card>
            <CardHeader>
              <CardTitle>Configuration</CardTitle>
              <CardDescription>Current report configuration</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-secondary-500">Report Name</p>
                  <p className="font-medium">{config.name}</p>
                </div>
                <div>
                  <p className="text-sm text-secondary-500">Format</p>
                  <Badge variant="outline" className="uppercase">
                    {config.format}
                  </Badge>
                </div>
                <div>
                  <p className="text-sm text-secondary-500">Period Type</p>
                  <Badge variant="outline" className="capitalize">
                    {config.period_type.replace(/_/g, " ")}
                  </Badge>
                </div>
                <div>
                  <p className="text-sm text-secondary-500">Status</p>
                  <Badge variant={config.status === "active" ? "default" : "secondary"}>
                    {config.status}
                  </Badge>
                </div>
                <div>
                  <p className="text-sm text-secondary-500">Timezone</p>
                  <p className="font-medium">{config.timezone}</p>
                </div>
                <div>
                  <p className="text-sm text-secondary-500">Generation Count</p>
                  <p className="font-medium">{config.generation_count || 0}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Recipients */}
          <Card>
            <CardHeader>
              <CardTitle>Recipients</CardTitle>
              <CardDescription>Email addresses receiving the report</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {config.recipients.map((email) => (
                  <Badge key={email} variant="secondary" className="flex items-center gap-1">
                    <Mail className="h-3 w-3" />
                    {email}
                  </Badge>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Analyst Query */}
          <Card>
            <CardHeader>
              <CardTitle>Analyst Query</CardTitle>
              <CardDescription>SQL query used for report generation</CardDescription>
            </CardHeader>
            <CardContent>
              <pre className="text-xs font-mono bg-secondary-50 p-3 rounded overflow-x-auto">
                {config.analyst_query}
              </pre>
            </CardContent>
          </Card>

          {/* Schedule Details */}
          <Card>
            <CardHeader>
              <CardTitle>Schedule Details</CardTitle>
              <CardDescription>When the report is generated</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-2">
                <Calendar className="h-4 w-4 text-secondary-400" />
                <span className="text-sm font-mono">
                  {config.schedule_cron} ({config.timezone})
                </span>
              </div>
              {config.next_generation_at && (
                <div>
                  <p className="text-sm text-secondary-500">Next Generation</p>
                  <p className="font-medium">
                    {format(new Date(config.next_generation_at), "MMM dd, yyyy HH:mm:ss")}
                  </p>
                </div>
              )}
              {config.last_generated_at && (
                <div>
                  <p className="text-sm text-secondary-500">Last Generated</p>
                  <p className="font-medium">
                    {format(new Date(config.last_generated_at), "MMM dd, yyyy HH:mm:ss")}
                  </p>
                  {config.last_generation_status && (
                    <Badge
                      variant={config.last_generation_status === "success" ? "default" : "destructive"}
                      className="mt-1"
                    >
                      {config.last_generation_status}
                    </Badge>
                  )}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Actions */}
          <Card>
            <CardHeader>
              <CardTitle>Actions</CardTitle>
              <CardDescription>Manage report configuration</CardDescription>
            </CardHeader>
            <CardContent className="space-y-2">
              <div className="flex flex-wrap gap-2">
                <Button
                  variant="outline"
                  onClick={() => navigate(`/workflows/${id}/report`)}
                >
                  <Edit className="h-4 w-4 mr-2" />
                  Edit Configuration
                </Button>
                <Button
                  variant="outline"
                  onClick={handleTestReport}
                  disabled={generateReport.isPending}
                >
                  {generateReport.isPending ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Generating...
                    </>
                  ) : (
                    <>
                      <Download className="h-4 w-4 mr-2" />
                      Test Report
                    </>
                  )}
                </Button>
                {config.status === "active" ? (
                  <Button
                    variant="outline"
                    onClick={() => handleStatusChange("paused")}
                    disabled={updateStatus.isPending}
                  >
                    <Pause className="h-4 w-4 mr-2" />
                    Pause
                  </Button>
                ) : (
                  <Button
                    variant="outline"
                    onClick={() => handleStatusChange("active")}
                    disabled={updateStatus.isPending}
                  >
                    <Play className="h-4 w-4 mr-2" />
                    Activate
                  </Button>
                )}
                <Button
                  variant="destructive"
                  onClick={handleDelete}
                  disabled={deleteConfig.isPending}
                >
                  {deleteConfig.isPending ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Deleting...
                    </>
                  ) : (
                    <>
                      <Trash2 className="h-4 w-4 mr-2" />
                      Delete
                    </>
                  )}
                </Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="history">
          {id && <WorkflowReportHistory workflowId={id} />}
        </TabsContent>
      </Tabs>
    </div>
  )
}

