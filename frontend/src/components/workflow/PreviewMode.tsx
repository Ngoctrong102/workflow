import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Eye, Play, X } from "lucide-react"
import type { Node, Edge } from "reactflow"
import { NODE_DEFINITIONS } from "@/constants/workflow-nodes"

interface PreviewModeProps {
  nodes: Node[]
  edges: Edge[]
  onClose: () => void
  onTest?: () => void
}

export function PreviewMode({ nodes, edges, onClose, onTest }: PreviewModeProps) {
  const triggerNodes = nodes.filter((node) => {
    const nodeDef = NODE_DEFINITIONS.find((n) => n.type === node.data.type)
    return nodeDef?.category === "trigger"
  })

  const actionNodes = nodes.filter((node) => {
    const nodeDef = NODE_DEFINITIONS.find((n) => n.type === node.data.type)
    return nodeDef?.category === "action"
  })

  const logicNodes = nodes.filter((node) => {
    const nodeDef = NODE_DEFINITIONS.find((n) => n.type === node.data.type)
    return nodeDef?.category === "logic"
  })

  const dataNodes = nodes.filter((node) => {
    const nodeDef = NODE_DEFINITIONS.find((n) => n.type === node.data.type)
    return nodeDef?.category === "data"
  })

  return (
    <Card className="h-full flex flex-col">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="flex items-center space-x-2">
              <Eye className="h-5 w-5" />
              <span>Preview Mode</span>
            </CardTitle>
            <CardDescription>Workflow structure overview</CardDescription>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose}>
            <X className="h-4 w-4" />
          </Button>
        </div>
      </CardHeader>
      <CardContent className="flex-1 overflow-y-auto space-y-6">
        {/* Summary */}
        <div className="grid grid-cols-2 gap-4">
          <div className="p-3 rounded-lg border border-secondary-200">
            <div className="text-sm text-secondary-500">Total Nodes</div>
            <div className="text-2xl font-bold">{nodes.length}</div>
          </div>
          <div className="p-3 rounded-lg border border-secondary-200">
            <div className="text-sm text-secondary-500">Connections</div>
            <div className="text-2xl font-bold">{edges.length}</div>
          </div>
        </div>

        {/* Node Breakdown */}
        <div className="space-y-4">
          <div>
            <h3 className="font-semibold mb-2 flex items-center space-x-2">
              <span>Triggers</span>
              <Badge variant="secondary">{triggerNodes.length}</Badge>
            </h3>
            {triggerNodes.length === 0 ? (
              <p className="text-sm text-secondary-500">No trigger nodes</p>
            ) : (
              <div className="space-y-1">
                {triggerNodes.map((node) => (
                  <div
                    key={node.id}
                    className="p-2 rounded border border-secondary-200 text-sm"
                  >
                    {node.data.label || node.id}
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            <h3 className="font-semibold mb-2 flex items-center space-x-2">
              <span>Actions</span>
              <Badge variant="secondary">{actionNodes.length}</Badge>
            </h3>
            {actionNodes.length === 0 ? (
              <p className="text-sm text-secondary-500">No action nodes</p>
            ) : (
              <div className="space-y-1">
                {actionNodes.map((node) => (
                  <div
                    key={node.id}
                    className="p-2 rounded border border-secondary-200 text-sm"
                  >
                    {node.data.label || node.id}
                  </div>
                ))}
              </div>
            )}
          </div>

          {logicNodes.length > 0 && (
            <div>
              <h3 className="font-semibold mb-2 flex items-center space-x-2">
                <span>Logic</span>
                <Badge variant="secondary">{logicNodes.length}</Badge>
              </h3>
              <div className="space-y-1">
                {logicNodes.map((node) => (
                  <div
                    key={node.id}
                    className="p-2 rounded border border-secondary-200 text-sm"
                  >
                    {node.data.label || node.id}
                  </div>
                ))}
              </div>
            </div>
          )}

          {dataNodes.length > 0 && (
            <div>
              <h3 className="font-semibold mb-2 flex items-center space-x-2">
                <span>Data</span>
                <Badge variant="secondary">{dataNodes.length}</Badge>
              </h3>
              <div className="space-y-1">
                {dataNodes.map((node) => (
                  <div
                    key={node.id}
                    className="p-2 rounded border border-secondary-200 text-sm"
                  >
                    {node.data.label || node.id}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Execution Flow */}
        <div>
          <h3 className="font-semibold mb-2">Execution Flow</h3>
          <div className="space-y-2">
            {triggerNodes.map((trigger) => {
              const connectedNodes = edges
                .filter((e) => e.source === trigger.id)
                .map((e) => nodes.find((n) => n.id === e.target))
                .filter(Boolean) as Node[]

              return (
                <div key={trigger.id} className="p-3 rounded border border-secondary-200">
                  <div className="font-medium text-sm mb-2">
                    {trigger.data.label || trigger.id}
                  </div>
                  {connectedNodes.length > 0 ? (
                    <div className="space-y-1 ml-4">
                      {connectedNodes.map((node) => (
                        <div key={node.id} className="text-sm text-secondary-600">
                          → {node.data.label || node.id}
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-sm text-secondary-500 ml-4">
                      No connections
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        </div>

        {/* Actions */}
        {onTest && (
          <div className="pt-4 border-t">
            <Button onClick={onTest} className="w-full">
              <Play className="h-4 w-4 mr-2" />
              Run Test
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

