import { lazy, Suspense } from "react"
import { Skeleton } from "@/components/ui/skeleton"
import { Card, CardContent } from "@/components/ui/card"

// Lazy load react-flow for code splitting
const WorkflowCanvas = lazy(() => import("./WorkflowCanvas").then((module) => ({ default: module.WorkflowCanvas })))

function CanvasSkeleton() {
  return (
    <Card className="h-full">
      <CardContent className="p-6">
        <div className="space-y-4">
          <Skeleton className="h-8 w-64" />
          <Skeleton className="h-96 w-full" />
        </div>
      </CardContent>
    </Card>
  )
}

export function LazyWorkflowCanvas(props: React.ComponentProps<typeof WorkflowCanvas>) {
  return (
    <Suspense fallback={<CanvasSkeleton />}>
      <WorkflowCanvas {...props} />
    </Suspense>
  )
}

