import { useState, useMemo } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Progress } from "@/components/ui/progress"
import { Badge } from "@/components/ui/badge"
import { ChevronLeft, ChevronRight, Check, Plus, X } from "lucide-react"
import { useTriggerRegistry } from "@/hooks/use-trigger-registry"
import { useActionRegistry } from "@/hooks/use-action-registry"
import { useCreateWorkflow } from "@/hooks/use-workflows"
import { useToast } from "@/hooks/use-toast"
import type { WorkflowDefinition, WorkflowNode, WorkflowEdge } from "@/types/workflow"
import { normalizeWorkflowDefinition } from "@/utils/node-type-utils"

interface GuidedWorkflowWizardProps {
  onComplete?: () => void
  onCancel?: () => void
}

type WizardStep = 1 | 2 | 3 | 4 | 5

interface TriggerConfig {
  triggerConfigId?: string // ID from trigger registry
  triggerType: string // api-call, scheduler, event
  instanceConfig?: Record<string, unknown> // Instance-specific overrides
}

interface ActionConfig {
  id: string
  type: string
  registryId?: string
  config: Record<string, unknown>
}

export function GuidedWorkflowWizard({ onComplete, onCancel }: GuidedWorkflowWizardProps) {
  const navigate = useNavigate()
  const { toast } = useToast()
  const createWorkflow = useCreateWorkflow()
  
  const [step, setStep] = useState<WizardStep>(1)
  const [workflowName, setWorkflowName] = useState("")
  const [workflowDescription, setWorkflowDescription] = useState("")
  const [selectedTriggerType, setSelectedTriggerType] = useState<string>("")
  const [triggerConfig, setTriggerConfig] = useState<TriggerConfig | null>(null)
  const [selectedActions, setSelectedActions] = useState<ActionConfig[]>([])
  const [configuringActionIndex, setConfiguringActionIndex] = useState<number | null>(null)

  const { data: triggersData } = useTriggerRegistry()
  const { data: actionsData } = useActionRegistry()

  const triggers = triggersData?.triggers || []
  const actions = (actionsData as any)?.actions || []

  const totalSteps = 5
  const progress = (step / totalSteps) * 100

  const selectedTrigger = useMemo(() => {
    // Find trigger by type (api-call, scheduler, event) or by id
    return triggers.find((t) => t.type === selectedTriggerType || t.id === selectedTriggerType)
  }, [triggers, selectedTriggerType])

  const configuringAction = useMemo(() => {
    if (configuringActionIndex === null) return null
    return selectedActions[configuringActionIndex]
  }, [selectedActions, configuringActionIndex])

  const handleNext = () => {
    if (step < totalSteps && canProceed()) {
      setStep((s) => (s + 1) as WizardStep)
    }
  }

  const handleBack = () => {
    if (step > 1) {
      setStep((s) => (s - 1) as WizardStep)
    }
  }

  const canProceed = (): boolean => {
    switch (step) {
      case 1:
        return selectedTriggerType !== ""
      case 2:
        return triggerConfig !== null
      case 3:
        return selectedActions.length > 0
      case 4:
        return configuringActionIndex === null && selectedActions.every((a) => Object.keys(a.config).length > 0)
      case 5:
        return workflowName.trim() !== ""
      default:
        return false
    }
  }

  const handleAddAction = (actionType: string, registryId?: string) => {
    const newAction: ActionConfig = {
      id: `action-${Date.now()}-${Math.random()}`,
      type: actionType,
      registryId,
      config: {},
    }
    setSelectedActions([...selectedActions, newAction])
    setConfiguringActionIndex(selectedActions.length)
  }

  const handleRemoveAction = (index: number) => {
    setSelectedActions(selectedActions.filter((_, i) => i !== index))
    if (configuringActionIndex === index) {
      setConfiguringActionIndex(null)
    } else if (configuringActionIndex !== null && configuringActionIndex > index) {
      setConfiguringActionIndex(configuringActionIndex - 1)
    }
  }

  const handleUpdateActionConfig = (index: number, config: Record<string, unknown>) => {
    const updated = [...selectedActions]
    updated[index] = { ...updated[index], config }
    setSelectedActions(updated)
  }

  const handleFinish = async () => {
    if (!triggerConfig || selectedActions.length === 0) {
      toast({
        variant: "destructive",
        title: "Validation Error",
        description: "Please complete all required steps",
      })
      return
    }

    try {
      // Build workflow definition
      const nodes: WorkflowNode[] = []
      const edges: WorkflowEdge[] = []

      // Add trigger node with new structure
      // Use triggerConfigId from trigger registry if available
      const triggerNodeId = selectedTrigger?.id || triggerConfig.triggerConfigId
      if (!triggerNodeId) {
        toast({
          variant: "destructive",
          title: "Validation Error",
          description: "Trigger config ID is required. Please select a trigger from registry.",
        })
        return
      }

      const triggerNode: WorkflowNode = {
        id: "trigger-1",
        type: `event-trigger` as any, // Placeholder type, will be normalized
        position: { x: 250, y: 100 },
        data: {
          label: selectedTrigger?.name || triggerConfig.triggerType,
          config: {
            triggerConfigId: triggerNodeId,
            triggerType: triggerConfig.triggerType,
            ...(triggerConfig.instanceConfig && { instanceConfig: triggerConfig.instanceConfig }),
          },
        },
      }
      nodes.push(triggerNode)

      // Add action nodes with new structure
      selectedActions.forEach((action, index) => {
        // Find action from registry
        const actionRegistryItem = actions.find((a) => a.id === action.registryId || a.type === action.type)
        if (!action.registryId && !actionRegistryItem) {
          toast({
            variant: "destructive",
            title: "Validation Error",
            description: `Action ${action.type} not found in registry. Please select a valid action.`,
          })
          return
        }

        const actionRegistryId = action.registryId || actionRegistryItem?.id
        if (!actionRegistryId) {
          toast({
            variant: "destructive",
            title: "Validation Error",
            description: `Action registry ID is required for ${action.type}.`,
          })
          return
        }

        const actionNode: WorkflowNode = {
          id: action.id,
          type: `api-trigger` as any, // Placeholder type, will be normalized to ACTION
          position: { x: 250, y: 250 + index * 150 },
          data: {
            label: actionRegistryItem?.name || action.type,
            config: {
              registryId: actionRegistryId,
              configValues: action.config, // Store config values in configValues
            },
          },
        }
        nodes.push(actionNode)

        // Add edge from previous node
        const sourceId = index === 0 ? triggerNode.id : selectedActions[index - 1].id
        edges.push({
          id: `edge-${sourceId}-${action.id}`,
          source: sourceId,
          target: action.id,
        })
      })

      const workflowDefinition: WorkflowDefinition = {
        name: workflowName,
        description: workflowDescription,
        status: "draft",
        nodes,
        edges,
      }

      // Normalize workflow definition before saving (convert to backend format)
      const normalizedDefinition = normalizeWorkflowDefinition(workflowDefinition)

      const created = await createWorkflow.mutateAsync({
        name: normalizedDefinition.name,
        description: normalizedDefinition.description,
        definition: normalizedDefinition,
        status: "draft",
      })

      toast({
        title: "Workflow Created",
        description: "Your workflow has been created successfully",
      })

      if (onComplete) {
        onComplete()
      } else {
        navigate(`/workflows/${created.id}`)
      }
    } catch (error) {
      toast({
        variant: "destructive",
        title: "Creation Failed",
        description: error instanceof Error ? error.message : "Failed to create workflow",
      })
    }
  }

  return (
    <div className="container mx-auto p-6 max-w-4xl">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Guided Workflow Creation</CardTitle>
              <CardDescription>Step {step} of {totalSteps}</CardDescription>
            </div>
            <Button variant="ghost" onClick={onCancel || (() => navigate("/workflows"))}>
              Cancel
            </Button>
          </div>
          <Progress value={progress} className="mt-4" />
        </CardHeader>
        <CardContent className="py-6">
          {/* Step 1: Select Trigger Type */}
          {step === 1 && (
            <div className="space-y-6">
              <div>
                <h2 className="text-xl font-semibold mb-2">Select Trigger Type</h2>
                <p className="text-secondary-600 mb-4">
                  Choose the event that will start your workflow
                </p>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {triggers.map((trigger) => (
                  <div
                    key={trigger.type}
                    className={`p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                      selectedTriggerType === trigger.type
                        ? "border-primary-600 bg-primary-50"
                        : "border-secondary-200 hover:border-secondary-300"
                    }`}
                    onClick={() => setSelectedTriggerType(trigger.type)}
                  >
                    <div className="font-semibold">{trigger.name}</div>
                    <p className="text-sm text-secondary-500 mt-1">{trigger.description}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Step 2: Configure Trigger */}
          {step === 2 && selectedTrigger && (
            <div className="space-y-6">
              <div>
                <h2 className="text-xl font-semibold mb-2">Configure Trigger</h2>
                <p className="text-secondary-600 mb-4">
                  Configure the settings for your {selectedTrigger.name} trigger
                </p>
              </div>
              <div className="space-y-4">
                {/* Basic trigger config form - simplified for now */}
                <div className="space-y-2">
                  <Label>Trigger Type</Label>
                  <div className="p-3 bg-secondary-50 rounded-md">
                    <Badge>{selectedTrigger.type}</Badge>
                  </div>
                </div>
                <div className="space-y-2">
                  <Label>Select Trigger Config</Label>
                  <p className="text-xs text-secondary-500 mb-2">
                    Select an existing trigger config from registry or create a new one.
                  </p>
                  <div className="space-y-2">
                    {triggers
                      .filter((t) => t.type === selectedTriggerType)
                      .map((trigger) => (
                        <div
                          key={trigger.id}
                          className={`p-3 border-2 rounded-lg cursor-pointer transition-colors ${
                            triggerConfig?.triggerConfigId === trigger.id
                              ? "border-primary-600 bg-primary-50"
                              : "border-secondary-200 hover:border-secondary-300"
                          }`}
                          onClick={() => {
                            setTriggerConfig({
                              triggerConfigId: trigger.id,
                              triggerType: trigger.type,
                              instanceConfig: {},
                            })
                          }}
                        >
                          <div className="font-semibold">{trigger.name}</div>
                          <p className="text-sm text-secondary-500 mt-1">{trigger.description}</p>
                        </div>
                      ))}
                  </div>
                  {triggerConfig && (
                    <div className="mt-4 p-3 bg-secondary-50 rounded-md">
                      <Label className="text-sm font-semibold">Instance Config (Optional)</Label>
                      <Textarea
                        placeholder='Enter instance-specific config as JSON, e.g., {"consumerGroup": "workflow-123-consumer"}'
                        rows={4}
                        onChange={(e) => {
                          try {
                            const instanceConfig = JSON.parse(e.target.value || "{}")
                            setTriggerConfig({
                              ...triggerConfig,
                              instanceConfig,
                            })
                          } catch {
                            // Invalid JSON, ignore
                          }
                        }}
                      />
                      <p className="text-xs text-secondary-500 mt-1">
                        Optional: Override trigger config settings for this workflow instance.
                      </p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Step 3: Add Actions */}
          {step === 3 && (
            <div className="space-y-6">
              <div>
                <h2 className="text-xl font-semibold mb-2">Add Actions</h2>
                <p className="text-secondary-600 mb-4">
                  Select the actions to execute in your workflow
                </p>
              </div>
              <div className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {actions.map((action) => (
                    <div
                      key={action.id}
                      className="p-4 border rounded-lg hover:border-primary-300 transition-colors"
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="font-semibold">{action.name}</div>
                          <p className="text-sm text-secondary-500 mt-1">{action.description}</p>
                          <Badge variant="outline" className="mt-2 text-xs">
                            {action.type}
                          </Badge>
                        </div>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleAddAction(action.type, action.id)}
                        >
                          <Plus className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
                {selectedActions.length > 0 && (
                  <div className="mt-6 space-y-2">
                    <Label>Selected Actions ({selectedActions.length})</Label>
                    <div className="space-y-2">
                      {selectedActions.map((action, index) => (
                        <div
                          key={action.id}
                          className="flex items-center justify-between p-3 bg-secondary-50 rounded-md"
                        >
                          <div className="flex items-center space-x-2">
                            <Badge>{index + 1}</Badge>
                            <span className="font-medium">
                              {actions.find((a) => a.id === action.registryId || a.type === action.type)?.name || action.type}
                            </span>
                          </div>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleRemoveAction(index)}
                          >
                            <X className="h-4 w-4" />
                          </Button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Step 4: Configure Actions */}
          {step === 4 && (
            <div className="space-y-6">
              <div>
                <h2 className="text-xl font-semibold mb-2">Configure Actions</h2>
                <p className="text-secondary-600 mb-4">
                  Configure each action in your workflow
                </p>
              </div>
              <div className="space-y-4">
                {selectedActions.map((action, index) => (
                  <Card key={action.id} className={configuringActionIndex === index ? "border-primary-600" : ""}>
                    <CardHeader>
                      <div className="flex items-center justify-between">
                        <CardTitle className="text-base">
                          Action {index + 1}: {actions.find((a) => a.id === action.registryId || a.type === action.type)?.name || action.type}
                        </CardTitle>
                        <Button
                          size="sm"
                          variant={configuringActionIndex === index ? "default" : "outline"}
                          onClick={() => setConfiguringActionIndex(configuringActionIndex === index ? null : index)}
                        >
                          {configuringActionIndex === index ? "Done" : "Configure"}
                        </Button>
                      </div>
                    </CardHeader>
                    {configuringActionIndex === index && (
                      <CardContent>
                        <div className="space-y-4">
                          <div className="space-y-2">
                            <Label>Action Configuration</Label>
                            <Textarea
                              placeholder='Enter action configuration as JSON'
                              rows={6}
                              defaultValue={JSON.stringify(action.config, null, 2)}
                              onChange={(e) => {
                                try {
                                  const config = JSON.parse(e.target.value || "{}")
                                  handleUpdateActionConfig(index, config)
                                } catch {
                                  // Invalid JSON, ignore
                                }
                              }}
                            />
                          </div>
                        </div>
                      </CardContent>
                    )}
                  </Card>
                ))}
              </div>
            </div>
          )}

          {/* Step 5: Review and Save */}
          {step === 5 && (
            <div className="space-y-6">
              <div>
                <h2 className="text-xl font-semibold mb-2">Review and Save</h2>
                <p className="text-secondary-600 mb-4">
                  Review your workflow configuration before creating
                </p>
              </div>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="workflow-name">Workflow Name *</Label>
                  <Input
                    id="workflow-name"
                    value={workflowName}
                    onChange={(e) => setWorkflowName(e.target.value)}
                    placeholder="Enter workflow name"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="workflow-description">Description</Label>
                  <Textarea
                    id="workflow-description"
                    value={workflowDescription}
                    onChange={(e) => setWorkflowDescription(e.target.value)}
                    placeholder="Enter workflow description"
                    rows={4}
                  />
                </div>
                <div className="p-4 bg-secondary-50 rounded-lg space-y-3">
                  <div>
                    <div className="text-sm font-semibold text-secondary-700">Trigger</div>
                    <div className="text-secondary-900">{selectedTrigger?.name || selectedTriggerType}</div>
                  </div>
                  <div>
                    <div className="text-sm font-semibold text-secondary-700">Actions ({selectedActions.length})</div>
                    <div className="space-y-1 mt-1">
                      {selectedActions.map((action, index) => (
                        <div key={action.id} className="text-secondary-900">
                          {index + 1}. {actions.find((a) => a.type === action.type)?.name || action.type}
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </CardContent>
        <CardContent className="border-t pt-6">
          <div className="flex items-center justify-between">
            <Button variant="outline" onClick={handleBack} disabled={step === 1}>
              <ChevronLeft className="h-4 w-4 mr-2" />
              Back
            </Button>
            {step < totalSteps ? (
              <Button onClick={handleNext} disabled={!canProceed()}>
                Next
                <ChevronRight className="h-4 w-4 ml-2" />
              </Button>
            ) : (
              <Button onClick={handleFinish} disabled={!canProceed() || createWorkflow.isPending}>
                {createWorkflow.isPending ? (
                  <>
                    <div className="h-4 w-4 mr-2 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    Creating...
                  </>
                ) : (
                  <>
                    <Check className="h-4 w-4 mr-2" />
                    Create Workflow
                  </>
                )}
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

