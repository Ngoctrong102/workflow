import { useState, useMemo, useCallback } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Skeleton } from "@/components/ui/skeleton"
import { Plus, Search, Edit, Trash2, Eye, Loader2 } from "lucide-react"
import { useObjectTypes, useDeleteObjectType } from "@/hooks/use-object-types"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { useDebounce } from "@/hooks/use-debounce"

export default function ObjectTypeList() {
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState("")
  const [tagFilter, setTagFilter] = useState<string>("all")

  // Debounce search query
  const debouncedSearchQuery = useDebounce(searchQuery, 300)

  // Memoize query params
  const queryParams = useMemo(
    () => ({
      search: debouncedSearchQuery || undefined,
      tags: tagFilter !== "all" ? [tagFilter] : undefined,
      limit: 50,
      offset: 0,
    }),
    [debouncedSearchQuery, tagFilter]
  )

  const { data, isLoading, error } = useObjectTypes(queryParams)
  const deleteObjectType = useDeleteObjectType()

  const objectTypes = useMemo(() => data?.data || [], [data?.data])
  const total = useMemo(() => data?.total || 0, [data?.total])

  // Get all unique tags
  const allTags = useMemo(() => {
    const tags = new Set<string>()
    objectTypes.forEach((type) => {
      type.tags?.forEach((tag) => tags.add(tag))
    })
    return Array.from(tags).sort()
  }, [objectTypes])

  const { confirm } = useConfirmDialog()

  const handleDelete = useCallback(
    async (id: string) => {
      const confirmed = await confirm({
        title: "Delete Object Type",
        description: "Are you sure you want to delete this object type? This action cannot be undone.",
        variant: "destructive",
        confirmText: "Delete",
        cancelText: "Cancel",
      })

      if (confirmed) {
        await deleteObjectType.mutateAsync(id)
      }
    },
    [confirm, deleteObjectType]
  )

  if (error) {
    return (
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Object Types</h1>
            <p className="text-secondary-600 mt-1 text-sm">
              Manage your object type definitions
            </p>
          </div>
        </div>
        <Card>
          <CardContent className="p-6">
            <p className="text-error-600">Failed to load object types: {error.message}</p>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Object Types</h1>
          <p className="text-secondary-600 mt-1 text-sm">
            Manage your object type definitions
          </p>
        </div>
        <Button size="sm" onClick={() => navigate("/object-types/new")}>
          <Plus className="h-3.5 w-3.5 mr-1.5" />
          Create Object Type
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-3">
          <div className="flex items-center space-x-3">
            <div className="flex-1 relative">
              <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 h-3.5 w-3.5 text-secondary-400" />
              <Input
                placeholder="Search object types..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-8 h-9 text-sm"
              />
            </div>
            {allTags.length > 0 && (
              <div className="flex items-center space-x-2">
                <span className="text-sm text-secondary-600">Tag:</span>
                <select
                  value={tagFilter}
                  onChange={(e) => setTagFilter(e.target.value)}
                  className="h-9 px-3 rounded-md border border-secondary-300 text-sm"
                >
                  <option value="all">All Tags</option>
                  {allTags.map((tag) => (
                    <option key={tag} value={tag}>
                      {tag}
                    </option>
                  ))}
                </select>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Object Types Table */}
      <Card>
        <CardHeader>
          <CardTitle>Object Types ({total})</CardTitle>
          <CardDescription>List of all defined object types</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-2">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : objectTypes.length === 0 ? (
            <div className="text-center py-8 text-secondary-500">
              <p className="text-sm">No object types found</p>
              <p className="text-xs mt-1">
                {searchQuery || tagFilter !== "all"
                  ? "Try adjusting your search or filters"
                  : "Create your first object type to get started"}
              </p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Display Name</TableHead>
                  <TableHead>Fields</TableHead>
                  <TableHead>Tags</TableHead>
                  <TableHead>Version</TableHead>
                  <TableHead>Updated</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {objectTypes.map((type) => (
                  <TableRow key={type.id}>
                    <TableCell className="font-medium">{type.name}</TableCell>
                    <TableCell>{type.displayName || "-"}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{type.fields?.length || 0} fields</Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {type.tags && type.tags.length > 0 ? (
                          type.tags.map((tag) => (
                            <Badge key={tag} variant="secondary" className="text-xs">
                              {tag}
                            </Badge>
                          ))
                        ) : (
                          <span className="text-secondary-500 text-xs">-</span>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline">v{type.version}</Badge>
                    </TableCell>
                    <TableCell className="text-sm text-secondary-500">
                      {type.updatedAt
                        ? new Date(type.updatedAt).toLocaleDateString()
                        : "-"}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end space-x-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => navigate(`/object-types/${type.id}`)}
                          className="h-8 w-8 p-0"
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => navigate(`/object-types/${type.id}?edit=true`)}
                          className="h-8 w-8 p-0"
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(type.id)}
                          disabled={deleteObjectType.isPending}
                          className="h-8 w-8 p-0 text-error-600 hover:text-error-700"
                        >
                          {deleteObjectType.isPending ? (
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
        </CardContent>
      </Card>
    </div>
  )
}

