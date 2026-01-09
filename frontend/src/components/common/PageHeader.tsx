import { memo } from 'react'
import type { ReactNode } from 'react'
import { Breadcrumbs } from './Breadcrumbs'
import { cn } from '@/lib/utils'

export interface PageHeaderProps {
  title: string
  description?: string
  actions?: ReactNode
  showBreadcrumbs?: boolean
  className?: string
}

export const PageHeader = memo(function PageHeader({
  title,
  description,
  actions,
  showBreadcrumbs = true,
  className,
}: PageHeaderProps) {
  return (
    <div className={cn('mb-6', className)}>
      {showBreadcrumbs && <Breadcrumbs />}
      <div className="flex items-start justify-between mt-4">
        <div>
          <h1 className="text-2xl font-bold text-secondary-900">{title}</h1>
          {description && (
            <p className="text-sm text-secondary-600 mt-1">{description}</p>
          )}
        </div>
        {actions && (
          <div className="flex items-center gap-2">{actions}</div>
        )}
      </div>
    </div>
  )
})

