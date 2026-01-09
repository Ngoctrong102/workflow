import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Search } from "lucide-react"
import { TEMPLATE_VARIABLES } from "@/constants/template-variables"
import { cn } from "@/lib/utils"

interface VariablePickerProps {
  onSelect: (variable: string) => void
}

export function VariablePicker({ onSelect }: VariablePickerProps) {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedType, setSelectedType] = useState<"all" | "user" | "workflow" | "custom" | "system">("all")

  const filteredVariables = TEMPLATE_VARIABLES.filter((variable) => {
    const matchesSearch =
      variable.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      variable.description?.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesType = selectedType === "all" || variable.type === selectedType
    return matchesSearch && matchesType
  })

  const handleVariableClick = (variable: string) => {
    onSelect(`{{${variable}}}`)
  }

  return (
    <Card>
      <CardHeader className="pb-3">
        <CardTitle className="text-lg">Variables</CardTitle>
        <CardDescription>Click to insert variable</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Search */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
          <Input
            placeholder="Search variables..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>

        {/* Type Filters */}
        <div className="flex flex-wrap gap-2">
          <Badge
            variant={selectedType === "all" ? "default" : "outline"}
            className="cursor-pointer"
            onClick={() => setSelectedType("all")}
          >
            All
          </Badge>
          {(["user", "workflow", "custom", "system"] as const).map((type) => (
            <Badge
              key={type}
              variant={selectedType === type ? "default" : "outline"}
              className="cursor-pointer capitalize"
              onClick={() => setSelectedType(type)}
            >
              {type}
            </Badge>
          ))}
        </div>

        {/* Variable List */}
        <div className="space-y-2 max-h-64 overflow-y-auto">
          {filteredVariables.length === 0 ? (
            <div className="text-center py-8 text-secondary-500 text-sm">
              No variables found
            </div>
          ) : (
            filteredVariables.map((variable) => (
              <div
                key={variable.name}
                className={cn(
                  "p-3 rounded-lg border border-secondary-200 bg-white cursor-pointer",
                  "hover:border-primary-400 hover:shadow-sm transition-all"
                )}
                onClick={() => handleVariableClick(variable.name)}
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="font-medium text-sm text-secondary-900">
                      {`{{${variable.name}}}`}
                    </div>
                    {variable.description && (
                      <div className="text-xs text-secondary-500 mt-1">
                        {variable.description}
                      </div>
                    )}
                    {variable.example && (
                      <div className="text-xs text-secondary-400 mt-1 font-mono">
                        Example: {variable.example}
                      </div>
                    )}
                  </div>
                  <Badge variant="outline" className="text-xs capitalize">
                    {variable.type}
                  </Badge>
                </div>
              </div>
            ))
          )}
        </div>
      </CardContent>
    </Card>
  )
}

