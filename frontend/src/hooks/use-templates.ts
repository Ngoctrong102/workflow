import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { templateService, type ListTemplatesParams, type CreateTemplateRequest, type UpdateTemplateRequest } from "@/services/template-service"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"

export function useTemplates(params?: ListTemplatesParams) {
  return useQuery({
    queryKey: ["templates", params],
    queryFn: () => templateService.list(params),
  })
}

export function useTemplate(id: string | undefined) {
  return useQuery({
    queryKey: ["template", id],
    queryFn: () => templateService.get(id!),
    enabled: !!id,
  })
}

export function useCreateTemplate() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (template: CreateTemplateRequest) => templateService.create(template),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["templates"] })
      toast({
        title: "Template Created",
        description: "Template has been created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Create Template",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useUpdateTemplate() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation<
    Awaited<ReturnType<typeof templateService.update>>,
    ApiException,
    { id: string } & Omit<UpdateTemplateRequest, "id">
  >({
    mutationFn: ({ id, ...template }) =>
      templateService.update(id, template),
    onSuccess: (_: unknown, variables: { id: string }) => {
      queryClient.invalidateQueries({ queryKey: ["templates"] })
      queryClient.invalidateQueries({ queryKey: ["template", variables.id] })
      toast({
        title: "Template Updated",
        description: "Template has been updated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Update Template",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useDeleteTemplate() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => templateService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["templates"] })
      toast({
        title: "Template Deleted",
        description: "Template has been deleted successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Delete Template",
        description: error.message || "An error occurred while deleting the template",
      })
    },
  })
}

