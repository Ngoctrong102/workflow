import { useApiQuery, useApiMutation, useInvalidateQueries } from "./use-api"
import { templateLibraryService } from "@/services/template-library-service"
import type {
  TemplateLibraryListParams,
  TemplateLibraryListResponse,
  TemplateLibraryItem,
} from "@/types/template-library"

/**
 * Hook to list template library items
 */
export function useTemplateLibrary(params?: TemplateLibraryListParams) {
  return useApiQuery<TemplateLibraryListResponse, Error>({
    queryKey: ["template-library", params],
    queryFn: () => templateLibraryService.list(params),
  })
}

/**
 * Hook to get template library item details
 */
export function useTemplateLibraryItem(id: string | undefined) {
  return useApiQuery<TemplateLibraryItem, Error>({
    queryKey: ["template-library-item", id],
    queryFn: () => {
      if (!id) throw new Error("Template ID is required")
      return templateLibraryService.get(id)
    },
    enabled: !!id,
  })
}

/**
 * Hook to install template from library
 */
export function useInstallTemplate() {
  const invalidateQueries = useInvalidateQueries()

  return useApiMutation<{ template_id: string; message: string }, Error, string>({
    mutationFn: (id: string) => templateLibraryService.install(id),
    onSuccess: () => {
      invalidateQueries(["template-library"])
      invalidateQueries(["templates"])
    },
  })
}

