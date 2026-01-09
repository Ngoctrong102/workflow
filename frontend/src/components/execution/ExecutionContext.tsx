import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"

interface ExecutionContextProps {
  executionId?: string
  context?: Record<string, unknown>
}

export function ExecutionContext({ executionId: _executionId, context }: ExecutionContextProps) {
  if (!context || Object.keys(context).length === 0) {
    return (
      <Card>
        <CardContent className="py-12 text-center text-secondary-500">
          <p>No context data available</p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Execution Context</CardTitle>
        <CardDescription>Data passed to the workflow execution</CardDescription>
      </CardHeader>
      <CardContent>
        <pre className="bg-secondary-100 p-4 rounded-lg overflow-x-auto text-sm">
          {JSON.stringify(context, null, 2)}
        </pre>
      </CardContent>
    </Card>
  )
}

