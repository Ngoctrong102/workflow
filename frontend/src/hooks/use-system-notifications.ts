import { useState, useEffect, useCallback } from "react"
import { useQuery, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { executionService } from "@/services/execution-service"
import { workflowService } from "@/services/workflow-service"
import type { Execution } from "@/types/execution"
import type { Workflow } from "@/types/workflow"

export interface SystemNotification {
  id: string
  type: "execution_completed" | "execution_failed" | "workflow_status_changed"
  title: string
  message: string
  timestamp: Date
  read: boolean
  data?: {
    executionId?: string
    workflowId?: string
    workflowName?: string
    executionStatus?: string
    previousStatus?: string
    newStatus?: string
  }
}

interface NotificationPreferences {
  executionCompleted: boolean
  executionFailed: boolean
  workflowStatusChanged: boolean
}

const DEFAULT_PREFERENCES: NotificationPreferences = {
  executionCompleted: true,
  executionFailed: true,
  workflowStatusChanged: false,
}

/**
 * Hook to manage system notifications
 */
export function useSystemNotifications() {
  const { toast } = useToast()
  const queryClient = useQueryClient()
  const [notifications, setNotifications] = useState<SystemNotification[]>([])
  const [preferences, setPreferences] = useState<NotificationPreferences>(() => {
    const stored = localStorage.getItem("notification-preferences")
    return stored ? JSON.parse(stored) : DEFAULT_PREFERENCES
  })

  // Save preferences to localStorage
  useEffect(() => {
    localStorage.setItem("notification-preferences", JSON.stringify(preferences))
  }, [preferences])

  // Poll for execution updates
  const { data: executionsData } = useQuery({
    queryKey: ["executions", { limit: 10, status: "running" }],
    queryFn: () => executionService.list({ limit: 10, status: "running" }),
    refetchInterval: 10000, // Poll every 10 seconds
  })

  // Track previous execution states
  const [previousExecutions, setPreviousExecutions] = useState<Map<string, Execution>>(new Map())

  // Check for execution status changes
  useEffect(() => {
    if (!executionsData?.executions) return

    const currentExecutions = new Map(
      executionsData.executions.map((e) => [e.id, e])
    )

    // Check for status changes
    currentExecutions.forEach((execution, id) => {
      const previous = previousExecutions.get(id)

      if (previous) {
        // Execution status changed
        if (previous.status !== execution.status) {
          if (execution.status === "completed" && preferences.executionCompleted) {
            const notification: SystemNotification = {
              id: `exec-${id}-${Date.now()}`,
              type: "execution_completed",
              title: "Execution Completed",
              message: `Workflow execution "${execution.workflow_name || execution.workflow_id}" has completed successfully`,
              timestamp: new Date(),
              read: false,
              data: {
                executionId: id,
                workflowId: execution.workflow_id,
                workflowName: execution.workflow_name,
                executionStatus: execution.status,
              },
            }

            setNotifications((prev) => [notification, ...prev])
            toast({
              title: notification.title,
              description: notification.message,
            })
          } else if (execution.status === "failed" && preferences.executionFailed) {
            const notification: SystemNotification = {
              id: `exec-${id}-${Date.now()}`,
              type: "execution_failed",
              title: "Execution Failed",
              message: `Workflow execution "${execution.workflow_name || execution.workflow_id}" has failed`,
              timestamp: new Date(),
              read: false,
              data: {
                executionId: id,
                workflowId: execution.workflow_id,
                workflowName: execution.workflow_name,
                executionStatus: execution.status,
              },
            }

            setNotifications((prev) => [notification, ...prev])
            toast({
              variant: "destructive",
              title: notification.title,
              description: notification.message,
            })
          }
        }
      }
    })

    setPreviousExecutions(currentExecutions)
  }, [executionsData, previousExecutions, preferences, toast])

  // Poll for workflow status changes
  const { data: workflowsData } = useQuery({
    queryKey: ["workflows", { limit: 100 }],
    queryFn: () => workflowService.list({ limit: 100 }),
    refetchInterval: 30000, // Poll every 30 seconds
  })

  const [previousWorkflows, setPreviousWorkflows] = useState<Map<string, Workflow>>(new Map())

  useEffect(() => {
    if (!workflowsData?.data || !preferences.workflowStatusChanged) return

    const currentWorkflows = new Map(
      workflowsData.data.map((w) => [w.id, w])
    )

    currentWorkflows.forEach((workflow, id) => {
      const previous = previousWorkflows.get(id)

      if (previous && previous.status !== workflow.status) {
        const notification: SystemNotification = {
          id: `workflow-${id}-${Date.now()}`,
          type: "workflow_status_changed",
          title: "Workflow Status Changed",
          message: `Workflow "${workflow.name}" status changed from ${previous.status} to ${workflow.status}`,
          timestamp: new Date(),
          read: false,
          data: {
            workflowId: id,
            workflowName: workflow.name,
            previousStatus: previous.status,
            newStatus: workflow.status,
          },
        }

        setNotifications((prev) => [notification, ...prev])
        toast({
          title: notification.title,
          description: notification.message,
        })
      }
    })

    setPreviousWorkflows(currentWorkflows)
  }, [workflowsData, previousWorkflows, preferences, toast])

  const markAsRead = useCallback((id: string) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n))
    )
  }, [])

  const markAllAsRead = useCallback(() => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })))
  }, [])

  const dismiss = useCallback((id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id))
  }, [])

  const unreadCount = notifications.filter((n) => !n.read).length

  return {
    notifications,
    unreadCount,
    preferences,
    setPreferences,
    markAsRead,
    markAllAsRead,
    dismiss,
  }
}

