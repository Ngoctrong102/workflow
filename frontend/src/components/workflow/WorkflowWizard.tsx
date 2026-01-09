import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Progress } from "@/components/ui/progress"
import { ChevronLeft, ChevronRight, Check, Loader2 } from "lucide-react"
import { useTemplateLibrary } from "@/hooks/use-template-library"
import type { WorkflowDefinition } from "@/types/workflow"

interface WorkflowWizardProps {
  onComplete: (workflow: Partial<WorkflowDefinition>) => void
  onCancel: () => void
}

type WorkflowType = "welcome" | "notification" | "reminder" | "marketing" | "custom"

const workflowTypes: Array<{ value: WorkflowType; label: string; description: string }> = [
  {
    value: "welcome",
    label: "Welcome Workflow",
    description: "Send welcome messages to new users",
  },
  {
    value: "notification",
    label: "Notification Workflow",
    description: "Send notifications for events",
  },
  {
    value: "reminder",
    label: "Reminder Workflow",
    description: "Send reminder notifications",
  },
  {
    value: "marketing",
    label: "Marketing Workflow",
    description: "Send marketing campaigns",
  },
  {
    value: "custom",
    label: "Custom Workflow",
    description: "Create a custom workflow from scratch",
  },
]

export function WorkflowWizard({ onComplete, onCancel }: WorkflowWizardProps) {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [workflowType, setWorkflowType] = useState<WorkflowType | "">("")
  const [selectedTemplate, setSelectedTemplate] = useState<string | null>(null)
  const [name, setName] = useState("")
  const [description, setDescription] = useState("")

  const { data: templatesData } = useTemplateLibrary({
    category: workflowType || undefined,
    limit: 20,
  })

  const templates = templatesData?.data || []
  const totalSteps = 4
  const progress = (step / totalSteps) * 100

  const handleNext = () => {
    if (step < totalSteps) {
      setStep(step + 1)
    }
  }

  const handleBack = () => {
    if (step > 1) {
      setStep(step - 1)
    }
  }

  const handleFinish = () => {
    const workflow: Partial<WorkflowDefinition> = {
      name,
      description,
      status: "draft",
      nodes: [],
      edges: [],
    }

    if (selectedTemplate) {
      // Template will be loaded in the builder
      navigate(`/workflows/new?template=${selectedTemplate}&name=${encodeURIComponent(name)}&description=${encodeURIComponent(description || "")}`)
    } else {
      onComplete(workflow)
    }
  }

  const canProceed = () => {
    switch (step) {
      case 1:
        return workflowType !== ""
      case 2:
        return true // Template selection is optional
      case 3:
        return name.trim() !== ""
      case 4:
        return true
      default:
        return false
    }
  }

  return (
    <div className="container mx-auto p-6 max-w-4xl">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Create Workflow</CardTitle>
              <CardDescription>Step {step} of {totalSteps}</CardDescription>
            </div>
            <Button variant="ghost" onClick={onCancel}>
              Cancel
            </Button>
          </div>
          <Progress value={progress} className="mt-4" />
        </CardHeader>
        <CardContent className="py-6">
          {/* Step 1: Select Workflow Type */}
          {step === 1 && (
            <div className="space-y-6">
              <div>
                <h2 className="text-xl font-semibold mb-2">Select Workflow Type</h2>
                <p className="text-secondary-600 mb-4">
                  Choose the type of workflow you want to create
                </p>
              </div>
              <RadioGroup value={workflowType} onValueChange={(v) => setWorkflowType(v as WorkflowType)}>
                <div className="space-y-4">
                  {workflowTypes.map((type) => (
                    <div
                      key={type.value}
                      className={`p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                        workflowType === type.value
                          ? "border-primary-600 bg-primary-50"
                          : "border-secondary-200 hover:border-secondary-300"
                      }`}
                      onClick={() => setWorkflowType(type.value)}
                    >
                      <div className="flex items-start space-x-3">
                        <RadioGroupItem value={type.value} id={type.value} />
                        <div className="flex-1">
                          <Label htmlFor={type.value} className="font-semibold cursor-pointer">
                            {type.label}
                          </Label>
                          <p className="text-sm text-secondary-500 mt-1">{type.description}</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </RadioGroup>
            </div>
          )}

          {/* Step 2: Choose Template (Optional) */}
          {step === 2 && (
            <div className="space-y-6">
              <div>
                <h2 className="text-xl font-semibold mb-2">Choose Template (Optional)</h2>
                <p className="text-secondary-600 mb-4">
                  Select a template to start from, or skip to create from scratch
                </p>
              </div>
              {templates.length === 0 ? (
                <div className="text-center py-8 text-secondary-500">
                  <p>No templates available for this workflow type</p>
                  <Button variant="outline" className="mt-4" onClick={handleNext}>
                    Skip and Continue
                  </Button>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div
                    className={`p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                      selectedTemplate === null
                        ? "border-primary-600 bg-primary-50"
                        : "border-secondary-200 hover:border-secondary-300"
                    }`}
                    onClick={() => setSelectedTemplate(null)}
                  >
                    <div className="font-semibold">Start from Scratch</div>
                    <p className="text-sm text-secondary-500 mt-1">
                      Create a custom workflow from the beginning
                    </p>
                  </div>
                  {templates.map((template) => (
                    <div
                      key={template.id}
                      className={`p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                        selectedTemplate === template.id
                          ? "border-primary-600 bg-primary-50"
                          : "border-secondary-200 hover:border-secondary-300"
                      }`}
                      onClick={() => setSelectedTemplate(template.id)}
                    >
                      <div className="font-semibold">{template.name}</div>
                      <p className="text-sm text-secondary-500 mt-1">{template.description}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Step 3: Basic Configuration */}
          {step === 3 && (
            <div className="space-y-6">
              <div>
                <h2 className="text-xl font-semibold mb-2">Basic Configuration</h2>
                <p className="text-secondary-600 mb-4">
                  Provide basic information about your workflow
                </p>
              </div>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Workflow Name *</Label>
                  <Input
                    id="name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Enter workflow name"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Enter workflow description"
                    rows={4}
                  />
                </div>
              </div>
            </div>
          )}

          {/* Step 4: Review and Confirm */}
          {step === 4 && (
            <div className="space-y-6">
              <div>
                <h2 className="text-xl font-semibold mb-2">Review and Confirm</h2>
                <p className="text-secondary-600 mb-4">
                  Review your workflow configuration before creating
                </p>
              </div>
              <div className="space-y-4 p-4 bg-secondary-50 rounded-lg">
                <div>
                  <div className="text-sm font-semibold text-secondary-700">Workflow Type</div>
                  <div className="text-secondary-900">
                    {workflowTypes.find((t) => t.value === workflowType)?.label}
                  </div>
                </div>
                <div>
                  <div className="text-sm font-semibold text-secondary-700">Template</div>
                  <div className="text-secondary-900">
                    {selectedTemplate
                      ? templates.find((t) => t.id === selectedTemplate)?.name || "Selected"
                      : "Start from Scratch"}
                  </div>
                </div>
                <div>
                  <div className="text-sm font-semibold text-secondary-700">Name</div>
                  <div className="text-secondary-900">{name}</div>
                </div>
                {description && (
                  <div>
                    <div className="text-sm font-semibold text-secondary-700">Description</div>
                    <div className="text-secondary-900">{description}</div>
                  </div>
                )}
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
              <Button onClick={handleFinish} disabled={!canProceed()}>
                <Check className="h-4 w-4 mr-2" />
                Create Workflow
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

