import { memo } from "react"
import { Button } from "@/components/ui/button"
import { Play, Pause, StepForward, StepBack, RotateCcw } from "lucide-react"
import { cn } from "@/lib/utils"

interface StepControlsProps {
  currentStep: number
  totalSteps: number
  hasNext: boolean
  hasPrevious: boolean
  isPlaying?: boolean
  onNext: () => void
  onPrevious: () => void
  onReset: () => void
  onPlayPause?: () => void
  className?: string
}

export const StepControls = memo(function StepControls({
  currentStep,
  totalSteps,
  hasNext,
  hasPrevious,
  isPlaying = false,
  onNext,
  onPrevious,
  onReset,
  onPlayPause,
  className,
}: StepControlsProps) {
  return (
    <div className={cn("flex items-center gap-2 p-4 bg-white border border-secondary-200 rounded-lg", className)}>
      {/* Step Counter */}
      <div className="flex items-center gap-2 px-4 py-2 bg-secondary-50 rounded-md">
        <span className="text-sm font-medium text-secondary-600">
          Step {currentStep} of {totalSteps}
        </span>
      </div>

      {/* Controls */}
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={onReset}
          title="Reset to start"
        >
          <RotateCcw className="h-4 w-4 mr-2" />
          Reset
        </Button>

        <Button
          variant="outline"
          size="sm"
          onClick={onPrevious}
          disabled={!hasPrevious}
          title="Previous step"
        >
          <StepBack className="h-4 w-4 mr-2" />
          Previous
        </Button>

        {onPlayPause && (
          <Button
            variant="outline"
            size="sm"
            onClick={onPlayPause}
            title={isPlaying ? "Pause" : "Play"}
          >
            {isPlaying ? (
              <>
                <Pause className="h-4 w-4 mr-2" />
                Pause
              </>
            ) : (
              <>
                <Play className="h-4 w-4 mr-2" />
                Play
              </>
            )}
          </Button>
        )}

        <Button
          variant="default"
          size="sm"
          onClick={onNext}
          disabled={!hasNext}
          title="Next step"
        >
          <StepForward className="h-4 w-4 mr-2" />
          Next
        </Button>
      </div>
    </div>
  )
})

