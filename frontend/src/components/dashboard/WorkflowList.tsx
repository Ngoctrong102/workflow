import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { Link } from "react-router-dom"
import { ArrowRight, Workflow as WorkflowIcon } from "lucide-react"
import { format } from "date-fns"
import type { Workflow } from "@/services/workflow-service"

interface WorkflowListProps {
  workflows?: Workflow[]
  isLoading?: boolean
  limit?: number
}

const statusColors = {
  draft: "secondary",
  active: "default",
  inactive: "secondary",
  paused: "warning",
  archived: "secondary",
} as const

export function WorkflowList({ workflows = [], isLoading = false, limit = 5 }: WorkflowListProps) {
  const displayWorkflows = workflows.slice(0, limit)

  if (isLoading) {
    return (
      <Card>
        <CardHeader className="px-4 py-3">
          <CardTitle className="text-base">Recent Workflows</CardTitle>
          <CardDescription className="text-xs">Latest workflows</CardDescription>
        </CardHeader>
        <CardContent className="px-4 pb-3">
          <div className="space-y-2">
            {[1, 2, 3, 4, 5].map((i) => (
              <Skeleton key={i} className="h-16 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (displayWorkflows.length === 0) {
    return (
      <Card>
        <CardHeader className="px-4 py-3">
          <CardTitle className="text-base">Recent Workflows</CardTitle>
          <CardDescription className="text-xs">Latest workflows</CardDescription>
        </CardHeader>
        <CardContent className="px-4 pb-3">
          <div className="text-center py-6 text-secondary-500 text-sm">
            <WorkflowIcon className="h-8 w-8 mx-auto mb-2 text-secondary-300" />
            <p>No workflows found</p>
            <p className="text-xs mt-1">Create your first workflow to get started</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="px-4 py-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-base">Recent Workflows</CardTitle>
            <CardDescription className="text-xs">Latest workflows</CardDescription>
          </div>
          <Link to="/workflows">
            <Button variant="ghost" size="sm" className="text-xs">
              View All
              <ArrowRight className="h-3 w-3 ml-1" />
            </Button>
          </Link>
        </div>
      </CardHeader>
      <CardContent className="px-4 pb-3">
        <div className="space-y-2">
          {displayWorkflows.map((workflow) => (
            <Link
              key={workflow.id}
              to={`/workflows/${workflow.id}`}
              className="block p-2 rounded-md border border-secondary-200 hover:bg-secondary-50 transition-colors duration-200"
            >
              <div className="flex items-center justify-between">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center space-x-2">
                    <h4 className="font-medium text-sm text-secondary-900 truncate">
                      {workflow.name}
                    </h4>
                    <Badge
                      variant={statusColors[workflow.status] || "secondary"}
                      className="text-xs px-1.5 py-0"
                    >
                      {workflow.status}
                    </Badge>
                  </div>
                  {workflow.description && (
                    <p className="text-xs text-secondary-500 mt-0.5 truncate">
                      {workflow.description}
                    </p>
                  )}
                  <div className="text-xs text-secondary-400 mt-1">
                    Updated {format(new Date(workflow.updatedAt), "MMM dd, yyyy")}
                  </div>
                </div>
                <ArrowRight className="h-4 w-4 text-secondary-400 ml-2 flex-shrink-0" />
              </div>
            </Link>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}

