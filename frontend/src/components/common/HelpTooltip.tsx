import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { HelpCircle } from "lucide-react"
import type { ReactNode } from "react"

interface HelpTooltipProps {
  content: ReactNode
  side?: "top" | "right" | "bottom" | "left"
  children?: ReactNode
}

export function HelpTooltip({ content, side = "top", children }: HelpTooltipProps) {
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          {children || (
            <button type="button" className="inline-flex items-center">
              <HelpCircle className="h-4 w-4 text-secondary-400 hover:text-secondary-600" />
            </button>
          )}
        </TooltipTrigger>
        <TooltipContent side={side} className="max-w-xs">
          {content}
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  )
}

