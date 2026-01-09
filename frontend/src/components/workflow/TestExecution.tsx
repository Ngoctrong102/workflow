import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Play, X, CheckCircle2, XCircle, Clock, Loader2 } from "lucide-react"
import { cn } from "@/lib/utils"
import type { Node } from "reactflow"

interface TestExecutionProps {
  nodes: Node[]
  onClose: () => void
  onExecute?: (testData: Record<string, unknown>) => Promise<ExecutionResult>
}

interface ExecutionResult {
  executionId: string
  status: "success" | "failed" | "running"
  nodeExecutions: NodeExecution[]
  error?: string
}

interface NodeExecution {
  nodeId: string
  nodeName: string
  status: "pending" | "running" | "success" | "failed"
  output?: Record<string, unknown>
  error?: string
  duration?: number
}

export function TestExecution({ nodes, onClose, onExecute }: TestExecutionProps) {
  const [testData, setTestData] = useState("{}")
  const [isExecuting, setIsExecuting] = useState(false)
  const [result, setResult] = useState<ExecutionResult | null>(null)

  const handleExecute = async () => {
    try {
      const parsedData = JSON.parse(testData)
      setIsExecuting(true)
      setResult(null)

      if (onExecute) {
        const executionResult = await onExecute(parsedData)
        setResult(executionResult)
      } else {
        // Mock execution for now
        await new Promise((resolve) => setTimeout(resolve, 2000))
        setResult({
          executionId: `exec-${Date.now()}`,
          status: "success",
          nodeExecutions: nodes.map((node) => ({
            nodeId: node.id,
            nodeName: node.data.label || node.id,
            status: "success" as const,
            output: { data: "mock output" },
            duration: Math.random() * 1000,
          })),
        })
      }
    } catch (error) {
      setResult({
        executionId: `exec-${Date.now()}`,
        status: "failed",
        nodeExecutions: [],
        error: error instanceof Error ? error.message : "Invalid JSON",
      })
    } finally {
      setIsExecuting(false)
    }
  }

  const getStatusIcon = (status: NodeExecution["status"]) => {
    switch (status) {
      case "success":
        return <CheckCircle2 className="h-4 w-4 text-success-600" />
      case "failed":
        return <XCircle className="h-4 w-4 text-error-600" />
      case "running":
        return <Loader2 className="h-4 w-4 text-primary-600 animate-spin" />
      default:
        return <Clock className="h-4 w-4 text-secondary-400" />
    }
  }

  const getStatusBadge = (status: NodeExecution["status"]) => {
    switch (status) {
      case "success":
        return <Badge variant="default" className="text-xs bg-success-600">Success</Badge>
      case "failed":
        return <Badge variant="destructive" className="text-xs">Failed</Badge>
      case "running":
        return <Badge variant="default" className="text-xs">Running</Badge>
      default:
        return <Badge variant="secondary" className="text-xs">Pending</Badge>
    }
  }

  return (
    <Card className="h-full flex flex-col">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="flex items-center space-x-2">
              <Play className="h-5 w-5" />
              <span>Test Execution</span>
            </CardTitle>
            <CardDescription>Run workflow with test data</CardDescription>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose}>
            <X className="h-4 w-4" />
          </Button>
        </div>
      </CardHeader>
      <CardContent className="flex-1 overflow-y-auto space-y-4">
        {/* Test Data Input */}
        <div className="space-y-2">
          <Label htmlFor="test-data">Test Data (JSON)</Label>
          <Textarea
            id="test-data"
            value={testData}
            onChange={(e) => setTestData(e.target.value)}
            placeholder='{"key": "value"}'
            className="font-mono text-sm"
            rows={6}
          />
          <p className="text-xs text-secondary-500">
            Enter JSON data to use as trigger input
          </p>
        </div>

        {/* Execute Button */}
        <Button
          onClick={handleExecute}
          disabled={isExecuting}
          className="w-full"
        >
          {isExecuting ? (
            <>
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              Executing...
            </>
          ) : (
            <>
              <Play className="h-4 w-4 mr-2" />
              Run Test
            </>
          )}
        </Button>

        {/* Execution Results */}
        {result && (
          <div className="space-y-4 pt-4 border-t">
            <div className="flex items-center justify-between">
              <h3 className="font-semibold">Execution Results</h3>
              <Badge
                variant={
                  result.status === "success"
                    ? "default"
                    : result.status === "failed"
                    ? "destructive"
                    : "default"
                }
                className={result.status === "success" ? "bg-success-600" : ""}
              >
                {result.status}
              </Badge>
            </div>

            {result.error && (
              <Alert variant="destructive">
                <XCircle className="h-4 w-4" />
                <AlertDescription>{result.error}</AlertDescription>
              </Alert>
            )}

            {result.executionId && (
              <div className="text-sm text-secondary-500">
                Execution ID: {result.executionId}
              </div>
            )}

            {/* Node Executions */}
            <div className="space-y-2">
              <h4 className="font-medium text-sm">Node Executions</h4>
              {result.nodeExecutions.length === 0 ? (
                <p className="text-sm text-secondary-500">No node executions</p>
              ) : (
                <div className="space-y-2">
                  {result.nodeExecutions.map((nodeExec) => (
                    <div
                      key={nodeExec.nodeId}
                      className={cn(
                        "p-3 rounded-lg border",
                        nodeExec.status === "success" &&
                          "border-success-200 bg-success-50",
                        nodeExec.status === "failed" &&
                          "border-error-200 bg-error-50",
                        nodeExec.status === "running" &&
                          "border-primary-200 bg-primary-50",
                        nodeExec.status === "pending" &&
                          "border-secondary-200 bg-secondary-50"
                      )}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center space-x-2">
                          {getStatusIcon(nodeExec.status)}
                          <span className="font-medium text-sm">
                            {nodeExec.nodeName}
                          </span>
                        </div>
                        {getStatusBadge(nodeExec.status)}
                      </div>
                      {nodeExec.duration && (
                        <div className="text-xs text-secondary-500">
                          Duration: {nodeExec.duration.toFixed(0)}ms
                        </div>
                      )}
                      {nodeExec.output && (
                        <div className="mt-2 p-2 bg-white rounded text-xs font-mono overflow-x-auto">
                          {JSON.stringify(nodeExec.output, null, 2)}
                        </div>
                      )}
                      {nodeExec.error && (
                        <Alert variant="destructive" className="mt-2">
                          <AlertDescription className="text-xs">
                            {nodeExec.error}
                          </AlertDescription>
                        </Alert>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

