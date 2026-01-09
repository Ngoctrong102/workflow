import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Eye, Code } from "lucide-react"
import type { FieldDefinition } from "@/utils/fieldTypeValidator"

interface ObjectTypePreviewProps {
  name: string
  displayName?: string
  description?: string
  tags?: string[]
  fields: FieldDefinition[]
}

export function ObjectTypePreview({
  name,
  displayName,
  description,
  tags,
  fields,
}: ObjectTypePreviewProps) {
  const getFieldTypeColor = (type: string) => {
    const colors: Record<string, string> = {
      string: "bg-blue-100 text-blue-800 border-blue-200",
      number: "bg-green-100 text-green-800 border-green-200",
      boolean: "bg-purple-100 text-purple-800 border-purple-200",
      date: "bg-orange-100 text-orange-800 border-orange-200",
      datetime: "bg-orange-100 text-orange-800 border-orange-200",
      email: "bg-cyan-100 text-cyan-800 border-cyan-200",
      phone: "bg-teal-100 text-teal-800 border-teal-200",
      url: "bg-indigo-100 text-indigo-800 border-indigo-200",
      json: "bg-gray-100 text-gray-800 border-gray-200",
      array: "bg-pink-100 text-pink-800 border-pink-200",
      object: "bg-yellow-100 text-yellow-800 border-yellow-200",
    }
    return colors[type] || "bg-gray-100 text-gray-800 border-gray-200"
  }

  // Generate example JSON
  const generateExampleJSON = () => {
    const example: Record<string, unknown> = {}
    fields.forEach((field) => {
      switch (field.type) {
        case "string":
        case "email":
        case "phone":
        case "url":
          example[field.name] = field.defaultValue || `"example ${field.name}"`
          break
        case "number":
          example[field.name] = field.defaultValue || 0
          break
        case "boolean":
          example[field.name] = field.defaultValue ?? false
          break
        case "date":
          example[field.name] = field.defaultValue || '"2024-01-01"'
          break
        case "datetime":
          example[field.name] = field.defaultValue || '"2024-01-01T00:00:00Z"'
          break
        case "array":
          example[field.name] = []
          break
        case "object":
          example[field.name] = {}
          break
        case "json":
          example[field.name] = null
          break
      }
    })
    return JSON.stringify(example, null, 2)
  }

  return (
    <div className="space-y-4">
      {/* Visual Preview Card */}
      <Card className="border-2 border-dashed border-secondary-200 bg-secondary-50/50">
        <CardHeader className="pb-3">
          <div className="flex items-center space-x-2">
            <Eye className="h-4 w-4 text-secondary-600" />
            <CardTitle className="text-sm font-semibold">Live Preview</CardTitle>
          </div>
          <CardDescription className="text-xs">
            This is how your object type will appear
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {/* Object Type Header */}
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <h3 className="font-semibold text-base text-secondary-900">
                {displayName || name || "Untitled Object Type"}
              </h3>
              {tags && tags.length > 0 && (
                <div className="flex flex-wrap gap-1">
                  {tags.map((tag) => (
                    <Badge key={tag} variant="secondary" className="text-xs">
                      {tag}
                    </Badge>
                  ))}
                </div>
              )}
            </div>
            {description && (
              <p className="text-sm text-secondary-600">{description}</p>
            )}
          </div>

          {/* Fields Preview */}
          {fields.length > 0 ? (
            <div className="space-y-2 pt-2 border-t border-secondary-200">
              <div className="text-xs font-medium text-secondary-700 mb-2">
                Fields ({fields.length})
              </div>
              <div className="grid grid-cols-1 gap-2">
                {fields.map((field, index) => (
                  <div
                    key={index}
                    className="flex items-center justify-between p-2 bg-white rounded border border-secondary-200 hover:border-secondary-300 transition-colors"
                  >
                    <div className="flex items-center space-x-2 flex-1 min-w-0">
                      <span className="font-medium text-sm text-secondary-900 truncate">
                        {field.displayName || field.name}
                      </span>
                      {field.required && (
                        <Badge variant="outline" className="text-xs border-error-300 text-error-700">
                          Required
                        </Badge>
                      )}
                    </div>
                    <Badge
                      variant="outline"
                      className={`text-xs ${getFieldTypeColor(field.type)}`}
                    >
                      {field.type}
                    </Badge>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="text-center py-6 text-secondary-400 text-sm border-t border-secondary-200">
              <p>No fields defined yet</p>
              <p className="text-xs mt-1">Add fields to see preview</p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* JSON Example */}
      {fields.length > 0 && (
        <Card className="border-secondary-200">
          <CardHeader className="pb-3">
            <div className="flex items-center space-x-2">
              <Code className="h-4 w-4 text-secondary-600" />
              <CardTitle className="text-sm font-semibold">Example JSON</CardTitle>
            </div>
            <CardDescription className="text-xs">
              Sample data structure for this object type
            </CardDescription>
          </CardHeader>
          <CardContent>
            <pre className="text-xs bg-secondary-900 text-secondary-100 p-3 rounded overflow-x-auto">
              {generateExampleJSON()}
            </pre>
          </CardContent>
        </Card>
      )}
    </div>
  )
}

