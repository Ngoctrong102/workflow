import { useNavigate, useParams } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Edit, ArrowLeft, Trash2, CheckCircle2, XCircle, AlertCircle, Loader2 } from "lucide-react"
import { useChannel, useDeleteChannel } from "@/hooks/use-channels"
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
      return <CheckCircle2 className="h-5 w-5 text-success-600" />
    case "error":
      return <XCircle className="h-5 w-5 text-error-600" />
    case "inactive":
      return <AlertCircle className="h-5 w-5 text-secondary-400" />
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

export default function ChannelDetails() {
  const navigate = useNavigate()
  const { id } = useParams()
  const { data: channel, isLoading } = useChannel(id)
  const deleteChannel = useDeleteChannel()

  const { confirm } = useConfirmDialog()

  const handleDelete = async () => {
    if (!id) return

    const confirmed = await confirm({
      title: "Delete Channel",
      description: "Are you sure you want to delete this channel? This action cannot be undone.",
      variant: "destructive",
      confirmText: "Delete",
      cancelText: "Cancel",
    })

    if (confirmed) {
      await deleteChannel.mutateAsync(id)
      navigate("/channels")
    }
  }

  if (isLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex items-center justify-center h-screen">
          <div className="text-secondary-500">Loading channel...</div>
        </div>
      </div>
    )
  }

  if (!channel) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardContent className="p-6 text-center">
            <p className="text-secondary-500">Channel not found</p>
            <Button
              variant="outline"
              className="mt-4"
              onClick={() => navigate("/channels")}
            >
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Channels
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <Button
            variant="ghost"
            onClick={() => navigate("/channels")}
            className="mb-2"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <div className="flex items-center space-x-3">
            {getStatusIcon(channel.status)}
            <h1 className="text-3xl font-bold">{channel.name}</h1>
          </div>
          <p className="text-secondary-600 mt-2">{channel.description}</p>
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            onClick={() => navigate(`/channels/${channel.id}`)}
          >
            <Edit className="h-4 w-4 mr-2" />
            Edit
          </Button>
          <Button
            variant="outline"
            onClick={handleDelete}
            disabled={deleteChannel.isPending}
          >
            {deleteChannel.isPending ? (
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <Trash2 className="h-4 w-4 mr-2" />
            )}
            Delete
          </Button>
        </div>
      </div>

      {/* Channel Info */}
      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Channel Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <div className="text-sm text-secondary-500">Type</div>
              <Badge variant="outline" className="mt-1">
                {channelTypeLabels[channel.type]}
              </Badge>
            </div>
            <div>
              <div className="text-sm text-secondary-500">Status</div>
              <div className="mt-1">{getStatusBadge(channel.status)}</div>
            </div>
            {channel.createdAt && (
              <div>
                <div className="text-sm text-secondary-500">Created</div>
                <div className="mt-1">
                  {new Date(channel.createdAt).toLocaleString()}
                </div>
              </div>
            )}
            {channel.updatedAt && (
              <div>
                <div className="text-sm text-secondary-500">Updated</div>
                <div className="mt-1">
                  {new Date(channel.updatedAt).toLocaleString()}
                </div>
              </div>
            )}
            {channel.lastTestedAt && (
              <div>
                <div className="text-sm text-secondary-500">Last Tested</div>
                <div className="mt-1">
                  {new Date(channel.lastTestedAt).toLocaleString()}
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Configuration */}
        <Card>
          <CardHeader>
            <CardTitle>Configuration</CardTitle>
            <CardDescription>Channel-specific settings</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {channel.type === "email" && (
                <>
                  <div>
                    <div className="text-sm text-secondary-500">SMTP Host</div>
                    <div className="mt-1 font-mono text-sm">
                      {(channel.config as { smtpHost?: string }).smtpHost || "-"}
                    </div>
                  </div>
                  <div>
                    <div className="text-sm text-secondary-500">SMTP Port</div>
                    <div className="mt-1 font-mono text-sm">
                      {(channel.config as { smtpPort?: number }).smtpPort || "-"}
                    </div>
                  </div>
                  <div>
                    <div className="text-sm text-secondary-500">From Email</div>
                    <div className="mt-1 font-mono text-sm">
                      {(channel.config as { fromEmail?: string }).fromEmail || "-"}
                    </div>
                  </div>
                </>
              )}
              {channel.type === "sms" && (
                <>
                  <div>
                    <div className="text-sm text-secondary-500">Provider</div>
                    <div className="mt-1 font-mono text-sm">
                      {(channel.config as { provider?: string }).provider || "-"}
                    </div>
                  </div>
                  <div>
                    <div className="text-sm text-secondary-500">From Number</div>
                    <div className="mt-1 font-mono text-sm">
                      {(channel.config as { fromNumber?: string }).fromNumber || "-"}
                    </div>
                  </div>
                </>
              )}
              {channel.type === "push" && (
                <>
                  <div>
                    <div className="text-sm text-secondary-500">Provider</div>
                    <div className="mt-1 font-mono text-sm">
                      {(channel.config as { provider?: string }).provider || "-"}
                    </div>
                  </div>
                </>
              )}
              {channel.type === "webhook" && (
                <>
                  <div>
                    <div className="text-sm text-secondary-500">URL</div>
                    <div className="mt-1 font-mono text-sm break-all">
                      {(channel.config as { url?: string }).url || "-"}
                    </div>
                  </div>
                  <div>
                    <div className="text-sm text-secondary-500">Method</div>
                    <div className="mt-1 font-mono text-sm">
                      {(channel.config as { method?: string }).method || "-"}
                    </div>
                  </div>
                </>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

