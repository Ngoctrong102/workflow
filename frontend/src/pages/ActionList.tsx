import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Plus, Search, Edit, Eye, Trash2, Loader2 } from "lucide-react"
import { useActionRegistry, useDeleteActionRegistry } from "@/hooks/use-action-registry"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { Skeleton } from "@/components/ui/skeleton"
import type { ActionRegistryItem } from "@/services/action-registry-service"

const actionTypeLabels: Record<string, string> = {
  "api-call": "API Call",
  "publish-event": "Publish Event",
  "function": "Function",
  "custom-action": "Custom Action",
  // Removed all "send-*" action types - these are legacy types
  // Custom actions should be registered in the action registry with custom-action type
}

export default function ActionListPage() {
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedType, setSelectedType] = useState<string>("all")

  const { data: actionRegistry, isLoading } = useActionRegistry()
  const actions = actionRegistry?.actions || []
  const deleteAction = useDeleteActionRegistry()
  const { confirm } = useConfirmDialog()

  const handleDelete = async (id: string, name: string) => {
    const confirmed = await confirm({
      title: "Delete Action Definition",
      description: `Are you sure you want to delete "${name}"? This action cannot be undone and will affect all workflows using this action.`,
      variant: "destructive",
      confirmText: "Delete",
      cancelText: "Cancel",
    })

    if (confirmed) {
      try {
        await deleteAction.mutateAsync(id)
      } catch (error) {
        // Error handling is done in the mutation hook
      }
    }
  }

  const filteredActions = actions.filter((action) => {
    const matchesSearch = searchQuery === "" || 
      action.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (action.description && action.description.toLowerCase().includes(searchQuery.toLowerCase()))
    
    const matchesType = selectedType === "all" || action.type === selectedType
    
    return matchesSearch && matchesType
  })

  const uniqueTypes = Array.from(new Set(actions.map((a) => a.type)))

  return (
    <div className="container mx-auto p-6 max-w-7xl">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-secondary-900 mb-2">Actions</h1>
        <p className="text-secondary-600">Browse and manage available actions</p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Action Registry</CardTitle>
              <CardDescription>View all available actions from the registry</CardDescription>
            </div>
            <Button onClick={() => navigate("/actions/new")} className="cursor-pointer">
              <Plus className="h-4 w-4 mr-2" />
              Create Custom Action
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {/* Filters */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
                <Input
                  placeholder="Search actions..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Select value={selectedType} onValueChange={setSelectedType}>
                <SelectTrigger>
                  <SelectValue placeholder="All Types" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Types</SelectItem>
                  {uniqueTypes.map((type) => (
                    <SelectItem key={type} value={type}>
                      {actionTypeLabels[type] || type}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Actions Table */}
            {isLoading ? (
              <div className="space-y-4">
                {[1, 2, 3, 4, 5].map((i) => (
                  <Skeleton key={i} className="h-16 w-full" />
                ))}
              </div>
            ) : filteredActions.length === 0 ? (
              <div className="text-center py-12 text-secondary-500">
                <p className="text-sm">No actions found</p>
                {searchQuery && (
                  <p className="text-xs mt-2">Try adjusting your search query</p>
                )}
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Description</TableHead>
                    <TableHead>Version</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredActions.map((action) => (
                    <TableRow 
                      key={action.id} 
                      className="cursor-pointer hover:bg-secondary-50 transition-colors"
                      onClick={() => navigate(`/actions/${action.id}`)}
                    >
                      <TableCell>
                        <div className="flex items-center gap-2">
                          {action.metadata?.icon && (
                            <div 
                              className="w-8 h-8 rounded flex items-center justify-center text-white text-sm font-medium"
                              style={{ backgroundColor: action.metadata.color || "#64748b" }}
                            >
                              {action.metadata.icon}
                            </div>
                          )}
                          <div>
                            <div className="font-medium text-sm">{action.name}</div>
                            <div className="text-xs text-secondary-500">{action.id}</div>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline">
                          {actionTypeLabels[action.type] || action.type}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm text-secondary-600 max-w-md truncate">
                          {action.description || "No description"}
                        </div>
                      </TableCell>
                      <TableCell>
                        {action.metadata?.version ? (
                          <Badge variant="secondary">{action.metadata.version}</Badge>
                        ) : (
                          <span className="text-sm text-secondary-400">-</span>
                        )}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end space-x-2" onClick={(e) => e.stopPropagation()}>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => navigate(`/actions/${action.id}`)}
                            title="View action details"
                            className="cursor-pointer"
                          >
                            <Eye className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => navigate(`/actions/${action.id}`)}
                            title="Edit action"
                            className="cursor-pointer"
                          >
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDelete(action.id, action.name)}
                            disabled={deleteAction.isPending}
                            title="Delete action"
                            className="cursor-pointer text-error-600 hover:text-error-700"
                          >
                            {deleteAction.isPending ? (
                              <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                              <Trash2 className="h-4 w-4" />
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

