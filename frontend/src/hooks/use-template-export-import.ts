import { useMutation, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { templateService, type CreateTemplateRequest } from "@/services/template-service"
import { exportToJSON } from "@/utils/export"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"

export function useExportTemplate() {
  const { toast } = useToast()

  return useMutation({
    mutationFn: async (id: string) => {
      const template = await templateService.export(id)
      exportToJSON([template], `template-${id}-${Date.now()}.json`)
      return template
    },
    onSuccess: () => {
      toast({
        title: "Template Exported",
        description: "Template has been exported successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Export Failed",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useImportTemplate() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: async ({
      template,
      options,
    }: {
      template: CreateTemplateRequest
      options?: { overwrite?: boolean; skipConflicts?: boolean }
    }) => {
      return await templateService.import(template, options)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["templates"] })
      toast({
        title: "Template Imported",
        description: "Template has been imported successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Import Failed",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

