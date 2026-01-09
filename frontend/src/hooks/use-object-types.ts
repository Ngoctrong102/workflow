import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { objectTypeService } from "@/services/objectTypeService"
import type {
  ListObjectTypesParams,
  CreateObjectTypeRequest,
  UpdateObjectTypeRequest,
} from "@/types/objectTypeTypes"
import { toast } from "sonner"

export function useObjectTypes(params?: ListObjectTypesParams) {
  return useQuery({
    queryKey: ["object-types", params],
    queryFn: () => objectTypeService.listObjectTypes(params),
  })
}

export function useObjectType(id: string | undefined) {
  return useQuery({
    queryKey: ["object-type", id],
    queryFn: () => objectTypeService.getObjectTypeById(id!),
    enabled: !!id,
  })
}

export function useCreateObjectType() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: CreateObjectTypeRequest) => objectTypeService.createObjectType(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["object-types"] })
      toast.success("Object type created successfully")
    },
    onError: (error: Error) => {
      toast.error(`Failed to create object type: ${error.message}`)
    },
  })
}

export function useUpdateObjectType() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Omit<UpdateObjectTypeRequest, "id"> }) =>
      objectTypeService.updateObjectType(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["object-types"] })
      queryClient.invalidateQueries({ queryKey: ["object-type", variables.id] })
      toast.success("Object type updated successfully")
    },
    onError: (error: Error) => {
      toast.error(`Failed to update object type: ${error.message}`)
    },
  })
}

export function useDeleteObjectType() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => objectTypeService.deleteObjectType(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["object-types"] })
      toast.success("Object type deleted successfully")
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete object type: ${error.message}`)
    },
  })
}

export function useObjectTypeFields(id: string | undefined) {
  return useQuery({
    queryKey: ["object-type-fields", id],
    queryFn: () => objectTypeService.getObjectTypeFields(id!),
    enabled: !!id,
  })
}

export function useSearchFields(params?: { query?: string; objectTypeId?: string; fieldType?: string }) {
  return useQuery({
    queryKey: ["search-fields", params],
    queryFn: () => objectTypeService.searchFields(params),
    enabled: !!params?.query || !!params?.objectTypeId,
  })
}

