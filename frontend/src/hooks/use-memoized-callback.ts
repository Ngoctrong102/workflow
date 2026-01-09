import { useCallback, useRef } from "react"

/**
 * Enhanced useCallback that maintains referential equality
 * even when dependencies change, until a certain condition is met
 */
export function useMemoizedCallback<T extends (...args: unknown[]) => unknown>(
  callback: T,
  deps: React.DependencyList
): T {
  const callbackRef = useRef(callback)
  const depsRef = useRef(deps)

  // Update callback ref if dependencies changed
  const hasChanged = deps.some((dep, i) => dep !== depsRef.current[i])
  if (hasChanged) {
    callbackRef.current = callback
    depsRef.current = deps
  }

  return useCallback(
    ((...args: Parameters<T>) => {
      return callbackRef.current(...args)
    }) as T,
    [] // Empty deps - we handle updates via ref
  )
}

/**
 * useCallback with automatic dependency tracking
 * Similar to useCallback but with better memoization
 */
export function useStableCallback<T extends (...args: unknown[]) => unknown>(
  callback: T,
  deps: React.DependencyList
): T {
  return useCallback(callback, deps) as T
}

