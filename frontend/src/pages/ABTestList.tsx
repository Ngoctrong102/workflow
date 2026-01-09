import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Plus,
  Search,
  Eye,
  Edit,
  Trash2,
  Play,
  Pause,
  Square,
  RefreshCw,
  CheckCircle2,
  Clock,
  XCircle,
  Archive,
  Loader2,
} from "lucide-react"
import { useABTests, useDeleteABTest, useStartABTest, usePauseABTest, useStopABTest } from "@/hooks/use-ab-tests"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { formatDistanceToNow } from "date-fns"
import type { ABTestStatus } from "@/types/ab-test"

const statusConfig: Record<ABTestStatus, { label: string; color: string; icon: typeof CheckCircle2 }> = {
  draft: {
    label: "Draft",
    color: "bg-secondary-600",
    icon: Edit,
  },
  running: {
    label: "Running",
    color: "bg-primary-600",
    icon: Play,
  },
  paused: {
    label: "Paused",
    color: "bg-warning-600",
    icon: Pause,
  },
  completed: {
    label: "Completed",
    color: "bg-success-600",
    icon: CheckCircle2,
  },
  archived: {
    label: "Archived",
    color: "bg-secondary-400",
    icon: Archive,
  },
}

export default function ABTestList() {
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<ABTestStatus | "all">("all")
  const [page, setPage] = useState(0)
  const limit = 20

  const { data, isLoading, error, refetch } = useABTests({
    status: statusFilter !== "all" ? statusFilter : undefined,
    search: searchQuery || undefined,
    limit,
    offset: page * limit,
  })

  const deleteTest = useDeleteABTest()
  const startTest = useStartABTest()
  const pauseTest = usePauseABTest()
  const stopTest = useStopABTest()
  const { confirm } = useConfirmDialog()

  const tests = data?.tests || []
  const total = data?.total || 0
  const totalPages = Math.ceil(total / limit)

  const handleDelete = async (id: string) => {
    const confirmed = await confirm({
      title: "Delete A/B Test",
      description: "Are you sure you want to delete this A/B test? This action cannot be undone.",
      variant: "destructive",
      confirmText: "Delete",
    })

    if (confirmed) {
      await deleteTest.mutateAsync(id)
    }
  }

  const handleStart = async (id: string) => {
    await startTest.mutateAsync(id)
  }

  const handlePause = async (id: string) => {
    await pauseTest.mutateAsync(id)
  }

  const handleStop = async (id: string) => {
    const confirmed = await confirm({
      title: "Stop A/B Test",
      description: "Are you sure you want to stop this test? Results will be finalized.",
      confirmText: "Stop",
    })

    if (confirmed) {
      await stopTest.mutateAsync(id)
    }
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">A/B Tests</h1>
          <p className="text-secondary-600 mt-2">Create and manage A/B tests for notifications</p>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="outline" onClick={() => refetch()}>
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
          <Button onClick={() => navigate("/ab-tests/new")}>
            <Plus className="h-4 w-4 mr-2" />
            New A/B Test
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex items-center space-x-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
              <Input
                placeholder="Search A/B tests..."
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value)
                  setPage(0)
                }}
                className="pl-9"
              />
            </div>
            <Select
              value={statusFilter}
              onValueChange={(v) => {
                setStatusFilter(v as ABTestStatus | "all")
                setPage(0)
              }}
            >
              <SelectTrigger className="w-48">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="draft">Draft</SelectItem>
                <SelectItem value="running">Running</SelectItem>
                <SelectItem value="paused">Paused</SelectItem>
                <SelectItem value="completed">Completed</SelectItem>
                <SelectItem value="archived">Archived</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Test List */}
      <Card>
        <CardHeader>
          <CardTitle>A/B Tests {total > 0 && `(${total})`}</CardTitle>
          <CardDescription>All A/B tests</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex items-center space-x-4">
                  <Skeleton className="h-12 w-full" />
                </div>
              ))}
            </div>
          ) : error ? (
            <div className="text-center py-12 text-error-600">
              <p>Failed to load A/B tests</p>
              <p className="text-sm mt-2">{error instanceof Error ? error.message : "Unknown error"}</p>
            </div>
          ) : tests.length === 0 ? (
            <div className="text-center py-12 text-secondary-500">
              <p>No A/B tests found</p>
              <Button
                variant="outline"
                className="mt-4"
                onClick={() => navigate("/ab-tests/new")}
              >
                <Plus className="h-4 w-4 mr-2" />
                Create A/B Test
              </Button>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Workflow</TableHead>
                  <TableHead>Variants</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Success Metric</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {tests.map((test) => {
                  const status = statusConfig[test.status]
                  const StatusIcon = status.icon

                  return (
                    <TableRow key={test.id}>
                      <TableCell>
                        <div>
                          <div className="font-medium">{test.name}</div>
                          {test.description && (
                            <div className="text-sm text-secondary-500">{test.description}</div>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm">{test.workflow_name || test.workflow_id}</div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center space-x-1">
                          {test.variants.map((variant) => (
                            <Badge key={variant.id} variant="outline" className="text-xs">
                              {variant.label} ({variant.traffic_percentage}%)
                            </Badge>
                          ))}
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="default" className={status.color}>
                          <StatusIcon className="h-3 w-3 mr-1" />
                          {status.label}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm capitalize">
                          {test.success_metric.replace("_", " ")}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm text-secondary-500">
                          {test.created_at
                            ? formatDistanceToNow(new Date(test.created_at), { addSuffix: true })
                            : "-"}
                        </div>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end space-x-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => navigate(`/ab-tests/${test.id}`)}
                          >
                            <Eye className="h-4 w-4" />
                          </Button>
                          {test.status === "draft" && (
                            <>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => navigate(`/ab-tests/${test.id}`)}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleStart(test.id)}
                                disabled={startTest.isPending}
                              >
                                {startTest.isPending ? (
                                  <Loader2 className="h-4 w-4 animate-spin" />
                                ) : (
                                  <Play className="h-4 w-4 text-success-600" />
                                )}
                              </Button>
                            </>
                          )}
                          {test.status === "running" && (
                            <>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handlePause(test.id)}
                                disabled={pauseTest.isPending}
                              >
                                {pauseTest.isPending ? (
                                  <Loader2 className="h-4 w-4 animate-spin" />
                                ) : (
                                  <Pause className="h-4 w-4 text-warning-600" />
                                )}
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleStop(test.id)}
                                disabled={stopTest.isPending}
                              >
                                {stopTest.isPending ? (
                                  <Loader2 className="h-4 w-4 animate-spin" />
                                ) : (
                                  <Square className="h-4 w-4 text-error-600" />
                                )}
                              </Button>
                            </>
                          )}
                          {test.status === "paused" && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleStart(test.id)}
                              disabled={startTest.isPending}
                            >
                              {startTest.isPending ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                              ) : (
                                <Play className="h-4 w-4 text-success-600" />
                              )}
                            </Button>
                          )}
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDelete(test.id)}
                            disabled={deleteTest.isPending}
                          >
                            {deleteTest.isPending ? (
                              <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                              <Trash2 className="h-4 w-4 text-error-600" />
                            )}
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

