import { apiClient } from "./api-client"
import type {
  ObjectTypeResponse,
  CreateObjectTypeRequest,
  UpdateObjectTypeRequest,
  ListObjectTypesParams,
  ListObjectTypesResponse,
  FieldDefinitionDTO,
  ValidateFieldReferenceRequest,
  ValidateFieldReferenceResponse,
  SearchFieldsParams,
  SearchFieldsResponse,
} from "@/types/objectTypeTypes"

export const objectTypeService = {
  /**
   * List object types with pagination and filtering
   */
  listObjectTypes: async (params?: ListObjectTypesParams): Promise<ListObjectTypesResponse> => {
    const response = await apiClient.get<ListObjectTypesResponse>("/object-types", {
      params,
    })
    return response.data
  },

  /**
   * Get object type by ID
   */
  getObjectTypeById: async (id: string): Promise<ObjectTypeResponse> => {
    const response = await apiClient.get<ObjectTypeResponse>(`/object-types/${id}`)
    return response.data
  },

  /**
   * Create new object type
   */
  createObjectType: async (data: CreateObjectTypeRequest): Promise<ObjectTypeResponse> => {
    const response = await apiClient.post<ObjectTypeResponse>("/object-types", data)
    return response.data
  },

  /**
   * Update object type
   */
  updateObjectType: async (
    id: string,
    data: Omit<UpdateObjectTypeRequest, "id">
  ): Promise<ObjectTypeResponse> => {
    const response = await apiClient.put<ObjectTypeResponse>(`/object-types/${id}`, data)
    return response.data
  },

  /**
   * Delete object type (soft delete)
   */
  deleteObjectType: async (id: string): Promise<void> => {
    await apiClient.delete(`/object-types/${id}`)
  },

  /**
   * Get fields for object type
   */
  getObjectTypeFields: async (id: string): Promise<FieldDefinitionDTO[]> => {
    const response = await apiClient.get<FieldDefinitionDTO[]>(`/object-types/${id}/fields`)
    return response.data
  },

  /**
   * Get specific field definition
   */
  getFieldDefinition: async (
    objectTypeId: string,
    fieldName: string
  ): Promise<FieldDefinitionDTO> => {
    const response = await apiClient.get<FieldDefinitionDTO>(
      `/object-types/${objectTypeId}/fields/${fieldName}`
    )
    return response.data
  },

  /**
   * Validate field reference
   */
  validateFieldReference: async (
    request: ValidateFieldReferenceRequest
  ): Promise<ValidateFieldReferenceResponse> => {
    const response = await apiClient.post<ValidateFieldReferenceResponse>(
      "/object-types/validate-field",
      request
    )
    return response.data
  },

  /**
   * Search fields across object types
   */
  searchFields: async (params?: SearchFieldsParams): Promise<SearchFieldsResponse> => {
    const response = await apiClient.get<SearchFieldsResponse>("/object-types/search-fields", {
      params,
    })
    return response.data
  },
}

