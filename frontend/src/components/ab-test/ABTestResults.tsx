import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Skeleton } from "@/components/ui/skeleton"
import { Trophy, TrendingUp } from "lucide-react"
import { useABTestResults } from "@/hooks/use-ab-tests"
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts"
import { cn } from "@/lib/utils"

interface ABTestResultsProps {
  testId: string
}

export function ABTestResults({ testId }: ABTestResultsProps) {
  const { data: results, isLoading, error } = useABTestResults(testId)

  if (isLoading) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="space-y-4">
            {[1, 2].map((i) => (
              <Skeleton key={i} className="h-20 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (error) {
    return (
      <Card>
        <CardContent className="py-12 text-center">
          <p className="text-error-600">Failed to load results</p>
          <p className="text-sm mt-2 text-secondary-500">
            {error instanceof Error ? error.message : "Unknown error"}
          </p>
        </CardContent>
      </Card>
    )
  }

  if (!results || results.results.length === 0) {
    return (
      <Card>
        <CardContent className="py-12 text-center text-secondary-500">
          <p>No results available yet</p>
          <p className="text-sm mt-2">Results will appear once the test starts collecting data</p>
        </CardContent>
      </Card>
    )
  }

  const chartData = results.results.map((result) => ({
    variant: result.variant_label,
    open_rate: result.open_rate || 0,
    click_rate: result.click_rate || 0,
    conversion_rate: result.conversion_rate || 0,
    engagement_rate: result.engagement_rate || 0,
  }))

  // Metric label determined by test name (currently unused but may be needed for future features)
  // const metricLabel = results.test_name.includes("open") ? "open_rate" :
  //   results.test_name.includes("click") ? "click_rate" :
  //   results.test_name.includes("conversion") ? "conversion_rate" : "engagement_rate"

  return (
    <div className="space-y-6">
      {/* Summary */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>{results.test_name}</CardTitle>
              <CardDescription>
                Total Samples: {results.total_samples.toLocaleString()}
                {results.winner && ` • Winner: Variant ${results.winner}`}
              </CardDescription>
            </div>
            {results.winner && (
              <Badge variant="default" className="bg-success-600">
                <Trophy className="h-4 w-4 mr-2" />
                Winner: {results.winner}
              </Badge>
            )}
          </div>
        </CardHeader>
        {results.recommendation && (
          <CardContent>
            <div className="p-4 bg-primary-50 border border-primary-200 rounded-lg">
              <div className="flex items-start space-x-2">
                <TrendingUp className="h-5 w-5 text-primary-600 mt-0.5" />
                <div>
                  <div className="font-semibold text-primary-900 mb-1">Recommendation</div>
                  <div className="text-sm text-primary-700">{results.recommendation}</div>
                </div>
              </div>
            </div>
          </CardContent>
        )}
      </Card>

      {/* Chart */}
      <Card>
        <CardHeader>
          <CardTitle>Variant Comparison</CardTitle>
          <CardDescription>Performance metrics by variant</CardDescription>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="variant" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="open_rate" fill="#3b82f6" name="Open Rate" />
              <Bar dataKey="click_rate" fill="#10b981" name="Click Rate" />
              <Bar dataKey="conversion_rate" fill="#f59e0b" name="Conversion Rate" />
              <Bar dataKey="engagement_rate" fill="#8b5cf6" name="Engagement Rate" />
            </BarChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      {/* Results Table */}
      <Card>
        <CardHeader>
          <CardTitle>Detailed Results</CardTitle>
          <CardDescription>Statistical analysis by variant</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Variant</TableHead>
                <TableHead>Sample Size</TableHead>
                <TableHead>Open Rate</TableHead>
                <TableHead>Click Rate</TableHead>
                <TableHead>Conversion Rate</TableHead>
                <TableHead>Engagement Rate</TableHead>
                <TableHead>Significance</TableHead>
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {results.results.map((result) => {
                const isWinner = result.is_winner

                return (
                  <TableRow
                    key={result.variant_id}
                    className={cn(isWinner && "bg-success-50")}
                  >
                    <TableCell>
                      <div className="flex items-center space-x-2">
                        <span className="font-medium">Variant {result.variant_label}</span>
                        {isWinner && (
                          <Badge variant="default" className="bg-success-600">
                            <Trophy className="h-3 w-3 mr-1" />
                            Winner
                          </Badge>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>{result.sample_size.toLocaleString()}</TableCell>
                    <TableCell>
                      {result.open_rate !== undefined ? `${(result.open_rate * 100).toFixed(2)}%` : "-"}
                    </TableCell>
                    <TableCell>
                      {result.click_rate !== undefined ? `${(result.click_rate * 100).toFixed(2)}%` : "-"}
                    </TableCell>
                    <TableCell>
                      {result.conversion_rate !== undefined
                        ? `${(result.conversion_rate * 100).toFixed(2)}%`
                        : "-"}
                    </TableCell>
                    <TableCell>
                      {result.engagement_rate !== undefined
                        ? `${(result.engagement_rate * 100).toFixed(2)}%`
                        : "-"}
                    </TableCell>
                    <TableCell>
                      {result.statistical_significance !== undefined ? (
                        <div className="flex items-center space-x-2">
                          <span>{(result.statistical_significance * 100).toFixed(1)}%</span>
                          {result.statistical_significance >= 0.95 && (
                            <Badge variant="default" className="bg-success-600 text-xs">
                              Significant
                            </Badge>
                          )}
                        </div>
                      ) : (
                        "-"
                      )}
                    </TableCell>
                    <TableCell>
                      {result.confidence_interval && (
                        <div className="text-xs text-secondary-500">
                          {result.confidence_interval.lower.toFixed(2)} - {result.confidence_interval.upper.toFixed(2)}
                        </div>
                      )}
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
}

