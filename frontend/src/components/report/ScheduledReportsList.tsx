import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Skeleton } from "@/components/ui/skeleton"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Edit, Trash2, MoreVertical, Play, Pause, Loader2, Calendar, Mail } from "lucide-react"
import { useReportSchedules, useDeleteReportSchedule, useToggleReportSchedule } from "@/hooks/use-report-schedules"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { format } from "date-fns"
import type { ReportSchedule } from "@/types/report"

interface ScheduledReportsListProps {
  onEdit?: (schedule: ReportSchedule) => void
}

export function ScheduledReportsList({ onEdit }: ScheduledReportsListProps) {
  const [editingId, setEditingId] = useState<string | null>(null)
  const { data, isLoading, error } = useReportSchedules()
  const deleteSchedule = useDeleteReportSchedule()
  const toggleSchedule = useToggleReportSchedule()
  const { confirm } = useConfirmDialog()

  const schedules = data?.data || []

  const handleDelete = async (schedule: ReportSchedule) => {
    const confirmed = await confirm({
      title: "Delete Report Schedule",
      description: `Are you sure you want to delete "${schedule.name}"? This action cannot be undone.`,
      variant: "destructive",
      confirmText: "Delete",
    })

    if (confirmed) {
      await deleteSchedule.mutateAsync(schedule.id)
    }
  }

  const handleToggle = async (schedule: ReportSchedule) => {
    await toggleSchedule.mutateAsync({
      id: schedule.id,
      enabled: !schedule.enabled,
    })
  }

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Scheduled Reports</CardTitle>
          <CardDescription>Manage your automated report schedules</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-16 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Scheduled Reports</CardTitle>
          <CardDescription>Manage your automated report schedules</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-error-600">
            <p className="text-sm">Failed to load schedules</p>
            <p className="text-xs mt-1">{error instanceof Error ? error.message : "Unknown error"}</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Scheduled Reports</CardTitle>
        <CardDescription>Manage your automated report schedules</CardDescription>
      </CardHeader>
      <CardContent>
        {schedules.length === 0 ? (
          <div className="text-center py-8 text-secondary-500">
            <p className="text-sm">No scheduled reports found</p>
            <p className="text-xs mt-1">Create a schedule to get started</p>
          </div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Frequency</TableHead>
                <TableHead>Format</TableHead>
                <TableHead>Recipients</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Next Run</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {schedules.map((schedule) => (
                <TableRow key={schedule.id}>
                  <TableCell>
                    <div>
                      <div className="font-medium">{schedule.name}</div>
                      {schedule.description && (
                        <div className="text-xs text-secondary-500 mt-0.5">
                          {schedule.description}
                        </div>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className="capitalize">
                      {schedule.frequency}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="secondary" className="uppercase">
                      {schedule.format}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1 text-sm">
                      <Mail className="h-3.5 w-3.5 text-secondary-400" />
                      <span>{schedule.recipients.length}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant={schedule.enabled ? "default" : "secondary"}>
                      {schedule.enabled ? "Active" : "Inactive"}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {schedule.nextRun ? (
                      <div className="flex items-center gap-1 text-sm text-secondary-600">
                        <Calendar className="h-3.5 w-3.5" />
                        <span>{format(new Date(schedule.nextRun), "MMM d, yyyy")}</span>
                      </div>
                    ) : (
                      <span className="text-sm text-secondary-400">-</span>
                    )}
                  </TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem
                          onClick={() => handleToggle(schedule)}
                          disabled={toggleSchedule.isPending}
                        >
                          {schedule.enabled ? (
                            <>
                              <Pause className="h-4 w-4 mr-2" />
                              Disable
                            </>
                          ) : (
                            <>
                              <Play className="h-4 w-4 mr-2" />
                              Enable
                            </>
                          )}
                        </DropdownMenuItem>
                        {onEdit && (
                          <DropdownMenuItem onClick={() => onEdit(schedule)}>
                            <Edit className="h-4 w-4 mr-2" />
                            Edit
                          </DropdownMenuItem>
                        )}
                        <DropdownMenuItem
                          onClick={() => handleDelete(schedule)}
                          disabled={deleteSchedule.isPending}
                          className="text-error-600"
                        >
                          {deleteSchedule.isPending ? (
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
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  )
}

