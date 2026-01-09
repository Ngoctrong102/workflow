import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import { Plus, Search, Edit, Trash2, Eye, CheckCircle2, XCircle, AlertCircle, Loader2 } from "lucide-react"
import { useChannels, useDeleteChannel } from "@/hooks/use-channels"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import type { ChannelType, ChannelStatus } from "@/types/channel"

const channelTypeLabels: Record<ChannelType, string> = {
  email: "Email",
  sms: "SMS",
  push: "Push",
  "in-app": "In-App",
  slack: "Slack",
  discord: "Discord",
  teams: "Teams",
  webhook: "Webhook",
}

const getStatusIcon = (status: ChannelStatus) => {
  switch (status) {
    case "active":
      return <CheckCircle2 className="h-3.5 w-3.5 text-success-600" />
    case "error":
      return <XCircle className="h-3.5 w-3.5 text-error-600" />
    case "inactive":
      return <AlertCircle className="h-3.5 w-3.5 text-secondary-400" />
  }
}

const getStatusBadge = (status: ChannelStatus) => {
  switch (status) {
    case "active":
      return <Badge variant="default" className="bg-success-600">Active</Badge>
    case "error":
      return <Badge variant="destructive">Error</Badge>
    case "inactive":
      return <Badge variant="secondary">Inactive</Badge>
  }
}

export default function ChannelList() {
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState("")
  const [typeFilter, setTypeFilter] = useState<ChannelType | "all">("all")
  const [statusFilter, setStatusFilter] = useState<ChannelStatus | "all">("all")

  const { data, isLoading, error } = useChannels({
    type: typeFilter !== "all" ? typeFilter : undefined,
    status: statusFilter !== "all" ? statusFilter : undefined,
    search: searchQuery || undefined,
    limit: 50,
    offset: 0,
  })

  const deleteChannel = useDeleteChannel()

  const channels = data?.data || []
  const total = data?.total || 0

  const { confirm } = useConfirmDialog()

  const handleDelete = async (id: string) => {
    const confirmed = await confirm({
      title: "Delete Channel",
      description: "Are you sure you want to delete this channel? This action cannot be undone.",
      variant: "destructive",
      confirmText: "Delete",
      cancelText: "Cancel",
    })

    if (confirmed) {
      await deleteChannel.mutateAsync(id)
    }
  }

  return (
    <div className="space-y-3">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Channels</h1>
          <p className="text-secondary-600 mt-1 text-sm">
            Manage your notification channels
          </p>
        </div>
        <Button size="sm" onClick={() => navigate("/channels/new")}>
          <Plus className="h-3.5 w-3.5 mr-1.5" />
          Add Channel
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-3">
          <div className="flex items-center space-x-3">
            <div className="flex-1 relative">
              <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 h-3.5 w-3.5 text-secondary-400" />
              <Input
                placeholder="Search channels..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-8 h-9 text-sm"
              />
            </div>
            <Select value={typeFilter} onValueChange={(v) => setTypeFilter(v as ChannelType | "all")}>
              <SelectTrigger className="w-44 h-9 text-sm">
                <SelectValue placeholder="Filter by type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Types</SelectItem>
                {Object.entries(channelTypeLabels).map(([value, label]) => (
                  <SelectItem key={value} value={value}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={statusFilter} onValueChange={(v) => setStatusFilter(v as ChannelStatus | "all")}>
              <SelectTrigger className="w-44 h-9 text-sm">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="active">Active</SelectItem>
                <SelectItem value="inactive">Inactive</SelectItem>
                <SelectItem value="error">Error</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Channel List */}
      <Card>
        <CardHeader className="px-4 py-3">
          <CardTitle className="text-base">Channels {total > 0 && `(${total})`}</CardTitle>
          <CardDescription className="text-xs">All your notification channels</CardDescription>
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
              <p className="text-sm">Failed to load channels</p>
              <p className="text-xs mt-1">{error instanceof Error ? error.message : "Unknown error"}</p>
            </div>
          ) : channels.length === 0 ? (
            <div className="text-center py-8 text-secondary-500">
              <p className="text-sm">No channels found</p>
              <Button
                variant="outline"
                size="sm"
                className="mt-3"
                onClick={() => navigate("/channels/new")}
              >
                <Plus className="h-3.5 w-3.5 mr-1.5" />
                Add Channel
              </Button>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="h-10 px-3 text-xs">Name</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Type</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Status</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Configuration</TableHead>
                  <TableHead className="h-10 px-3 text-xs">Updated</TableHead>
                  <TableHead className="h-10 px-3 text-right text-xs">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {(channels as Array<{ id: string; name: string; description?: string; type: ChannelType; status: ChannelStatus; config: Record<string, unknown>; updatedAt?: string }>).map((channel) => (
                  <TableRow key={channel.id} className="hover:bg-secondary-50/50">
                    <TableCell className="px-3 py-2">
                      <div className="flex items-center space-x-2">
                        <div className="flex-shrink-0">{getStatusIcon(channel.status)}</div>
                        <div>
                          <div className="font-medium text-sm">{channel.name}</div>
                          {channel.description && (
                            <div className="text-xs text-secondary-500 mt-0.5">
                              {channel.description}
                            </div>
                          )}
                        </div>
                      </div>
                    </TableCell>
                    <TableCell className="px-3 py-2">
                      <Badge variant="outline" className="text-xs">
                        {channelTypeLabels[channel.type]}
                      </Badge>
                    </TableCell>
                    <TableCell className="px-3 py-2">{getStatusBadge(channel.status)}</TableCell>
                    <TableCell className="px-3 py-2">
                      <div className="text-xs text-secondary-500">
                        {channel.type === "email" && (
                          <span>
                            {(channel.config as { smtpHost?: string }).smtpHost || "Not configured"}
                          </span>
                        )}
                        {channel.type === "sms" && (
                          <span>
                            {(channel.config as { fromNumber?: string }).fromNumber || "Not configured"}
                          </span>
                        )}
                        {channel.type === "webhook" && (
                          <span>
                            {(channel.config as { url?: string }).url || "Not configured"}
                          </span>
                        )}
                        {channel.type === "push" && (
                          <span>
                            {(channel.config as { provider?: string }).provider || "Not configured"}
                          </span>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="px-3 py-2 text-xs text-secondary-500">
                      {channel.updatedAt
                        ? new Date(channel.updatedAt).toLocaleDateString()
                        : "-"}
                    </TableCell>
                    <TableCell className="px-3 py-2 text-right">
                      <div className="flex items-center justify-end space-x-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => navigate(`/channels/${channel.id}`)}
                        >
                          <Eye className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => navigate(`/channels/${channel.id}`)}
                        >
                          <Edit className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-7 w-7 p-0"
                          onClick={() => handleDelete(channel.id)}
                          disabled={deleteChannel.isPending}
                        >
                          {deleteChannel.isPending ? (
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
    </div>
  )
}
