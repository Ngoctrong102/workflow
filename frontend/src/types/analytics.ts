export interface WorkflowAnalytics {
  workflowId: string
  totalExecutions: number
  successfulExecutions: number
  failedExecutions: number
  averageExecutionTime: number
  executionRate: number
  period: {
    start: string
    end: string
  }
}

export interface DeliveryAnalytics {
  totalSent: number
  delivered: number
  failed: number
  pending: number
  deliveryRate: number
  averageDeliveryTime: number
  period: {
    start: string
    end: string
  }
  byChannel: {
    channel: string
    sent: number
    delivered: number
    failed: number
    deliveryRate: number
  }[]
}

export interface ChannelAnalytics {
  channelId: string
  channelType: string
  totalSent: number
  delivered: number
  failed: number
  deliveryRate: number
  averageDeliveryTime: number
  period: {
    start: string
    end: string
  }
}

export interface ErrorAnalytics {
  totalErrors: number
  errorRate: number
  byType: {
    type: string
    count: number
    percentage: number
  }[]
  byChannel: {
    channel: string
    count: number
    percentage: number
  }[]
  period: {
    start: string
    end: string
  }
}

export interface AnalyticsDateRange {
  start: string
  end: string
  granularity?: "hourly" | "daily" | "weekly" | "monthly"
}

