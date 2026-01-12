import { useEffect, useMemo, useRef } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useForm } from "react-hook-form"
import type { Node } from "reactflow"
import { NodeTypeEnum } from "@/types/workflow"
import { NODE_DEFINITIONS, NODE_ICONS } from "@/constants/workflow-nodes"
import { X, RefreshCw, Info } from "lucide-react"
import { useActionRegistryById } from "@/hooks/use-action-registry"
import { useTriggerRegistryById } from "@/hooks/use-trigger-registry"
import { FieldSourceSelector, type FieldMapping } from "./FieldSourceSelector"
import { MvelExpressionEditor } from "./MvelExpressionEditor"
import { JsonEditor } from "@/components/registry/JsonEditor"
import { Switch } from "@/components/ui/switch"
import { Badge } from "@/components/ui/badge"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { useToast } from "@/hooks/use-toast"
import type { SchemaDefinition, FieldDefinition } from "@/components/registry/SchemaEditor"
import type { ActionConfigTemplate, ActionType } from "@/components/registry/types"
import { generateDefaultOutputMapping } from "@/utils/generate-config-template-schema"
import { isTriggerNode, getNodeCategory } from "@/utils/node-type-utils"

interface PropertiesPanelProps {
  selectedNode: Node | null
  nodes?: Node[]
  onSave: (nodeId: string, config: Record<string, unknown>) => void
  onCancel: () => void
  onClose?: () => void
}

export function PropertiesPanel({
  selectedNode,
  nodes = [],
  onSave,
  onClose,
}: PropertiesPanelProps) {
  const { register, reset, watch, setValue, formState } = useForm()
  const { errors } = formState
  const { toast } = useToast()
  
  // Track last selected node ID to prevent unnecessary resets
  const lastSelectedNodeIdRef = useRef<string | null>(null)

  // Find node definition from NODE_DEFINITIONS first
  let nodeDef = selectedNode
    ? NODE_DEFINITIONS.find((n) => n.type === selectedNode.data.type)
    : null
  
  // Load trigger/action config template from registry if node has registryId or triggerConfigId
  // Check both node.data.config (new structure) and node.data (old structure) for backward compatibility
  // MEMOIZE to prevent infinite re-renders - extract primitive values for stable dependencies
  const nodeConfig = useMemo(() => {
    return (selectedNode?.data as any)?.config || {}
  }, [selectedNode?.id, selectedNode?.data?.config])
  
  // Extract primitive values from nodeConfig to avoid object reference issues
  // Check both top-level and nested config structure
  const configRegistryId = (nodeConfig?.registryId || (nodeConfig as any)?.config?.registryId) as string | undefined
  const configTriggerConfigId = (nodeConfig?.triggerConfigId || (nodeConfig as any)?.config?.triggerConfigId) as string | undefined
  const dataRegistryId = (selectedNode?.data as any)?.registryId as string | undefined
  const dataTriggerConfigId = (selectedNode?.data as any)?.triggerConfigId as string | undefined
  
  const registryId = useMemo(() => {
    return configRegistryId || dataRegistryId
  }, [configRegistryId, dataRegistryId])
  
  const triggerConfigId = useMemo(() => {
    // Check multiple locations for triggerConfigId
    return configTriggerConfigId || 
      dataTriggerConfigId ||
      (nodeConfig?.config as any)?.triggerConfigId ||
      (selectedNode?.data as any)?.triggerConfigId
  }, [configTriggerConfigId, dataTriggerConfigId, nodeConfig, selectedNode])
  
  const nodeType = useMemo(() => {
    return (selectedNode?.data?.type as string) || ''
  }, [selectedNode?.data?.type])
  
  // CRITICAL: Use helper function to correctly detect node category
  // This follows backend enum NodeType (TRIGGER, LOGIC, ACTION)
  // Priority: 1. triggerConfigId → trigger
  //           2. registryId (without triggerConfigId) → action
  //           3. Backend enum value (TRIGGER, ACTION, LOGIC)
  //           4. Frontend node type (fallback)
  // Pass only primitive values to getNodeCategory to avoid object reference issues
  const nodeCategory = useMemo(() => {
    const configForCategory = {
      triggerConfigId: configTriggerConfigId,
      registryId: configRegistryId || dataRegistryId,
    }
    return getNodeCategory(nodeType, configForCategory)
  }, [nodeType, configTriggerConfigId, configRegistryId, dataRegistryId])
  
  const isTrigger = useMemo(() => nodeCategory === NodeTypeEnum.TRIGGER, [nodeCategory])
  const isAction = useMemo(() => nodeCategory === NodeTypeEnum.ACTION, [nodeCategory])
  
  // Debug logging for node type (after all variables are declared)
  useEffect(() => {
    if (import.meta.env.DEV && selectedNode) {
      console.log('[PropertiesPanel] Node type check:', {
        nodeId: selectedNode.id,
        nodeDataType: selectedNode.data.type,
        nodeDataTypeType: typeof selectedNode.data.type,
        nodeConfig: (selectedNode.data as any)?.config,
        nodeCategory,
        isTrigger,
        isAction,
        nodeDefFound: !!nodeDef,
      })
    }
  }, [selectedNode?.id, selectedNode?.data?.type, nodeCategory, isTrigger, isAction, nodeDef])
  
  // For triggers, use triggerConfigId if available, otherwise use registryId
  // IMPORTANT: Only use registryId for triggers, not for actions
  // Actions have their own registry and should NOT call trigger API
  // CRITICAL: Only set triggerId if this is actually a trigger node
  const triggerId = useMemo(() => {
    return isTrigger ? (triggerConfigId || registryId) : undefined
  }, [isTrigger, triggerConfigId, registryId])
  
  // Memoize action registry ID to prevent infinite loops
  const actionRegistryId = useMemo(() => {
    return isAction ? registryId : undefined
  }, [isAction, registryId])
  
  // Debug logging in development - use useEffect to prevent infinite loops
  useEffect(() => {
    if (import.meta.env.DEV) {
    console.log('[PropertiesPanel] Node registry lookup:', {
      nodeId: selectedNode?.id,
      nodeType,
        nodeCategory,
      registryId,
      triggerConfigId,
        configRegistryId,
        dataRegistryId,
      isTrigger,
      isAction,
      triggerId,
        actionRegistryId,
      willCallTriggerAPI: !!triggerId,
        willCallActionAPI: !!actionRegistryId,
      triggerAPIUrl: triggerId ? `/triggers/registry/${triggerId}` : 'none',
        actionAPIUrl: actionRegistryId ? `/actions/registry/${actionRegistryId}` : 'none',
        nodeConfig: nodeConfig,
        selectedNodeData: selectedNode?.data,
    })
  }
  }, [selectedNode?.id, nodeType, nodeCategory, registryId, triggerConfigId, configRegistryId, dataRegistryId, isTrigger, isAction, triggerId, actionRegistryId, nodeConfig, selectedNode?.data])
  
  const { data: triggerRegistryItem, isLoading: isLoadingTrigger } = useTriggerRegistryById(triggerId)
  const { data: actionRegistryItem, isLoading: isLoadingAction } = useActionRegistryById(actionRegistryId)
  
  // Save trigger schema to node config immediately when triggerRegistryItem is loaded
  // This ensures schema is available for other nodes to reference via "From Previous Node"
  useEffect(() => {
    if (selectedNode && isTrigger && triggerRegistryItem?.configTemplate?.schemas) {
      const triggerSchemas = triggerRegistryItem.configTemplate.schemas as SchemaDefinition[]
      if (triggerSchemas && triggerSchemas.length > 0) {
        const currentConfig = (selectedNode.data as any)?.config || {}
        const currentSchemas = currentConfig.schemas || []
        
        // Only update if schemas are missing or different
        if (JSON.stringify(currentSchemas) !== JSON.stringify(triggerSchemas)) {
          const updatedConfig = {
            ...currentConfig,
            schemas: triggerSchemas,
          }
          // Save immediately without debounce
          if (onSave) {
            onSave(selectedNode.id, updatedConfig)
          }
        }
      }
    }
  }, [selectedNode?.id, isTrigger, triggerRegistryItem?.configTemplate?.schemas, onSave])
  
  // Create nodeDef from registry data if not found in NODE_DEFINITIONS
  // This is similar to WorkflowNode component logic
  const finalNodeDef = useMemo(() => {
    if (!selectedNode) return null
    
    // If already found in NODE_DEFINITIONS, use it
    if (nodeDef) return nodeDef
    
    // If not found and this is a registry node, create nodeDef from registry data
    if (registryId || triggerConfigId) {
      const registryItem = isTrigger ? triggerRegistryItem : actionRegistryItem
      if (registryItem) {
        return {
          type: nodeType as NodeTypeEnum,
          label: registryItem.name || selectedNode.data.label || nodeType,
          description: registryItem.description || '',
          icon: (registryItem.metadata?.icon as string) || 'api-call',
          color: (registryItem.metadata?.color as string) || (isTrigger ? '#0ea5e9' : '#22c55e'),
          inputs: isTrigger ? 0 : 1,
          outputs: 1,
        }
      }
    }
    
    // If still not found, create a minimal default nodeDef
    return {
      type: nodeType as NodeTypeEnum,
      label: selectedNode.data.label || nodeType,
      description: '',
      icon: 'api-call',
      color: '#22c55e',
      inputs: 1,
      outputs: 1,
    }
  }, [selectedNode, nodeDef, registryId, triggerConfigId, isTrigger, triggerRegistryItem, actionRegistryItem, nodeType])
  
  // Debug logging for registry items
  useEffect(() => {
    if (import.meta.env.DEV) {
      console.log('[PropertiesPanel] Registry items:', {
        triggerId,
        actionRegistryId,
        hasTriggerRegistryItem: !!triggerRegistryItem,
        hasActionRegistryItem: !!actionRegistryItem,
        isLoadingTrigger,
        isLoadingAction,
        triggerRegistryItemKeys: triggerRegistryItem ? Object.keys(triggerRegistryItem) : [],
        actionRegistryItemKeys: actionRegistryItem ? Object.keys(actionRegistryItem) : [],
      })
    }
  }, [triggerId, actionRegistryId, triggerRegistryItem, actionRegistryItem, isLoadingTrigger, isLoadingAction])
  
  // Validate: Node must have registryId/triggerConfigId for trigger/action nodes from registry
  // Note: Some built-in nodes may not have registryId, so only validate if node type suggests it's from registry
  useEffect(() => {
    // Only show error if:
    // 1. Node is selected
    // 2. Node type suggests it's from registry (has trigger/action in type and no built-in node type)
    // 3. Missing both registryId and triggerConfigId
    const isRegistryNodeType = selectedNode && (
      (isTrigger && !['api-trigger', 'schedule-trigger', 'event-trigger'].includes(nodeType || '')) ||
      (isAction && !nodeType?.includes('action'))
    )
    
    if (isRegistryNodeType && !registryId && !triggerConfigId) {
      // Only show warning, not error, as node might be in the process of being configured
      console.warn('[PropertiesPanel] Node may be missing registryId:', {
        nodeType,
        registryId,
        triggerConfigId,
        nodeData: selectedNode?.data
      })
    }
  }, [selectedNode, isTrigger, isAction, registryId, triggerConfigId, nodeType, toast])
  
  const IconComponent = finalNodeDef ? NODE_ICONS[finalNodeDef.icon as keyof typeof NODE_ICONS] : null

  useEffect(() => {
    if (selectedNode) {
      // Only reset form if this is a different node
      // Preserve form values if user is editing the same node
      const currentNodeId = selectedNode.id
      const isDifferentNode = lastSelectedNodeIdRef.current !== currentNodeId
      
      // Update ref
      lastSelectedNodeIdRef.current = currentNodeId
      
      // If same node, preserve current form values (especially inputMappings)
      const currentFormValues = watch() as Record<string, unknown>
      
      // Reset form with node data
      const nodeConfig = selectedNode.data.config || {}
      
      // Set default values for wait-events node (legacy - check subtype in config instead)
      const nodeSubtype = (nodeConfig as any)?.subtype
      if (nodeSubtype === "wait-events") {
        reset({
          label: selectedNode.data.label || finalNodeDef?.label || "",
          apiCall: {
            enabled: nodeConfig.apiCall?.enabled || false,
            url: nodeConfig.apiCall?.url || "",
            method: nodeConfig.apiCall?.method || "POST",
            headers: nodeConfig.apiCall?.headers || {},
            body: nodeConfig.apiCall?.body || "",
            correlationIdField: nodeConfig.apiCall?.correlationIdField || "",
            correlationIdHeader: nodeConfig.apiCall?.correlationIdHeader || "",
            executionIdField: nodeConfig.apiCall?.executionIdField || "",
            executionIdHeader: nodeConfig.apiCall?.executionIdHeader || "",
            timeout: nodeConfig.apiCall?.timeout || 30,
            required: nodeConfig.apiCall?.required || false,
          },
          kafkaEvent: {
            enabled: nodeConfig.kafkaEvent?.enabled || false,
            topic: nodeConfig.kafkaEvent?.topic || "",
            correlationIdField: nodeConfig.kafkaEvent?.correlationIdField || "",
            executionIdField: nodeConfig.kafkaEvent?.executionIdField || "",
            filter: nodeConfig.kafkaEvent?.filter || {},
            timeout: nodeConfig.kafkaEvent?.timeout || 30,
            required: nodeConfig.kafkaEvent?.required || false,
          },
          aggregationStrategy: nodeConfig.aggregationStrategy || "all",
          requiredEvents: nodeConfig.requiredEvents || [],
          timeout: nodeConfig.timeout || 60,
          onTimeout: nodeConfig.onTimeout || "fail",
          outputMapping: {
            apiResponse: nodeConfig.outputMapping?.apiResponse || "api_response",
            kafkaEvent: nodeConfig.outputMapping?.kafkaEvent || "kafka_event",
          },
        })
      } else {
        // For event-trigger, handle special fields
        // FLATTEN nodeConfig - don't copy nested config structures
        // Only extract necessary fields for form, exclude metadata fields like configTemplate, objectTypeId
        const flattenedConfig: Record<string, unknown> = {}
        
        // Fields that should NOT be in config (they're metadata, not config values)
        const excludeFromConfig = ['configTemplate', 'objectTypeId', 'label', 'config']
        
        // Only copy top-level fields from nodeConfig, skip nested config and metadata
        Object.keys(nodeConfig).forEach((key) => {
          // Skip nested config structure and metadata fields
          if (key !== 'config' && !excludeFromConfig.includes(key)) {
            flattenedConfig[key] = (nodeConfig as Record<string, unknown>)[key]
          }
        })
        
        // Extract fields from nested config if it exists (for backward compatibility)
        // But flatten them to top level and exclude metadata
        const nestedConfig = (nodeConfig as any)?.config
        if (nestedConfig && typeof nestedConfig === 'object') {
          // Extract only necessary fields from nested config (exclude metadata)
          Object.keys(nestedConfig).forEach((key) => {
            if (!excludeFromConfig.includes(key)) {
              flattenedConfig[key] = (nestedConfig as Record<string, unknown>)[key]
            }
          })
        }
        
        const defaultValues: Record<string, unknown> = {
          label: selectedNode.data.label || finalNodeDef?.label || "",
          ...flattenedConfig,
        }
        
        // Ensure objectTypeId is set correctly (null becomes "none" for Select)
        if (defaultValues.objectTypeId === null || defaultValues.objectTypeId === undefined) {
          defaultValues.objectTypeId = "none"
        }
        
        // Check subtype in config instead of nodeType (legacy)
        // Get subtype once for all checks
        const nodeSubtype = (nodeConfig as any)?.subtype
        
        // Convert brokers array to string (for textarea)
        if (nodeSubtype === "event-trigger" && defaultValues.brokers) {
          if (Array.isArray(defaultValues.brokers)) {
            defaultValues.brokers = defaultValues.brokers.join('\n')
          }
        }
        
        // Parse eventFilter if it's a string
        if (nodeSubtype === "event-trigger" && defaultValues.eventFilter) {
          if (typeof defaultValues.eventFilter === 'string') {
            try {
              defaultValues.eventFilter = JSON.parse(defaultValues.eventFilter)
            } catch {
              // Keep as string if invalid JSON
            }
          }
        }
        
        // Handle action/trigger node config fields from registry
        // For trigger nodes, try to get triggerConfigId from multiple sources
        let nodeRegistryId = (selectedNode.data as any)?.registryId || 
          (selectedNode.data as any)?.config?.triggerConfigId || 
          (selectedNode.data as any)?.config?.registryId
        
        // If triggerConfigId is missing, try to extract from node ID
        // Node ID format: {registryId.replace(/[^a-zA-Z0-9]/g, '_')}_{timestamp}
        // Only try this for trigger nodes (check using helper function)
        if (!nodeRegistryId && isTriggerNode(selectedNode.data.type, nodeConfig) && selectedNode.id) {
          const nodeId = selectedNode.id
          if (nodeId.includes('_')) {
            const parts = nodeId.split('_')
            // UUID format: 8-4-4-4-12 (e.g., "13292674-c473-4615-9166-89606b604e95")
            // After replace: 8_4_4_4_12_timestamp
            if (parts.length >= 5) {
              const uuidParts = parts.slice(0, 5)
              // Verify UUID format
              if (uuidParts[0].length === 8 && uuidParts[1].length === 4 && 
                  uuidParts[2].length === 4 && uuidParts[3].length === 4 && uuidParts[4].length === 12) {
                nodeRegistryId = uuidParts.join('-')
                // Store it back to nodeConfig so it's preserved
                if (!nodeConfig.triggerConfigId) {
                  nodeConfig.triggerConfigId = nodeRegistryId
                  // Update node data immediately
                  if (selectedNode && onSave) {
                    const updatedConfig = {
                      ...(selectedNode.data.config || {}),
                      triggerConfigId: nodeRegistryId,
                    }
                    onSave(selectedNode.id, updatedConfig)
                  }
                }
              }
            }
          }
        }
        
        // Use helper function to correctly detect node category
        const nodeCategory = getNodeCategory(selectedNode.data.type, nodeConfig)
        const isActionFromRegistry = nodeRegistryId && nodeCategory === NodeTypeEnum.ACTION
        const isTriggerFromRegistry = nodeRegistryId && nodeCategory === NodeTypeEnum.TRIGGER
        
        // Ensure triggerConfigId is in defaultValues for trigger nodes
        if (isTriggerFromRegistry && nodeRegistryId && !defaultValues.triggerConfigId) {
          defaultValues.triggerConfigId = nodeRegistryId
        }
        
        if (isActionFromRegistry || isTriggerFromRegistry) {
          // Use configValues from node.data.config.configValues if available (new format)
          if (nodeConfig.configValues) {
            defaultValues.configValues = nodeConfig.configValues
          } else {
            // Initialize empty configValues if configTemplate schema exists
            const registryConfigTemplate = isTrigger 
              ? triggerRegistryItem?.configTemplate
              : actionRegistryItem?.configTemplate
            if (registryConfigTemplate && (
              ('configTemplate' in registryConfigTemplate && Array.isArray((registryConfigTemplate as any).configTemplate)) ||
              ('schemas' in registryConfigTemplate && Array.isArray((registryConfigTemplate as any).schemas))
            )) {
              defaultValues.configValues = {}
            }
          }
          
          // Load inputMappings for action nodes (from inputSchema)
          if (isActionFromRegistry) {
            // If same node and user has inputMappings in form, preserve them
            if (!isDifferentNode && currentFormValues.inputMappings) {
              defaultValues.inputMappings = currentFormValues.inputMappings
            } else if (nodeConfig.inputMappings) {
              defaultValues.inputMappings = nodeConfig.inputMappings
            } else {
              // Initialize empty inputMappings if inputSchema exists
              const actionConfigTemplate = actionRegistryItem?.configTemplate as ActionConfigTemplate | undefined
              const actionInputSchema = actionConfigTemplate?.inputSchema || []
              if (actionInputSchema && actionInputSchema.length > 0) {
                defaultValues.inputMappings = {}
              }
            }
          }
        }
        
        // Only reset if this is a different node
        if (isDifferentNode) {
          reset(defaultValues)
        } else {
          // For same node, only update specific fields that might have changed from registry
          // but preserve user input like inputMappings
          const fieldsToUpdate: Record<string, unknown> = {}
          if (defaultValues.configValues !== undefined && defaultValues.configValues !== currentFormValues.configValues) {
            fieldsToUpdate.configValues = defaultValues.configValues
          }
          if (Object.keys(fieldsToUpdate).length > 0) {
            Object.keys(fieldsToUpdate).forEach((key) => {
              setValue(key as any, fieldsToUpdate[key], { shouldDirty: false })
            })
          }
        }
      }
    }
  }, [selectedNode, reset, finalNodeDef, triggerRegistryItem, actionRegistryItem, isTrigger])

  // Watch for changes and update node state immediately
  const formValues = watch() as Record<string, unknown>
  useEffect(() => {
    if (!selectedNode) return
    
    // Debounce updates to avoid too many state updates
    const timeoutId = setTimeout(() => {
      // Transform data before saving (same logic as onSubmit)
      const transformedData = { ...formValues }
      
      // Handle trigger nodes: save triggerConfigId, instanceConfig, and schemas
      // Use helper function to correctly detect trigger nodes
      if (selectedNode && isTriggerNode(selectedNode.data.type, (selectedNode.data as any)?.config)) {
        // Get existing config but FLATTEN it - don't copy nested config structures
        const existingConfig = (selectedNode.data as any)?.config || {} as Record<string, unknown>
        
        // Build clean config object - only include necessary fields, no nested config
        const cleanConfig: Record<string, unknown> = {}
        
        // CRITICAL: Check triggerConfigId in multiple places (top level, nested config, form values)
        // Backend expects triggerConfigId at node.data.config.triggerConfigId (top level of config)
        const triggerConfigId = transformedData.triggerConfigId || 
          existingConfig.triggerConfigId || 
          (existingConfig.config as any)?.triggerConfigId ||
          (selectedNode.data as any)?.triggerConfigId
        if (triggerConfigId) {
          cleanConfig.triggerConfigId = triggerConfigId
          delete transformedData.triggerConfigId
        }
        
        // Save schemas from registry (for "From Previous Node" functionality)
        // Check both top level and nested config for existing schemas
        if (triggerRegistryItem?.configTemplate?.schemas) {
          const triggerSchemas = triggerRegistryItem.configTemplate.schemas as SchemaDefinition[]
          if (triggerSchemas && triggerSchemas.length > 0) {
            cleanConfig.schemas = triggerSchemas
          }
        } else if (existingConfig.schemas) {
          // Preserve existing schemas if registry doesn't have them
          cleanConfig.schemas = existingConfig.schemas
        } else if ((existingConfig.config as any)?.schemas) {
          cleanConfig.schemas = (existingConfig.config as any).schemas
        }
        
        // Save instanceConfig if provided
        // Check both top level and nested config
        if (transformedData.instanceConfig) {
          cleanConfig.instanceConfig = transformedData.instanceConfig
          delete transformedData.instanceConfig
        } else if (existingConfig.instanceConfig) {
          cleanConfig.instanceConfig = existingConfig.instanceConfig
        } else if ((existingConfig.config as any)?.instanceConfig) {
          cleanConfig.instanceConfig = (existingConfig.config as any).instanceConfig
        }
        
        // Save configValues if provided
        // Check both top level and nested config
        if (transformedData.configValues) {
          cleanConfig.configValues = transformedData.configValues
          delete transformedData.configValues
        } else if (existingConfig.configValues) {
          cleanConfig.configValues = existingConfig.configValues
        } else if ((existingConfig.config as any)?.configValues) {
          cleanConfig.configValues = (existingConfig.config as any).configValues
        }
        
        // Save triggerType if provided (for backward compatibility)
        if (transformedData.triggerType) {
          cleanConfig.triggerType = transformedData.triggerType
          delete transformedData.triggerType
        } else if (existingConfig.triggerType) {
          cleanConfig.triggerType = existingConfig.triggerType
        }
        
        // Set the clean config (no nested structures, no metadata fields)
        // This will be saved to node.data.config (top level, not nested)
        // CRITICAL: Remove metadata fields that shouldn't be in config
        const { configTemplate, objectTypeId, label, config: _, ...finalCleanConfig } = cleanConfig
        transformedData.config = finalCleanConfig
      }
      
      // Save configValues to node.data.config.configValues for action/trigger nodes from registry
      const existingConfig = (selectedNode?.data as any)?.config || {} as Record<string, unknown>
      const registryId = selectedNode ? (
        (existingConfig.triggerConfigId as string | undefined) ||
        (existingConfig.registryId as string | undefined) ||
        (selectedNode.data as any)?.registryId || 
        (selectedNode.data as any)?.config?.triggerConfigId || 
        (selectedNode.data as any)?.config?.registryId
      ) : undefined
      // Use helper function to correctly detect node category
      const nodeCategory = selectedNode ? getNodeCategory(selectedNode.data.type, (selectedNode.data as any)?.config) : null
      const isActionFromRegistry = registryId && selectedNode && nodeCategory === NodeTypeEnum.ACTION
      const isTriggerFromRegistry = registryId && selectedNode && nodeCategory === NodeTypeEnum.TRIGGER
      
      if (selectedNode && (isActionFromRegistry || isTriggerFromRegistry)) {
        // Build clean config object - only include necessary fields, no nested config
        // For trigger nodes, config was already built above, so skip if it exists
        if (isTriggerFromRegistry && transformedData.config) {
          // Config already built for trigger nodes above, just ensure registryId is not needed
          // (trigger nodes use triggerConfigId, not registryId)
        } else {
          // For action nodes or if trigger config wasn't built above
          const cleanConfig: Record<string, unknown> = {}
          
          // Preserve registryId for action nodes
          if (isActionFromRegistry) {
            const actionRegistryId = existingConfig.registryId || registryId
            if (actionRegistryId) {
              cleanConfig.registryId = actionRegistryId
            }
          }
          
          // Preserve triggerConfigId for trigger nodes (if not already set above)
          if (isTriggerFromRegistry) {
            const triggerConfigId = existingConfig.triggerConfigId || registryId
            if (triggerConfigId) {
              cleanConfig.triggerConfigId = triggerConfigId
            }
          }
          
          // Save configValues
          if (transformedData.configValues) {
            cleanConfig.configValues = transformedData.configValues
            delete transformedData.configValues
          } else if (existingConfig.configValues) {
            cleanConfig.configValues = existingConfig.configValues
          }
          
          // Save inputMappings for action nodes (from inputSchema)
          if (isActionFromRegistry && transformedData.inputMappings) {
            cleanConfig.inputMappings = transformedData.inputMappings
            delete transformedData.inputMappings
          } else if (isActionFromRegistry && existingConfig.inputMappings) {
            cleanConfig.inputMappings = existingConfig.inputMappings
          }
          
          // Save outputMapping for action nodes (custom output mapping)
          if (isActionFromRegistry && transformedData.outputMapping) {
            cleanConfig.outputMapping = transformedData.outputMapping
            delete transformedData.outputMapping
          } else if (isActionFromRegistry && existingConfig.outputMapping) {
            cleanConfig.outputMapping = existingConfig.outputMapping
          }
          
          // Save outputSchema if exists
          if (existingConfig.outputSchema) {
            cleanConfig.outputSchema = existingConfig.outputSchema
          }
          
          // Set the clean config (no nested structures, no metadata fields)
          // CRITICAL: Remove metadata fields that shouldn't be in config
          const { configTemplate, objectTypeId, label, config: _, ...finalCleanConfig } = cleanConfig
          transformedData.config = finalCleanConfig
        }
      }
      
      // Convert objectTypeId from "none" to null for all trigger nodes
      if (transformedData.objectTypeId === "none") {
        transformedData.objectTypeId = null
      }
      
      // Transform data for event-trigger (check subtype in config, not nodeType)
      const nodeSubtype = (selectedNode?.data as any)?.config?.subtype
      if (nodeSubtype === "event-trigger") {
        // Convert brokers string to array
        if (transformedData.brokers && typeof transformedData.brokers === 'string') {
          transformedData.brokers = transformedData.brokers
            .split('\n')
            .map((s: string) => s.trim())
            .filter((s: string) => s.length > 0)
        }
        
        // Convert eventFilter to JSON string if it's an object
        if (transformedData.eventFilter && typeof transformedData.eventFilter === 'object') {
          try {
            transformedData.eventFilter = JSON.stringify(transformedData.eventFilter)
          } catch {
            // Keep as object if stringify fails
          }
        }
      }
      
      // Legacy transformation logic removed - all actions now use configValues structure
      
      // Update node state immediately
      onSave(selectedNode.id, transformedData)
    }, 300) // 300ms debounce
    
    return () => clearTimeout(timeoutId)
  }, [formValues, selectedNode, onSave])

  if (!selectedNode || !finalNodeDef) {
    return (
      <Card className="h-full">
        <CardContent className="flex items-center justify-center h-full text-secondary-500">
          <div className="text-center">
            <p className="text-sm">No node selected</p>
            <p className="text-xs mt-1">Click on a node to configure it</p>
          </div>
        </CardContent>
      </Card>
    )
  }


  return (
    <Card className="h-full flex flex-col border-0 shadow-none">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-lg flex items-center space-x-2">
              {IconComponent && (
                <IconComponent className="w-5 h-5" style={{ color: finalNodeDef.color }} />
              )}
              <span>{finalNodeDef.label}</span>
            </CardTitle>
            <CardDescription className="mt-1">{finalNodeDef.description}</CardDescription>
          </div>
          {onClose && (
            <Button
              variant="ghost"
              size="sm"
              onClick={onClose}
              className="h-8 w-8 p-0"
            >
              <X className="h-4 w-4" />
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent className="flex-1 overflow-y-auto pb-4">
        <div className="space-y-4">
          {/* Common Fields */}
          <div className="space-y-2">
            <Label htmlFor="label">Node Label</Label>
            <Input
              id="label"
              {...register("label", { required: "Label is required" })}
              placeholder="Enter node label"
            />
            {errors.label && (
              <p className="text-sm text-error-600">{errors.label.message as string}</p>
            )}
          </div>

          {/* Trigger Config Info (for trigger nodes with triggerConfigId) */}
          {isTrigger && triggerConfigId && triggerRegistryItem && (
            <div className="space-y-4 p-4 border border-primary-200 rounded-lg bg-primary-50">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label className="text-sm font-semibold">Trigger Config</Label>
                  <Badge variant="outline">{triggerRegistryItem.type}</Badge>
                </div>
                <p className="text-sm font-medium">{triggerRegistryItem.name}</p>
                {triggerRegistryItem.description && (
                  <p className="text-xs text-secondary-500">{triggerRegistryItem.description}</p>
                )}
              </div>
            </div>
          )}

          {/* Instance Config Fields (for trigger nodes with triggerConfigId) */}
          {/* CRITICAL: Check both isTrigger and nodeType to ensure trigger nodes are detected correctly */}
          {(() => {
            const shouldShowInstanceConfig = (isTrigger || nodeType === NodeTypeEnum.TRIGGER) && (triggerConfigId || triggerId)
            
            // Debug logging
            if (import.meta.env.DEV && selectedNode) {
              console.log('[PropertiesPanel] Instance config check:', {
                isTrigger,
                nodeType,
                nodeTypeEnum: NodeTypeEnum.TRIGGER,
                triggerConfigId,
                triggerId,
                shouldShowInstanceConfig,
                triggerRegistryItem: triggerRegistryItem ? {
                  id: triggerRegistryItem.id,
                  type: triggerRegistryItem.type,
                  name: triggerRegistryItem.name
                } : null,
              })
            }
            
            return shouldShowInstanceConfig
          })() && (
            <div className="space-y-4 pt-4 border-t">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">Instance-Specific Configuration</Label>
                <p className="text-xs text-secondary-500">
                  Override trigger config settings for this workflow instance. These settings are specific to this workflow and will override the base trigger config.
                </p>
              </div>
              
              {/* Consumer Group for Event Triggers */}
              {/* Check triggerType from registry item or subtype from config (for backward compatibility) */}
              {(() => {
                // First check triggerType from registry item (new way)
                const triggerTypeFromRegistry = triggerRegistryItem?.type
                // Fallback to subtype from config (legacy)
                const nodeSubtype = (selectedNode?.data as any)?.config?.subtype
                // Event triggers have type "event" in registry or subtype "event-trigger" in config
                const isEventTrigger = triggerTypeFromRegistry === "event" || nodeSubtype === "event-trigger"
                
                // Debug logging
                if (import.meta.env.DEV && selectedNode) {
                  console.log('[PropertiesPanel] Event trigger check:', {
                    triggerTypeFromRegistry,
                    nodeSubtype,
                    isEventTrigger,
                    triggerRegistryItem: triggerRegistryItem ? {
                      type: triggerRegistryItem.type,
                      id: triggerRegistryItem.id
                    } : null
                  })
                }
                
                return isEventTrigger
              })() && (
                <div className="space-y-2">
                  <Label htmlFor="instanceConfig.consumerGroup">Consumer Group</Label>
                  <Input
                    id="instanceConfig.consumerGroup"
                    {...register("instanceConfig.consumerGroup")}
                    placeholder="workflow-{workflowId}-consumer"
                    defaultValue={(selectedNode?.data?.config as any)?.instanceConfig?.consumerGroup || ""}
                  />
                  <p className="text-xs text-secondary-500">
                    Unique consumer group for this workflow. If not specified, a default will be generated based on the trigger config.
                  </p>
                </div>
              )}
              
              {/* Additional instance config fields can be added here for other trigger types */}
              {/* For example, API triggers might have instance-specific authentication overrides */}
            </div>
          )}

          {/* Node-specific fields based on type */}
          {renderNodeSpecificFields(
            nodeCategory, 
            errors, 
            watch, 
            setValue, 
            nodes, 
            selectedNode.id,
            triggerRegistryItem,
            actionRegistryItem,
            selectedNode,
            isLoadingTrigger,
            isLoadingAction,
            formValues
          )}
        </div>
      </CardContent>
    </Card>
  )
}

function renderNodeSpecificFields(
  nodeType: NodeTypeEnum,
  errors: ReturnType<typeof useForm>["formState"]["errors"],
  watch: ReturnType<typeof useForm>["watch"],
  setValue: ReturnType<typeof useForm>["setValue"],
  nodes: Node[],
  currentNodeId: string,
  triggerRegistryItem?: any,
  actionRegistryItem?: any,
  selectedNode?: Node | null,
  isLoadingTrigger?: boolean,
  isLoadingAction?: boolean,
  formValues?: Record<string, unknown>
) {
  // Get trigger node from workflow
  const triggerNode = nodes.find((n) => {
    const nodeData = n.data as any
    const config = nodeData?.config || {}
    return config.triggerConfigId || nodeData?.triggerConfigId
  })

  // CRITICAL: Use nodeCategory (passed from parent) to determine node type
  // nodeCategory is already calculated correctly using getNodeCategory
  // IMPORTANT: Use actionRegistryId from parent component (already calculated correctly)
  // Don't recalculate here as it may cause inconsistencies
  const actionNodeDataConfig = selectedNode ? ((selectedNode.data as any)?.config || {}) : {}
  const localActionRegistryId = actionNodeDataConfig.registryId || (selectedNode ? (selectedNode.data as any)?.registryId : undefined)
  const triggerConfigId = actionNodeDataConfig.triggerConfigId
  
  // Use nodeCategory directly (already calculated correctly in parent component)
  // Use actionRegistryId from parent (passed via props) instead of recalculating
  const isActionNodeFromRegistry = nodeType === NodeTypeEnum.ACTION && !!actionRegistryItem
  
  // Debug logging
  if (import.meta.env.DEV) {
      console.log('[PropertiesPanel] Node detection:', {
        nodeType: typeof nodeType === 'string' ? nodeType : String(nodeType),
        nodeCategory: nodeType,
        localActionRegistryId,
        triggerConfigId,
        isActionNodeFromRegistry,
        hasActionRegistryItem: !!actionRegistryItem,
        hasTriggerRegistryItem: !!triggerRegistryItem,
        willRenderActionForm: isActionNodeFromRegistry,
      })
  }
  
  // If this is an action node from registry, render action form
  if (isActionNodeFromRegistry) {
    // Debug logging for action node rendering
    if (import.meta.env.DEV) {
      console.log('[PropertiesPanel] Rendering action form:', {
        isActionNodeFromRegistry,
        actionRegistryId: localActionRegistryId,
        hasActionRegistryItem: !!actionRegistryItem,
        isLoadingAction,
        actionRegistryItemKeys: actionRegistryItem ? Object.keys(actionRegistryItem) : [],
      })
    }
    
      // If still loading, show loading state
    if (localActionRegistryId && isLoadingAction) {
        return (
          <div className="space-y-4">
            <div className="text-sm text-secondary-500 text-center py-4">
            Loading action configuration...
            </div>
          </div>
        )
      }
      
    // If no actionRegistryItem after loading, show error
    if (localActionRegistryId && !isLoadingAction && !actionRegistryItem) {
      console.error('[PropertiesPanel] Action node missing registry item:', {
        localActionRegistryId,
        selectedNode: selectedNode?.data,
      })
        return (
          <div className="space-y-4">
            <div className="text-sm text-error-600 text-center py-4 border border-error-200 rounded-lg bg-error-50 p-4">
            <p className="font-semibold">Error: Missing action configuration</p>
            <p className="text-xs mt-1">This action node requires a valid registryId. Please recreate the node from the palette.</p>
              <p className="text-xs mt-2 font-mono text-error-700">Check console for details.</p>
            </div>
          </div>
        )
      }
      
    const nodeConfig = watch() as Record<string, unknown>
    // Get trigger schema from trigger node in workflow (for field source selector context)
    const triggerSchema = triggerNode ? (
      (triggerNode.data as any)?.config?.schemas || []
    ) : []
    
    // Get configTemplate from actionRegistryItem (not from props)
    const actionConfigTemplate = actionRegistryItem?.configTemplate as ActionConfigTemplate | undefined
    
    // Extract inputSchema, outputSchema and outputMapping from configTemplate
    const actionInputSchema = actionConfigTemplate?.inputSchema || []
    const outputSchema = actionConfigTemplate?.outputSchema || []
    const registryOutputMapping = actionConfigTemplate?.outputMapping || {}
    
    // Get custom output mapping from node config (if user customized it)
    // If not customized, use registry default
    const nodeOutputMapping = (nodeConfig.outputMapping || registryOutputMapping) as Record<string, string>
    
    // Get action type for output mapping
    const actionType = (actionRegistryItem?.type || "custom-action") as ActionType
      
      return (
        <div className="space-y-6">
        {/* Input Mapping Section - From Input Schema */}
        <div className="space-y-4 pt-4 border-t">
              <div className="space-y-2">
            <Label className="text-sm font-semibold">Input Mapping</Label>
                <p className="text-xs text-secondary-500">
              Map data from previous nodes to action input fields. These values can be referenced in MVEL expressions using @{"{"}fieldName{"}"}.
                </p>
              </div>
              
          {actionInputSchema && Array.isArray(actionInputSchema) && actionInputSchema.length > 0 ? (
            renderInputMappingFields(
              actionInputSchema,
              (formValues?.inputMappings || nodeConfig.inputMappings || {}) as Record<string, unknown>,
                (fieldName: string, value: unknown) => {
                // Use form values instead of nodeConfig to preserve user input
                const currentInputMappings = (formValues?.inputMappings || nodeConfig.inputMappings || {}) as Record<string, unknown>
                setValue("inputMappings", {
                  ...currentInputMappings,
                  [fieldName]: value,
                }, { shouldDirty: true })
                },
                nodes,
                currentNodeId,
              triggerSchema
            )
          ) : (
            <div className="text-sm text-secondary-500 py-4 text-center border border-dashed border-secondary-200 rounded-md">
              <p>No input schema defined for this action. Input fields will be available when inputSchema is configured in the action registry.</p>
            </div>
          )}
        </div>
        
        {/* Output Mapping Section - Optional, user can customize output mapping */}
        {outputSchema && Array.isArray(outputSchema) && outputSchema.length > 0 && (
          <div className="space-y-4 pt-4 border-t">
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <div>
                  <Label className="text-sm font-semibold">Output Mapping (Optional)</Label>
                  <p className="text-xs text-secondary-500 mt-1">
                    Customize how action response is mapped to output fields. If not customized, registry default mapping will be used.
              </p>
            </div>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    const generated = generateDefaultOutputMapping(actionType, outputSchema)
                    setValue("outputMapping", generated, { shouldDirty: true })
                  }}
                  className="cursor-pointer"
                >
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Reset to Default
                </Button>
            </div>
              </div>
            
            {renderOutputMappingFields(
              outputSchema,
              nodeOutputMapping,
              (outputMapping: Record<string, string>) => {
                setValue("outputMapping", outputMapping, { shouldDirty: true })
              },
              nodes,
              currentNodeId,
              triggerSchema
            )}
            </div>
        )}
            </div>
    )
  }

  // Only handle TRIGGER, LOGIC, ACTION based on NodeTypeEnum
  switch (nodeType) {
    case NodeTypeEnum.TRIGGER:
      // Get triggerConfigId from selectedNode if available
      // Check both top-level config and nested config structure
      const nodeDataConfig = (selectedNode?.data as any)?.config || {}
      const apiTriggerConfigId = nodeDataConfig.triggerConfigId || 
        (nodeDataConfig.config as any)?.triggerConfigId ||
        (selectedNode?.data as any)?.triggerConfigId
      
      // If still loading, show loading state
      if (apiTriggerConfigId && isLoadingTrigger) {
        return (
          <div className="space-y-4">
            <div className="text-sm text-secondary-500 text-center py-4">
              Loading trigger configuration...
            </div>
          </div>
        )
      }
      
      // If no triggerRegistryItem after loading, show error
      if (apiTriggerConfigId && !isLoadingTrigger && !triggerRegistryItem) {
        console.error('[PropertiesPanel] Trigger node missing registry item:', {
          triggerConfigId: apiTriggerConfigId,
          selectedNode: selectedNode?.data,
        })
        return (
          <div className="space-y-4">
            <div className="text-sm text-error-600 text-center py-4 border border-error-200 rounded-lg bg-error-50 p-4">
              <p className="font-semibold">Error: Missing trigger configuration</p>
              <p className="text-xs mt-1">This trigger node requires a valid triggerConfigId. Please recreate the node from the palette.</p>
              <p className="text-xs mt-2 font-mono text-error-700">Check console for details.</p>
            </div>
          </div>
        )
      }
      
      // If no triggerConfigId, show error message
      if (!apiTriggerConfigId) {
        console.error('[PropertiesPanel] api-trigger node missing triggerConfigId:', selectedNode?.data)
      return (
          <div className="space-y-4">
            <div className="text-sm text-error-600 text-center py-4 border border-error-200 rounded-lg bg-error-50 p-4">
              <p className="font-semibold">Error: Missing trigger configuration</p>
              <p className="text-xs mt-1">This trigger node requires a triggerConfigId. Please select a trigger config or recreate the node.</p>
              <p className="text-xs mt-2 font-mono text-error-700">Check console for details.</p>
              </div>
        </div>
      )
      }
      
      // Trigger nodes don't need configuration form
      // Trigger is already configured in trigger registry
      // Schema from trigger is used as output schema for other nodes to reference
      // (e.g., "From Trigger" in FieldSourceSelector)
      // Schema is automatically saved to node.data.config.schemas in the main component (lines 373-384)
      
      // Get trigger schema from registry (for display info only)
      const apiTriggerSchema = triggerRegistryItem?.configTemplate?.schemas as SchemaDefinition[] || []
      
      // Trigger nodes only show:
      // 1. Node Label (already shown above)
      // 2. Trigger Config Info (already shown above)
      // 3. Instance-Specific Configuration (already shown above)
      // No additional configuration fields needed
      return (
        <div className="space-y-4">
          <div className="text-sm text-secondary-500 text-center py-4 border border-dashed border-secondary-200 rounded-md">
            <p className="font-medium">Trigger Configuration Complete</p>
            <p className="text-xs mt-1">This trigger is configured using the trigger config settings from the registry.</p>
            <p className="text-xs mt-2">The trigger's output schema is available for other nodes to reference (e.g., "From Trigger" in field mappings).</p>
            {apiTriggerSchema && apiTriggerSchema.length > 0 && (
              <p className="text-xs mt-2 text-primary-600">
                Output schema: {apiTriggerSchema.length} schema(s) available
              </p>
            )}
          </div>
        </div>
      )

    case NodeTypeEnum.LOGIC:
      // Logic nodes will be re-implemented later
      // For now, show a placeholder message
      return (
        <div className="space-y-4">
          <div className="text-sm text-secondary-500 text-center py-4 border border-dashed border-secondary-200 rounded-md">
            <p>Logic node configuration will be available when logic nodes are re-implemented.</p>
            <p className="text-xs mt-2">Logic nodes are identified by subtype in node.data.config.</p>
                      </div>
          </div>
        )
      
    default:
      // Action nodes from registry are already handled above (before switch case)
      // This default case handles any other node types
      return (
        <div className="text-sm text-secondary-500 text-center py-4">
          Configuration options for this node type will be available soon.
        </div>
      )
  }
}

// Legacy cases removed - all node types now use NodeTypeEnum (TRIGGER, LOGIC, ACTION)
// Legacy cases that were removed:
// - "schedule-trigger", "file-trigger", "event-trigger" → Now use NodeTypeEnum.TRIGGER
// - "condition", "delay", "switch", "loop", "merge", "ab-test", "wait-events" → Will use NodeTypeEnum.LOGIC when re-implemented
// - "transform", "filter", "read-file" → Legacy data nodes, removed
// - "map" → Legacy placeholder, removed

// Helper function to render input mapping fields from inputSchema
function renderInputMappingFields(
  inputSchemas: SchemaDefinition[],
  inputMappings: Record<string, unknown>,
  onInputMappingChange: (fieldName: string, value: unknown) => void,
  nodes: Node[],
  currentNodeId: string,
  triggerSchema: SchemaDefinition[]
) {
  if (!inputSchemas || inputSchemas.length === 0) {
    return null
  }

      return (
        <div className="space-y-4">
      {inputSchemas.map((schema, schemaIndex) => (
        <div key={schema.schemaId || schemaIndex} className="space-y-3 p-3 bg-secondary-50 rounded-lg border">
          {schema.schemaId && (
            <Label className="text-sm font-medium">{schema.schemaId}</Label>
          )}
          {schema.description && (
            <p className="text-xs text-secondary-500">{schema.description}</p>
          )}
          
          {schema.fields && schema.fields.length > 0 && (
            <div className="space-y-3">
              {schema.fields.map((field) => {
                const fieldPath = schema.schemaId 
                  ? `${schema.schemaId}.${field.name}` 
                  : field.name
                const currentValue = inputMappings[fieldPath] as FieldMapping | string | null | undefined
      
      return (
                  <div key={field.name} className="space-y-2">
                    <div className="flex items-center gap-2">
                      <Label className="text-sm">
                        {field.name}
                        {field.required && <span className="text-destructive ml-1">*</span>}
                      </Label>
                      {field.description && (
                        <Tooltip>
                          <TooltipTrigger>
                            <Info className="h-4 w-4 text-muted-foreground" />
                          </TooltipTrigger>
                          <TooltipContent>{field.description}</TooltipContent>
                        </Tooltip>
                      )}
          </div>

          <FieldSourceSelector
                      value={currentValue || null}
                      onChange={(value) => {
                        onInputMappingChange(fieldPath, value)
                      }}
                      fieldType={field.type}
                      required={field.required}
                      label=""
                      description=""
            nodes={nodes}
            currentNodeId={currentNodeId}
            variables={[]}
          />
        </div>
      )
              })}
            </div>
          )}
          </div>
      ))}
        </div>
      )
}

// Helper function to render config fields from configTemplate schema
function renderConfigFieldsFromSchema(
  configTemplateSchemas: SchemaDefinition[],
  configValues: Record<string, unknown>,
  onConfigValueChange: (fieldName: string, value: unknown) => void,
  nodes: Node[],
  currentNodeId: string,
  triggerSchema: SchemaDefinition[],
  errors: ReturnType<typeof useForm>["formState"]["errors"],
  actionRegistryItem?: any // To get default values from registry
) {
  if (!configTemplateSchemas || configTemplateSchemas.length === 0) {
    return (
      <div className="text-sm text-secondary-500 py-4 text-center border border-dashed border-secondary-200 rounded-md">
        <p>No configuration fields defined in action registry</p>
      </div>
    )
  }

  // Helper to get nested value
    const getNestedValue = (obj: any, path: string): unknown => {
      const parts = path.split(".")
      let current = obj
      for (const part of parts) {
        if (current && typeof current === "object" && part in current) {
          current = current[part]
        } else {
          return undefined
        }
      }
      return current
  }

  // Helper to find field in registry configTemplate
  const findFieldInRegistry = (schemas: SchemaDefinition[], path: string[]): FieldDefinition | null => {
    for (const schema of schemas) {
      if (schema.fields) {
        for (const f of schema.fields) {
          if (f.name === path[0]) {
            if (path.length === 1) {
              return f
            }
            if (f.fields && path.length > 1) {
              // Create a temporary schema for nested fields
              const tempSchema: SchemaDefinition = {
                schemaId: schema.schemaId || "nested",
                fields: f.fields,
              }
              return findFieldInRegistry([tempSchema], path.slice(1))
            }
          }
        }
      }
    }
    return null
  }

  // Helper to get default value from action registry configTemplate (object, not schema)
  const getRegistryDefaultValue = (fieldPath: string): unknown => {
    if (!actionRegistryItem?.configTemplate) return undefined
    
    // configTemplate can be an object with default values at root level
    // e.g., { url: "...", method: "...", body: "..." }
    const configTemplateObj = actionRegistryItem.configTemplate
    if (typeof configTemplateObj === "object" && !Array.isArray(configTemplateObj)) {
      return getNestedValue(configTemplateObj, fieldPath)
    }
    
    return undefined
  }

  // Filter fields to only show those that need configuration
  const shouldShowField = (field: FieldDefinition, fieldPath: string): boolean => {
    const currentValue = getNestedValue(configValues, fieldPath)
    
    // Check if field has default value in action registry configTemplate (object)
    const registryDefaultValue = getRegistryDefaultValue(fieldPath)
    
    // Only show field if:
    // 1. Field is required (always show)
    // 2. Field has been overridden (has value in configValues)
    // 3. Field doesn't have default value in registry (needs to be configured)
    const hasOverride = currentValue !== undefined
    const hasRegistryDefault = registryDefaultValue !== undefined && registryDefaultValue !== null && registryDefaultValue !== ""
    return field.required || hasOverride || !hasRegistryDefault
  }

  const renderField = (field: FieldDefinition, fieldPath: string = ""): React.ReactNode => {
    const fullFieldPath = fieldPath ? `${fieldPath}.${field.name}` : field.name
    
    // Skip if field shouldn't be shown
    if (!shouldShowField(field, fullFieldPath)) {
      return null
    }
    
    const currentValue = getNestedValue(configValues, fullFieldPath)
    
    // Get registry default value from configTemplate object (not schema)
    const registryDefaultValue = getRegistryDefaultValue(fullFieldPath)
    
    const fieldValue = typeof currentValue === "string" 
      ? currentValue 
      : (typeof currentValue === "object" && currentValue !== null)
        ? JSON.stringify(currentValue, null, 2)
        : String(currentValue || field.defaultValue || registryDefaultValue || "")

    switch (field.type) {
      case "string":
      case "url":
      case "email":
      case "phone":
        return (
          <MvelExpressionEditor
            key={fullFieldPath}
            value={fieldValue}
            onChange={(value) => onConfigValueChange(fullFieldPath, value)}
            field={field}
            nodes={nodes}
            currentNodeId={currentNodeId}
            errors={errors[fullFieldPath]?.message as string}
            label={field.name}
            description={field.description}
            required={field.required}
            placeholder={field.description || `Enter ${field.name}`}
            showMvelFormatHelp={false} // Hide MVEL format help - shown once at top
          />
        )

      case "number":
        return (
          <div key={fullFieldPath} className="space-y-2">
            <Label htmlFor={fullFieldPath}>
              {field.name}
              {field.required && <span className="text-error-600 ml-1">*</span>}
            </Label>
            <Input
              id={fullFieldPath}
              type="number"
              value={fieldValue}
              onChange={(e) => onConfigValueChange(fullFieldPath, Number(e.target.value))}
              min={field.validation?.min}
              max={field.validation?.max}
            />
            {field.description && (
              <p className="text-xs text-secondary-500">{field.description}</p>
            )}
          </div>
        )

      case "boolean":
        return (
          <div key={fullFieldPath} className="flex items-center space-x-2">
            <Switch
              id={fullFieldPath}
              checked={currentValue as boolean || field.defaultValue || false}
              onCheckedChange={(checked) => onConfigValueChange(fullFieldPath, checked)}
            />
            <Label htmlFor={fullFieldPath} className="cursor-pointer">
              {field.name}
              {field.required && <span className="text-error-600 ml-1">*</span>}
            </Label>
            {field.description && (
              <p className="text-xs text-secondary-500 ml-2">{field.description}</p>
            )}
          </div>
        )

      case "json":
        return (
          <div key={fullFieldPath} className="space-y-2">
            <Label htmlFor={fullFieldPath}>
              {field.name}
              {field.required && <span className="text-error-600 ml-1">*</span>}
            </Label>
            <JsonEditor
              value={currentValue || field.defaultValue || {}}
              onChange={(value) => onConfigValueChange(fullFieldPath, value)}
              placeholder={field.description || '{"key": "value"} or use @{variable} for MVEL expressions'}
              label=""
              description={field.description}
            />
          </div>
        )

      case "object":
        if (field.fields && field.fields.length > 0) {
          return (
            <div key={fullFieldPath} className="space-y-3 pl-4 border-l-2 border-secondary-200">
              <Label className="text-sm font-medium">
                {field.name}
                {field.required && <span className="text-error-600 ml-1">*</span>}
              </Label>
              {field.description && (
                <p className="text-xs text-secondary-500">{field.description}</p>
              )}
              {field.fields.map((nestedField) => renderField(nestedField, fullFieldPath))}
            </div>
          )
        }
        return null

      case "array":
        // For arrays, render as JSON editor for now
        return (
          <div key={fullFieldPath} className="space-y-2">
            <Label htmlFor={fullFieldPath}>
              {field.name}
              {field.required && <span className="text-error-600 ml-1">*</span>}
            </Label>
            <JsonEditor
              value={currentValue || field.defaultValue || []}
              onChange={(value) => onConfigValueChange(fullFieldPath, value)}
              placeholder={field.description || '["item1", "item2"] or use @{variable} for MVEL expressions'}
              label=""
              description={field.description}
            />
          </div>
        )

      default:
        // Check if field has enum validation
        if (field.validation?.enum && field.validation.enum.length > 0) {
          return (
            <div key={fullFieldPath} className="space-y-2">
              <Label htmlFor={fullFieldPath}>
                {field.name}
                {field.required && <span className="text-error-600 ml-1">*</span>}
              </Label>
              <Select
                value={fieldValue}
                onValueChange={(value) => onConfigValueChange(fullFieldPath, value)}
              >
                <SelectTrigger id={fullFieldPath}>
                  <SelectValue placeholder={`Select ${field.name}`} />
                </SelectTrigger>
                <SelectContent>
                  {field.validation.enum.map((option) => (
                    <SelectItem key={option} value={option}>
                      {option}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {field.description && (
                <p className="text-xs text-secondary-500">{field.description}</p>
              )}
            </div>
          )
        }

        // Default: use MVEL expression editor
        return (
          <MvelExpressionEditor
            key={fullFieldPath}
            value={fieldValue}
            onChange={(value) => onConfigValueChange(fullFieldPath, value)}
            field={field}
            nodes={nodes}
            currentNodeId={currentNodeId}
            label={field.name}
            description={field.description}
            required={field.required}
            placeholder={field.description || `Enter ${field.name}`}
            showMvelFormatHelp={false} // Hide MVEL format help - shown once at top
          />
        )
    }
  }

  // Filter fields to only show those that need configuration
  const fieldsToShow = configTemplateSchemas.flatMap((schema) => {
    if (!schema.fields || schema.fields.length === 0) return []
    return schema.fields
      .map((field) => {
        const fieldPath = schema.schemaId ? `${schema.schemaId}.${field.name}` : field.name
        return shouldShowField(field, fieldPath) ? { field, schema } : null
      })
      .filter((item): item is { field: FieldDefinition; schema: SchemaDefinition } => item !== null)
  })

  if (fieldsToShow.length === 0) {
    return (
      <div className="text-sm text-secondary-500 py-4 text-center border border-dashed border-secondary-200 rounded-md">
        <p>All configuration fields have default values. Override them here if needed.</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {/* Show MVEL Expression Format help only once at the top */}
      <div className="p-2 bg-secondary-50 rounded-md border border-secondary-200">
        <p className="text-xs font-medium text-secondary-900 mb-1">MVEL Expression Format:</p>
        <ul className="text-xs text-secondary-600 space-y-1 list-disc list-inside">
          <li>
            <code className="px-1 py-0.5 bg-white rounded">{"@{nodeId.field}"}</code> - Reference previous node output
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">{"@{_trigger.field}"}</code> - Reference trigger data
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">{"@{_vars.varName}"}</code> - Reference workflow variable
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">{"@{_now()}"}</code> - Built-in functions
          </li>
        </ul>
      </div>

      {configTemplateSchemas.map((schema, schemaIndex) => {
        const schemaFields = fieldsToShow.filter((item) => item.schema === schema).map((item) => item.field)
        if (schemaFields.length === 0) return null
        
        return (
        <div key={schema.schemaId || schemaIndex} className="space-y-3">
          {configTemplateSchemas.length > 1 && (
            <div className="flex items-center justify-between p-2 bg-secondary-50 rounded">
              <Label className="text-sm font-medium">{schema.schemaId || `Schema ${schemaIndex + 1}`}</Label>
              {schema.description && (
                <p className="text-xs text-secondary-500">{schema.description}</p>
              )}
            </div>
          )}
          
            <div className="space-y-4">
              {schemaFields.map((field) => renderField(field))}
            </div>
          </div>
        )
      })}
    </div>
  )
}

/**
 * Extract all field paths from output schema (recursive)
 */
function extractOutputFieldPaths(schema: SchemaDefinition[], prefix = ""): string[] {
  const paths: string[] = []

  schema.forEach((s) => {
    if (s.fields && s.fields.length > 0) {
      s.fields.forEach((field) => {
        const fieldPath = prefix ? `${prefix}.${field.name}` : field.name
        paths.push(fieldPath)

        // Handle nested fields
        if (field.fields && field.fields.length > 0) {
          paths.push(...extractOutputFieldPaths([{ ...s, fields: field.fields }], fieldPath))
        }
      })
    }
  })

  return paths
}

/**
 * Get field definition by path from output schema
 */
function getOutputFieldByPath(schema: SchemaDefinition[], path: string): FieldDefinition | null {
  const parts = path.split(".")
  let currentFields: FieldDefinition[] | undefined = schema.flatMap((s) => s.fields || [])

  for (let i = 0; i < parts.length; i++) {
    const part = parts[i]
    const field: FieldDefinition | undefined = currentFields?.find((f) => f.name === part)

    if (!field) {
      return null
    }

    if (i === parts.length - 1) {
      return field
    }

    currentFields = field.fields
  }

  return null
}

/**
 * Render output mapping fields from output schema
 */
function renderOutputMappingFields(
  outputSchema: SchemaDefinition[],
  outputMapping: Record<string, string>,
  onOutputMappingChange: (outputMapping: Record<string, string>) => void,
  nodes: Node[],
  currentNodeId: string,
  triggerSchema: SchemaDefinition[]
) {
  const fieldPaths = extractOutputFieldPaths(outputSchema)

  if (fieldPaths.length === 0) {
    return (
      <div className="text-sm text-secondary-500 py-4 text-center border border-dashed border-secondary-200 rounded-md">
        <p>Add fields to Output Schema to configure output mapping</p>
      </div>
    )
  }

  const handleMappingChange = (fieldPath: string, expression: string) => {
    onOutputMappingChange({
      ...outputMapping,
      [fieldPath]: expression,
    })
  }

  return (
    <div className="space-y-3">
      {fieldPaths.map((fieldPath) => {
        const field = getOutputFieldByPath(outputSchema, fieldPath)
        const currentExpression = outputMapping[fieldPath] || ""

        return (
          <div key={fieldPath} className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor={`output-mapping-${fieldPath}`} className="text-sm font-medium">
                {fieldPath}
                {field?.required && <span className="text-error-600 ml-1">*</span>}
              </Label>
              {field && (
                <Badge variant="outline" className="text-xs">
                  {field.type}
                </Badge>
          )}
        </div>
            <MvelExpressionEditor
              value={currentExpression}
              onChange={(value) => handleMappingChange(fieldPath, value)}
              field={(field || { name: fieldPath, type: "string", required: false }) as FieldDefinition}
              nodes={nodes}
              currentNodeId={currentNodeId}
              label=""
              description={field?.description}
              required={field?.required}
              placeholder={`@{_response.${fieldPath.split(".").pop() || "field"}}`}
            />
            <p className="text-xs text-secondary-500">
              MVEL expression to map from raw response. Use <code className="px-1 py-0.5 bg-secondary-100 rounded">{"@{_response.field}"}</code> to reference response data.
            </p>
          </div>
        )
      })}

      <div className="mt-4 p-3 bg-secondary-50 rounded-md border border-secondary-200">
        <p className="text-xs font-medium text-secondary-900 mb-1">Context Variables:</p>
        <ul className="text-xs text-secondary-600 space-y-1 list-disc list-inside">
          <li>
            <code className="px-1 py-0.5 bg-white rounded">{"@{_response}"}</code> - Raw action response
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">{"@{_response.statusCode}"}</code> - HTTP status code (API Call)
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">{"@{_response.body}"}</code> - Response body (API Call)
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">{"@{_response.result}"}</code> - Function result (Function)
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">{"@{_response.topic}"}</code> - Kafka topic (Publish Event)
          </li>
        </ul>
      </div>
    </div>
  )
}

