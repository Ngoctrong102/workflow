import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { WorkflowWizard as TemplateWizardComponent } from "@/components/workflow/WorkflowWizard"
import { GuidedWorkflowWizard } from "@/components/workflow/GuidedWorkflowWizard"
import type { WorkflowDefinition } from "@/types/workflow"

type WizardMode = "template" | "guided"

export default function WorkflowWizardPage() {
  const navigate = useNavigate()
  const [mode, setMode] = useState<WizardMode>("template")

  const handleTemplateComplete = (workflow: Partial<WorkflowDefinition>) => {
    // Navigate to workflow builder with the workflow data
    navigate("/workflows/new", {
      state: { workflow },
    })
  }

  const handleTemplateCancel = () => {
    navigate("/workflows")
  }

  const handleGuidedComplete = () => {
    navigate("/workflows")
  }

  const handleGuidedCancel = () => {
    navigate("/workflows")
  }

  if (mode === "guided") {
    return <GuidedWorkflowWizard onComplete={handleGuidedComplete} onCancel={handleGuidedCancel} />
  }

  return (
    <div className="container mx-auto p-6 max-w-2xl">
      <Card>
        <CardContent className="p-6">
          <div className="space-y-4">
            <div>
              <h1 className="text-2xl font-bold mb-2">Create Workflow</h1>
              <p className="text-secondary-600">Choose how you want to create your workflow</p>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div
                className={`p-6 border-2 rounded-lg cursor-pointer transition-colors ${
                  mode === "template"
                    ? "border-primary-600 bg-primary-50"
                    : "border-secondary-200 hover:border-secondary-300"
                }`}
                onClick={() => setMode("template")}
              >
                <h3 className="font-semibold mb-2">Template-Based</h3>
                <p className="text-sm text-secondary-500">
                  Start from a pre-built template or create from scratch
                </p>
              </div>
              <div
                className={`p-6 border-2 rounded-lg cursor-pointer transition-colors ${
                  mode === "guided"
                    ? "border-primary-600 bg-primary-50"
                    : "border-secondary-200 hover:border-secondary-300"
                }`}
                onClick={() => setMode("guided")}
              >
                <h3 className="font-semibold mb-2">Guided Creation</h3>
                <p className="text-sm text-secondary-500">
                  Step-by-step wizard to build your workflow
                </p>
              </div>
            </div>
            <div className="flex justify-end">
              <Button variant="outline" onClick={handleTemplateCancel}>
                Cancel
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
      {mode === "template" && (
        <div className="mt-6">
          <TemplateWizardComponent onComplete={handleTemplateComplete} onCancel={handleTemplateCancel} />
        </div>
      )}
    </div>
  )
}

