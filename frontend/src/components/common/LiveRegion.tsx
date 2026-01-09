import { useEffect, useRef } from "react"

interface LiveRegionProps {
  message: string
  priority?: "polite" | "assertive"
}

/**
 * Live region component for screen reader announcements
 * Use this for dynamic content updates that should be announced to screen readers
 */
export function LiveRegion({ message, priority = "polite" }: LiveRegionProps) {
  const regionRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (message && regionRef.current) {
      // Clear and set message to trigger announcement
      regionRef.current.textContent = ""
      setTimeout(() => {
        if (regionRef.current) {
          regionRef.current.textContent = message
        }
      }, 100)
    }
  }, [message])

  return (
    <div
      ref={regionRef}
      role="status"
      aria-live={priority}
      aria-atomic="true"
      className="sr-only"
    >
      {message}
    </div>
  )
}

