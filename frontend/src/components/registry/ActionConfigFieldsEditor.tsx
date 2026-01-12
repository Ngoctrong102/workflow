import { ApiCallConfigFields, type ApiCallConfig } from "./ApiCallConfigFields"
import { PublishEventConfigFields, type PublishEventConfig } from "./PublishEventConfigFields"
import { FunctionConfigFields, type FunctionConfig } from "./FunctionConfigFields"
import type { ActionType, ActionConfigTemplate } from "./types"

interface ActionConfigFieldsEditorProps {
  actionType: ActionType
  configTemplate: ActionConfigTemplate
  onChange: (configTemplate: ActionConfigTemplate) => void
}

export function ActionConfigFieldsEditor({
  actionType,
  configTemplate,
  onChange,
}: ActionConfigFieldsEditorProps) {
  const handleApiCallConfigChange = (config: ApiCallConfig) => {
    // Merge API Call config with existing configTemplate, preserving inputSchema/outputSchema
    onChange({
      ...configTemplate,
      ...config,
      inputSchema: configTemplate.inputSchema,
      outputSchema: configTemplate.outputSchema,
    })
  }

  const handlePublishEventConfigChange = (config: PublishEventConfig) => {
    // Merge Publish Event config with existing configTemplate, preserving inputSchema/outputSchema
    onChange({
      ...configTemplate,
      ...config,
      inputSchema: configTemplate.inputSchema,
      outputSchema: configTemplate.outputSchema,
    })
  }

  const handleFunctionConfigChange = (config: FunctionConfig) => {
    // Merge Function config with existing configTemplate, preserving inputSchema/outputSchema
    onChange({
      ...configTemplate,
      ...config,
      inputSchema: configTemplate.inputSchema,
      outputSchema: configTemplate.outputSchema,
    })
  }

  // Render based on action type
  switch (actionType) {
    case "api-call":
      return (
        <ApiCallConfigFields
          config={configTemplate as ApiCallConfig}
          onChange={handleApiCallConfigChange}
        />
      )

    case "publish-event":
      return (
        <PublishEventConfigFields
          config={configTemplate as PublishEventConfig}
          onChange={handlePublishEventConfigChange}
        />
      )

    case "function":
      return (
        <FunctionConfigFields
          config={configTemplate as FunctionConfig}
          onChange={handleFunctionConfigChange}
        />
      )

    case "custom-action":
      return (
        <div className="text-sm text-secondary-500 py-4 text-center border border-dashed border-secondary-200 rounded-md">
          Custom actions use their own configuration structure defined by actionType.
        </div>
      )

    default:
      return null
  }
}

