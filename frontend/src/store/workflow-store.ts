import { create } from 'zustand'
import type { Node, Edge } from 'reactflow'
import type { WorkflowDefinition, WorkflowNodeType } from '@/types/workflow'
import { normalizeWorkflowNode, denormalizeWorkflowNode } from '@/utils/node-type-utils'
import { createUndoRedo } from '@/utils/undo-redo'

type WorkflowStatus = "draft" | "active" | "inactive" | "paused" | "archived"

interface WorkflowSnapshot {
  nodes: Node[]
  edges: Edge[]
  workflowName: string
  workflowDescription: string
}

interface WorkflowState {
  nodes: Node[]
  edges: Edge[]
  selectedNodeId: string | null
  workflowName: string
  workflowDescription: string
  workflowStatus: WorkflowStatus
  isDirty: boolean
  undoRedo: ReturnType<typeof createUndoRedo<WorkflowSnapshot>> | null
  setNodes: (nodes: Node[] | ((prevNodes: Node[]) => Node[])) => void
  setEdges: (edges: Edge[] | ((prevEdges: Edge[]) => Edge[])) => void
  addNode: (node: Node) => void
  updateNode: (nodeId: string, data: Partial<Node['data']>) => void
  deleteNode: (nodeId: string) => void
  addEdge: (edge: Edge) => void
  deleteEdge: (edgeId: string) => void
  setSelectedNodeId: (nodeId: string | null) => void
  setWorkflowName: (name: string) => void
  setWorkflowDescription: (description: string) => void
  setWorkflowStatus: (status: WorkflowStatus) => void
  loadWorkflow: (workflow: WorkflowDefinition, status?: WorkflowStatus) => void
  getWorkflowDefinition: () => WorkflowDefinition
  resetWorkflow: () => void
  markDirty: () => void
  markClean: () => void
  undo: () => void
  redo: () => void
  canUndo: () => boolean
  canRedo: () => boolean
  saveSnapshot: () => void
}

export const useWorkflowStore = create<WorkflowState>((set, get) => {
  const initialSnapshot: WorkflowSnapshot = {
    nodes: [],
    edges: [],
    workflowName: '',
    workflowDescription: '',
  }

  const undoRedo = createUndoRedo<WorkflowSnapshot>(initialSnapshot, 50)

  return {
    nodes: [],
    edges: [],
    selectedNodeId: null,
    workflowName: '',
    workflowDescription: '',
    workflowStatus: 'draft',
    isDirty: false,
    undoRedo,

  setNodes: (nodes) => {
    // Ensure nodes is always an array
    const nodesArray = Array.isArray(nodes) ? nodes : (typeof nodes === 'function' ? nodes(get().nodes) : [])
    const finalNodes = Array.isArray(nodesArray) ? nodesArray : []
    const state = get()
    undoRedo.setState({
      nodes: finalNodes,
      edges: state.edges,
      workflowName: state.workflowName,
      workflowDescription: state.workflowDescription,
    })
    set({ nodes: finalNodes, isDirty: true })
  },
  setEdges: (edges) => {
    // Ensure edges is always an array
    const edgesArray = Array.isArray(edges) ? edges : (typeof edges === 'function' ? edges(get().edges) : [])
    const finalEdges = Array.isArray(edgesArray) ? edgesArray : []
    const state = get()
    undoRedo.setState({
      nodes: state.nodes,
      edges: finalEdges,
      workflowName: state.workflowName,
      workflowDescription: state.workflowDescription,
    })
    set({ edges: finalEdges, isDirty: true })
  },

  addNode: (node) => {
    const state = get()
    undoRedo.setState({
      nodes: [...state.nodes, node],
      edges: state.edges,
      workflowName: state.workflowName,
      workflowDescription: state.workflowDescription,
    })
    set((state) => ({
      nodes: [...state.nodes, node],
      isDirty: true,
    }))
  },

  updateNode: (nodeId, data) =>
    set((state) => ({
      nodes: state.nodes.map((node) =>
        node.id === nodeId
          ? { ...node, data: { ...node.data, ...data } }
          : node
      ),
      isDirty: true,
    })),

  deleteNode: (nodeId) => {
    const state = get()
    undoRedo.setState({
      nodes: state.nodes.filter((node) => node.id !== nodeId),
      edges: state.edges.filter(
        (edge) => edge.source !== nodeId && edge.target !== nodeId
      ),
      workflowName: state.workflowName,
      workflowDescription: state.workflowDescription,
    })
    set((state) => ({
      nodes: state.nodes.filter((node) => node.id !== nodeId),
      edges: state.edges.filter(
        (edge) => edge.source !== nodeId && edge.target !== nodeId
      ),
      selectedNodeId:
        state.selectedNodeId === nodeId ? null : state.selectedNodeId,
      isDirty: true,
    }))
  },

  addEdge: (edge) => {
    const state = get()
    undoRedo.setState({
      nodes: state.nodes,
      edges: [...state.edges, edge],
      workflowName: state.workflowName,
      workflowDescription: state.workflowDescription,
    })
    set((state) => ({
      edges: [...state.edges, edge],
      isDirty: true,
    }))
  },

  deleteEdge: (edgeId) => {
    const state = get()
    undoRedo.setState({
      nodes: state.nodes,
      edges: state.edges.filter((edge) => edge.id !== edgeId),
      workflowName: state.workflowName,
      workflowDescription: state.workflowDescription,
    })
    set((state) => ({
      edges: state.edges.filter((edge) => edge.id !== edgeId),
      isDirty: true,
    }))
  },

  setSelectedNodeId: (nodeId) => set({ selectedNodeId: nodeId }),

  setWorkflowName: (name) => {
    const state = get()
    undoRedo.setState({
      nodes: state.nodes,
      edges: state.edges,
      workflowName: name,
      workflowDescription: state.workflowDescription,
    })
    set({ workflowName: name, isDirty: true })
  },
  setWorkflowDescription: (description) => {
    const state = get()
    undoRedo.setState({
      nodes: state.nodes,
      edges: state.edges,
      workflowName: state.workflowName,
      workflowDescription: description,
    })
    set({ workflowDescription: description, isDirty: true })
  },
  setWorkflowStatus: (status) => set({ workflowStatus: status, isDirty: true }),

  loadWorkflow: (workflow, status) => {
    // Denormalize workflow nodes from backend format (enum + subtype) to frontend format (specific type)
    // Backend sends: { type: "ACTION", data: { config: { subtype: "send_webhook" } } }
    // Frontend needs: { type: "send-webhook", data: { config: {...} } }
    const denormalizedNodes = workflow.nodes.map(denormalizeWorkflowNode)
    
    // Convert workflow definition to react-flow format
    const nodes: Node[] = denormalizedNodes.map((node) => ({
      id: node.id,
      type: 'workflow',
      position: node.position,
      data: {
        label: node.data.label,
        type: node.type,
        config: node.data.config,
      },
    }))

    const edges: Edge[] = workflow.edges.map((edge) => ({
      id: edge.id,
      source: edge.source,
      target: edge.target,
      sourceHandle: edge.sourceHandle,
      targetHandle: edge.targetHandle,
    }))

    const currentState = get()
    // Always use workflow.name if provided, otherwise keep current name, otherwise empty
    // Prioritize workflow.name from the definition
    const newWorkflowName = workflow.name || currentState.workflowName || ''

    set({
      nodes,
      edges,
      workflowName: newWorkflowName,
      workflowDescription: workflow.description || currentState.workflowDescription || '',
      workflowStatus: status || currentState.workflowStatus || 'draft',
      isDirty: false,
    })
  },

  getWorkflowDefinition: () => {
    const state = get()
    const definition: WorkflowDefinition = {
      name: state.workflowName,
      description: state.workflowDescription,
      nodes: state.nodes.map((node) => {
        const nodeData = node.data as { type: WorkflowNodeType; label: string; config?: Record<string, unknown> }
        return {
          id: node.id,
          type: nodeData.type,
          position: node.position,
          data: {
            label: nodeData.label,
            config: nodeData.config,
          },
        }
      }),
      edges: state.edges.map((edge) => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        sourceHandle: edge.sourceHandle || undefined,
        targetHandle: edge.targetHandle || undefined,
      })),
    }
    
    // Normalize nodes to ensure compatibility with backend enum
    return {
      ...definition,
      nodes: definition.nodes.map(normalizeWorkflowNode),
    }
  },

  resetWorkflow: () => set({
    nodes: [],
    edges: [],
    selectedNodeId: null,
    workflowName: '',
    workflowDescription: '',
    workflowStatus: 'draft',
    isDirty: false,
  }),

  markDirty: () => set({ isDirty: true }),
  markClean: () => set({ isDirty: false }),

  undo: () => {
    const snapshot = undoRedo.actions.undo()
    if (snapshot) {
      set({
        nodes: snapshot.nodes,
        edges: snapshot.edges,
        workflowName: snapshot.workflowName,
        workflowDescription: snapshot.workflowDescription,
        isDirty: true,
      })
    }
  },

  redo: () => {
    const snapshot = undoRedo.actions.redo()
    if (snapshot) {
      set({
        nodes: snapshot.nodes,
        edges: snapshot.edges,
        workflowName: snapshot.workflowName,
        workflowDescription: snapshot.workflowDescription,
        isDirty: true,
      })
    }
  },

  canUndo: () => undoRedo.actions.canUndo(),
  canRedo: () => undoRedo.actions.canRedo(),

  saveSnapshot: () => {
    const state = get()
    undoRedo.setState({
      nodes: state.nodes,
      edges: state.edges,
      workflowName: state.workflowName,
      workflowDescription: state.workflowDescription,
    })
  },
  }
})

