import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { VariablePicker } from "./VariablePicker"
import { TemplatePreview } from "./TemplatePreview"
import { useForm, Controller } from "react-hook-form"
import type { Template, TemplateChannel } from "@/types/template"

interface TemplateEditorProps {
  template?: Template
  onSave: (template: Omit<Template, "id" | "createdAt" | "updatedAt">) => void
  onCancel: () => void
}

export function TemplateEditor({ template, onSave, onCancel }: TemplateEditorProps) {
  const [activeTab, setActiveTab] = useState("editor")
  const { register, handleSubmit, watch, control, setValue, formState: { errors } } = useForm({
    defaultValues: {
      name: template?.name || "",
      description: template?.description || "",
      channel: (template?.channel || "email") as TemplateChannel,
      subject: template?.subject || "",
      body: template?.body || "",
    },
  })

  const channel = watch("channel")
  const body = watch("body")
  const subject = watch("subject")

  const handleVariableInsert = (variable: string) => {
    const currentBody = body || ""
    setValue("body", currentBody + variable)
  }

  const onSubmit = (data: {
    name: string
    description: string
    channel: TemplateChannel
    subject: string
    body: string
  }) => {
    // Extract variables from body and subject
    const variableRegex = /\{\{([^}]+)\}\}/g
    const variables: string[] = []
    let match

    // Extract from body
    while ((match = variableRegex.exec(data.body)) !== null) {
      if (!variables.includes(match[1])) {
        variables.push(match[1])
      }
    }

    // Extract from subject
    if (data.subject) {
      while ((match = variableRegex.exec(data.subject)) !== null) {
        if (!variables.includes(match[1])) {
          variables.push(match[1])
        }
      }
    }

    onSave({
      name: data.name,
      description: data.description,
      channel: data.channel,
      subject: data.channel === "email" ? data.subject : undefined,
      body: data.body,
      variables,
      status: "draft",
    })
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Basic Info */}
      <Card>
        <CardHeader>
          <CardTitle>Template Information</CardTitle>
          <CardDescription>Basic template details</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Template Name *</Label>
            <Input
              id="name"
              {...register("name", { required: "Name is required" })}
              placeholder="Enter template name"
            />
            {errors.name && (
              <p className="text-sm text-error-600">{errors.name.message as string}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Input
              id="description"
              {...register("description")}
              placeholder="Enter template description"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="channel">Channel *</Label>
            <Controller
              name="channel"
              control={control}
              rules={{ required: "Channel is required" }}
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select channel" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="email">Email</SelectItem>
                    <SelectItem value="sms">SMS</SelectItem>
                    <SelectItem value="push">Push Notification</SelectItem>
                    <SelectItem value="in-app">In-App</SelectItem>
                    <SelectItem value="slack">Slack</SelectItem>
                    <SelectItem value="discord">Discord</SelectItem>
                    <SelectItem value="teams">Teams</SelectItem>
                    <SelectItem value="webhook">Webhook</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
            {errors.channel && (
              <p className="text-sm text-error-600">{errors.channel.message as string}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Editor */}
      <Card>
        <CardHeader>
          <CardTitle>Template Content</CardTitle>
          <CardDescription>Create your template content</CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList>
              <TabsTrigger value="editor">Editor</TabsTrigger>
              <TabsTrigger value="preview">Preview</TabsTrigger>
            </TabsList>

            <TabsContent value="editor" className="space-y-4 mt-4">
              {channel === "email" && (
                <div className="space-y-2">
                  <Label htmlFor="subject">Subject *</Label>
                  <Input
                    id="subject"
                    {...register("subject", { required: "Subject is required for email" })}
                    placeholder="Enter email subject"
                  />
                  {errors.subject && (
                    <p className="text-sm text-error-600">{errors.subject.message as string}</p>
                  )}
                </div>
              )}

              <div className="space-y-2">
                <Label htmlFor="body">
                  {channel === "email" ? "Body (HTML)" : channel === "sms" ? "Message" : "Content"} *
                </Label>
                <Textarea
                  id="body"
                  {...register("body", { required: "Body is required" })}
                  placeholder={
                    channel === "email"
                      ? "Enter HTML content..."
                      : channel === "sms"
                      ? "Enter SMS message..."
                      : "Enter content..."
                  }
                  rows={channel === "email" ? 12 : 8}
                  className="font-mono text-sm"
                />
                {errors.body && (
                  <p className="text-sm text-error-600">{errors.body.message as string}</p>
                )}
                {channel === "sms" && (
                  <p className="text-xs text-secondary-500">
                    Character count: {(body || "").length} / 160
                  </p>
                )}
              </div>
            </TabsContent>

            <TabsContent value="preview" className="mt-4">
              <TemplatePreview
                channel={channel}
                subject={subject}
                body={body}
              />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      {/* Variable Picker */}
      <VariablePicker onSelect={handleVariableInsert} />

      {/* Actions */}
      <div className="flex justify-end space-x-2">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit">Save Template</Button>
      </div>
    </form>
  )
}

