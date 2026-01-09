export interface TemplateLibraryItem {
  id: string
  name: string
  description?: string
  category: string
  tags?: string[]
  channel?: string
  preview?: string
  author?: string
  version?: string
  installed?: boolean
  shared?: boolean
  rating?: number
  downloads?: number
}

export interface TemplateCategory {
  id: string
  name: string
  description?: string
  icon?: string
  count?: number
}

export interface TemplateLibraryListParams {
  category?: string
  tags?: string[]
  channel?: string
  search?: string
  limit?: number
  offset?: number
}

export interface TemplateLibraryListResponse {
  templates: TemplateLibraryItem[]
  categories: TemplateCategory[]
  total: number
  limit: number
  offset: number
}

