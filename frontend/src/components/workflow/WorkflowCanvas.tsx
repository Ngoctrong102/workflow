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
import { NodeTypeEnum } from "@/types/workflow"
import { NODE_DEFINITIONS } from "@/constants/workflow-nodes"
import { useToast } from "@/hooks/use-toast"
import { getNodeCategory } from "@/utils/node-type-utils"

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
  const reactFlowInstanceRef = useRef<any>(null)
  const [nodes, setNodes, onNodesChangeInternal] = useNodesState(initialNodes)
  const [edges, setEdges, onEdgesChangeInternal] = useEdgesState(initialEdges)
  const { toast } = useToast()

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

      // Try to get node data from new format first
      const nodeDataStr = event.dataTransfer.getData("application/reactflow-node")
      let registryId: string | undefined
      let configTemplate: Record<string, unknown> | undefined
      let nodeType: NodeTypeEnum | undefined
      let nodeLabel: string | undefined

      if (nodeDataStr) {
        try {
          const nodeData = JSON.parse(nodeDataStr)
          nodeType = nodeData.nodeType as NodeTypeEnum
          nodeLabel = nodeData.label
          registryId = nodeData.registryId
          configTemplate = nodeData.configTemplate
        } catch (error) {
          console.warn('[WorkflowCanvas] Failed to parse node data:', error)
        }
      }

      // Fallback to legacy format if new format not found
      if (!nodeType) {
        nodeType = event.dataTransfer.getData("application/reactflow") as NodeTypeEnum
      }

      if (!nodeType) {
        console.warn('[WorkflowCanvas] No node type in drop event')
        return
      }

      // Validate: registryId is required for trigger and action nodes from registry
      // nodeType should be a valid WorkflowNodeType
      // Use helper function to correctly detect node category
      // For new nodes being created, detect category based on registryId and nodeType
      // If registryId exists and nodeType is TRIGGER, it's a trigger from registry
      // If registryId exists and nodeType is ACTION, it's an action from registry
      const nodeCategory = getNodeCategory(nodeType, configTemplate ? { registryId, configTemplate } : { registryId })
      const isTrigger = nodeCategory === NodeTypeEnum.TRIGGER
      const isAction = nodeCategory === NodeTypeEnum.ACTION
      const isLogic = nodeCategory === NodeTypeEnum.LOGIC
      
      // Debug logging
      if (import.meta.env.DEV) {
        console.log('[WorkflowCanvas] Node category detection:', {
          nodeType,
          registryId,
          nodeCategory,
          isTrigger,
          isAction,
          isLogic,
          hasConfigTemplate: !!configTemplate,
        })
      }
      
      // If this is a registry node (has registryId), validate it
      if (registryId) {
        // Registry nodes are valid (triggers or actions)
      } else if (isTrigger) {
        // Non-registry trigger nodes might be built-in, allow them
      }

      if (!reactFlowWrapper.current) {
        console.warn('[WorkflowCanvas] ReactFlow wrapper not found')
        return
      }

      // Find node definition - try from NODE_DEFINITIONS first, then use label from drag data
      const nodeDef = NODE_DEFINITIONS.find((n) => n.type === nodeType)
      const finalLabel = nodeLabel || nodeDef?.label || nodeType

      // Calculate position relative to ReactFlow viewport
      // Account for ReactFlow's transform (zoom and pan) if available
      const reactFlowBounds = reactFlowWrapper.current.getBoundingClientRect()
      let position = {
        x: event.clientX - reactFlowBounds.left,
        y: event.clientY - reactFlowBounds.top,
      }

      // If ReactFlow instance is available, use screenToFlowPosition
      if (reactFlowInstanceRef.current?.screenToFlowPosition) {
        position = reactFlowInstanceRef.current.screenToFlowPosition({
          x: event.clientX - reactFlowBounds.left,
          y: event.clientY - reactFlowBounds.top,
        })
      }
      
      console.log('[WorkflowCanvas] Drop position:', {
        clientX: event.clientX,
        clientY: event.clientY,
        bounds: reactFlowBounds,
        calculatedPosition: position,
      })

      // Generate unique node ID
      // For trigger nodes: use label (sanitized) to create short, MVEL-compatible IDs
      // For action nodes: use registryId (already short like "call-webhook")
      // For logic nodes: use nodeType
      let nodeIdBase: string
      if (isTrigger) {
        // Use label for trigger nodes to create short IDs (e.g., "direct_debit_event_1234567890")
        // This makes MVEL expressions easier to write
        nodeIdBase = finalLabel.toLowerCase().replace(/[^a-z0-9]/g, '_')
      } else if (isAction && registryId) {
        // Action nodes: use registryId (already short)
        nodeIdBase = registryId
      } else {
        // Fallback: use nodeType
        nodeIdBase = nodeType
      }
      const nodeId = `${nodeIdBase}_${Date.now()}`
      
      // Backend expects:
      // - For triggers: node.type = "TRIGGER", node.data.config.triggerConfigId = registryId
      // - For actions: node.type = "ACTION", node.data.config.registryId = registryId
      // - For logic: node.type = "LOGIC", node.data.config.subtype = nodeType
      
      // Build node data structure compatible with backend
      const nodeData: Record<string, unknown> = {
        label: finalLabel,
        type: nodeType as NodeTypeEnum,
      }
      
      // Add registry-specific fields
      if (isTrigger && registryId) {
        // Triggers: store triggerConfigId (which is the registryId from trigger registry)
        nodeData.config = {
          triggerConfigId: registryId,
          ...(configTemplate && { configTemplate }),
        }
        console.log('[WorkflowCanvas] Created trigger node with triggerConfigId:', {
          nodeId,
          nodeType,
          registryId,
          triggerConfigId: registryId,
          config: nodeData.config
        })
      } else if (isAction && registryId) {
        // Actions: store registryId
        nodeData.config = {
          registryId: registryId,
          ...(configTemplate && { configTemplate }),
        }
        console.log('[WorkflowCanvas] Created action node with registryId:', {
          nodeId,
          nodeType,
          registryId,
          config: nodeData.config
        })
      } else if (configTemplate) {
        // Logic nodes or nodes with config template
        nodeData.config = {
          ...(configTemplate && { configTemplate }),
        }
      }
      
      const newNode: Node = {
        id: nodeId,
        type: "workflow",
        position,
        data: nodeData,
      }
      
      console.log('[WorkflowCanvas] Creating new node:', {
        nodeId,
        nodeType,
        registryId,
        configTemplate: configTemplate ? 'present' : 'missing',
        finalLabel,
        position,
        isTrigger,
        isAction,
        isLogic,
      })

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
    [setNodes, externalOnAddNode, externalOnNodesChange, toast, reactFlowInstanceRef]
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
        (n) => n.type === (node.data?.type as NodeTypeEnum)
      )
      return nodeDef?.color || "#64748b"
    },
    []
  )

  // Store ReactFlow instance when initialized
  const onInit = useCallback((reactFlowInstance: any) => {
    reactFlowInstanceRef.current = reactFlowInstance
  }, [])

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
        onInit={onInit}
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


