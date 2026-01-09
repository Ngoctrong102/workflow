import { useState, useMemo, useCallback } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { Skeleton } from "@/components/ui/skeleton"
import { Plus, Search, Edit, Trash2, Eye, Loader2, Download, Upload } from "lucide-react"
import { useTemplates, useDeleteTemplate } from "@/hooks/use-templates"
import { useExportTemplate, useImportTemplate } from "@/hooks/use-template-export-import"
import { useBulkDeleteTemplates } from "@/hooks/use-bulk-operations"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { useDebounce } from "@/hooks/use-debounce"
import { BulkActions } from "@/components/common/BulkActions"
import { ImportDialog } from "@/components/common/ImportDialog"
import { validateTemplateImport } from "@/utils/import"
import type { TemplateChannel } from "@/types/template"
import type { CreateTemplateRequest } from "@/services/template-service"

const channelLabels: Record<TemplateChannel, string> = {
  email: "Email",
  sms: "SMS",
  push: "Push",
  "in-app": "In-App",
  slack: "Slack",
  discord: "Discord",
  teams: "Teams",
  webhook: "Webhook",
}

export default function TemplateList() {
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState("")
  const [channelFilter, setChannelFilter] = useState<TemplateChannel | "all">("all")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [selectedTemplates, setSelectedTemplates] = useState<Set<string>>(new Set())
  const [importDialogOpen, setImportDialogOpen] = useState(false)

  // Debounce search query
  const debouncedSearchQuery = useDebounce(searchQuery, 300)

  // Memoize query params
  const queryParams = useMemo(
    () => ({
      channel: channelFilter !== "all" ? channelFilter : undefined,
      status: statusFilter !== "all" ? statusFilter : undefined,
      search: debouncedSearchQuery || undefined,
      limit: 50,
      offset: 0,
    }),
    [channelFilter, statusFilter, debouncedSearchQuery]
  )

  const { data, isLoading, error } = useTemplates(queryParams)

  const deleteTemplate = useDeleteTemplate()
  const exportTemplate = useExportTemplate()
  const importTemplate = useImportTemplate()
  const bulkDeleteTemplates = useBulkDeleteTemplates()

  const templates = useMemo(() => data?.data || [], [data?.data])
  const total = useMemo(() => data?.total || 0, [data?.total])

  const { confirm } = useConfirmDialog()

  const handleDelete = useCallback(
    async (id: string) => {
      const confirmed = await confirm({
        title: "Delete Template",
        description: "Are you sure you want to delete this template? This action cannot be undone.",
        variant: "destructive",
        confirmText: "Delete",
        cancelText: "Cancel",
      })

      if (confirmed) {
        await deleteTemplate.mutateAsync(id)
      }
    },
    [confirm, deleteTemplate]
  )

  const handleSelectAll = useCallback(() => {
    if (selectedTemplates.size === templates.length) {
      setSelectedTemplates(new Set())
    } else {
      setSelectedTemplates(new Set(templates.map((t: { id: string }) => t.id)))
    }
  }, [selectedTemplates.size, templates])

  const handleSelectTemplate = useCallback(
    (id: string) => {
      const newSelected = new Set(selectedTemplates)
      if (newSelected.has(id)) {
        newSelected.delete(id)
      } else {
        newSelected.add(id)
      }
      setSelectedTemplates(newSelected)
    },
    [selectedTemplates]
  )

  const handleExport = useCallback(
    async (id: string) => {
      await exportTemplate.mutateAsync(id)
    },
    [exportTemplate]
  )

  const handleImport = useCallback(
    async (template: CreateTemplateRequest, options?: { overwrite?: boolean; skipConflicts?: boolean }) => {
      await importTemplate.mutateAsync({ template, options })
    },
    [importTemplate]
  )

  const handleBulkDelete = useCallback(
    async (items: Array<{ id: string }>) => {
      const ids = items.map((item) => item.id)
      await bulkDeleteTemplates.mutateAsync(ids)
      setSelectedTemplates(new Set())
    },
    [bulkDeleteTemplates]
  )

  const selectedTemplateItems = useMemo(
    () => templates.filter((t: { id: string }) => selectedTemplates.has(t.id)),
    [templates, selectedTemplates]
  )

  return (
    <div className="space-y-3">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Templates</h1>
          <p className="text-secondary-600 mt-1 text-sm">
            Manage your notification templates
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setImportDialogOpen(true)}
            disabled={importTemplate.isPending}
          >
            <Upload className="h-3.5 w-3.5 mr-1.5" />
            Import
          </Button>
          <Button size="sm" onClick={() => navigate("/templates/new")}>
            <Plus className="h-3.5 w-3.5 mr-1.5" />
            Create Template
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
                placeholder="Search templates..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-8 h-9 text-sm"
              />
            </div>
            <Select value={channelFilter} onValueChange={(v) => setChannelFilter(v as TemplateChannel | "all")}>
              <SelectTrigger className="w-44 h-9 text-sm">
                <SelectValue placeholder="Filter by channel" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Channels</SelectItem>
                {Object.entries(channelLabels).map(([value, label]) => (
                  <SelectItem key={value} value={value}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-44 h-9 text-sm">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="active">Active</SelectItem>
                <SelectItem value="inactive">Inactive</SelectItem>
                <SelectItem value="draft">Draft</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Bulk Actions */}
      {selectedTemplates.size > 0 && (
        <BulkActions
          selectedItems={selectedTemplateItems}
          onBulkDelete={handleBulkDelete}
          getItemId={(item) => item.id}
          getItemName={(item) => item.name}
        />
      )}

      {/* Template List */}
      <Card>
        <CardHeader className="px-4 py-3">
          <CardTitle className="text-base">Templates {total > 0 && `(${total})`}</CardTitle>
          <CardDescription className="text-xs">All your notification templates</CardDescription>
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
              <p className="text-sm">Failed to load templates</p>
              <p className="text-xs mt-1">{error instanceof Error ? error.message : "Unknown error"}</p>
            </div>
          ) : templates.length === 0 ? (
            <div className="text-center py-8 text-secondary-500">
              <p className="text-sm">No templates found</p>
              <Button
                variant="outline"
                size="sm"
                className="mt-3"
                onClick={() => navigate("/templates/new")}
              >
                <Plus className="h-3.5 w-3.5 mr-1.5" />
                Create Template
              </Button>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="h-10 px-3 text-xs w-12">
                    <Checkbox
                      checked={selectedTemplates.size === templates.length && templates.length > 0}
                      onCheckedChange={handleSelectAll}
                    />
                  </TableHead>
                  <TableHead className="h-10 px-3 text-xs">Name</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Channel</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Variables</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Status</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Updated</TableHead>
                  <TableHead className="h-10 px-3 text-right text-xs">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {(templates as Array<{ id: string; name: string; description?: string; channel: TemplateChannel; variables?: string[]; status?: string; updatedAt?: string }>).map((template) => (
                  <TableRow key={template.id} className="hover:bg-secondary-50/50">
                    <TableCell className="px-3 py-2">
                      <Checkbox
                        checked={selectedTemplates.has(template.id)}
                        onCheckedChange={() => handleSelectTemplate(template.id)}
                      />
                    </TableCell>
                    <TableCell className="px-3 py-2">
                      <div>
                        <div className="font-medium text-sm">{template.name}</div>
                        {template.description && (
                          <div className="text-xs text-secondary-500 mt-0.5">
                            {template.description}
                          </div>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="px-3 py-2">
                      <Badge variant="outline" className="text-xs">
                        {channelLabels[template.channel]}
                      </Badge>
                    </TableCell>
                    <TableCell className="px-3 py-2">
                      <div className="flex flex-wrap gap-1">
                        {template.variables?.slice(0, 2).map((variable) => (
                          <Badge key={variable} variant="secondary" className="text-xs px-1.5 py-0">
                            {variable}
                          </Badge>
                        ))}
                        {template.variables && template.variables.length > 2 && (
                          <Badge variant="secondary" className="text-xs px-1.5 py-0">
                            +{template.variables.length - 2}
                          </Badge>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="px-3 py-2">
                      <Badge
                        variant={
                          template.status === "active"
                            ? "default"
                            : template.status === "inactive"
                            ? "secondary"
                            : "outline"
                        }
                        className="text-xs"
                      >
                        {template.status || "draft"}
                      </Badge>
                    </TableCell>
                    <TableCell className="px-3 py-2 text-xs text-secondary-500">
                      {template.updatedAt
                        ? new Date(template.updatedAt).toLocaleDateString()
                        : "-"}
                    </TableCell>
                    <TableCell className="px-3 py-2 text-right">
                      <div className="flex items-center justify-end space-x-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => handleExport(template.id)}
                          disabled={exportTemplate.isPending}
                          title="Export template"
                        >
                          {exportTemplate.isPending ? (
                            <Loader2 className="h-3.5 w-3.5 animate-spin" />
                          ) : (
                            <Download className="h-3.5 w-3.5" />
                          )}
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => navigate(`/templates/${template.id}`)}
                          title="View template"
                        >
                          <Eye className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => navigate(`/templates/${template.id}`)}
                          title="Edit template"
                        >
                          <Edit className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => handleDelete(template.id)}
                          disabled={deleteTemplate.isPending}
                          title="Delete template"
                        >
                          {deleteTemplate.isPending ? (
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
      <ImportDialog<CreateTemplateRequest>
        open={importDialogOpen}
        onOpenChange={setImportDialogOpen}
        onImport={handleImport}
        validator={validateTemplateImport}
        existingItems={templates}
        getItemId={(item) => (item as { id?: string }).id || ""}
        getItemName={(item) => (item as { name: string }).name || ""}
        title="Import Template"
        description="Upload a JSON file to import a template"
      />
    </div>
  )
}
