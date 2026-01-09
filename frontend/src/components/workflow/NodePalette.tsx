import { useState, useMemo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { Search, X } from "lucide-react"
import { NODES_BY_CATEGORY, NODE_DEFINITIONS, NODE_ICONS } from "@/constants/workflow-nodes"
import { useTriggerRegistry } from "@/hooks/use-trigger-registry"
import { useActionRegistry } from "@/hooks/use-action-registry"
import type { NodeType } from "@/types/workflow"
import { cn } from "@/lib/utils"
import { HelpTooltip } from "@/components/common/HelpTooltip"
import type { NodeDefinition } from "@/types/workflow"

interface NodePaletteProps {
  onNodeDragStart?: (event: React.DragEvent, nodeType: string) => void
  onClose?: () => void
}

const categoryLabels: Record<NodeType, string> = {
  trigger: "Triggers",
  action: "Actions",
  logic: "Logic",
  data: "Data",
}

export function NodePalette({ onNodeDragStart, onClose }: NodePaletteProps) {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedCategory, setSelectedCategory] = useState<NodeType | "all">("all")
  const [selectedActionType, setSelectedActionType] = useState<string>("all")
  
  // Fetch triggers and actions from registry
  const { data: triggerRegistry, isLoading: isLoadingTriggers } = useTriggerRegistry()
  const { data: actionRegistry, isLoading: isLoadingActions } = useActionRegistry()

  // Group actions by type
  const actionsByType = useMemo(() => {
    const groups: Record<string, NodeDefinition[]> = {
      "api-call": [],
      "publish-event": [],
      "function": [],
      "custom-action": [],
    }
    
    if (actionRegistry?.actions) {
      actionRegistry.actions.forEach((action) => {
        // Map action type to node type
        let nodeType: string = action.type
        if (action.type === "api-call") nodeType = "api-call"
        else if (action.type === "publish-event") nodeType = "publish-event"
        else if (action.type === "function") nodeType = "function"
        else if (action.type === "custom-action") {
          // Use actionType for custom actions (e.g., "send-email")
          nodeType = (action as any).actionType || action.id
        }
        
        const nodeDef: NodeDefinition = {
          type: nodeType as any,
          category: "action",
          label: action.name,
          description: action.description || "",
          icon: action.metadata?.icon || "api-call",
          color: action.metadata?.color || "#22c55e",
          inputs: 1,
          outputs: 1,
          // Store registry ID and config template for later use
          registryId: action.id,
          configTemplate: action.configTemplate,
        }
        
        const actionType = action.type || "custom-action"
        if (groups[actionType]) {
          groups[actionType].push(nodeDef)
        } else {
          groups["custom-action"].push(nodeDef)
        }
      })
    }
    
    return groups
  }, [actionRegistry])

  // Combine registry data with built-in logic nodes
  const allNodes = useMemo(() => {
    const nodes: NodeDefinition[] = []
    
    // Add trigger nodes from registry
    if (triggerRegistry?.triggers) {
      triggerRegistry.triggers.forEach((trigger) => {
        // Map trigger type to node type
        let nodeType: string = trigger.type
        if (trigger.type === "api-call") nodeType = "api-trigger"
        else if (trigger.type === "scheduler") nodeType = "schedule-trigger"
        else if (trigger.type === "event") nodeType = "event-trigger"
        else if (trigger.type === "file") nodeType = "file-trigger"
        
        nodes.push({
          type: nodeType as any,
          category: "trigger",
          label: trigger.name,
          description: trigger.description || "",
          icon: trigger.metadata?.icon || "api-trigger",
          color: trigger.metadata?.color || "#0ea5e9",
          inputs: 0,
          outputs: 1,
        })
      })
    }
    
    // Add action nodes from registry (flattened for filtering)
    Object.values(actionsByType).forEach((actionNodes) => {
      nodes.push(...actionNodes)
    })
    
    // Add built-in logic nodes from constants
    const logicNodes = NODE_DEFINITIONS.filter((n) => n.category === "logic")
    nodes.push(...logicNodes)
    
    return nodes
  }, [triggerRegistry, actionsByType])

  const filteredNodes = allNodes.filter((node) => {
    const matchesSearch =
      node.label.toLowerCase().includes(searchQuery.toLowerCase()) ||
      node.description.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesCategory =
      selectedCategory === "all" || node.category === selectedCategory
    
    // Filter actions by type if action category is selected
    let matchesActionType = true
    if (node.category === "action" && selectedCategory === "action" && selectedActionType !== "all") {
      // Find which type group this action belongs to
      const actionType = Object.keys(actionsByType).find((type) =>
        actionsByType[type].some((a) => a.type === node.type)
      )
      matchesActionType = actionType === selectedActionType
    }
    
    return matchesSearch && matchesCategory && matchesActionType
  })

  const isLoading = isLoadingTriggers || isLoadingActions

  const handleDragStart = (event: React.DragEvent, nodeType: string) => {
    event.dataTransfer.setData("application/reactflow", nodeType)
    event.dataTransfer.effectAllowed = "move"
    onNodeDragStart?.(event, nodeType)
  }

  return (
    <Card className="h-full flex flex-col border-0 shadow-none">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-lg">Node Palette</CardTitle>
            <CardDescription>Drag nodes to canvas</CardDescription>
          </div>
          {onClose && (
            <Button
              variant="ghost"
              size="sm"
              onClick={onClose}
              className="h-8 w-8 p-0 cursor-pointer"
              title="Close panel"
            >
              <X className="h-4 w-4" />
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent className="flex-1 overflow-hidden flex flex-col space-y-4">
        {/* Search */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
          <Input
            placeholder="Search nodes..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>

        {/* Category Filters */}
        <div className="flex flex-wrap gap-2">
          <Badge
            variant={selectedCategory === "all" ? "default" : "outline"}
            className="cursor-pointer"
            onClick={() => {
              setSelectedCategory("all")
              setSelectedActionType("all")
            }}
          >
            All
          </Badge>
          {(Object.keys(NODES_BY_CATEGORY) as NodeType[]).map((category) => (
            <Badge
              key={category}
              variant={selectedCategory === category ? "default" : "outline"}
              className="cursor-pointer"
              onClick={() => {
                setSelectedCategory(category)
                if (category !== "action") {
                  setSelectedActionType("all")
                }
              }}
            >
              {categoryLabels[category]}
            </Badge>
          ))}
        </div>

        {/* Action Type Filters (shown when Actions category is selected) */}
        {selectedCategory === "action" && (
          <div className="flex flex-wrap gap-2">
            <Badge
              variant={selectedActionType === "all" ? "default" : "outline"}
              className="cursor-pointer"
              onClick={() => setSelectedActionType("all")}
            >
              All Actions
            </Badge>
            <Badge
              variant={selectedActionType === "api-call" ? "default" : "outline"}
              className="cursor-pointer"
              onClick={() => setSelectedActionType("api-call")}
            >
              API Call
            </Badge>
            <Badge
              variant={selectedActionType === "publish-event" ? "default" : "outline"}
              className="cursor-pointer"
              onClick={() => setSelectedActionType("publish-event")}
            >
              Publish Event
            </Badge>
            <Badge
              variant={selectedActionType === "function" ? "default" : "outline"}
              className="cursor-pointer"
              onClick={() => setSelectedActionType("function")}
            >
              Function
            </Badge>
            <Badge
              variant={selectedActionType === "custom-action" ? "default" : "outline"}
              className="cursor-pointer"
              onClick={() => setSelectedActionType("custom-action")}
            >
              Custom Actions
            </Badge>
          </div>
        )}

        {/* Node List */}
        <div className="flex-1 overflow-y-auto space-y-2">
          {isLoading ? (
            <div className="space-y-2">
              {[1, 2, 3, 4, 5].map((i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          ) : filteredNodes.length === 0 ? (
            <div className="text-center py-8 text-secondary-500 text-sm">
              No nodes found
            </div>
          ) : (
            filteredNodes.map((node) => {
              const IconComponent = NODE_ICONS[node.icon] || NODE_ICONS["api-trigger"]
              return (
                <div
                  key={node.type}
                  draggable
                  onDragStart={(e) => handleDragStart(e, node.type)}
                  className={cn(
                    "p-3 rounded-lg border border-secondary-200 bg-white cursor-move",
                    "hover:border-primary-400 hover:shadow-sm transition-all",
                    "flex items-center space-x-3"
                  )}
                >
                  {IconComponent && (
                    <IconComponent
                      className="flex-shrink-0"
                      size={20}
                      style={{ color: node.color }}
                    />
                  )}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center space-x-2">
                      <div className="font-medium text-sm text-secondary-900">
                        {node.label}
                      </div>
                      <HelpTooltip content={node.description}>
                        <button type="button" className="text-secondary-400 hover:text-secondary-600">
                          <span className="text-xs">ℹ️</span>
                        </button>
                      </HelpTooltip>
                    </div>
                    <div className="text-xs text-secondary-500 truncate">
                      {node.description}
                    </div>
                  </div>
                </div>
              )
            })
          )}
        </div>
      </CardContent>
    </Card>
  )
}

