import { NodeTypeEnum } from "@/types/workflow"
import type { NodeDefinition } from "@/types/workflow"
import type { LucideIcon } from "lucide-react"
import {
  Globe,
  Clock,
  FolderUp,
  Radio,
  TestTube,
  GitBranch,
  Shuffle,
  Repeat,
  Timer,
  GitMerge,
  Hourglass,
} from "lucide-react"

// Icon mapping - using registryId or subtype for identification
export const NODE_ICONS: Record<string, LucideIcon> = {
  // Trigger icons (identified by triggerConfigId)
  "api-call": Globe,
  "scheduler": Clock,
  "file": FolderUp,
  "event": Radio,
  // Logic icons (identified by subtype)
  "ab-test": TestTube,
  "condition": GitBranch,
  "switch": Shuffle,
  "loop": Repeat,
  "delay": Timer,
  "merge": GitMerge,
  "wait-events": Hourglass,
}

// Built-in logic node definitions (when re-implemented)
// These are temporary - will be replaced by registry-based approach
export const NODE_DEFINITIONS: NodeDefinition[] = [
  // Logic Nodes - Amber (#f59e0b)
  // Note: These are legacy and will be re-implemented with registry-based approach
  {
    type: NodeTypeEnum.LOGIC,
    label: "A/B Test",
    description: "Split traffic between variants",
    icon: "ab-test",
    color: "#f59e0b",
    inputs: 1,
    outputs: 2,
  },
  {
    type: NodeTypeEnum.LOGIC,
    label: "Condition",
    description: "If/Then/Else branching",
    icon: "condition",
    color: "#f59e0b",
    inputs: 1,
    outputs: 2,
  },
  {
    type: NodeTypeEnum.LOGIC,
    label: "Switch",
    description: "Multi-case branching",
    icon: "switch",
    color: "#f59e0b",
    inputs: 1,
    outputs: 3,
  },
  {
    type: NodeTypeEnum.LOGIC,
    label: "Loop",
    description: "Iterate over array",
    icon: "loop",
    color: "#f59e0b",
    inputs: 1,
    outputs: 1,
  },
  {
    type: NodeTypeEnum.LOGIC,
    label: "Delay",
    description: "Wait for specified time",
    icon: "delay",
    color: "#f59e0b",
    inputs: 1,
    outputs: 1,
  },
  {
    type: NodeTypeEnum.LOGIC,
    label: "Merge",
    description: "Combine multiple branches",
    icon: "merge",
    color: "#f59e0b",
    inputs: 2,
    outputs: 1,
  },
  {
    type: NodeTypeEnum.LOGIC,
    label: "Wait for Events",
    description: "Wait for API response and/or Kafka event",
    icon: "wait-events",
    color: "#14b8a6",
    inputs: 1,
    outputs: 1,
  },
]

export const NODES_BY_CATEGORY = {
  [NodeTypeEnum.TRIGGER]: [] as NodeDefinition[], // Triggers come from registry
  [NodeTypeEnum.ACTION]: [] as NodeDefinition[], // Actions come from registry
  [NodeTypeEnum.LOGIC]: NODE_DEFINITIONS.filter((n) => n.type === NodeTypeEnum.LOGIC),
}
