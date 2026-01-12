import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Workflow, BarChart3, ArrowRight } from "lucide-react"
import { useNavigate, Link } from "react-router-dom"

interface QuickAction {
  label: string
  icon: React.ReactNode
  onClick: () => void
  variant?: "default" | "secondary" | "outline"
}

export function QuickActions() {
  const navigate = useNavigate()

  const actions: QuickAction[] = [
    {
      label: "Create Workflow",
      icon: <Workflow className="h-4 w-4" />,
      onClick: () => navigate("/workflows/new"),
      variant: "default",
    },
    {
      label: "View Analytics",
      icon: <BarChart3 className="h-4 w-4" />,
      onClick: () => navigate("/analytics"),
      variant: "outline",
    },
  ]

  return (
    <Card>
      <CardHeader className="px-4 py-3">
        <CardTitle className="text-base">Quick Actions</CardTitle>
        <CardDescription className="text-xs">Common actions and shortcuts</CardDescription>
      </CardHeader>
      <CardContent className="px-4 pb-3">
        <div className="space-y-3">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
            {actions.map((action) => (
              <Button
                key={action.label}
                variant={action.variant}
                size="sm"
                onClick={action.onClick}
                className="justify-start cursor-pointer"
              >
                {action.icon}
                <span className="ml-1.5 text-xs">{action.label}</span>
              </Button>
            ))}
          </div>
          <div className="flex items-center gap-4 pt-2 border-t border-secondary-200">
            <Link
              to="/workflows"
              className="flex items-center text-sm text-primary-600 hover:text-primary-700 transition-colors"
            >
              View all workflows
              <ArrowRight className="h-3 w-3 ml-1" />
            </Link>
            <Link
              to="/executions"
              className="flex items-center text-sm text-primary-600 hover:text-primary-700 transition-colors"
            >
              View all executions
              <ArrowRight className="h-3 w-3 ml-1" />
            </Link>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

