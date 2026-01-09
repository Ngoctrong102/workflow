import { useParams, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Skeleton } from "@/components/ui/skeleton"
import {
  ArrowLeft,
  Edit,
  Play,
  Pause,
  Square,
  RefreshCw,
  CheckCircle2,
  Clock,
  Archive,
  Edit as EditIcon,
} from "lucide-react"
import { useABTest, useStartABTest, usePauseABTest, useStopABTest } from "@/hooks/use-ab-tests"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { formatDistanceToNow } from "date-fns"
import { ABTestResults } from "@/components/ab-test/ABTestResults"

const statusConfig: Record<string, { label: string; color: string; icon: typeof CheckCircle2 }> = {
  draft: {
    label: "Draft",
    color: "bg-secondary-600",
    icon: Edit,
  },
  running: {
    label: "Running",
    color: "bg-primary-600",
    icon: Play,
  },
  paused: {
    label: "Paused",
    color: "bg-warning-600",
    icon: Pause,
  },
  completed: {
    label: "Completed",
    color: "bg-success-600",
    icon: CheckCircle2,
  },
  archived: {
    label: "Archived",
    color: "bg-secondary-400",
    icon: Archive,
  },
}

export default function ABTestDetails() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: test, isLoading, error, refetch } = useABTest(id)
  const startTest = useStartABTest()
  const pauseTest = usePauseABTest()
  const stopTest = useStopABTest()
  const { confirm } = useConfirmDialog()

  const handleStart = async () => {
    if (!id) return
    await startTest.mutateAsync(id)
    refetch()
  }

  const handlePause = async () => {
    if (!id) return
    await pauseTest.mutateAsync(id)
    refetch()
  }

  const handleStop = async () => {
    if (!id) return

    const confirmed = await confirm({
      title: "Stop A/B Test",
      description: "Are you sure you want to stop this test? Results will be finalized.",
      confirmText: "Stop",
    })

    if (confirmed) {
      await stopTest.mutateAsync(id)
      refetch()
    }
  }

  if (isLoading) {
    return (
      <div className="container mx-auto p-6 space-y-6">
        <Skeleton className="h-10 w-1/3" />
        <Skeleton className="h-64 w-full" />
      </div>
    )
  }

  if (error || !test) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardContent className="py-12 text-center">
            <p className="text-error-600">Failed to load A/B test</p>
            <p className="text-sm mt-2 text-secondary-500">
              {error instanceof Error ? error.message : "Unknown error"}
            </p>
            <Button variant="outline" className="mt-4" onClick={() => navigate("/ab-tests")}>
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to A/B Tests
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  const status = statusConfig[test.status] || statusConfig.draft
  const StatusIcon = status.icon

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <Button variant="ghost" size="sm" onClick={() => navigate("/ab-tests")}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <div>
            <h1 className="text-3xl font-bold">{test.name}</h1>
            <p className="text-secondary-600 mt-2">{test.description || "A/B Test Details"}</p>
          </div>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="outline" onClick={() => refetch()}>
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
          {test.status === "draft" && (
            <>
              <Button variant="outline" onClick={() => navigate(`/ab-tests/${test.id}`)}>
                <EditIcon className="h-4 w-4 mr-2" />
                Edit
              </Button>
              <Button onClick={handleStart} disabled={startTest.isPending}>
                <Play className="h-4 w-4 mr-2" />
                Start Test
              </Button>
            </>
          )}
          {test.status === "running" && (
            <>
              <Button variant="outline" onClick={handlePause} disabled={pauseTest.isPending}>
                <Pause className="h-4 w-4 mr-2" />
                Pause
              </Button>
              <Button variant="destructive" onClick={handleStop} disabled={stopTest.isPending}>
                <Square className="h-4 w-4 mr-2" />
                Stop
              </Button>
            </>
          )}
          {test.status === "paused" && (
            <Button onClick={handleStart} disabled={startTest.isPending}>
              <Play className="h-4 w-4 mr-2" />
              Resume
            </Button>
          )}
        </div>
      </div>

      {/* Test Info */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center space-x-2">
                <StatusIcon className={`h-5 w-5 ${status.color.replace("bg-", "text-")}`} />
                <span>{status.label}</span>
              </CardTitle>
              <CardDescription className="mt-2">
                Workflow: {test.workflow_name || test.workflow_id}
              </CardDescription>
            </div>
            <Badge variant="default" className={status.color}>
              {status.label}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <div className="text-sm text-secondary-500">Success Metric</div>
              <div className="font-medium capitalize">{test.success_metric.replace("_", " ")}</div>
            </div>
            <div>
              <div className="text-sm text-secondary-500">Duration</div>
              <div className="font-medium">{test.duration_days || "-"} days</div>
            </div>
            <div>
              <div className="text-sm text-secondary-500">Min Sample Size</div>
              <div className="font-medium">{test.min_sample_size?.toLocaleString() || "-"}</div>
            </div>
            <div>
              <div className="text-sm text-secondary-500">Variants</div>
              <div className="font-medium">{test.variants.length}</div>
            </div>
          </div>

          {/* Variants */}
          <div className="mt-6">
            <h3 className="font-semibold mb-4">Variants</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {test.variants.map((variant) => (
                <Card key={variant.id} className="border-2">
                  <CardHeader className="pb-3">
                    <CardTitle className="text-lg">
                      Variant {variant.label} - {variant.traffic_percentage}%
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2 text-sm">
                      <div>
                        <span className="text-secondary-500">Name:</span> {variant.name}
                      </div>
                      {variant.template_id && (
                        <div>
                          <span className="text-secondary-500">Template:</span> {variant.template_id}
                        </div>
                      )}
                      {variant.channel && (
                        <div>
                          <span className="text-secondary-500">Channel:</span> {variant.channel}
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs defaultValue="results" className="space-y-4">
        <TabsList>
          <TabsTrigger value="results">Results</TabsTrigger>
        </TabsList>
        <TabsContent value="results">
          {id && <ABTestResults testId={id} />}
        </TabsContent>
      </Tabs>
    </div>
  )
}

