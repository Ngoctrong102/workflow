import { useNavigate, useParams } from "react-router-dom"
import { ChannelEditor as ChannelEditorComponent } from "@/components/channel/ChannelEditor"
import { useChannel, useCreateChannel, useUpdateChannel, useTestChannelConnection } from "@/hooks/use-channels"
import type { Channel } from "@/types/channel"

export default function ChannelEditorPage() {
  const navigate = useNavigate()
  const { id } = useParams()

  const isEditMode = id !== "new" && id !== undefined
  const { data: channel, isLoading: isLoadingChannel } = useChannel(id)
  const createChannel = useCreateChannel()
  const updateChannel = useUpdateChannel()
  const testConnection = useTestChannelConnection()

  const handleSave = async (channelData: Omit<Channel, "id" | "createdAt" | "updatedAt">) => {
    try {
      if (isEditMode && id) {
        await updateChannel.mutateAsync({
          id,
          ...channelData,
        })
      } else {
        await createChannel.mutateAsync(channelData)
      }
      navigate("/channels")
    } catch (error) {
      // Error handling is done in the mutation hooks
      console.error("Failed to save channel:", error)
    }
  }

  const handleCancel = () => {
    navigate("/channels")
  }

  const handleTest = async (config: Record<string, unknown>): Promise<boolean> => {
    if (!id || id === "new") {
      // For new channels, we can't test yet
      return false
    }

    try {
      const result = await testConnection.mutateAsync({ id, config })
      return result.success
    } catch (error) {
      return false
    }
  }

  if (isLoadingChannel && isEditMode) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex items-center justify-center h-screen">
          <div className="text-secondary-500">Loading channel...</div>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold">
          {isEditMode ? "Edit Channel" : "Add Channel"}
        </h1>
        <p className="text-secondary-600 mt-2">
          {isEditMode
            ? "Update your notification channel configuration"
            : "Configure a new notification channel"}
        </p>
      </div>
      <ChannelEditorComponent
        channel={channel}
        onSave={handleSave}
        onCancel={handleCancel}
        onTest={handleTest}
      />
    </div>
  )
}

