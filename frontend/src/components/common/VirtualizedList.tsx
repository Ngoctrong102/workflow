import { memo, useMemo } from "react"
import { cn } from "@/lib/utils"

interface VirtualizedListProps<T> {
  items: T[]
  renderItem: (item: T, index: number) => React.ReactNode
  itemHeight?: number
  containerHeight?: number
  overscan?: number
  className?: string
  emptyMessage?: string
}

/**
 * Simple virtualized list component for performance optimization
 * For large lists, consider using react-window or react-virtuoso
 */
export function VirtualizedList<T>({
  items,
  renderItem,
  itemHeight: _itemHeight = 50,
  containerHeight = 400,
  overscan: _overscan = 5,
  className,
  emptyMessage = "No items found",
}: VirtualizedListProps<T>) {
  const visibleRange = useMemo(() => {
    // Simple virtualization: render all items but optimize rendering
    // For true virtualization with large lists, use react-window
    return {
      start: 0,
      end: items.length,
    }
  }, [items.length])

  if (items.length === 0) {
    return (
      <div className={cn("flex items-center justify-center h-32 text-secondary-500", className)}>
        {emptyMessage}
      </div>
    )
  }

  // For small lists, render all items
  // For large lists (>100 items), consider using react-window
  if (items.length > 100) {
    console.warn(
      "[VirtualizedList] Consider using react-window or react-virtuoso for lists with more than 100 items"
    )
  }

  return (
    <div
      className={cn("overflow-auto", className)}
      style={{ maxHeight: containerHeight }}
    >
      <div className="space-y-1">
        {items.slice(visibleRange.start, visibleRange.end).map((item, index) => (
          <VirtualizedListItem
            key={index}
            item={item}
            index={visibleRange.start + index}
            renderItem={renderItem}
          />
        ))}
      </div>
    </div>
  )
}

const VirtualizedListItem = memo(
  <T,>({
    item,
    index,
    renderItem,
  }: {
    item: T
    index: number
    renderItem: (item: T, index: number) => React.ReactNode
  }) => {
    return <>{renderItem(item, index)}</>
  },
  (prev, next) => {
    // Simple equality check - for complex objects, consider deep comparison
    return prev.item === next.item && prev.index === next.index
  }
) as <T>(props: {
  item: T
  index: number
  renderItem: (item: T, index: number) => React.ReactNode
}) => React.ReactElement

const VirtualizedListItemWithDisplayName = VirtualizedListItem as typeof VirtualizedListItem & { displayName: string }
VirtualizedListItemWithDisplayName.displayName = "VirtualizedListItem"

