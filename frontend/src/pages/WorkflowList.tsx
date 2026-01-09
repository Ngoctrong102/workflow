import { useState, useMemo, useCallback } from "react"
import { useNavigate } from "react-router-dom"
import { useTranslation } from "react-i18next"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { Plus, Search, Edit, Trash2, Eye, Loader2, BarChart3, Download, Upload, Activity } from "lucide-react"
import { useWorkflows, useDeleteWorkflow } from "@/hooks/use-workflows"
import { useExportWorkflow, useImportWorkflow } from "@/hooks/use-workflow-export-import"
import { useBulkDeleteWorkflows, useBulkUpdateWorkflowStatus } from "@/hooks/use-bulk-operations"
import { useBulkExportWorkflows } from "@/hooks/use-workflow-bulk-export"
import { Skeleton } from "@/components/ui/skeleton"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { useDebounce } from "@/hooks/use-debounce"
import { BulkActions } from "@/components/common/BulkActions"
import { ImportDialog } from "@/components/common/ImportDialog"
import { validateWorkflowImport } from "@/utils/import"
import type { WorkflowDefinition } from "@/types/workflow"

export default function WorkflowList() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [tagsFilter, setTagsFilter] = useState<string>("")
  const [selectedWorkflows, setSelectedWorkflows] = useState<Set<string>>(new Set())
  const [importDialogOpen, setImportDialogOpen] = useState(false)
  
  // Debounce search query to reduce API calls
  const debouncedSearchQuery = useDebounce(searchQuery, 300)
  const debouncedTagsFilter = useDebounce(tagsFilter, 300)

  // Memoize query params
  const queryParams = useMemo(
    () => ({
      search: debouncedSearchQuery || undefined,
      status: statusFilter !== "all" ? statusFilter : undefined,
      tags: debouncedTagsFilter ? debouncedTagsFilter.split(",").map((t) => t.trim()).filter(Boolean).join(",") : undefined,
      limit: 50,
      offset: 0,
    }),
    [debouncedSearchQuery, statusFilter, debouncedTagsFilter]
  )

  const { data, isLoading, error } = useWorkflows(queryParams)

  const deleteWorkflow = useDeleteWorkflow()
  const exportWorkflow = useExportWorkflow()
  const importWorkflow = useImportWorkflow()
  const bulkDeleteWorkflows = useBulkDeleteWorkflows()
  const bulkUpdateStatus = useBulkUpdateWorkflowStatus()
  const bulkExportWorkflows = useBulkExportWorkflows()

  const workflows = useMemo(() => data?.data || [], [data?.data])
  const total = useMemo(() => data?.total || 0, [data?.total])

  const { confirm } = useConfirmDialog()

  const handleDelete = useCallback(
    async (id: string) => {
      const confirmed = await confirm({
        title: "Delete Workflow",
        description: "Are you sure you want to delete this workflow? This action cannot be undone.",
        variant: "destructive",
        confirmText: "Delete",
        cancelText: "Cancel",
      })

      if (confirmed) {
        await deleteWorkflow.mutateAsync(id)
      }
    },
    [confirm, deleteWorkflow]
  )

  const handleNavigate = useCallback(
    (path: string) => {
      navigate(path)
    },
    [navigate]
  )

  const handleSelectAll = useCallback(() => {
    if (selectedWorkflows.size === workflows.length) {
      setSelectedWorkflows(new Set())
    } else {
      setSelectedWorkflows(new Set(workflows.map((w: { id: string }) => w.id)))
    }
  }, [selectedWorkflows.size, workflows])

  const handleSelectWorkflow = useCallback(
    (id: string) => {
      const newSelected = new Set(selectedWorkflows)
      if (newSelected.has(id)) {
        newSelected.delete(id)
      } else {
        newSelected.add(id)
      }
      setSelectedWorkflows(newSelected)
    },
    [selectedWorkflows]
  )

  const handleExport = useCallback(
    async (id: string) => {
      await exportWorkflow.mutateAsync(id)
    },
    [exportWorkflow]
  )

  const handleImport = useCallback(
    async (workflow: WorkflowDefinition, options?: { overwrite?: boolean; skipConflicts?: boolean }) => {
      await importWorkflow.mutateAsync({ workflow, options })
    },
    [importWorkflow]
  )

  const handleBulkDelete = useCallback(
    async (items: Array<{ id: string }>) => {
      const ids = items.map((item) => item.id)
      await bulkDeleteWorkflows.mutateAsync(ids)
      setSelectedWorkflows(new Set())
    },
    [bulkDeleteWorkflows]
  )

  const handleBulkUpdateStatus = useCallback(
    async (items: Array<{ id: string }>, status: string) => {
      const ids = items.map((item) => item.id)
      await bulkUpdateStatus.mutateAsync({
        ids,
        status: status as "draft" | "active" | "inactive" | "paused" | "archived",
      })
      setSelectedWorkflows(new Set())
    },
    [bulkUpdateStatus]
  )

  const handleBulkExport = useCallback(
    async (format: "csv" | "json" = "json") => {
      const ids = Array.from(selectedWorkflows)
      if (ids.length === 0) return
      await bulkExportWorkflows.mutateAsync({ ids, format })
      setSelectedWorkflows(new Set())
    },
    [bulkExportWorkflows, selectedWorkflows]
  )

  const selectedWorkflowItems = useMemo(
    () => workflows.filter((w: { id: string }) => selectedWorkflows.has(w.id)),
    [workflows, selectedWorkflows]
  )

  return (
    <div className="space-y-3">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Workflows</h1>
          <p className="text-secondary-600 mt-1 text-sm">
            Manage your notification workflows
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setImportDialogOpen(true)}
            disabled={importWorkflow.isPending}
          >
            <Upload className="h-3.5 w-3.5 mr-1.5" />
            Import
          </Button>
          <Button variant="outline" size="sm" onClick={() => handleNavigate("/workflows/wizard")}>
            <Plus className="h-3.5 w-3.5 mr-1.5" />
            Guided Creation
          </Button>
          <Button size="sm" onClick={() => handleNavigate("/workflows/new")}>
            <Plus className="h-3.5 w-3.5 mr-1.5" />
            Create Workflow
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-3">
          <div className="flex items-center space-x-3">
            <div className="flex-1 relative">
              <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 h-3.5 w-3.5 text-secondary-400" />
              <Input
                placeholder="Search workflows..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-8 h-9 text-sm"
              />
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-44 h-9 text-sm">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="draft">Draft</SelectItem>
                <SelectItem value="active">Active</SelectItem>
                <SelectItem value="inactive">Inactive</SelectItem>
                <SelectItem value="paused">Paused</SelectItem>
                <SelectItem value="archived">Archived</SelectItem>
              </SelectContent>
            </Select>
            <Input
              placeholder="Filter by tags (comma-separated)"
              value={tagsFilter}
              onChange={(e) => setTagsFilter(e.target.value)}
              className="w-64 h-9 text-sm"
            />
          </div>
        </CardContent>
      </Card>

      {/* Bulk Actions */}
      {selectedWorkflows.size > 0 && (
        <BulkActions
          selectedItems={selectedWorkflowItems}
          onBulkDelete={handleBulkDelete}
          onBulkUpdateStatus={handleBulkUpdateStatus}
          onBulkExport={handleBulkExport}
          getItemId={(item) => item.id}
          getItemName={(item) => item.name}
        />
      )}

      {/* Workflow List */}
      <Card>
        <CardHeader className="px-4 py-3">
                  <CardTitle className="text-base" role="heading" aria-level={2}>
                    {t("workflow.title")} {total > 0 && `(${total})`}
                  </CardTitle>
                  <CardDescription className="text-xs">All your notification workflows</CardDescription>
        </CardHeader>
        <CardContent className="px-4 pb-3">
          {isLoading ? (
            <div className="space-y-2">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex items-center space-x-4">
                  <Skeleton className="h-10 w-full" />
                </div>
              ))}
            </div>
          ) : error ? (
            <div className="text-center py-8 text-error-600">
              <p className="text-sm">Failed to load workflows</p>
              <p className="text-xs mt-1">{error instanceof Error ? error.message : "Unknown error"}</p>
            </div>
          ) : workflows.length === 0 ? (
            <div className="text-center py-8 text-secondary-500">
              <p className="text-sm">No workflows found</p>
              <Button
                variant="outline"
                size="sm"
                className="mt-3"
                onClick={() => handleNavigate("/workflows/new")}
              >
                <Plus className="h-3.5 w-3.5 mr-1.5" />
                Create Workflow
              </Button>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="h-10 px-3 text-xs w-12">
                    <Checkbox
                      checked={selectedWorkflows.size === workflows.length && workflows.length > 0}
                      onCheckedChange={handleSelectAll}
                    />
                  </TableHead>
                  <TableHead className="h-10 px-3 text-xs">Name</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Status</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Version</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Updated</TableHead>
                  <TableHead className="h-10 px-3 text-right text-xs">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {workflows.map((workflow: { id: string; name: string; description?: string; status: string; version: number; updatedAt: string }) => (
                  <TableRow key={workflow.id} className="hover:bg-secondary-50/50">
                    <TableCell className="px-3 py-2">
                      <Checkbox
                        checked={selectedWorkflows.has(workflow.id)}
                        onCheckedChange={() => handleSelectWorkflow(workflow.id)}
                      />
                    </TableCell>
                    <TableCell className="px-3 py-2">
                      <div>
                        <div className="font-medium text-sm">{workflow.name}</div>
                        {workflow.description && (
                          <div className="text-xs text-secondary-500 mt-0.5">
                            {workflow.description}
                          </div>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="px-3 py-2">
                      <Badge
                        variant={
                          workflow.status === "active"
                            ? "default"
                            : workflow.status === "draft"
                            ? "outline"
                            : "secondary"
                        }
                        className="text-xs"
                      >
                        {workflow.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="px-3 py-2">
                      <span className="text-xs text-secondary-500">
                        v{workflow.version}
                      </span>
                    </TableCell>
                    <TableCell className="px-3 py-2 text-xs text-secondary-500">
                      {new Date(workflow.updatedAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell className="px-3 py-2 text-right">
                      <div className="flex items-center justify-end space-x-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => handleNavigate(`/workflows/${workflow.id}/dashboard`)}
                          title="View dashboard"
                        >
                          <Activity className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => handleExport(workflow.id)}
                          disabled={exportWorkflow.isPending}
                          title="Export workflow"
                        >
                          {exportWorkflow.isPending ? (
                            <Loader2 className="h-3.5 w-3.5 animate-spin" />
                          ) : (
                            <Download className="h-3.5 w-3.5" />
                          )}
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => handleNavigate(`/workflows/${workflow.id}`)}
                          title="View workflow"
                        >
                          <Eye className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => handleNavigate(`/workflows/${workflow.id}`)}
                          title="Edit workflow"
                        >
                          <Edit className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => handleDelete(workflow.id)}
                          disabled={deleteWorkflow.isPending}
                          title="Delete workflow"
                        >
                          {deleteWorkflow.isPending ? (
                            <Loader2 className="h-3.5 w-3.5 animate-spin" />
                          ) : (
                            <Trash2 className="h-3.5 w-3.5 text-error-600" />
                          )}
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Import Dialog */}
      <ImportDialog<WorkflowDefinition>
        open={importDialogOpen}
        onOpenChange={setImportDialogOpen}
        onImport={handleImport}
        validator={validateWorkflowImport}
        existingItems={workflows}
        getItemId={(item) => (item as { id?: string }).id || ""}
        getItemName={(item) => (item as { name: string }).name || ""}
        title="Import Workflow"
        description="Upload a JSON file to import a workflow"
      />
    </div>
  )
}
