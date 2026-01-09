/**
 * Object Type Types
 * 
 * TypeScript interfaces for object type management
 */

import type { FieldType, FieldDefinition } from "@/utils/fieldTypeValidator"

export interface ObjectType {
  id: string
  name: string
  displayName?: string
  description?: string
  fields: FieldDefinition[]
  tags?: string[]
  version: number
  createdAt: string
  updatedAt: string
  deletedAt?: string
}

export interface CreateObjectTypeRequest {
  name: string
  displayName?: string
  description?: string
  fields: FieldDefinition[]
  tags?: string[]
}

export interface UpdateObjectTypeRequest {
  id: string
  name?: string
  displayName?: string
  description?: string
  fields?: FieldDefinition[]
  tags?: string[]
}

export interface ObjectTypeResponse {
  id: string
  name: string
  displayName?: string
  description?: string
  fields: FieldDefinition[]
  tags?: string[]
  version: number
  createdAt: string
  updatedAt: string
  deletedAt?: string | null
}

export interface FieldDefinitionDTO {
  name: string
  displayName?: string
  type: FieldType
  required?: boolean
  defaultValue?: unknown
  validation?: {
    minLength?: number
    maxLength?: number
    min?: number
    max?: number
    pattern?: string
    enum?: string[]
    minItems?: number
    maxItems?: number
    objectTypeId?: string
    itemType?: FieldType
    itemObjectTypeId?: string
  }
  description?: string
  examples?: string[]
}

export interface FieldValidation {
  valid: boolean
  error?: string
  warnings?: string[]
}

export interface ValidateFieldReferenceRequest {
  objectTypeId: string
  fieldPath: string
}

export interface ValidateFieldReferenceResponse {
  valid: boolean
  error?: string
  fieldDefinition?: FieldDefinitionDTO
}

export interface SearchFieldsParams {
  query?: string
  objectTypeId?: string
  fieldType?: FieldType
  limit?: number
  offset?: number
}

export interface SearchFieldsResponse {
  fields: Array<{
    objectTypeId: string
    objectTypeName: string
    field: FieldDefinitionDTO
    fieldPath: string
  }>
  total: number
  limit: number
  offset: number
}

export interface ListObjectTypesParams {
  search?: string
  tags?: string[]
  limit?: number
  offset?: number
}

export interface PagedResponse<T> {
  data: T[]
  total: number
  limit: number
  offset: number
  hasMore: boolean
}

export interface ListObjectTypesResponse extends PagedResponse<ObjectTypeResponse> {}

