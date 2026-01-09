import { useApiQuery, useApiMutation, useInvalidateQueries } from "./use-api"
import type { ApiException } from "@/utils/error-handler"
import { abTestService } from "@/services/ab-test-service"
import type {
  ABTest,
  ABTestListParams,
  ABTestListResponse,
  ABTestResults,
} from "@/types/ab-test"

/**
 * Hook to list A/B tests
 */
export function useABTests(params?: ABTestListParams) {
  return useApiQuery<ABTestListResponse>(["ab-tests", params], () => abTestService.list(params))
}

/**
 * Hook to get A/B test details
 */
export function useABTest(testId: string | undefined) {
  return useApiQuery<ABTest>(
    ["ab-test", testId],
    () => {
      if (!testId) throw new Error("Test ID is required")
      return abTestService.get(testId)
    },
    { enabled: !!testId }
  )
}

/**
 * Hook to get A/B test results
 */
export function useABTestResults(testId: string | undefined) {
  return useApiQuery<ABTestResults>(
    ["ab-test-results", testId],
    () => {
      if (!testId) throw new Error("Test ID is required")
      return abTestService.getResults(testId)
    },
    { enabled: !!testId }
  )
}

/**
 * Hook to create A/B test
 */
export function useCreateABTest() {
  const { invalidate } = useInvalidateQueries()

  return useApiMutation<ABTest, ApiException, Omit<ABTest, "id" | "created_at" | "updated_at">>({
    mutationFn: (test) => abTestService.create(test),
    onSuccess: () => {
      invalidate(["ab-tests"])
    },
  })
}

/**
 * Hook to update A/B test
 */
export function useUpdateABTest() {
  const { invalidate } = useInvalidateQueries()

  return useApiMutation<ABTest, ApiException, { testId: string; test: Partial<ABTest> }>({
    mutationFn: ({ testId, test }) => abTestService.update(testId, test),
    onSuccess: () => {
      invalidate(["ab-tests"])
      invalidate(["ab-test"])
    },
  })
}

/**
 * Hook to delete A/B test
 */
export function useDeleteABTest() {
  const { invalidate } = useInvalidateQueries()

  return useApiMutation<{ message: string }, ApiException, string>({
    mutationFn: (testId: string) => abTestService.delete(testId),
    onSuccess: () => {
      invalidate(["ab-tests"])
    },
  })
}

/**
 * Hook to start A/B test
 */
export function useStartABTest() {
  const { invalidate } = useInvalidateQueries()

  return useApiMutation<ABTest, ApiException, string>({
    mutationFn: (testId: string) => abTestService.start(testId),
    onSuccess: () => {
      invalidate(["ab-tests"])
      invalidate(["ab-test"])
      invalidate(["ab-test-results"])
    },
  })
}

/**
 * Hook to pause A/B test
 */
export function usePauseABTest() {
  const { invalidate } = useInvalidateQueries()

  return useApiMutation<ABTest, ApiException, string>({
    mutationFn: (testId: string) => abTestService.pause(testId),
    onSuccess: () => {
      invalidate(["ab-tests"])
      invalidate(["ab-test"])
    },
  })
}

/**
 * Hook to stop A/B test
 */
export function useStopABTest() {
  const { invalidate } = useInvalidateQueries()

  return useApiMutation<ABTest, ApiException, string>({
    mutationFn: (testId: string) => abTestService.stop(testId),
    onSuccess: () => {
      invalidate(["ab-tests"])
      invalidate(["ab-test"])
      invalidate(["ab-test-results"])
    },
  })
}

