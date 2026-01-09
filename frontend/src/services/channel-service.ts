import { apiClient } from "./api-client"
import type { Channel, ChannelType } from "@/types/channel"

export interface CreateChannelRequest {
  name: string
  description?: string
  type: ChannelType
  status?: "active" | "inactive" | "error"
  config: Record<string, unknown>
}

export interface UpdateChannelRequest extends CreateChannelRequest {
  id: string
}

export interface ListChannelsParams {
  type?: ChannelType
  status?: string
  limit?: number
  offset?: number
  search?: string
}

export interface PagedResponse<T> {
  data: T[]
  total: number
  limit: number
  offset: number
  hasMore: boolean
}

export interface ListChannelsResponse extends PagedResponse<Channel> {}

export interface TestConnectionResponse {
  success: boolean
  message: string
  details?: Record<string, unknown>
}

export const channelService = {
  /**
   * List all channels
   */
  list: async (params?: ListChannelsParams): Promise<ListChannelsResponse> => {
    const response = await apiClient.get<PagedResponse<Channel>>("/channels", {
      params,
    })
    return response.data
  },

  /**
   * Get channel by ID
   */
  get: async (id: string): Promise<Channel> => {
    const response = await apiClient.get<Channel>(`/channels/${id}`)
    return response.data
  },

  /**
   * Create new channel
   */
  create: async (channel: CreateChannelRequest): Promise<Channel> => {
    const response = await apiClient.post<Channel>("/channels", channel)
    return response.data
  },

  /**
   * Update channel
   */
  update: async (id: string, channel: Omit<UpdateChannelRequest, "id">): Promise<Channel> => {
    const response = await apiClient.put<Channel>(`/channels/${id}`, channel)
    return response.data
  },

  /**
   * Delete channel
   */
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/channels/${id}`)
  },

  /**
   * Test channel connection
   */
  testConnection: async (id: string, config: Record<string, unknown>): Promise<TestConnectionResponse> => {
    const response = await apiClient.post<TestConnectionResponse>(`/channels/${id}/test`, { config })
    return response.data
  },
}

