export type ABTestStatus = "draft" | "running" | "paused" | "completed" | "archived"

export type SuccessMetric = "open_rate" | "click_rate" | "conversion_rate" | "engagement_rate"

export type AssignmentStrategy = "random" | "consistent" | "stratified"

export interface ABTestVariant {
  id: string
  name: string
  label: string // A, B, C, etc.
  template_id?: string
  channel?: string
  config?: Record<string, unknown>
  traffic_percentage: number
}

export interface ABTest {
  id: string
  name: string
  description?: string
  workflow_id: string
  workflow_name?: string
  status: ABTestStatus
  variants: ABTestVariant[]
  success_metric: SuccessMetric
  assignment_strategy?: AssignmentStrategy
  start_date?: string
  end_date?: string
  duration_days?: number
  min_sample_size?: number
  created_at?: string
  updated_at?: string
}

export interface ABTestResult {
  test_id: string
  variant_id: string
  variant_label: string
  sample_size: number
  open_rate?: number
  click_rate?: number
  conversion_rate?: number
  engagement_rate?: number
  statistical_significance?: number
  confidence_interval?: {
    lower: number
    upper: number
  }
  is_winner?: boolean
}

export interface ABTestResults {
  test_id: string
  test_name: string
  status: ABTestStatus
  start_date?: string
  end_date?: string
  total_samples: number
  results: ABTestResult[]
  winner?: string
  recommendation?: string
}

export interface ABTestListParams {
  workflow_id?: string
  status?: ABTestStatus
  limit?: number
  offset?: number
  search?: string
}

export interface ABTestListResponse {
  tests: ABTest[]
  total: number
  limit: number
  offset: number
}

