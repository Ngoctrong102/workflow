/**
 * Export chart as image
 */
export function exportChartAsImage(
  chartElement: HTMLElement | null,
  filename: string = "chart.png"
): void {
  if (!chartElement) {
    console.error("Chart element not found")
    return
  }

  // Use html2canvas library if available, otherwise fallback to canvas
  // For now, we'll use a simple approach with canvas
  try {
    const canvas = document.createElement("canvas")
    const ctx = canvas.getContext("2d")
    if (!ctx) {
      console.error("Could not get canvas context")
      return
    }

    // Set canvas size
    canvas.width = chartElement.offsetWidth
    canvas.height = chartElement.offsetHeight

    // For SVG charts (Recharts), we can convert SVG to image
    const svg = chartElement.querySelector("svg")
    if (svg) {
      const svgData = new XMLSerializer().serializeToString(svg)
      const svgBlob = new Blob([svgData], { type: "image/svg+xml;charset=utf-8" })
      const url = URL.createObjectURL(svgBlob)

      const img = new Image()
      img.onload = () => {
        ctx.drawImage(img, 0, 0)
        canvas.toBlob((blob) => {
          if (blob) {
            const link = document.createElement("a")
            link.href = URL.createObjectURL(blob)
            link.download = filename
            link.click()
            URL.revokeObjectURL(link.href)
          }
        })
        URL.revokeObjectURL(url)
      }
      img.src = url
    } else {
      // Fallback: try to capture as image
      // Note: This is a simplified implementation
      // For production, consider using html2canvas library
      console.warn("SVG not found, using fallback method")
    }
  } catch (error) {
    console.error("Failed to export chart:", error)
  }
}

/**
 * Export dashboard data as CSV
 */
export function exportDashboardDataAsCSV(data: Record<string, unknown>[]): void {
  if (data.length === 0) {
    console.warn("No data to export")
    return
  }

  const columns = Object.keys(data[0])
  const header = columns.join(",")
  const rows = data.map((row) =>
    columns.map((col) => {
      const value = row[col]
      const stringValue = String(value ?? "")
      if (stringValue.includes(",") || stringValue.includes('"') || stringValue.includes("\n")) {
        return `"${stringValue.replace(/"/g, '""')}"`
      }
      return stringValue
    }).join(",")
  )

  const csvContent = [header, ...rows].join("\n")
  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" })
  const link = document.createElement("a")
  const url = URL.createObjectURL(blob)
  link.setAttribute("href", url)
  link.setAttribute("download", `dashboard-${Date.now()}.csv`)
  link.style.visibility = "hidden"
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

