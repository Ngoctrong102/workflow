import { memo, type ComponentType, type PropsWithChildren } from "react"

/**
 * Higher-order component for memoizing components
 * Use this to prevent unnecessary re-renders
 */
export function withMemo<P extends object>(
  Component: ComponentType<P>,
  areEqual?: (prevProps: P, nextProps: P) => boolean
): ComponentType<P> {
  return memo(Component, areEqual) as ComponentType<P>
}

/**
 * Memoized wrapper component
 */
export const MemoizedComponent = memo(
  <P extends object>({ children }: PropsWithChildren<P>) => {
    return <>{children}</>
  }
)

MemoizedComponent.displayName = "MemoizedComponent"

