import { memo, useCallback, useRef, useMemo } from "react"
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  addEdge,
  useNodesState,
  useEdgesState,
  type Connection,
  type Edge,
  type Node,
  type NodeTypes,
  type NodeChange,
  type EdgeChange,
} from "reactflow"
import "reactflow/dist/style.css"
import { WorkflowNode } from "./WorkflowNode"
import type { WorkflowNodeType } from "@/types/workflow"
import { NODE_DEFINITIONS } from "@/constants/workflow-nodes"

const nodeTypes: NodeTypes = {
  workflow: WorkflowNode,
}

interface WorkflowCanvasProps {
  nodes?: Node[]
  edges?: Edge[]
  onNodesChange?: (changes: NodeChange[]) => void
  onEdgesChange?: (changes: EdgeChange[]) => void
  onConnect?: (connection: Connection) => void
  onAddNode?: (node: Node) => void
}

export const WorkflowCanvas = memo(function WorkflowCanvas({
  nodes: initialNodes = [],
  edges: initialEdges = [],
  onNodesChange: externalOnNodesChange,
  onEdgesChange: externalOnEdgesChange,
  onConnect: externalOnConnect,
  onAddNode: externalOnAddNode,
}: WorkflowCanvasProps) {
  const reactFlowWrapper = useRef<HTMLDivElement>(null)
  const [nodes, setNodes, onNodesChangeInternal] = useNodesState(initialNodes)
  const [edges, setEdges, onEdgesChangeInternal] = useEdgesState(initialEdges)

  const handleConnect = useCallback(
    (params: Connection) => {
      if (externalOnConnect) {
        externalOnConnect(params)
      } else {
        setEdges((eds) => addEdge(params, eds))
      }
    },
    [externalOnConnect, setEdges]
  )

  const onDragOver = useCallback((event: React.DragEvent) => {
    event.preventDefault()
    event.dataTransfer.dropEffect = "move"
  }, [])

  const onDrop = useCallback(
    (event: React.DragEvent) => {
      event.preventDefault()
      event.stopPropagation()

      const nodeType = event.dataTransfer.getData("application/reactflow") as WorkflowNodeType

      if (!nodeType) {
        console.warn('[WorkflowCanvas] No node type in drop event')
        return
      }

      if (!reactFlowWrapper.current) {
        console.warn('[WorkflowCanvas] ReactFlow wrapper not found')
        return
      }

      const nodeDef = NODE_DEFINITIONS.find((n) => n.type === nodeType)
      if (!nodeDef) {
        console.warn('[WorkflowCanvas] Node definition not found for type:', nodeType)
        return
      }

      // Calculate position relative to ReactFlow viewport
      const reactFlowBounds = reactFlowWrapper.current.getBoundingClientRect()
      const position = {
        x: event.clientX - reactFlowBounds.left,
        y: event.clientY - reactFlowBounds.top,
      }

      // Convert nodeType from kebab-case to camelCase for node ID
      // e.g., "event-trigger" -> "eventTrigger", "send-webhook" -> "sendWebhook"
      const camelCaseNodeType = nodeType
        .split('-')
        .map((word, index) => 
          index === 0 
            ? word.toLowerCase() 
            : word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
        )
        .join('')
      
      const newNode: Node = {
        id: `${camelCaseNodeType}${Date.now()}`,
        type: "workflow",
        position,
        data: {
          label: nodeDef.label,
          type: nodeType,
        },
      }

      // If external handler exists, use it; otherwise use internal state
      if (externalOnAddNode) {
        console.log('[WorkflowCanvas] Adding node via externalOnAddNode:', newNode)
        externalOnAddNode(newNode)
      } else if (externalOnNodesChange) {
        // Add node through onNodesChange if available
        console.log('[WorkflowCanvas] Adding node via externalOnNodesChange:', newNode)
        externalOnNodesChange([{ type: 'add', item: newNode }])
      } else {
        // Fallback to internal state
        console.log('[WorkflowCanvas] Adding node via internal state:', newNode)
        setNodes((nds) => nds.concat(newNode))
      }
    },
    [setNodes, externalOnAddNode, externalOnNodesChange]
  )

  // Use external handlers if provided, otherwise use internal state
  const handleNodesChange = useCallback(
    (changes: NodeChange[]) => {
      if (externalOnNodesChange) {
        externalOnNodesChange(changes)
      } else {
        onNodesChangeInternal(changes)
      }
    },
    [externalOnNodesChange, onNodesChangeInternal]
  )

  const handleEdgesChange = useCallback(
    (changes: EdgeChange[]) => {
      if (externalOnEdgesChange) {
        externalOnEdgesChange(changes)
      } else {
        onEdgesChangeInternal(changes)
      }
    },
    [externalOnEdgesChange, onEdgesChangeInternal]
  )

  // Use external nodes/edges if provided
  const displayNodes = useMemo(
    () => (externalOnNodesChange ? initialNodes : nodes),
    [externalOnNodesChange, initialNodes, nodes]
  )
  const displayEdges = useMemo(
    () => (externalOnEdgesChange ? initialEdges : edges),
    [externalOnEdgesChange, initialEdges, edges]
  )

  // Memoize node color function for MiniMap
  const nodeColor = useCallback(
    (node: Node) => {
      const nodeDef = NODE_DEFINITIONS.find(
        (n) => n.type === (node.data?.type as WorkflowNodeType)
      )
      return nodeDef?.color || "#64748b"
    },
    []
  )

  return (
    <div className="w-full h-full" ref={reactFlowWrapper}>
      <ReactFlow
        nodes={displayNodes}
        edges={displayEdges}
        onNodesChange={handleNodesChange}
        onEdgesChange={handleEdgesChange}
        onConnect={handleConnect}
        onDrop={onDrop}
        onDragOver={onDragOver}
        onNodeClick={() => {
          // Selection is handled by react-flow through onNodesChange
        }}
        nodeTypes={nodeTypes}
        fitView
        className="bg-secondary-50"
      >
        <Background color="#cbd5e1" gap={16} />
        <Controls 
          className="bg-white border border-secondary-200"
          position="bottom-left"
          style={{ bottom: 0, left: 0 }}
        />
        <MiniMap
          className="bg-white border border-secondary-200"
          position="bottom-right"
          style={{ bottom: 0, right: 0 }}
          nodeColor={nodeColor}
        />
      </ReactFlow>
    </div>
  )
})

