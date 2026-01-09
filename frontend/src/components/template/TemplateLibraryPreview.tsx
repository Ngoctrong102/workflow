import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { X, Download, Star } from "lucide-react"
import { useTemplateLibraryItem, useInstallTemplate } from "@/hooks/use-template-library"
import { useToast } from "@/hooks/use-toast"
import { useNavigate } from "react-router-dom"

interface TemplateLibraryPreviewProps {
  templateId: string
  onClose: () => void
}

export function TemplateLibraryPreview({ templateId, onClose }: TemplateLibraryPreviewProps) {
  const navigate = useNavigate()
  const { toast } = useToast()
  const { data: template, isLoading } = useTemplateLibraryItem(templateId)
  const installTemplate = useInstallTemplate()

  const handleInstall = async () => {
    if (!templateId) return

    try {
      const result = await installTemplate.mutateAsync(templateId)
      toast({
        title: "Template Installed",
        description: result.message || "Template has been installed successfully",
      })
      onClose()
    } catch (error: unknown) {
      toast({
        title: "Installation Failed",
        description: error instanceof Error ? error.message : "Failed to install template",
        variant: "destructive",
      })
    }
  }

  const handleUseTemplate = () => {
    navigate(`/workflows/new?template=${templateId}`)
    onClose()
  }

  return (
    <Dialog open={!!templateId} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-[600px] max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Template Preview</DialogTitle>
          <DialogDescription>Preview template details</DialogDescription>
        </DialogHeader>

        {isLoading ? (
          <div className="space-y-4">
            <Skeleton className="h-8 w-3/4" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-2/3" />
            <Skeleton className="h-32 w-full" />
          </div>
        ) : template ? (
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold">{template.name}</h3>
              {template.description && (
                <p className="text-sm text-secondary-600 mt-1">{template.description}</p>
              )}
            </div>

            <div className="flex flex-wrap gap-2">
              {template.category && (
                <Badge variant="outline">{template.category}</Badge>
              )}
              {template.channel && (
                <Badge variant="secondary">{template.channel}</Badge>
              )}
              {template.tags?.map((tag) => (
                <Badge key={tag} variant="secondary" className="text-xs">
                  {tag}
                </Badge>
              ))}
            </div>

            {template.rating !== undefined && (
              <div className="flex items-center space-x-2 text-sm">
                <Star className="h-4 w-4 fill-warning-400 text-warning-400" />
                <span>{template.rating.toFixed(1)}</span>
                {template.downloads !== undefined && (
                  <>
                    <span className="text-secondary-400">•</span>
                    <span>{template.downloads} downloads</span>
                  </>
                )}
              </div>
            )}

            {template.preview && (
              <div className="p-4 bg-secondary-50 rounded-lg">
                <pre className="text-xs whitespace-pre-wrap">{template.preview}</pre>
              </div>
            )}

            {template.author && (
              <div className="text-sm text-secondary-500">
                Author: {template.author}
              </div>
            )}

            <div className="flex items-center space-x-2 pt-4 border-t">
              <Button variant="outline" onClick={onClose} className="flex-1">
                <X className="h-4 w-4 mr-2" />
                Close
              </Button>
              {template.installed ? (
                <Button onClick={handleUseTemplate} className="flex-1">
                  Use Template
                </Button>
              ) : (
                <Button
                  onClick={handleInstall}
                  disabled={installTemplate.isPending}
                  className="flex-1"
                >
                  <Download className="h-4 w-4 mr-2" />
                  Install
                </Button>
              )}
            </div>
          </div>
        ) : (
          <div className="text-center py-8 text-secondary-500">
            <p>Template not found</p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}

