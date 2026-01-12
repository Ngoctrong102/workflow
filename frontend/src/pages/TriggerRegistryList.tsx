import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Plus, Search, Edit, Trash2, Eye, Loader2 } from "lucide-react"
import { useTriggerRegistry, useDeleteTriggerRegistry } from "@/hooks/use-trigger-registry"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/hooks/use-toast"

const triggerTypeLabels: Record<string, string> = {
  "api-call": "API Call",
  "scheduler": "Scheduler",
  "event": "Event",
}

export default function TriggerRegistryListPage() {
  const navigate = useNavigate()
  const { toast } = useToast()
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedType, setSelectedType] = useState<string>("all")

  const { data: triggerRegistry, isLoading } = useTriggerRegistry()
  const triggers = triggerRegistry?.triggers || []
  const deleteTrigger = useDeleteTriggerRegistry()
  const { confirm } = useConfirmDialog()

  const handleDelete = async (id: string, name: string) => {
    const confirmed = await confirm({
      title: "Delete Trigger Definition",
      description: `Are you sure you want to delete "${name}"? This action cannot be undone and will affect all workflows using this trigger.`,
      variant: "destructive",
      confirmText: "Delete",
      cancelText: "Cancel",
    })

    if (confirmed) {
      try {
        await deleteTrigger.mutateAsync(id)
      } catch (error) {
        // Error handling is done in the mutation hook
      }
    }
  }

  const filteredTriggers = triggers.filter((trigger) => {
    const matchesSearch = searchQuery === "" || 
      trigger.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (trigger.description && trigger.description.toLowerCase().includes(searchQuery.toLowerCase())) ||
      trigger.id.toLowerCase().includes(searchQuery.toLowerCase())
    
    const matchesType = selectedType === "all" || trigger.type === selectedType
    
    return matchesSearch && matchesType
  })

  const uniqueTypes = Array.from(new Set(triggers.map((t) => t.type)))

  return (
    <div className="container mx-auto p-6 max-w-7xl">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-secondary-900 mb-2">Trigger Registry</h1>
        <p className="text-secondary-600">Manage trigger definitions that can be used in workflows</p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Trigger Definitions</CardTitle>
              <CardDescription>View and manage all trigger definitions from the registry</CardDescription>
            </div>
            <Button onClick={() => navigate("/trigger-registry/new")} className="cursor-pointer">
              <Plus className="h-4 w-4 mr-2" />
              Create Trigger Definition
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
                  placeholder="Search triggers..."
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
                      {triggerTypeLabels[type] || type}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Triggers Table */}
            {isLoading ? (
              <div className="space-y-4">
                {[1, 2, 3, 4, 5].map((i) => (
                  <Skeleton key={i} className="h-16 w-full" />
                ))}
              </div>
            ) : filteredTriggers.length === 0 ? (
              <div className="text-center py-12 text-secondary-500">
                <p className="text-sm">No trigger definitions found</p>
                {searchQuery && (
                  <p className="text-xs mt-2">Try adjusting your search query</p>
                )}
                {!searchQuery && (
                  <Button
                    variant="outline"
                    className="mt-4 cursor-pointer"
                    onClick={() => navigate("/trigger-registry/new")}
                  >
                    <Plus className="h-4 w-4 mr-2" />
                    Create First Trigger Definition
                  </Button>
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
                  {filteredTriggers.map((trigger) => (
                    <TableRow 
                      key={trigger.id} 
                      className="cursor-pointer hover:bg-secondary-50 transition-colors"
                      onClick={() => navigate(`/trigger-registry/${trigger.id}`)}
                    >
                      <TableCell>
                        <div className="flex items-center gap-2">
                          {trigger.metadata?.icon && (
                            <div 
                              className="w-8 h-8 rounded flex items-center justify-center text-white text-sm font-medium"
                              style={{ backgroundColor: trigger.metadata.color || "#0ea5e9" }}
                            >
                              {trigger.metadata.icon}
                            </div>
                          )}
                          <div>
                            <div className="font-medium text-sm">{trigger.name}</div>
                            <div className="text-xs text-secondary-500 font-mono">{trigger.id}</div>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline">
                          {triggerTypeLabels[trigger.type] || trigger.type}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm text-secondary-600 max-w-md truncate">
                          {trigger.description || "No description"}
                        </div>
                      </TableCell>
                      <TableCell>
                        {trigger.metadata?.version ? (
                          <Badge variant="secondary">{trigger.metadata.version}</Badge>
                        ) : (
                          <span className="text-sm text-secondary-400">-</span>
                        )}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end space-x-2" onClick={(e) => e.stopPropagation()}>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => navigate(`/trigger-registry/${trigger.id}`)}
                            title="View trigger details"
                            className="cursor-pointer"
                          >
                            <Eye className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => navigate(`/trigger-registry/${trigger.id}`)}
                            title="Edit trigger"
                            className="cursor-pointer"
                          >
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDelete(trigger.id, trigger.name)}
                            disabled={deleteTrigger.isPending}
                            title="Delete trigger"
                            className="cursor-pointer text-error-600 hover:text-error-700"
                          >
                            {deleteTrigger.isPending ? (
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

