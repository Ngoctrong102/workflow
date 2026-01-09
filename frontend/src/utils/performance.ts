/**
 * Performance monitoring utilities
 */

interface PerformanceMetric {
  name: string
  duration: number
  timestamp: number
}

class PerformanceMonitor {
  private metrics: PerformanceMetric[] = []
  private marks: Map<string, number> = new Map()

  /**
   * Mark the start of a performance measurement
   */
  mark(name: string): void {
    if (typeof performance !== 'undefined' && performance.mark) {
      performance.mark(name)
      this.marks.set(name, performance.now())
    }
  }

  /**
   * Measure the duration between two marks
   */
  measure(name: string, startMark: string, endMark?: string): number | null {
    if (typeof performance !== 'undefined' && performance.measure) {
      try {
        if (endMark) {
          performance.measure(name, startMark, endMark)
        } else {
          performance.measure(name, startMark)
        }
        const measure = performance.getEntriesByName(name, 'measure')[0]
        if (measure) {
          const duration = measure.duration
          this.metrics.push({
            name,
            duration,
            timestamp: Date.now(),
          })
          return duration
        }
      } catch (error) {
        console.warn(`Performance measure failed for ${name}:`, error)
      }
    }
    return null
  }

  /**
   * Measure function execution time
   */
  async measureAsync<T>(
    name: string,
    fn: () => Promise<T>
  ): Promise<T> {
    this.mark(`${name}-start`)
    try {
      const result = await fn()
      this.mark(`${name}-end`)
      const duration = this.measure(name, `${name}-start`, `${name}-end`)
      if (duration !== null && import.meta.env.DEV) {
        console.log(`[Performance] ${name}: ${duration.toFixed(2)}ms`)
      }
      return result
    } catch (error) {
      this.mark(`${name}-end`)
      this.measure(name, `${name}-start`, `${name}-end`)
      throw error
    }
  }

  /**
   * Measure synchronous function execution time
   */
  measureSync<T>(name: string, fn: () => T): T {
    this.mark(`${name}-start`)
    try {
      const result = fn()
      this.mark(`${name}-end`)
      const duration = this.measure(name, `${name}-start`, `${name}-end`)
      if (duration !== null && import.meta.env.DEV) {
        console.log(`[Performance] ${name}: ${duration.toFixed(2)}ms`)
      }
      return result
    } catch (error) {
      this.mark(`${name}-end`)
      this.measure(name, `${name}-start`, `${name}-end`)
      throw error
    }
  }

  /**
   * Get all metrics
   */
  getMetrics(): PerformanceMetric[] {
    return [...this.metrics]
  }

  /**
   * Get metrics by name
   */
  getMetricsByName(name: string): PerformanceMetric[] {
    return this.metrics.filter((m) => m.name === name)
  }

  /**
   * Clear all metrics
   */
  clear(): void {
    this.metrics = []
    this.marks.clear()
    if (typeof performance !== 'undefined' && performance.clearMarks) {
      performance.clearMarks()
      performance.clearMeasures()
    }
  }

  /**
   * Get average duration for a metric name
   */
  getAverageDuration(name: string): number | null {
    const metrics = this.getMetricsByName(name)
    if (metrics.length === 0) return null
    const sum = metrics.reduce((acc, m) => acc + m.duration, 0)
    return sum / metrics.length
  }
}

export const performanceMonitor = new PerformanceMonitor()

/**
 * React hook to measure component render time
 */
export function usePerformanceMeasure(componentName: string) {
  if (import.meta.env.DEV) {
    const startTime = performance.now()
    
    return () => {
      const duration = performance.now() - startTime
      if (duration > 16) { // Warn if render takes longer than one frame (16ms)
        console.warn(`[Performance] ${componentName} render took ${duration.toFixed(2)}ms`)
      }
    }
  }
  
  return () => {}
}

/**
 * Debounce function for performance optimization
 */
export function debounce<T extends (...args: unknown[]) => unknown>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null

  return function executedFunction(...args: Parameters<T>) {
    const later = () => {
      timeout = null
      func(...args)
    }

    if (timeout) {
      clearTimeout(timeout)
    }
    timeout = setTimeout(later, wait)
  }
}

/**
 * Throttle function for performance optimization
 */
export function throttle<T extends (...args: unknown[]) => unknown>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean

  return function executedFunction(...args: Parameters<T>) {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => {
        inThrottle = false
      }, limit)
    }
  }
}

/**
 * Check if code is running in production
 */
export function isProduction(): boolean {
  return import.meta.env.PROD
}

/**
 * Log bundle size information (for development)
 */
export function logBundleInfo(): void {
  if (import.meta.env.DEV && typeof window !== 'undefined') {
    const scripts = Array.from(document.querySelectorAll('script[src]'))
    const styles = Array.from(document.querySelectorAll('link[rel="stylesheet"]'))
    
    console.log('[Bundle Info]', {
      scripts: scripts.length,
      styles: styles.length,
      scriptsSize: scripts.reduce((acc, script) => {
        const src = script.getAttribute('src')
        return acc + (src ? new URL(src, window.location.href).pathname.length : 0)
      }, 0),
    })
  }
}

