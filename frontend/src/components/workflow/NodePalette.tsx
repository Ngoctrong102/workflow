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
import { NodeTypeEnum } from "@/types/workflow"
import { cn } from "@/lib/utils"
import { HelpTooltip } from "@/components/common/HelpTooltip"
import type { NodeDefinition } from "@/types/workflow"

interface NodePaletteProps {
  onNodeDragStart?: (event: React.DragEvent, nodeType: string) => void
  onClose?: () => void
}

const categoryLabels: Record<NodeTypeEnum, string> = {
  [NodeTypeEnum.TRIGGER]: "Triggers",
  [NodeTypeEnum.ACTION]: "Actions",
  [NodeTypeEnum.LOGIC]: "Logic",
}

export function NodePalette({ onNodeDragStart, onClose }: NodePaletteProps) {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedCategory, setSelectedCategory] = useState<NodeTypeEnum | "all">("all")
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
        // Actions from registry are identified by registryId, not by node type
        // Backend uses NodeTypeEnum.ACTION as node.type and stores registryId in node.data.config.registryId
        const nodeDef: NodeDefinition = {
          type: NodeTypeEnum.ACTION,
          label: action.name,
          description: action.description || "",
          icon: action.metadata?.icon || "api-call",
          color: action.metadata?.color || "#22c55e",
          inputs: 1,
          outputs: 1,
          // Store registry ID and config template for later use
          registryId: action.id,
          configTemplate: action.configTemplate as Record<string, unknown> | undefined,
        }
        
        // Group by ActionType (api-call, publish-event, function, custom-action)
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
        // Triggers from registry use NodeTypeEnum.TRIGGER
        // They are identified by triggerConfigId in node.data.config
        nodes.push({
          type: NodeTypeEnum.TRIGGER,
          label: trigger.name,
          description: trigger.description || "",
          icon: trigger.metadata?.icon || "api-call",
          color: trigger.metadata?.color || "#0ea5e9",
          inputs: 0,
          outputs: 1,
          // Store registry ID (trigger config ID) and config template for later use
          // Backend expects triggerConfigId in node.data.config.triggerConfigId
          registryId: trigger.id, // This is the trigger config ID
          configTemplate: trigger.configTemplate as Record<string, unknown> | undefined,
        })
      })
    }
    
    // Add action nodes from registry (flattened for filtering)
    Object.values(actionsByType).forEach((actionNodes) => {
      nodes.push(...actionNodes)
    })
    
    // Add built-in logic nodes from constants
    const logicNodes = NODE_DEFINITIONS.filter((n) => n.type === NodeTypeEnum.LOGIC)
    nodes.push(...logicNodes)
    
    return nodes
  }, [triggerRegistry, actionsByType])

  const filteredNodes = allNodes.filter((node) => {
    const matchesSearch =
      node.label.toLowerCase().includes(searchQuery.toLowerCase()) ||
      node.description.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesCategory =
      selectedCategory === "all" || node.type === selectedCategory
    
    // Filter actions by type if action category is selected
    let matchesActionType = true
    if (node.type === NodeTypeEnum.ACTION && selectedCategory === NodeTypeEnum.ACTION && selectedActionType !== "all") {
      // Find which type group this action belongs to
      const actionType = Object.keys(actionsByType).find((type) =>
        actionsByType[type].some((a) => a.registryId === node.registryId)
      )
      matchesActionType = actionType === selectedActionType
    }
    
    return matchesSearch && matchesCategory && matchesActionType
  })

  const isLoading = isLoadingTriggers || isLoadingActions

  const handleDragStart = (event: React.DragEvent, nodeType: string) => {
    // Find node definition to get registryId, configTemplate, and label
    const nodeDef = allNodes.find((n) => n.type === nodeType || (n.registryId && n.type === nodeType))
    
    // Set legacy data for backward compatibility
    event.dataTransfer.setData("application/reactflow", nodeType)
    
    // Set new data format with registryId, configTemplate, and label
    event.dataTransfer.setData("application/reactflow-node", JSON.stringify({
      nodeType,
      label: nodeDef?.label || nodeType,
      registryId: nodeDef?.registryId,
      configTemplate: nodeDef?.configTemplate,
    }))
    
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
          {Object.values(NodeTypeEnum).map((category) => (
            <Badge
              key={category}
              variant={selectedCategory === category ? "default" : "outline"}
              className="cursor-pointer"
              onClick={() => {
                setSelectedCategory(category)
                if (category !== NodeTypeEnum.ACTION) {
                  setSelectedActionType("all")
                }
              }}
            >
              {categoryLabels[category]}
            </Badge>
          ))}
        </div>

        {/* Action Type Filters (shown when Actions category is selected) */}
        {selectedCategory === NodeTypeEnum.ACTION && (
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
              const IconComponent = NODE_ICONS[node.icon] || NODE_ICONS["api-call"]
              return (
                <div
                  key={`${node.type}-${node.registryId || node.label}`}
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
