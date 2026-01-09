/**
 * Local storage cache utility for static data
 * Provides caching layer for data that doesn't change frequently
 */

const CACHE_PREFIX = "workflow_cache_"
const CACHE_VERSION = "1.0"

interface CacheEntry<T> {
  data: T
  timestamp: number
  version: string
}

/**
 * Get cached data from localStorage
 */
export function getCachedData<T>(key: string, maxAge: number = 24 * 60 * 60 * 1000): T | null {
  try {
    const cached = localStorage.getItem(`${CACHE_PREFIX}${key}`)
    if (!cached) return null

    const entry: CacheEntry<T> = JSON.parse(cached)
    
    // Check version
    if (entry.version !== CACHE_VERSION) {
      localStorage.removeItem(`${CACHE_PREFIX}${key}`)
      return null
    }

    // Check age
    const age = Date.now() - entry.timestamp
    if (age > maxAge) {
      localStorage.removeItem(`${CACHE_PREFIX}${key}`)
      return null
    }

    return entry.data
  } catch (error) {
    console.error(`[LocalStorageCache] Error reading cache for key "${key}":`, error)
    return null
  }
}

/**
 * Set cached data in localStorage
 */
export function setCachedData<T>(key: string, data: T): void {
  const entry: CacheEntry<T> = {
    data,
    timestamp: Date.now(),
    version: CACHE_VERSION,
  }
  try {
    localStorage.setItem(`${CACHE_PREFIX}${key}`, JSON.stringify(entry))
  } catch (error) {
    console.error(`[LocalStorageCache] Error writing cache for key "${key}":`, error)
    // Handle quota exceeded error
    if (error instanceof DOMException && error.name === "QuotaExceededError") {
      // Clear old cache entries
      clearOldCacheEntries()
      // Retry once
      try {
        localStorage.setItem(`${CACHE_PREFIX}${key}`, JSON.stringify(entry))
      } catch (retryError) {
        console.error(`[LocalStorageCache] Retry failed for key "${key}":`, retryError)
      }
    }
  }
}

/**
 * Clear cached data from localStorage
 */
export function clearCachedData(key: string): void {
  try {
    localStorage.removeItem(`${CACHE_PREFIX}${key}`)
  } catch (error) {
    console.error(`[LocalStorageCache] Error clearing cache for key "${key}":`, error)
  }
}

/**
 * Clear all cached data
 */
export function clearAllCachedData(): void {
  try {
    const keys = Object.keys(localStorage)
    keys.forEach((key) => {
      if (key.startsWith(CACHE_PREFIX)) {
        localStorage.removeItem(key)
      }
    })
  } catch (error) {
    console.error("[LocalStorageCache] Error clearing all cache:", error)
  }
}

/**
 * Clear old cache entries (older than 7 days)
 */
function clearOldCacheEntries(): void {
  try {
    const keys = Object.keys(localStorage)
    const sevenDaysAgo = Date.now() - 7 * 24 * 60 * 60 * 1000

    keys.forEach((key) => {
      if (key.startsWith(CACHE_PREFIX)) {
        try {
          const cached = localStorage.getItem(key)
          if (cached) {
            const entry: CacheEntry<unknown> = JSON.parse(cached)
            if (entry.timestamp < sevenDaysAgo) {
              localStorage.removeItem(key)
            }
          }
        } catch (error) {
          // Invalid entry, remove it
          localStorage.removeItem(key)
        }
      }
    })
  } catch (error) {
    console.error("[LocalStorageCache] Error clearing old cache entries:", error)
  }
}

/**
 * Get cache size in bytes (approximate)
 */
export function getCacheSize(): number {
  try {
    const keys = Object.keys(localStorage)
    let size = 0

    keys.forEach((key) => {
      if (key.startsWith(CACHE_PREFIX)) {
        const value = localStorage.getItem(key)
        if (value) {
          size += key.length + value.length
        }
      }
    })

    return size
  } catch (error) {
    console.error("[LocalStorageCache] Error calculating cache size:", error)
    return 0
  }
}

