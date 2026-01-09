import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Search,
  Download,
  Eye,
  Star,
  Filter,
  Grid3x3,
  List,
  CheckCircle2,
  Loader2,
} from "lucide-react"
import { useTemplateLibrary, useInstallTemplate } from "@/hooks/use-template-library"
import { useToast } from "@/hooks/use-toast"
import { TemplateLibraryPreview } from "@/components/template/TemplateLibraryPreview"

type ViewMode = "grid" | "list"

export default function TemplateLibrary() {
  const navigate = useNavigate()
  const { toast } = useToast()
  const [searchQuery, setSearchQuery] = useState("")
  const [categoryFilter, setCategoryFilter] = useState<string>("all")
  const [channelFilter, setChannelFilter] = useState<string>("all")
  const [viewMode, setViewMode] = useState<ViewMode>("grid")
  const [previewTemplate, setPreviewTemplate] = useState<string | null>(null)

  const { data, isLoading, error } = useTemplateLibrary({
    search: searchQuery || undefined,
    category: categoryFilter !== "all" ? categoryFilter : undefined,
    channel: channelFilter !== "all" ? channelFilter : undefined,
    limit: 50,
  })

  const installTemplate = useInstallTemplate()

  const templates = data?.templates || []
  const categories = data?.categories || []

  const handleInstall = async (id: string) => {
    try {
      const result = await installTemplate.mutateAsync(id)
      toast({
        title: "Template Installed",
        description: result.message || "Template has been installed successfully",
      })
    } catch (error) {
      toast({
        title: "Installation Failed",
        description: error instanceof Error ? error.message : "Failed to install template",
        variant: "destructive",
      })
    }
  }

  const handleUseTemplate = (id: string) => {
    navigate(`/workflows/new?template=${id}`)
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Template Library</h1>
          <p className="text-secondary-600 mt-2">Browse and install pre-built workflow templates</p>
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setViewMode(viewMode === "grid" ? "list" : "grid")}
          >
            {viewMode === "grid" ? <List className="h-4 w-4" /> : <Grid3x3 className="h-4 w-4" />}
          </Button>
          <Button variant="outline" onClick={() => navigate("/workflows/new")}>
            Create from Scratch
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex items-center space-x-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
              <Input
                placeholder="Search templates..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
            <select
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="px-3 py-2 border border-secondary-300 rounded-md text-sm"
            >
              <option value="all">All Categories</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name} {cat.count !== undefined && `(${cat.count})`}
                </option>
              ))}
            </select>
            <select
              value={channelFilter}
              onChange={(e) => setChannelFilter(e.target.value)}
              className="px-3 py-2 border border-secondary-300 rounded-md text-sm"
            >
              <option value="all">All Channels</option>
              <option value="email">Email</option>
              <option value="sms">SMS</option>
              <option value="push">Push</option>
              <option value="in-app">In-App</option>
              <option value="slack">Slack</option>
              <option value="discord">Discord</option>
              <option value="teams">Teams</option>
            </select>
          </div>
        </CardContent>
      </Card>

      {/* Template List */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <Skeleton className="h-32 w-full mb-4" />
                <Skeleton className="h-6 w-3/4 mb-2" />
                <Skeleton className="h-4 w-full mb-2" />
                <Skeleton className="h-4 w-2/3" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : error ? (
        <Card>
          <CardContent className="py-12 text-center">
            <p className="text-error-600">Failed to load templates</p>
            <p className="text-sm mt-2 text-secondary-500">
              {error instanceof Error ? error.message : "Unknown error"}
            </p>
          </CardContent>
        </Card>
      ) : templates.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center text-secondary-500">
            <p>No templates found</p>
          </CardContent>
        </Card>
      ) : viewMode === "grid" ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {templates.map((template) => (
            <Card key={template.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <CardTitle className="text-lg">{template.name}</CardTitle>
                    {template.category && (
                      <Badge variant="outline" className="mt-2">
                        {template.category}
                      </Badge>
                    )}
                  </div>
                  {template.installed && (
                    <CheckCircle2 className="h-5 w-5 text-success-600" />
                  )}
                </div>
                <CardDescription className="mt-2">{template.description}</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {template.tags && template.tags.length > 0 && (
                    <div className="flex flex-wrap gap-2">
                      {template.tags.map((tag) => (
                        <Badge key={tag} variant="secondary" className="text-xs">
                          {tag}
                        </Badge>
                      ))}
                    </div>
                  )}

                  <div className="flex items-center justify-between text-sm text-secondary-500">
                    {template.rating !== undefined && (
                      <div className="flex items-center space-x-1">
                        <Star className="h-4 w-4 fill-warning-400 text-warning-400" />
                        <span>{template.rating.toFixed(1)}</span>
                      </div>
                    )}
                    {template.downloads !== undefined && (
                      <div className="flex items-center space-x-1">
                        <Download className="h-4 w-4" />
                        <span>{template.downloads}</span>
                      </div>
                    )}
                  </div>

                  <div className="flex items-center space-x-2 pt-2">
                    <Button
                      variant="outline"
                      size="sm"
                      className="flex-1"
                      onClick={() => setPreviewTemplate(template.id)}
                    >
                      <Eye className="h-4 w-4 mr-2" />
                      Preview
                    </Button>
                    {template.installed ? (
                      <Button
                        size="sm"
                        className="flex-1"
                        onClick={() => handleUseTemplate(template.id)}
                      >
                        Use Template
                      </Button>
                    ) : (
                      <Button
                        size="sm"
                        className="flex-1"
                        onClick={() => handleInstall(template.id)}
                        disabled={installTemplate.isPending}
                      >
                        {installTemplate.isPending ? (
                          <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        ) : (
                          <Download className="h-4 w-4 mr-2" />
                        )}
                        Install
                      </Button>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <Card>
          <CardContent className="p-0">
            <div className="divide-y">
              {templates.map((template) => (
                <div key={template.id} className="p-4 hover:bg-secondary-50 transition-colors">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-2">
                        <h3 className="font-semibold">{template.name}</h3>
                        {template.installed && (
                          <CheckCircle2 className="h-4 w-4 text-success-600" />
                        )}
                        {template.category && (
                          <Badge variant="outline">{template.category}</Badge>
                        )}
                      </div>
                      <p className="text-sm text-secondary-500 mt-1">{template.description}</p>
                      {template.tags && template.tags.length > 0 && (
                        <div className="flex flex-wrap gap-2 mt-2">
                          {template.tags.map((tag) => (
                            <Badge key={tag} variant="secondary" className="text-xs">
                              {tag}
                            </Badge>
                          ))}
                        </div>
                      )}
                    </div>
                    <div className="flex items-center space-x-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPreviewTemplate(template.id)}
                      >
                        <Eye className="h-4 w-4 mr-2" />
                        Preview
                      </Button>
                      {template.installed ? (
                        <Button size="sm" onClick={() => handleUseTemplate(template.id)}>
                          Use Template
                        </Button>
                      ) : (
                        <Button
                          size="sm"
                          onClick={() => handleInstall(template.id)}
                          disabled={installTemplate.isPending}
                        >
                          {installTemplate.isPending ? (
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                          ) : (
                            <Download className="h-4 w-4 mr-2" />
                          )}
                          Install
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Preview Dialog */}
      {previewTemplate && (
        <TemplateLibraryPreview
          templateId={previewTemplate}
          onClose={() => setPreviewTemplate(null)}
        />
      )}
    </div>
  )
}

