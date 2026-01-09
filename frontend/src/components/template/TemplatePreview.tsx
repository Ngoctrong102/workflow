import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import type { TemplateChannel } from "@/types/template"

interface TemplatePreviewProps {
  channel: TemplateChannel
  subject?: string
  body: string
  previewData?: Record<string, unknown>
}

// Mock preview data
const defaultPreviewData: Record<string, unknown> = {
  user: {
    id: "123",
    name: "John Doe",
    email: "john@example.com",
    phone: "+1234567890",
  },
  workflow: {
    id: "wf-123",
    name: "Welcome Workflow",
  },
  execution: {
    id: "exec-456",
    timestamp: new Date().toISOString(),
  },
  date: {
    now: new Date().toISOString(),
    format: new Date().toLocaleDateString(),
  },
  url: {
    base: "https://example.com",
  },
}

function renderTemplate(content: string, data: Record<string, unknown>): string {
  if (!content) return ""

  // Simple variable replacement: {{variable.path}}
  return content.replace(/\{\{([^}]+)\}\}/g, (match, path) => {
    const keys = path.split(".")
    let value: unknown = data

    for (const key of keys) {
      if (value && typeof value === "object" && key in value) {
        value = (value as Record<string, unknown>)[key]
      } else {
        return match // Return original if not found
      }
    }

    return String(value ?? match)
  })
}

export function TemplatePreview({
  channel,
  subject,
  body,
  previewData = defaultPreviewData,
}: TemplatePreviewProps) {
  const renderedSubject = subject ? renderTemplate(subject, previewData) : undefined
  const renderedBody = renderTemplate(body, previewData)

  if (channel === "email") {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Email Preview</CardTitle>
          <CardDescription>Preview with sample data</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {renderedSubject && (
            <div>
              <div className="text-sm font-medium text-secondary-500 mb-1">Subject:</div>
              <div className="p-2 bg-secondary-50 rounded border border-secondary-200">
                {renderedSubject}
              </div>
            </div>
          )}
          <div>
            <div className="text-sm font-medium text-secondary-500 mb-1">Body:</div>
            <div
              className="p-4 bg-white rounded border border-secondary-200 min-h-[200px]"
              dangerouslySetInnerHTML={{ __html: renderedBody }}
            />
          </div>
        </CardContent>
      </Card>
    )
  }

  if (channel === "sms") {
    return (
      <Card>
        <CardHeader>
          <CardTitle>SMS Preview</CardTitle>
          <CardDescription>Preview with sample data</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="p-4 bg-secondary-50 rounded border border-secondary-200 font-mono text-sm whitespace-pre-wrap">
            {renderedBody}
          </div>
          <div className="mt-2 text-xs text-secondary-500">
            Character count: {renderedBody.length} / 160
          </div>
        </CardContent>
      </Card>
    )
  }

  if (channel === "push") {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Push Notification Preview</CardTitle>
          <CardDescription>Preview with sample data</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="p-4 bg-secondary-50 rounded border border-secondary-200 space-y-2">
            <div className="font-semibold">{renderedBody}</div>
            <div className="text-sm text-secondary-500">
              This is how the push notification will appear
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Template Preview</CardTitle>
        <CardDescription>Preview with sample data</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="p-4 bg-secondary-50 rounded border border-secondary-200">
          <pre className="text-sm whitespace-pre-wrap">{renderedBody}</pre>
        </div>
      </CardContent>
    </Card>
  )
}

