import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { ArrowUpDown, ArrowUp, ArrowDown, Download } from "lucide-react"

export type SortDirection = "asc" | "desc" | null

export interface Column<T> {
  key: keyof T | string
  label: string
  sortable?: boolean
  render?: (value: unknown, row: T) => React.ReactNode
}

interface AnalyticsTableProps<T> {
  title: string
  description?: string
  columns: Column<T>[]
  data: T[]
  isLoading?: boolean
  onExport?: () => void
  exportLabel?: string
}

export function AnalyticsTable<T extends Record<string, unknown>>({
  title,
  description,
  columns,
  data,
  isLoading = false,
  onExport,
  exportLabel = "Export",
}: AnalyticsTableProps<T>) {
  const [sortColumn, setSortColumn] = useState<string | null>(null)
  const [sortDirection, setSortDirection] = useState<SortDirection>(null)

  const handleSort = (columnKey: string) => {
    if (sortColumn === columnKey) {
      if (sortDirection === "asc") {
        setSortDirection("desc")
      } else if (sortDirection === "desc") {
        setSortColumn(null)
        setSortDirection(null)
      } else {
        setSortDirection("asc")
      }
    } else {
      setSortColumn(columnKey)
      setSortDirection("asc")
    }
  }

  const sortedData = [...data].sort((a, b) => {
    if (!sortColumn || !sortDirection) return 0

    const column = columns.find((col) => col.key === sortColumn)
    if (!column || !column.sortable) return 0

    const aValue = a[sortColumn as keyof T]
    const bValue = b[sortColumn as keyof T]

    if (aValue === undefined || aValue === null) return 1
    if (bValue === undefined || bValue === null) return -1

    if (typeof aValue === "string" && typeof bValue === "string") {
      return sortDirection === "asc"
        ? aValue.localeCompare(bValue)
        : bValue.localeCompare(aValue)
    }

    if (typeof aValue === "number" && typeof bValue === "number") {
      return sortDirection === "asc" ? aValue - bValue : bValue - aValue
    }

    return 0
  })

  const getSortIcon = (columnKey: string) => {
    if (sortColumn !== columnKey) {
      return <ArrowUpDown className="h-4 w-4 ml-1 opacity-50" />
    }
    if (sortDirection === "asc") {
      return <ArrowUp className="h-4 w-4 ml-1" />
    }
    if (sortDirection === "desc") {
      return <ArrowDown className="h-4 w-4 ml-1" />
    }
    return <ArrowUpDown className="h-4 w-4 ml-1 opacity-50" />
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>{title}</CardTitle>
            {description && <CardDescription>{description}</CardDescription>}
          </div>
          {onExport && (
            <Button variant="outline" size="sm" onClick={onExport}>
              <Download className="h-4 w-4 mr-2" />
              {exportLabel}
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="text-center py-8 text-secondary-500">Loading...</div>
        ) : sortedData.length === 0 ? (
          <div className="text-center py-8 text-secondary-500">No data available</div>
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  {columns.map((column) => (
                    <TableHead key={String(column.key)}>
                      {column.sortable ? (
                        <button
                          className="flex items-center hover:opacity-70"
                          onClick={() => handleSort(String(column.key))}
                        >
                          {column.label}
                          {getSortIcon(String(column.key))}
                        </button>
                      ) : (
                        column.label
                      )}
                    </TableHead>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>
                {sortedData.map((row, index) => (
                  <TableRow key={index}>
                    {columns.map((column) => (
                      <TableCell key={String(column.key)}>
                        {column.render
                          ? column.render(row[column.key as keyof T], row)
                          : String(row[column.key as keyof T] ?? "-")}
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

