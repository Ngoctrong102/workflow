import { memo, useMemo } from "react"
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  type Node,
  type Edge,
  type NodeTypes,
} from "reactflow"
import "reactflow/dist/style.css"
import { WorkflowNode } from "@/components/workflow/WorkflowNode"
import { cn } from "@/lib/utils"

const nodeTypes: NodeTypes = {
  workflow: WorkflowNode,
}

interface ExecutionVisualizationCanvasProps {
  nodes: Node[]
  edges: Edge[]
  currentNodeId?: string
  nodeStatuses?: Record<string, "pending" | "running" | "completed" | "failed">
  className?: string
}

export const ExecutionVisualizationCanvas = memo(function ExecutionVisualizationCanvas({
  nodes,
  edges,
  currentNodeId,
  nodeStatuses = {},
  className,
}: ExecutionVisualizationCanvasProps) {
  // Enhance nodes with status information
  const enhancedNodes = useMemo(() => {
    return nodes.map((node) => {
      const status = nodeStatuses[node.id] || "pending"
      const isCurrent = node.id === currentNodeId
      
      // Add status indicator to node data
      const nodeData = {
        ...node.data,
        status,
        isCurrent,
        invalid: status === "failed",
      }

      return {
        ...node,
        data: nodeData,
        // Add visual styling based on status
        style: {
          ...node.style,
          opacity: status === "pending" ? 0.5 : 1,
          border: isCurrent ? "3px solid #3b82f6" : status === "failed" ? "2px solid #ef4444" : status === "completed" ? "2px solid #22c55e" : "2px solid #e2e8f0",
        },
      }
    })
  }, [nodes, nodeStatuses, currentNodeId])

  // Memoize node color function for MiniMap
  const nodeColor = useMemo(() => {
    return (node: Node) => {
      const status = nodeStatuses[node.id] || "pending"
      const isCurrent = node.id === currentNodeId
      
      if (isCurrent) return "#3b82f6" // Blue for current
      if (status === "completed") return "#22c55e" // Green for completed
      if (status === "failed") return "#ef4444" // Red for failed
      if (status === "running") return "#f59e0b" // Amber for running
      return "#94a3b8" // Gray for pending
    }
  }, [nodeStatuses, currentNodeId])

  return (
    <div className={cn("w-full h-full", className)}>
      <ReactFlow
        nodes={enhancedNodes}
        edges={edges}
        nodeTypes={nodeTypes}
        fitView
        className="bg-secondary-50"
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={false}
      >
        <Background color="#cbd5e1" gap={16} />
        <Controls 
          className="bg-white border border-secondary-200"
          position="bottom-left"
        />
        <MiniMap
          className="bg-white border border-secondary-200"
          position="bottom-right"
          nodeColor={nodeColor}
        />
      </ReactFlow>
    </div>
  )
})

