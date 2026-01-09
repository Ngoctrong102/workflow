import { useParams, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import { ABTestEditor as ABTestEditorComponent } from "@/components/ab-test/ABTestEditor"
import { useABTest, useCreateABTest, useUpdateABTest } from "@/hooks/use-ab-tests"
import { useToast } from "@/hooks/use-toast"
import type { ABTest } from "@/types/ab-test"

export default function ABTestEditorPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { toast } = useToast()
  const { data: test, isLoading } = useABTest(id)
  const createTest = useCreateABTest()
  const updateTest = useUpdateABTest()

  const isEditMode = !!id

  const handleSave = async (testData: Omit<ABTest, "id" | "created_at" | "updated_at">) => {
    try {
      if (isEditMode && id) {
        await updateTest.mutateAsync({ testId: id, test: testData })
        toast({
          title: "A/B Test Updated",
          description: "The A/B test has been updated successfully.",
        })
      } else {
        const newTest = await createTest.mutateAsync(testData)
        toast({
          title: "A/B Test Created",
          description: "The A/B test has been created successfully.",
        })
        navigate(`/ab-tests/${newTest.id}`)
      }
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to save A/B test",
        variant: "destructive",
      })
    }
  }

  const handleCancel = () => {
    navigate("/ab-tests")
  }

  if (isEditMode && isLoading) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardContent className="py-12 text-center">
            <p className="text-secondary-500">Loading...</p>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      <div className="flex items-center space-x-4">
        <Button variant="ghost" size="sm" onClick={() => navigate("/ab-tests")}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
        <div>
          <h1 className="text-3xl font-bold">{isEditMode ? "Edit A/B Test" : "New A/B Test"}</h1>
          <p className="text-secondary-600 mt-2">
            {isEditMode ? "Update A/B test configuration" : "Create a new A/B test"}
          </p>
        </div>
      </div>

      <ABTestEditorComponent
        test={test}
        onSave={handleSave}
        onCancel={handleCancel}
      />
    </div>
  )
}

