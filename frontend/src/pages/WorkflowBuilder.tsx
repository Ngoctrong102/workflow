import { useState, useCallback, useEffect, useMemo, useRef } from "react"
import { useParams, useNavigate, useLocation, useSearchParams } from "react-router-dom"
import { WorkflowCanvas } from "@/components/workflow/WorkflowCanvas"
import { NodePalette } from "@/components/workflow/NodePalette"
import { PropertiesPanel } from "@/components/workflow/PropertiesPanel"
import { ValidationErrors } from "@/components/workflow/ValidationErrors"
import { PreviewMode } from "@/components/workflow/PreviewMode"
import { TestExecution } from "@/components/workflow/TestExecution"
import { FieldReferenceProvider } from "@/providers/FieldReferenceContext"
import { useObjectTypes } from "@/hooks/use-object-types"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Textarea } from "@/components/ui/textarea"
import { useToast } from "@/hooks/use-toast"
import { cn } from "@/lib/utils"
import type { Node, Edge, Connection, NodeChange, EdgeChange } from "reactflow"
import { applyNodeChanges, applyEdgeChanges } from "reactflow"
import { useWorkflowStore } from "@/store/workflow-store"
import { useUIStore } from "@/store/ui-store"
import { validateConnection, validateWorkflow, validateWorkflowWithFieldReferences } from "@/utils/workflow-validation"
import { normalizeWorkflowDefinition, validateWorkflowDefinition } from "@/utils/node-type-utils"
import { useCreateWorkflow, useUpdateWorkflow, useWorkflow, useExecuteWorkflow, useWorkflowVersions, useWorkflowByVersion, useRollbackWorkflow } from "@/hooks/use-workflows"
import { copyNodes, pasteNodes, storeCopiedNodes, getCopiedNodes } from "@/utils/node-copy-paste"
import { createGroup } from "@/utils/node-grouping"
import type { WorkflowDefinition, WorkflowNodeType } from "@/types/workflow"
import type { NodeGroup } from "@/utils/node-grouping"
import { Eye, Play, CheckCircle2, ChevronLeft, ChevronRight, X, AlertCircle, History, Clock, BarChart3, Undo2, Redo2, Copy, Clipboard, Group, Save } from "lucide-react"

type ViewMode = "build" | "preview" | "test"

export default function WorkflowBuilder() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()
  const isEditMode = id !== "new" && id !== undefined

  useUIStore() // Keep for potential future use
  const {
    nodes: nodesFromStore,
    edges: edgesFromStore,
    selectedNodeId,
    workflowName,
    workflowDescription,
    workflowStatus,
    isDirty,
    setNodes,
    setEdges,
    updateNode,
    setSelectedNodeId,
    setWorkflowName,
    setWorkflowDescription,
    setWorkflowStatus,
    markClean,
    loadWorkflow,
    resetWorkflow,
    undo,
    redo,
    canUndo,
    canRedo,
    saveSnapshot,
  } = useWorkflowStore()

  // Ensure nodes and edges are always arrays
  const nodes = Array.isArray(nodesFromStore) ? nodesFromStore : []
  const edges = Array.isArray(edgesFromStore) ? edgesFromStore : []

  const { toast } = useToast()
  const [selectedNode, setSelectedNode] = useState<Node | null>(null)
  const [viewMode, setViewMode] = useState<ViewMode>("build")
  const [showTestDialog, setShowTestDialog] = useState(false)
  // Local state for workflow name to ensure it's always editable
  // Initialize with workflowName from store if available, or empty string
  const [localWorkflowName, setLocalWorkflowName] = useState<string>("")
  const [validationErrors, setValidationErrors] = useState<
    ReturnType<typeof validateWorkflow>["errors"]
  >([])
  const [showNodePalette, setShowNodePalette] = useState(false)
  const [showPropertiesPanel, setShowPropertiesPanel] = useState(false)
  const [showWorkflowDetails, setShowWorkflowDetails] = useState(true)
  const [showValidationErrors, setShowValidationErrors] = useState(false) // Don't show validation errors by default
  const [selectedVersion, setSelectedVersion] = useState<number | null>(null)
  const [showVersionHistory, setShowVersionHistory] = useState(false)
  const [copiedNodes, setCopiedNodes] = useState<ReturnType<typeof copyNodes> | null>(null)
  const [nodeGroups, setNodeGroups] = useState<NodeGroup[]>([])
  const [showSaveTemplateDialog, setShowSaveTemplateDialog] = useState(false)
  const [templateName, setTemplateName] = useState("")
  const [templateDescription, setTemplateDescription] = useState("")
  
  // Track previous id to detect when switching to new workflow
  const previousIdRef = useRef<string | undefined>(id)

  // Load workflow if editing
  const { data: workflow, isLoading: isLoadingWorkflow } = useWorkflow(id)
  const { data: versions, isLoading: isLoadingVersions, error: versionsError } = useWorkflowVersions(id)
  const { data: versionWorkflow, isLoading: isLoadingVersion, error: versionError } = useWorkflowByVersion(id, selectedVersion || undefined)
  const createWorkflow = useCreateWorkflow()
  const updateWorkflow = useUpdateWorkflow()
  const executeWorkflow = useExecuteWorkflow()
  const rollbackWorkflow = useRollbackWorkflow()

  // Reset workflow state when creating a new workflow (only when id changes to "new")
  useEffect(() => {
    // Only reset when id changes from a different value to "new"
    const idChangedToNew = previousIdRef.current !== "new" && id === "new"
    
    if (idChangedToNew && !isEditMode) {
      // Check if there's workflow data from location state or query params
      const workflowFromState = (location.state as { workflow?: Partial<WorkflowDefinition> })?.workflow
      const templateId = searchParams.get("template")
      const nameFromQuery = searchParams.get("name")
      const descriptionFromQuery = searchParams.get("description")

      // Only reset if there's no initial data to load
      if (!workflowFromState && !templateId) {
        // Reset everything to initial state
        resetWorkflow()
        setLocalWorkflowName("")
      } else if (nameFromQuery && !workflowFromState) {
        // If there's name from query but no workflow state, set name/description but reset nodes/edges
        resetWorkflow()
        setWorkflowName(nameFromQuery)
        setLocalWorkflowName(nameFromQuery)
        if (descriptionFromQuery) {
          setWorkflowDescription(descriptionFromQuery)
        }
      }
      // If workflowFromState exists, it will be handled by the workflow loading logic below
    }
    
    // Update previous id ref
    previousIdRef.current = id
  }, [id, isEditMode, location.state, searchParams, resetWorkflow, setWorkflowName, setWorkflowDescription])

  // Sync localWorkflowName with workflowName from store or workflow.name from API
  // This ensures the input always shows the current workflow name
  useEffect(() => {
    // Priority: workflow.name from API > workflowName from store
    const nameToSync = workflow?.name || workflowName || ""
    // Always sync if different (including when localWorkflowName is empty and nameToSync has value)
    if (nameToSync !== localWorkflowName) {
      setLocalWorkflowName(nameToSync)
    }
  }, [workflow?.name, workflowName])

  // Load object types for field reference context
  const { data: objectTypesData } = useObjectTypes({ limit: 1000 })
  const objectTypesForContext = useMemo(() => {
    if (!objectTypesData?.data) return []
    return objectTypesData.data.map((ot) => ({
      id: ot.id,
      name: ot.name,
      fields: ot.fields || [],
    }))
  }, [objectTypesData])

  // Load workflow data when editing
  useEffect(() => {
    if (selectedVersion && versionWorkflow && isEditMode && !isLoadingVersion) {
      // Load specific version
      const nameToSet = versionWorkflow.name || ""
      setWorkflowName(nameToSet)
      setLocalWorkflowName(nameToSet)
      setWorkflowDescription(versionWorkflow.description || "")
      if (versionWorkflow.definition) {
        // Ensure definition has name before loading
        const definitionWithName = {
          ...versionWorkflow.definition,
          name: versionWorkflow.definition.name || nameToSet,
        }
        loadWorkflow(definitionWithName, versionWorkflow.status as "draft" | "active" | "inactive" | "paused" | "archived")
      } else {
        setWorkflowStatus(versionWorkflow.status as "draft" | "active" | "inactive" | "paused" | "archived")
      }
    } else if (workflow && isEditMode && !selectedVersion) {
      // Load current version
      const nameToSet = workflow.name || ""
      setWorkflowName(nameToSet)
      setLocalWorkflowName(nameToSet)
      setWorkflowDescription(workflow.description || "")
      if (workflow.definition) {
        // Ensure definition has name before loading
        const definitionWithName = {
          ...workflow.definition,
          name: workflow.definition.name || nameToSet,
        }
        // Pass name explicitly to loadWorkflow to ensure it's set correctly
        const definitionWithNameAndExplicitName = {
          ...definitionWithName,
          name: nameToSet, // Ensure name is always set from API response
        }
        loadWorkflow(definitionWithNameAndExplicitName, workflow.status as "draft" | "active" | "inactive" | "paused" | "archived")
        // Ensure localWorkflowName is set after loadWorkflow completes
        // This handles the case where loadWorkflow might update workflowName in store
        setTimeout(() => {
          if (nameToSet) {
            setLocalWorkflowName(nameToSet)
          }
        }, 0)
      } else {
        setWorkflowStatus(workflow.status as "draft" | "active" | "inactive" | "paused" | "archived")
      }
    }
  }, [workflow, versionWorkflow, selectedVersion, isEditMode, isLoadingVersion, setWorkflowName, setWorkflowDescription, setWorkflowStatus, loadWorkflow])

  // Ensure localWorkflowName is synced when workflow loads (fallback for edge cases)
  useEffect(() => {
    if (workflow && workflow.name && isEditMode && !selectedVersion) {
      // If workflowName in store doesn't match workflow.name, update it
      // This will trigger the sync useEffect above to update localWorkflowName
      if (workflowName !== workflow.name) {
        setWorkflowName(workflow.name)
      }
    }
  }, [workflow?.name, workflowName, isEditMode, selectedVersion, setWorkflowName])

  // Show error toast if version loading fails
  useEffect(() => {
    if (versionError && selectedVersion) {
      toast({
        variant: "destructive",
        title: "Failed to load version",
        description: versionError instanceof Error ? versionError.message : "Could not load workflow version",
      })
      // Reset to current version on error
      setSelectedVersion(null)
    }
  }, [versionError, selectedVersion, toast])

  // Sync selected node when nodes or selectedNodeId changes
  useEffect(() => {
    // Ensure nodes is an array before calling find
    if (!Array.isArray(nodes)) {
      return
    }
    
    const node = nodes.find((n) => n.id === selectedNodeId) || null
    setSelectedNode(node)
  }, [nodes, selectedNodeId])

  // Manage properties panel visibility - automatically open when node is selected, close when deselected
  useEffect(() => {
    // Automatically open panel when a node is selected
    if (selectedNodeId !== null) {
      setShowPropertiesPanel(true)
    } else {
      // Automatically close panel when no node is selected
      setShowPropertiesPanel(false)
    }
  }, [selectedNodeId])


  // Highlight invalid nodes (only when validation has been run)
  const nodesWithValidation = useMemo(() => {
    // Ensure nodes is an array
    if (!Array.isArray(nodes)) {
      return []
    }

    // Only highlight invalid nodes if validation errors exist (validation has been run)
    if (validationErrors.length === 0) {
      return nodes
    }

    const invalidNodeIds = new Set(
      validationErrors.filter((e) => e.nodeId).map((e) => e.nodeId!)
    )

    return nodes.map((node) => ({
      ...node,
      data: {
        ...node.data,
        invalid: invalidNodeIds.has(node.id),
      },
    }))
  }, [nodes, validationErrors])

  const handleNodesChange = useCallback(
    (changes: NodeChange[]) => {
      // Ensure nodes is an array before applying changes
      if (!Array.isArray(nodes)) {
        return
      }
      
      const updatedNodes = applyNodeChanges(changes, nodes)
      setNodes(updatedNodes)

      // Handle node selection - process all changes together to avoid toggle
      // First, find if any node is being selected
      let newlySelectedNodeId: string | null = null
      let isDeselectingCurrentNode = false
      
      changes.forEach((change) => {
        if (change.type === "select") {
          if (change.selected) {
            // A node is being selected - remember it
            newlySelectedNodeId = change.id
          } else if (change.id === selectedNodeId) {
            // The currently selected node is being deselected
            isDeselectingCurrentNode = true
          }
        }
      })
      
      // Determine the final selectedNodeId after all changes
      // Priority: newly selected node > current node (if not deselected) > null
      let finalSelectedNodeId: string | null = selectedNodeId
      
      if (newlySelectedNodeId !== null) {
        // A new node is being selected - use it
        finalSelectedNodeId = newlySelectedNodeId
      } else if (isDeselectingCurrentNode) {
        // Current node is being deselected and no new node is selected
        finalSelectedNodeId = null
      }
      // Otherwise, keep the current selectedNodeId
      
      // Only update selectedNodeId once after processing all changes
      // This prevents toggle when switching between nodes
      if (finalSelectedNodeId !== selectedNodeId) {
        setSelectedNodeId(finalSelectedNodeId)
      }
    },
    [nodes, setNodes, setSelectedNodeId, selectedNodeId]
  )

  const handleAddNode = useCallback(
    (newNode: Node) => {
      setNodes((nds) => {
        // Ensure nds is an array before concatenating
        if (!Array.isArray(nds)) {
          return [newNode]
        }
        return nds.concat(newNode)
      })
      saveSnapshot()
    },
    [setNodes, saveSnapshot]
  )

  // Undo/Redo handlers
  const handleUndo = useCallback(() => {
    undo()
  }, [undo])

  const handleRedo = useCallback(() => {
    redo()
  }, [redo])

  // Copy/Paste handlers
  const handleCopy = useCallback(() => {
    if (!selectedNodeId) {
      toast({
        variant: "destructive",
        title: "No Selection",
        description: "Please select a node to copy",
      })
      return
    }
    const copied = copyNodes(nodes, edges, [selectedNodeId])
    if (copied) {
      setCopiedNodes(copied)
      storeCopiedNodes(copied)
      toast({
        title: "Copied",
        description: "Node copied to clipboard",
      })
    }
  }, [nodes, edges, selectedNodeId, toast])

  const handlePaste = useCallback(() => {
    const copied = copiedNodes || getCopiedNodes()
    if (!copied) {
      toast({
        variant: "destructive",
        title: "Nothing to Paste",
        description: "No nodes in clipboard",
      })
      return
    }
    const pasted = pasteNodes(copied, { x: 50, y: 50 })
    setNodes([...nodes, ...pasted.nodes])
    setEdges([...edges, ...pasted.edges])
    saveSnapshot()
    toast({
      title: "Pasted",
      description: `${pasted.nodes.length} node(s) pasted`,
    })
  }, [copiedNodes, nodes, edges, setNodes, setEdges, saveSnapshot, toast])

  // Group handlers
  const handleCreateGroup = useCallback(() => {
    if (!selectedNodeId) {
      toast({
        variant: "destructive",
        title: "No Selection",
        description: "Please select nodes to group",
      })
      return
    }
    const group = createGroup([selectedNodeId], nodes)
    if (group) {
      setNodeGroups([...nodeGroups, group])
      toast({
        title: "Group Created",
        description: "Nodes grouped successfully",
      })
    }
  }, [nodes, selectedNodeId, nodeGroups, toast])

  // Save as template handler
  const handleSaveAsTemplate = useCallback(async () => {
    if (!templateName.trim()) {
      toast({
        variant: "destructive",
        title: "Template Name Required",
        description: "Please enter a template name",
      })
      return
    }

    try {
      const workflowDef = {
        name: workflowName || templateName,
        description: templateDescription || workflowDescription,
        nodes: nodes.map((node) => {
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
        edges: edges.map((edge) => ({
          id: edge.id,
          source: edge.source,
          target: edge.target,
          sourceHandle: edge.sourceHandle || undefined,
          targetHandle: edge.targetHandle || undefined,
        })),
      }

      // Store template in localStorage (temporary solution until API is available)
      const template = {
        id: `template-${Date.now()}`,
        name: templateName,
        description: templateDescription,
        workflow: workflowDef,
        createdAt: new Date().toISOString(),
      }

      const existingTemplates = JSON.parse(localStorage.getItem("workflow-templates") || "[]")
      existingTemplates.push(template)
      localStorage.setItem("workflow-templates", JSON.stringify(existingTemplates))
      
      toast({
        title: "Template Saved",
        description: "Workflow saved as template successfully",
      })
      setShowSaveTemplateDialog(false)
      setTemplateName("")
      setTemplateDescription("")
    } catch (error) {
      toast({
        variant: "destructive",
        title: "Save Failed",
        description: error instanceof Error ? error.message : "Failed to save template",
      })
    }
  }, [templateName, templateDescription, workflowName, workflowDescription, nodes, edges, toast])

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Ctrl+Z or Cmd+Z for undo
      if ((e.ctrlKey || e.metaKey) && e.key === "z" && !e.shiftKey) {
        e.preventDefault()
        if (canUndo()) {
          handleUndo()
        }
      }
      // Ctrl+Shift+Z or Cmd+Shift+Z for redo
      if ((e.ctrlKey || e.metaKey) && e.key === "z" && e.shiftKey) {
        e.preventDefault()
        if (canRedo()) {
          handleRedo()
        }
      }
      // Ctrl+Y for redo (alternative)
      if ((e.ctrlKey || e.metaKey) && e.key === "y") {
        e.preventDefault()
        if (canRedo()) {
          handleRedo()
        }
      }
      // Ctrl+C or Cmd+C for copy
      if ((e.ctrlKey || e.metaKey) && e.key === "c" && selectedNodeId) {
        e.preventDefault()
        handleCopy()
      }
      // Ctrl+V or Cmd+V for paste
      if ((e.ctrlKey || e.metaKey) && e.key === "v") {
        e.preventDefault()
        handlePaste()
      }
    }

    window.addEventListener("keydown", handleKeyDown)
    return () => window.removeEventListener("keydown", handleKeyDown)
  }, [canUndo, canRedo, selectedNodeId, handleUndo, handleRedo, handleCopy, handlePaste])

  const handleEdgesChange = useCallback(
    (changes: EdgeChange[]) => {
      const updatedEdges = applyEdgeChanges(changes, edges)
      setEdges(updatedEdges)
      saveSnapshot()
    },
    [edges, setEdges, saveSnapshot]
  )

  const handleConnect = useCallback(
    (connection: Connection) => {
      if (!connection.source || !connection.target) {
        return
      }

      // Ensure nodes is an array before finding
      if (!Array.isArray(nodes)) {
        return
      }

      const sourceNode = nodes.find((n) => n.id === connection.source)
      const targetNode = nodes.find((n) => n.id === connection.target)

      if (!sourceNode || !targetNode) {
        return
      }

      // Validate connection
      const validation = validateConnection(sourceNode, targetNode, edges)
      if (!validation.isValid) {
        toast({
          variant: "destructive",
          title: "Invalid Connection",
          description: validation.message || "Cannot create this connection",
        })
        return
      }

      const newEdge: Edge = {
        id: `${connection.source}-${connection.target}-${Date.now()}`,
        source: connection.source,
        target: connection.target,
        sourceHandle: connection.sourceHandle,
        targetHandle: connection.targetHandle,
      }

      setEdges([...edges, newEdge])
      saveSnapshot()
    },
    [nodes, edges, setEdges, saveSnapshot]
  )

  const handleNodeSave = useCallback(
    (nodeId: string, config: Record<string, unknown>) => {
      updateNode(nodeId, { config, label: config.label as string })
    },
    [updateNode]
  )

  const handleNodeCancel = useCallback(() => {
    setSelectedNodeId(null)
  }, [setSelectedNodeId])

  const handleValidate = useCallback(() => {
    // Ensure nodes and edges are arrays
    if (!Array.isArray(nodes) || !Array.isArray(edges)) {
      toast({
        variant: "destructive",
        title: "Validation Error",
        description: "Invalid workflow state",
      })
      return
    }

    // Use field reference validation if object types are available
    let validation
    if (objectTypesForContext.length > 0) {
      const objectTypesMap = new Map()
      objectTypesForContext.forEach((ot) => {
        objectTypesMap.set(ot.id, {
          name: ot.name,
          fields: ot.fields || [],
        })
      })
      
      validation = validateWorkflowWithFieldReferences(nodes, edges, {
        objectTypes: objectTypesMap,
        validateTypes: true,
        allowOldFormat: true,
      })
    } else {
      validation = validateWorkflow(nodes, edges)
    }
    
    setValidationErrors(validation.errors)
    setShowValidationErrors(true) // Show validation errors panel when validate is clicked
    
    const errorCount = validation.errors.filter((e) => e.type === "error").length
    const warningCount = validation.errors.filter((e) => e.type === "warning").length

    if (errorCount === 0 && warningCount === 0) {
      toast({
        title: "Validation Successful",
        description: "Workflow is valid and ready to save",
      })
    } else if (errorCount > 0) {
      const errorMessages = validation.errors
        .filter((e) => e.type === "error")
        .map((e) => e.message)
        .join(", ")

      toast({
        variant: "destructive",
        title: "Validation Failed",
        description: errorMessages || "Workflow has validation errors",
      })
    } else {
      toast({
        variant: "default",
        title: "Validation Warnings",
        description: `Workflow has ${warningCount} warning(s) but is valid`,
      })
    }
  }, [nodes, edges, objectTypesForContext, toast])

  const handleSave = useCallback(async () => {
    // Ensure nodes and edges are arrays
    if (!Array.isArray(nodes) || !Array.isArray(edges)) {
      toast({
        variant: "destructive",
        title: "Cannot Save",
        description: "Invalid workflow state",
      })
      return
    }

    // Validate workflow with field references if object types are available
    let validation
    if (objectTypesForContext.length > 0) {
      // Convert object types to Map format for validation
      const objectTypesMap = new Map()
      objectTypesForContext.forEach((ot) => {
        objectTypesMap.set(ot.id, {
          name: ot.name,
          fields: ot.fields || [],
        })
      })
      
      validation = validateWorkflowWithFieldReferences(nodes, edges, {
        objectTypes: objectTypesMap,
        validateTypes: true,
        allowOldFormat: true, // Allow old format during transition
      })
    } else {
      // Fallback to basic validation if no object types available
      validation = validateWorkflow(nodes, edges)
    }
    
    setValidationErrors(validation.errors)
    
    // Only block save on errors, not warnings
    const hasErrors = validation.errors.some((e) => e.type === "error")
    if (hasErrors) {
      toast({
        variant: "destructive",
        title: "Cannot Save",
        description: "Please fix validation errors before saving",
      })
      setShowValidationErrors(true) // Show validation errors when save fails
      return
    }
    
    // Show warnings but allow save
    const hasWarnings = validation.errors.some((e) => e.type === "warning")
    if (hasWarnings) {
      toast({
        variant: "default",
        title: "Validation Warnings",
        description: "Workflow has validation warnings but can still be saved",
      })
    }

    const workflowDefinition: WorkflowDefinition = {
      name: workflowName,
      description: workflowDescription,
      nodes: nodes.map((node) => {
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
      edges: edges.map((edge) => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        sourceHandle: edge.sourceHandle || undefined,
        targetHandle: edge.targetHandle || undefined,
      })),
    }

    // Validate workflow definition node types BEFORE normalizing (validate frontend format)
    const nodeTypeValidation = validateWorkflowDefinition(workflowDefinition)
    if (!nodeTypeValidation.isValid) {
      console.error("Workflow definition validation failed:", nodeTypeValidation.errors)
      toast({
        variant: "destructive",
        title: "Validation Error",
        description: `Invalid node types found: ${nodeTypeValidation.errors.join(", ")}`,
      })
      return
    }

    // Normalize workflow definition to ensure compatibility with backend enum
    const normalizedDefinition = normalizeWorkflowDefinition(workflowDefinition)

    try {
      if (isEditMode && id) {
        await updateWorkflow.mutateAsync({
          id,
          name: workflowName,
          description: workflowDescription,
          definition: normalizedDefinition,
          status: workflowStatus,
        })
      } else {
        const created = await createWorkflow.mutateAsync({
          name: workflowName,
          description: workflowDescription,
          definition: normalizedDefinition,
          status: workflowStatus,
        })
        // Navigate to the workflow detail page (not /edit since route is just /workflows/:id)
        if (created?.id) {
          navigate(`/workflows/${created.id}`)
        } else {
          // Fallback: navigate to workflows list if ID is missing
          navigate("/workflows")
        }
      }
      markClean()
    } catch (error) {
      // Error handling is done in the mutation hooks
      console.error("Failed to save workflow:", error)
    }
  }, [
    nodes,
    edges,
    workflowName,
    workflowDescription,
    workflowStatus,
    isEditMode,
    id,
    createWorkflow,
    updateWorkflow,
    markClean,
    navigate,
    toast,
  ])

  const handleNodeClick = useCallback(
    (nodeId: string) => {
      setSelectedNodeId(nodeId)
      // Scroll to node in canvas (would need canvas ref for actual scrolling)
    },
    [setSelectedNodeId]
  )

  const handleTestExecute = useCallback(
    async (testData: Record<string, unknown>) => {
      if (!id || id === "new") {
        toast({
          variant: "destructive",
          title: "Cannot Execute",
          description: "Please save the workflow first before executing",
        })
        return {
          executionId: "",
          status: "failed" as const,
          nodeExecutions: [],
          error: "Workflow must be saved before execution",
        }
      }

      try {
        const result = await executeWorkflow.mutateAsync({
          id,
          data: { data: testData },
        })
        return result
      } catch (error) {
        return {
          executionId: "",
          status: "failed" as const,
          nodeExecutions: [],
          error: error instanceof Error ? error.message : "Execution failed",
        }
      }
    },
    [id, executeWorkflow, toast]
  )

  // Only validate when explicitly requested (save, verify, etc.)
  // Don't validate automatically on every change

  if (isLoadingWorkflow && isEditMode) {
      return (
        <div className="flex items-center justify-center h-full w-full">
          <div className="text-secondary-500">Loading workflow...</div>
        </div>
      )
  }

  return (
    <FieldReferenceProvider initialObjectTypes={objectTypesForContext}>
      <div className="h-full w-full">
      {/* Compact Workflow Header - Floating Top Right, Responsive, Adjusts when Properties Panel is open */}
      {showWorkflowDetails && (
        <div 
          className={cn(
            "fixed top-16 sm:top-20 z-[60] max-w-full sm:max-w-md w-[calc(100%-1rem)] sm:w-auto transition-all duration-300 ease-in-out",
            showPropertiesPanel 
              ? "right-[calc(18rem+0.5rem)] md:right-[calc(20rem+0.5rem)]" 
              : "right-2 sm:right-4"
          )}
        >
          <Card className="p-2 shadow-lg">
            <div className="flex items-center gap-1 sm:gap-2 flex-wrap sm:flex-nowrap">
              <div className="flex-1 min-w-0 w-full sm:w-auto">
                <Input
                  value={localWorkflowName}
                  onChange={(e) => {
                    const newName = e.target.value
                    setLocalWorkflowName(newName)
                    setWorkflowName(newName)
                  }}
                  placeholder="Workflow name"
                  className="h-8 text-sm font-medium"
                />
              </div>
              <Select value={workflowStatus} onValueChange={(value) => setWorkflowStatus(value as "draft" | "active" | "inactive" | "paused" | "archived")}>
                <SelectTrigger className="w-24 sm:w-28 h-8 text-xs">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="draft">Draft</SelectItem>
                  <SelectItem value="active">Active</SelectItem>
                  <SelectItem value="inactive">Inactive</SelectItem>
                  <SelectItem value="paused">Paused</SelectItem>
                </SelectContent>
              </Select>
              {isEditMode && workflow && (
                <div className="relative">
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-8 w-8 p-0"
                    onClick={() => setShowVersionHistory(!showVersionHistory)}
                    title="View version history"
                  >
                    <History className="h-4 w-4" />
                  </Button>
                  {showVersionHistory && (
                    <div className="absolute top-10 right-0 z-[70] bg-white border border-secondary-200 rounded-lg shadow-xl min-w-[200px] max-h-[300px] overflow-y-auto">
                      <div className="p-2 border-b border-secondary-200">
                        <div className="text-xs font-semibold text-secondary-700">Version History</div>
                        <div className="text-xs text-secondary-500">Select a version to view</div>
                      </div>
                      <div className="p-1">
                        {isLoadingVersions ? (
                          <div className="text-center py-4 text-xs text-secondary-500">Loading versions...</div>
                        ) : versions && versions.length > 0 ? (
                          <>
                            {versions
                              .sort((a, b) => (b.version || 0) - (a.version || 0))
                              .map((v) => (
                                <button
                                  key={v.version}
                                  onClick={() => {
                                    console.log('[WorkflowBuilder] Selecting version:', v.version)
                                    setSelectedVersion(v.version || null)
                                    setShowVersionHistory(false)
                                  }}
                                  className={cn(
                                    "w-full text-left px-2 py-1.5 rounded text-xs hover:bg-secondary-50 transition-colors",
                                    selectedVersion === v.version && "bg-primary-50 text-primary-700",
                                    !selectedVersion && v.version === workflow.version && "bg-primary-50 text-primary-700"
                                  )}
                                >
                                  <div className="flex items-center justify-between">
                                    <span className="font-medium">v{v.version}</span>
                                    {(!selectedVersion && v.version === workflow.version) || selectedVersion === v.version ? (
                                      <span className="text-xs text-primary-600">Current</span>
                                    ) : null}
                                  </div>
                                  <div className="text-xs text-secondary-500 mt-0.5">
                                    {new Date(v.updatedAt).toLocaleDateString()}
                                  </div>
                                </button>
                              ))}
                            {selectedVersion && selectedVersion !== workflow.version && (
                              <>
                                <button
                                  onClick={async () => {
                                    if (id && selectedVersion) {
                                      try {
                                        await rollbackWorkflow.mutateAsync({ id, version: selectedVersion })
                                        setSelectedVersion(null)
                                        setShowVersionHistory(false)
                                      } catch (error) {
                                        // Error handling is done in the mutation hook
                                      }
                                    }
                                  }}
                                  className="w-full text-left px-2 py-1.5 rounded text-xs hover:bg-secondary-50 transition-colors mt-1 border-t border-secondary-200 pt-1 text-primary-600 font-medium disabled:opacity-50"
                                  disabled={rollbackWorkflow.isPending}
                                >
                                  {rollbackWorkflow.isPending ? "Rolling back..." : "Rollback to this version"}
                                </button>
                                <button
                                  onClick={() => {
                                    setSelectedVersion(null)
                                    setShowVersionHistory(false)
                                  }}
                                  className="w-full text-left px-2 py-1.5 rounded text-xs hover:bg-secondary-50 transition-colors mt-1"
                                >
                                  <div className="flex items-center gap-1">
                                    <Clock className="h-3 w-3" />
                                    <span>Back to current (v{workflow.version})</span>
                                  </div>
                                </button>
                              </>
                            )}
                          </>
                        ) : (
                          <div className="text-center py-4 text-xs text-secondary-500">
                            {workflow ? (
                              <>
                                <div className="font-medium">Current version: v{workflow.version}</div>
                                {versionsError ? (
                                  <div className="mt-1 text-xs text-error-600">
                                    Error loading versions. API endpoint may not be implemented yet.
                                  </div>
                                ) : (
                                  <div className="mt-1 text-xs text-secondary-400">Version history not available</div>
                                )}
                              </>
                            ) : (
                              "No versions found"
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              )}
              <div className="flex items-center gap-1">
                {isEditMode && id && id !== "new" && (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-8 w-8 p-0"
                    onClick={() => navigate(`/workflows/${id}/dashboard`)}
                    title="View Dashboard"
                  >
                    <BarChart3 className="h-4 w-4" />
                  </Button>
                )}
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0"
                  onClick={() => setViewMode(viewMode === "preview" ? "build" : "preview")}
                  title="Preview"
                >
                  <Eye className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0"
                  onClick={() => setShowTestDialog(true)}
                  title="Test"
                >
                  <Play className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0 hidden sm:flex"
                  onClick={handleValidate}
                  title="Validate"
                >
                  <CheckCircle2 className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0 hidden sm:flex"
                  onClick={handleUndo}
                  disabled={!canUndo()}
                  title="Undo (Ctrl+Z)"
                >
                  <Undo2 className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0 hidden sm:flex"
                  onClick={handleRedo}
                  disabled={!canRedo()}
                  title="Redo (Ctrl+Y)"
                >
                  <Redo2 className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0 hidden sm:flex"
                  onClick={handleCopy}
                  disabled={!selectedNodeId}
                  title="Copy (Ctrl+C)"
                >
                  <Copy className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0 hidden sm:flex"
                  onClick={handlePaste}
                  title="Paste (Ctrl+V)"
                >
                  <Clipboard className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0 hidden sm:flex"
                  onClick={handleCreateGroup}
                  disabled={!selectedNodeId}
                  title="Group Selected Nodes"
                >
                  <Group className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0 hidden sm:flex"
                  onClick={() => setShowSaveTemplateDialog(true)}
                  title="Save as Template"
                >
                  <Save className="h-4 w-4" />
                </Button>
                    <Button
                      size="sm"
                      className="h-8 px-2 sm:px-3 text-xs sm:text-sm"
                      onClick={handleSave}
                      disabled={!isDirty || (selectedVersion !== null && selectedVersion !== workflow?.version)}
                      title={selectedVersion !== null && selectedVersion !== workflow?.version ? "Cannot save when viewing an old version" : undefined}
                    >
                      Save
                    </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 w-8 p-0"
                  onClick={() => setShowWorkflowDetails(false)}
                  title="Hide workflow details"
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* Toggle Workflow Details Button (When Hidden), Responsive, Adjusts when Properties Panel is open */}
      {!showWorkflowDetails && (
        <Button
          variant="default"
          size="sm"
          className={cn(
            "fixed top-16 sm:top-20 z-[60] h-8 w-8 p-0 shadow-lg cursor-pointer transition-all duration-300 ease-in-out",
            showPropertiesPanel 
              ? "right-[calc(18rem+0.5rem)] md:right-[calc(20rem+0.5rem)]" 
              : "right-2 sm:right-4"
          )}
          onClick={() => setShowWorkflowDetails(true)}
          title="Show workflow details"
        >
          <ChevronLeft className="h-4 w-4" />
        </Button>
      )}


      {/* Builder Layout */}
      {viewMode === "preview" ? (
        <div className="h-full w-full">
          <PreviewMode
            nodes={nodes}
            edges={edges}
            onClose={() => setViewMode("build")}
            onTest={() => {
              setViewMode("build")
              setShowTestDialog(true)
            }}
          />
        </div>
      ) : (
        <div className="relative h-full w-full overflow-hidden canvas-container">
          {/* Canvas - Always Full Width/Height, No Padding */}
          <div className="absolute inset-0 z-10" style={{ pointerEvents: 'auto' }}>
            <Card className="h-full border-0 shadow-none bg-white">
              <WorkflowCanvas
                nodes={nodesWithValidation}
                edges={edges}
                onNodesChange={handleNodesChange}
                onEdgesChange={handleEdgesChange}
                onConnect={handleConnect}
                onAddNode={handleAddNode}
              />
            </Card>
          </div>

          {/* Overlay when panels are open on mobile/tablet */}
          {(showNodePalette || showPropertiesPanel) && (
            <div
              className="absolute inset-0 bg-black/20 z-15 md:hidden"
              onClick={() => {
                setShowNodePalette(false)
                setShowPropertiesPanel(false)
              }}
            />
          )}

          {/* Node Palette - Inside Canvas, Responsive, Stick to left edge */}
          <div
            className={cn(
              "absolute left-0 top-0 bottom-0 z-20 bg-white border-r border-secondary-200 shadow-xl transition-transform duration-300 ease-in-out overflow-hidden flex flex-col",
              "w-full sm:w-72 md:w-80", // Responsive width
              showNodePalette ? "translate-x-0" : "-translate-x-full"
            )}
            style={{ 
              position: 'absolute', 
              left: 0, 
              top: 0, 
              bottom: 0,
              pointerEvents: showNodePalette ? 'auto' : 'none' 
            }}
            onDragOver={(e) => {
              // Allow drag over but don't prevent default - let it pass through to canvas
              if (!showNodePalette) {
                e.stopPropagation()
              }
            }}
            onDrop={(e) => {
              // Prevent drop on panel itself, let it pass through to canvas
              if (showNodePalette) {
                e.stopPropagation()
              }
            }}
          >
            <div className="h-full overflow-hidden">
              <NodePalette onClose={() => setShowNodePalette(false)} />
            </div>
            {/* Toggle Button on Panel Edge - Always visible on desktop */}
            <Button
              variant="ghost"
              size="sm"
              className="absolute -right-10 top-4 h-10 w-10 p-0 bg-white border border-secondary-200 shadow-md z-30 cursor-pointer hover:bg-secondary-50 hidden md:flex"
              onClick={() => setShowNodePalette(false)}
              title="Close panel"
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
          </div>

          {/* Toggle Button for Node Palette (When Hidden) - Inside Canvas, Stick to left edge */}
          {!showNodePalette && (
            <Button
              variant="default"
              size="sm"
              className="absolute left-2 top-2 z-30 h-8 w-8 sm:h-10 sm:w-10 p-0 shadow-lg cursor-pointer"
              style={{ position: 'absolute', left: '8px', top: '8px' }}
              onClick={() => setShowNodePalette(true)}
            >
              <ChevronRight className="h-3.5 w-3.5 sm:h-4 sm:w-4" />
            </Button>
          )}

          {/* Properties Panel - Inside Canvas, Responsive, Stick to right edge */}
          <div
            className={cn(
              "absolute right-0 top-0 bottom-0 z-[70] bg-white border-l border-secondary-200 shadow-xl transition-transform duration-300 ease-in-out overflow-hidden flex flex-col",
              "w-full sm:w-72 md:w-80", // Responsive width
              showPropertiesPanel ? "translate-x-0" : "translate-x-full"
            )}
            style={{ 
              position: 'absolute', 
              right: 0, 
              top: 0, 
              bottom: 0,
              pointerEvents: showPropertiesPanel ? 'auto' : 'none' 
            }}
            onDragOver={(e) => {
              // Allow drag over but don't prevent default - let it pass through to canvas
              if (!showPropertiesPanel) {
                e.stopPropagation()
              }
            }}
            onDrop={(e) => {
              // Prevent drop on panel itself, let it pass through to canvas
              if (showPropertiesPanel) {
                e.stopPropagation()
              }
            }}
          >
            <div className="h-full overflow-hidden">
              <PropertiesPanel
                selectedNode={selectedNode}
                nodes={nodes}
                onSave={handleNodeSave}
                onCancel={handleNodeCancel}
                onClose={() => setShowPropertiesPanel(false)}
              />
            </div>
          </div>

          {/* Validation Errors - Top Center, Inside Canvas, Optimized to minimize content obstruction */}
          {validationErrors.length > 0 && showValidationErrors && (
            <div 
              className="absolute top-20 sm:top-24 left-1/2 -translate-x-1/2 z-30 w-[calc(100%-2rem)] sm:w-auto max-w-2xl shadow-lg"
            >
              <ValidationErrors
                errors={validationErrors}
                onNodeClick={handleNodeClick}
                onClose={() => setShowValidationErrors(false)}
              />
            </div>
          )}

          {/* Toggle Validation Errors Button (When Hidden) - Top Center, Inside Canvas */}
          {validationErrors.length > 0 && !showValidationErrors && (
            <Button
              variant="default"
              size="sm"
              className="absolute top-20 sm:top-24 left-1/2 -translate-x-1/2 z-30 h-8 px-3 sm:px-4 shadow-lg cursor-pointer text-xs sm:text-sm"
              onClick={() => setShowValidationErrors(true)}
              title="Show validation errors"
            >
              <AlertCircle className="h-3.5 w-3.5 sm:h-4 sm:w-4 mr-1.5" />
              <span>{validationErrors.length} Error{validationErrors.length > 1 ? 's' : ''}</span>
            </Button>
          )}
        </div>
      )}

      {/* Test Execution Dialog */}
      <Dialog open={showTestDialog} onOpenChange={setShowTestDialog}>
        <DialogContent className="max-w-2xl max-h-[80vh]">
          <TestExecution
            nodes={nodes}
            onClose={() => setShowTestDialog(false)}
            onExecute={handleTestExecute}
          />
        </DialogContent>
      </Dialog>

      {/* Save as Template Dialog */}
      <Dialog open={showSaveTemplateDialog} onOpenChange={setShowSaveTemplateDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Save Workflow as Template</DialogTitle>
            <DialogDescription>
              Save this workflow as a reusable template
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="template-name">Template Name *</Label>
              <Input
                id="template-name"
                value={templateName}
                onChange={(e) => setTemplateName(e.target.value)}
                placeholder="Enter template name"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="template-description">Description</Label>
              <Textarea
                id="template-description"
                value={templateDescription}
                onChange={(e) => setTemplateDescription(e.target.value)}
                placeholder="Enter template description"
                rows={4}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowSaveTemplateDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleSaveAsTemplate} disabled={!templateName.trim()}>
              Save Template
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      </div>
    </FieldReferenceProvider>
  )
}
