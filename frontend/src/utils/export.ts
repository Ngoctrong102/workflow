/**
 * Export data to CSV format
 */
export function exportToCSV<T extends Record<string, unknown>>(
  data: T[],
  columns: Array<{ key: keyof T | string; label: string }>,
  filename: string = "export.csv"
): void {
  // Create CSV header
  const header = columns.map((col) => col.label).join(",")

  // Create CSV rows
  const rows = data.map((row) =>
    columns
      .map((col) => {
        const value = row[col.key as keyof T]
        // Escape commas and quotes in CSV
        const stringValue = String(value ?? "")
        if (stringValue.includes(",") || stringValue.includes('"') || stringValue.includes("\n")) {
          return `"${stringValue.replace(/"/g, '""')}"`
        }
        return stringValue
      })
      .join(",")
  )

  // Combine header and rows
  const csvContent = [header, ...rows].join("\n")

  // Create blob and download
  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" })
  const link = document.createElement("a")
  const url = URL.createObjectURL(blob)
  link.setAttribute("href", url)
  link.setAttribute("download", filename)
  link.style.visibility = "hidden"
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * Export data to JSON format
 */
export function exportToJSON<T>(data: T[], filename: string = "export.json"): void {
  const jsonContent = JSON.stringify(data, null, 2)
  const blob = new Blob([jsonContent], { type: "application/json" })
  const link = document.createElement("a")
  const url = URL.createObjectURL(blob)
  link.setAttribute("href", url)
  link.setAttribute("download", filename)
  link.style.visibility = "hidden"
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * Export data to Excel format (XLSX)
 * Note: This is a simplified implementation. For full Excel support, consider using a library like 'xlsx'
 */
export function exportToExcel<T extends Record<string, unknown>>(
  data: T[],
  columns: Array<{ key: keyof T | string; label: string }>,
  filename: string = "export.xlsx"
): void {
  // For now, export as CSV with .xlsx extension
  // In production, use a library like 'xlsx' for proper Excel format
  exportToCSV(data, columns, filename.replace(".xlsx", ".csv"))
}

/**
 * Export data to PDF format
 * Note: This requires a PDF generation library. For now, we'll trigger a download
 * that the backend can handle, or use a library like 'jspdf' or 'pdfmake'
 */
export function exportToPDF<T extends Record<string, unknown>>(
  data: T[],
  columns: Array<{ key: keyof T | string; label: string }>,
  filename: string = "export.pdf"
): void {
  // For now, export as CSV
  // In production, use a library like 'jspdf' or call backend API for PDF generation
  exportToCSV(data, columns, filename.replace(".pdf", ".csv"))
}

