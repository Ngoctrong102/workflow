import { useState, useMemo, useCallback } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Skeleton } from "@/components/ui/skeleton"
import { ArrowLeft, AlertCircle, CheckCircle2, Clock, XCircle } from "lucide-react"
import { ExecutionVisualizationCanvas } from "@/components/execution/ExecutionVisualizationCanvas"
import { StepControls } from "@/components/execution/StepControls"
import { ExecutionContext } from "@/components/execution/ExecutionContext"
import {
  useExecutionVisualization,
  useExecuteVisualizationStep,
  useResetVisualization,
  useVisualizationContext,
} from "@/hooks/use-execution-visualization"
import { useToast } from "@/hooks/use-toast"
import type { Node, Edge } from "reactflow"

const statusConfig: Record<string, { label: string; color: string; icon: typeof CheckCircle2 }> = {
  completed: {
    label: "Completed",
    color: "bg-success-600",
    icon: CheckCircle2,
  },
  failed: {
    label: "Failed",
    color: "bg-error-600",
    icon: XCircle,
  },
  running: {
    label: "Running",
    color: "bg-primary-600",
    icon: Clock,
  },
  waiting: {
    label: "Waiting",
    color: "bg-warning-600",
    icon: Clock,
  },
  cancelled: {
    label: "Cancelled",
    color: "bg-secondary-600",
    icon: AlertCircle,
  },
}

export default function ExecutionVisualization() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { toast } = useToast()
  const [isPlaying, setIsPlaying] = useState(false)
  const [playInterval, setPlayInterval] = useState<NodeJS.Timeout | null>(null)

  const { data: visualizationData, isLoading, error, refetch } = useExecutionVisualization(id)
  const { data: contextData } = useVisualizationContext(id)
  const executeStep = useExecuteVisualizationStep()
  const resetVisualization = useResetVisualization()

  // Convert workflow definition to React Flow nodes and edges
  const { nodes, edges, nodeStatuses, currentNodeId } = useMemo(() => {
    if (!visualizationData) {
      return { nodes: [], edges: [], nodeStatuses: {}, currentNodeId: undefined }
    }

    const workflowNodes: Node[] = visualizationData.workflow.definition.nodes.map((node) => ({
      id: node.id,
      type: "workflow",
      position: node.position,
      data: {
        type: node.type as any,
        label: node.data.label,
        config: node.data.config,
      },
    }))

    const workflowEdges: Edge[] = visualizationData.workflow.definition.edges.map((edge) => ({
      id: edge.id,
      source: edge.source,
      target: edge.target,
      sourceHandle: edge.sourceHandle,
      targetHandle: edge.targetHandle,
    }))

    // Create node statuses map
    const statuses: Record<string, "pending" | "running" | "completed" | "failed"> = {}
    visualizationData.nodes.forEach((node) => {
      statuses[node.id] = node.status
    })

    // Find current node (the one at current step)
    const currentNode = visualizationData.nodes.find(
      (node, index) => index === visualizationData.current_step
    )

    return {
      nodes: workflowNodes,
      edges: workflowEdges,
      nodeStatuses: statuses,
      currentNodeId: currentNode?.id,
    }
  }, [visualizationData])

  // Handle step execution
  const handleNext = useCallback(async () => {
    if (!id) return

    try {
      await executeStep.mutateAsync({
        executionId: id,
        direction: "forward",
      })
    } catch (error) {
      // Error is handled by the mutation hook
      console.error("Failed to execute step:", error)
    }
  }, [id, executeStep])

  const handlePrevious = useCallback(async () => {
    if (!id) return

    try {
      await executeStep.mutateAsync({
        executionId: id,
        direction: "backward",
      })
    } catch (error) {
      // Error is handled by the mutation hook
      console.error("Failed to execute previous step:", error)
    }
  }, [id, executeStep])

  const handleReset = useCallback(async () => {
    if (!id) return

    try {
      await resetVisualization.mutateAsync(id)
      setIsPlaying(false)
      if (playInterval) {
        clearInterval(playInterval)
        setPlayInterval(null)
      }
    } catch (error) {
      // Error is handled by the mutation hook
      console.error("Failed to reset visualization:", error)
    }
  }, [id, resetVisualization, playInterval])

  const handlePlayPause = useCallback(() => {
    if (isPlaying) {
      // Pause
      setIsPlaying(false)
      if (playInterval) {
        clearInterval(playInterval)
        setPlayInterval(null)
      }
    } else {
      // Play
      setIsPlaying(true)
      const interval = setInterval(() => {
        handleNext()
      }, 2000) // Execute step every 2 seconds
      setPlayInterval(interval)
    }
  }, [isPlaying, playInterval, handleNext])

  // Get current step info
  const currentStepInfo = useMemo(() => {
    if (!visualizationData) return null

    const currentNode = visualizationData.nodes[visualizationData.current_step]
    return currentNode
  }, [visualizationData])

  // Check if we can navigate
  const hasNext = useMemo(() => {
    if (!visualizationData) return false
    return visualizationData.current_step < visualizationData.total_steps - 1
  }, [visualizationData])

  const hasPrevious = useMemo(() => {
    if (!visualizationData) return false
    return visualizationData.current_step > 0
  }, [visualizationData])

  if (isLoading) {
    return (
      <div className="container mx-auto p-6 space-y-6">
        <Skeleton className="h-12 w-64" />
        <Skeleton className="h-96" />
      </div>
    )
  }

  if (error || !visualizationData) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardHeader>
            <CardTitle>Error Loading Visualization</CardTitle>
            <CardDescription>
              {error ? "Failed to load execution visualization" : "Execution not found"}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate("/executions")} variant="outline">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Executions
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  const execution = visualizationData.execution
  const statusInfo = statusConfig[execution.status] || statusConfig.completed

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => navigate("/executions")}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <div>
            <h1 className="text-2xl font-bold">Execution Visualization</h1>
            <p className="text-sm text-secondary-600 mt-1">
              Step-by-step execution replay for debugging
            </p>
          </div>
        </div>
        <Badge className={statusInfo.color} variant="default">
          <statusInfo.icon className="h-3 w-3 mr-1" />
          {statusInfo.label}
        </Badge>
      </div>

      {/* Execution Info */}
      <Card>
        <CardHeader>
          <CardTitle>Execution Information</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <p className="text-sm text-secondary-600">Execution ID</p>
              <p className="font-mono text-sm">{execution.id}</p>
            </div>
            <div>
              <p className="text-sm text-secondary-600">Workflow</p>
              <p className="text-sm">{visualizationData.workflow.name}</p>
            </div>
            <div>
              <p className="text-sm text-secondary-600">Trigger</p>
              <p className="text-sm capitalize">{visualizationData.trigger.type}</p>
            </div>
            <div>
              <p className="text-sm text-secondary-600">Status</p>
              <Badge className={statusInfo.color} variant="default">
                {statusInfo.label}
              </Badge>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Step Controls */}
      <StepControls
        currentStep={visualizationData.current_step + 1}
        totalSteps={visualizationData.total_steps}
        hasNext={hasNext}
        hasPrevious={hasPrevious}
        isPlaying={isPlaying}
        onNext={handleNext}
        onPrevious={handlePrevious}
        onReset={handleReset}
        onPlayPause={handlePlayPause}
      />

      {/* Current Step Info */}
      {currentStepInfo && (
        <Card>
          <CardHeader>
            <CardTitle>Current Step</CardTitle>
            <CardDescription>
              Node: {currentStepInfo.id} ({currentStepInfo.type})
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-2">
              <Badge
                variant="default"
                className={
                  currentStepInfo.status === "completed"
                    ? "bg-success-600"
                    : currentStepInfo.status === "failed"
                    ? "bg-error-600"
                    : currentStepInfo.status === "running"
                    ? "bg-primary-600"
                    : "bg-secondary-600"
                }
              >
                {currentStepInfo.status}
              </Badge>
              {currentStepInfo.execution && (
                <span className="text-sm text-secondary-600">
                  Executed at: {new Date(currentStepInfo.execution.started_at).toLocaleString()}
                </span>
              )}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Visualization Canvas and Context */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Canvas */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Workflow Visualization</CardTitle>
              <CardDescription>Visual representation of execution flow</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-[600px] w-full">
                <ExecutionVisualizationCanvas
                  nodes={nodes}
                  edges={edges}
                  currentNodeId={currentNodeId}
                  nodeStatuses={nodeStatuses}
                />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Context Panel */}
        <div className="lg:col-span-1">
          <Card>
            <CardHeader>
              <CardTitle>Execution Context</CardTitle>
              <CardDescription>Current execution context and variables</CardDescription>
            </CardHeader>
            <CardContent>
              <ExecutionContext
                executionId={id}
                context={contextData || visualizationData.context}
              />
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}

