import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Plus, Search, Edit, Trash2, Power, PowerOff, Loader2 } from "lucide-react"
import { useWorkflows } from "@/hooks/use-workflows"
import { useTriggers, useDeleteTrigger, useActivateTrigger, useDeactivateTrigger } from "@/hooks/use-triggers"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/hooks/use-toast"
import type { Trigger } from "@/types/trigger"

const triggerTypeLabels: Record<Trigger["type"], string> = {
  api: "API",
  schedule: "Schedule",
  file: "File Upload",
  event: "Event",
}

export default function TriggerListPage() {
  const navigate = useNavigate()
  const { toast } = useToast()
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedWorkflowId, setSelectedWorkflowId] = useState<string>("all")
  const [selectedType, setSelectedType] = useState<string>("all")

  const { data: workflowsData, isLoading: isLoadingWorkflows } = useWorkflows({ limit: 100 })
  const workflows = workflowsData?.data || []

  // Get triggers for selected workflow
  const { data: triggers, isLoading: isLoadingTriggers } = useTriggers(
    selectedWorkflowId !== "all" ? selectedWorkflowId : undefined
  )

  const deleteTrigger = useDeleteTrigger()
  const activateTrigger = useActivateTrigger()
  const deactivateTrigger = useDeactivateTrigger()
  const { confirm } = useConfirmDialog()

  const handleDelete = async (id: string) => {
    const confirmed = await confirm({
      title: "Delete Trigger",
      description: "Are you sure you want to delete this trigger? This action cannot be undone.",
      variant: "destructive",
      confirmText: "Delete",
      cancelText: "Cancel",
    })

    if (confirmed) {
      try {
        await deleteTrigger.mutateAsync(id)
        toast({
          title: "Success",
          description: "Trigger deleted successfully",
        })
      } catch (error) {
        toast({
          variant: "destructive",
          title: "Error",
          description: error instanceof Error ? error.message : "Failed to delete trigger",
        })
      }
    }
  }

  const handleToggleStatus = async (trigger: Trigger) => {
    try {
      if (trigger.status === "active") {
        await deactivateTrigger.mutateAsync(trigger.id)
        toast({
          title: "Success",
          description: "Trigger deactivated",
        })
      } else {
        await activateTrigger.mutateAsync(trigger.id)
        toast({
          title: "Success",
          description: "Trigger activated",
        })
      }
    } catch (error) {
      toast({
        variant: "destructive",
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to update trigger status",
      })
    }
  }

  const filteredTriggers = (triggers || []).filter((trigger) => {
    const matchesSearch = searchQuery === "" || 
      triggerTypeLabels[trigger.type].toLowerCase().includes(searchQuery.toLowerCase()) ||
      (trigger.config && JSON.stringify(trigger.config).toLowerCase().includes(searchQuery.toLowerCase()))
    
    const matchesType = selectedType === "all" || trigger.type === selectedType
    
    return matchesSearch && matchesType
  })

  return (
    <div className="container mx-auto p-6 max-w-7xl">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-secondary-900 mb-2">Triggers</h1>
        <p className="text-secondary-600">Manage workflow triggers</p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>All Triggers</CardTitle>
              <CardDescription>View and manage all workflow triggers</CardDescription>
            </div>
            <Button onClick={() => navigate("/triggers/new")}>
              <Plus className="h-4 w-4 mr-2" />
              Create Trigger
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {/* Filters */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
                <Input
                  placeholder="Search triggers..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Select value={selectedWorkflowId} onValueChange={setSelectedWorkflowId}>
                <SelectTrigger>
                  <SelectValue placeholder="All Workflows" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Workflows</SelectItem>
                  {isLoadingWorkflows ? (
                    <SelectItem value="loading" disabled>Loading...</SelectItem>
                  ) : (
                    workflows.map((workflow) => (
                      <SelectItem key={workflow.id} value={workflow.id}>
                        {workflow.name}
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
              <Select value={selectedType} onValueChange={setSelectedType}>
                <SelectTrigger>
                  <SelectValue placeholder="All Types" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Types</SelectItem>
                  <SelectItem value="api">API</SelectItem>
                  <SelectItem value="schedule">Schedule</SelectItem>
                  <SelectItem value="file">File Upload</SelectItem>
                  <SelectItem value="event">Event</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Triggers Table */}
            {isLoadingTriggers ? (
              <div className="space-y-4">
                {[1, 2, 3].map((i) => (
                  <Skeleton key={i} className="h-16 w-full" />
                ))}
              </div>
            ) : filteredTriggers.length === 0 ? (
              <div className="text-center py-12 text-secondary-500">
                <p className="text-sm">No triggers found</p>
                {selectedWorkflowId === "all" && (
                  <Button
                    variant="outline"
                    className="mt-4"
                    onClick={() => navigate("/triggers/new")}
                  >
                    <Plus className="h-4 w-4 mr-2" />
                    Create First Trigger
                  </Button>
                )}
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Type</TableHead>
                    <TableHead>Workflow</TableHead>
                    <TableHead>Configuration</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredTriggers.map((trigger) => (
                    <TableRow key={trigger.id} className="cursor-pointer hover:bg-secondary-50" onClick={() => navigate(`/triggers/${trigger.id}`)}>
                      <TableCell>
                        <Badge variant="outline">
                          {triggerTypeLabels[trigger.type]}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm font-medium">
                          {workflows.find((w) => w.id === trigger.workflow_id)?.name || trigger.workflow_id}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm text-secondary-600">
                          {trigger.type === "api" && (
                            <span>
                              {(trigger.config as { path?: string; method?: string }).method || "POST"}{" "}
                              {(trigger.config as { path?: string }).path || "-"}
                            </span>
                          )}
                          {trigger.type === "schedule" && (
                            <span>
                              {(trigger.config as { cronExpression?: string }).cronExpression || "-"}
                            </span>
                          )}
                          {trigger.type === "file" && (
                            <span>
                              Formats:{" "}
                              {((trigger.config as { formats?: string[] }).formats || []).join(", ")}
                            </span>
                          )}
                          {trigger.type === "event" && (
                            <span>
                              {(trigger.config as { source?: string }).source || "-"}:{" "}
                              {(trigger.config as { topic?: string; queue?: string }).topic ||
                                (trigger.config as { topic?: string; queue?: string }).queue ||
                                "-"}
                            </span>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant={trigger.status === "active" ? "default" : "secondary"}
                        >
                          {trigger.status}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end space-x-2" onClick={(e) => e.stopPropagation()}>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleToggleStatus(trigger)}
                            disabled={
                              activateTrigger.isPending || deactivateTrigger.isPending
                            }
                            title={trigger.status === "active" ? "Deactivate" : "Activate"}
                          >
                            {activateTrigger.isPending || deactivateTrigger.isPending ? (
                              <Loader2 className="h-4 w-4 animate-spin" />
                            ) : trigger.status === "active" ? (
                              <PowerOff className="h-4 w-4" />
                            ) : (
                              <Power className="h-4 w-4" />
                            )}
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => navigate(`/triggers/${trigger.id}/edit`)}
                            title="Edit trigger"
                          >
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDelete(trigger.id)}
                            disabled={deleteTrigger.isPending}
                            title="Delete trigger"
                          >
                            {deleteTrigger.isPending ? (
                              <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                              <Trash2 className="h-4 w-4 text-error-600" />
                            )}
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

