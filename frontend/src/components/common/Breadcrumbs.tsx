import { Link, useLocation } from 'react-router-dom'
import { ChevronRight, Home } from 'lucide-react'

interface BreadcrumbItem {
  label: string
  path?: string
}

export function Breadcrumbs() {
  const location = useLocation()
  
  // Generate breadcrumbs from pathname
  const pathSegments = location.pathname.split('/').filter(Boolean)
  const breadcrumbs: BreadcrumbItem[] = [
    { label: 'Home', path: '/' },
  ]

  // Build breadcrumbs from segments
  let currentPath = ''
  pathSegments.forEach((segment, index) => {
    currentPath += `/${segment}`
    const isLast = index === pathSegments.length - 1
    
    // Capitalize and format segment
    const label = segment
      .split('-')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ')
    
    breadcrumbs.push({
      label,
      path: isLast ? undefined : currentPath,
    })
  })

  if (breadcrumbs.length <= 1) {
    return null
  }

  return (
    <nav className="container mx-auto px-4 py-2" aria-label="Breadcrumb">
      <ol className="flex items-center space-x-1.5 text-xs">
        {breadcrumbs.map((item, index) => {
          const isLast = index === breadcrumbs.length - 1
          
          return (
            <li key={item.path || item.label} className="flex items-center">
              {index > 0 && (
                <ChevronRight className="w-3 h-3 text-secondary-400 mx-1" />
              )}
              {isLast ? (
                <span className="text-secondary-900 font-medium">
                  {item.label}
                </span>
              ) : (
                <Link
                  to={item.path || '#'}
                  className="text-secondary-600 hover:text-secondary-900 flex items-center transition-colors cursor-pointer"
                >
                  {index === 0 && <Home className="w-3 h-3 mr-0.5" />}
                  {item.label}
                </Link>
              )}
            </li>
          )
        })}
      </ol>
    </nav>
  )
}

