import { useState, useEffect } from "react"
import { useForm } from "react-hook-form"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { KeyValuePairsEditor } from "./KeyValuePairsEditor"
import { JsonEditor } from "./JsonEditor"
import { MvelInputEditor } from "./MvelInputEditor"
import { cn } from "@/lib/utils"
import type { SchemaDefinition } from "./SchemaEditor"

export interface PublishEventConfig {
  kafka?: {
    brokers?: string[]
    topic?: string
    key?: string
    headers?: Record<string, string>
  }
  message?: any
}

interface PublishEventConfigFieldsProps {
  config: PublishEventConfig
  onChange: (config: PublishEventConfig) => void
  inputSchema?: SchemaDefinition[]
}

// Broker format validation: host:port
const validateBrokerFormat = (broker: string): boolean => {
  if (!broker.trim()) return false
  const parts = broker.trim().split(":")
  if (parts.length !== 2) return false
  const [host, port] = parts
  if (!host || !port) return false
  const portNum = parseInt(port, 10)
  return !isNaN(portNum) && portNum > 0 && portNum <= 65535
}

// Parse brokers string to array
const parseBrokers = (brokersString: string): string[] => {
  return brokersString
    .split("\n")
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
}

// Format brokers array to string
const formatBrokers = (brokers: string[]): string => {
  return brokers.join("\n")
}

export function PublishEventConfigFields({ config, onChange, inputSchema = [] }: PublishEventConfigFieldsProps) {
  const { register, watch, setValue, reset, formState: { errors } } = useForm<PublishEventConfig>({
    defaultValues: {
      kafka: {
        brokers: config.kafka?.brokers || [],
        topic: config.kafka?.topic || "",
        key: config.kafka?.key || "",
        headers: config.kafka?.headers || {},
      },
      message: config.message || {},
    },
  })

  // Update form values when config prop changes (e.g., when loading from backend)
  useEffect(() => {
    reset({
      kafka: {
        brokers: config.kafka?.brokers || [],
        topic: config.kafka?.topic || "",
        key: config.kafka?.key || "",
        headers: config.kafka?.headers || {},
      },
      message: config.message || {},
    })
    // Update brokers string state
    setBrokersString(formatBrokers(config.kafka?.brokers || []))
  }, [config, reset])

  const watchedValues = watch()

  // Convert brokers array to string for textarea
  const [brokersString, setBrokersString] = useState<string>(() => {
    return formatBrokers(config.kafka?.brokers || [])
  })

  // Broker validation state
  const [brokerErrors, setBrokerErrors] = useState<Map<number, string>>(new Map())

  // Validate brokers when string changes
  useEffect(() => {
    const brokers = parseBrokers(brokersString)
    const errors = new Map<number, string>()

    brokers.forEach((broker, index) => {
      if (!validateBrokerFormat(broker)) {
        errors.set(index, `Invalid format. Expected "host:port" (e.g., "localhost:9092")`)
      }
    })

    setBrokerErrors(errors)

    // Update config if brokers are valid
    if (errors.size === 0 && brokers.length > 0) {
      const newConfig = {
        ...watchedValues,
        kafka: {
          ...watchedValues.kafka,
          brokers,
        },
      }
      setValue("kafka.brokers", brokers)
      onChange(newConfig)
    } else if (brokers.length === 0) {
      // Clear brokers if empty
      const newConfig = {
        ...watchedValues,
        kafka: {
          ...watchedValues.kafka,
          brokers: [],
        },
      }
      setValue("kafka.brokers", [])
      onChange(newConfig)
    }
  }, [brokersString, watchedValues, setValue, onChange])

  const handleBrokersChange = (value: string) => {
    setBrokersString(value)
  }

  const handleKafkaFieldChange = (field: "topic" | "key", value: string) => {
    const newConfig = {
      ...watchedValues,
      kafka: {
        ...watchedValues.kafka,
        [field]: value,
      },
    }
    setValue(`kafka.${field}`, value)
    onChange(newConfig)
  }

  const handleHeadersChange = (headers: Record<string, string>) => {
    const newConfig = {
      ...watchedValues,
      kafka: {
        ...watchedValues.kafka,
        headers,
      },
    }
    setValue("kafka.headers", headers)
    onChange(newConfig)
  }

  const handleMessageChange = (message: any) => {
    const newConfig = {
      ...watchedValues,
      message,
    }
    setValue("message", message)
    onChange(newConfig)
  }

  const brokers = parseBrokers(brokersString)
  const hasBrokerErrors = brokerErrors.size > 0
  const hasBrokers = brokers.length > 0

  return (
    <div className="space-y-6">
      {/* Kafka Brokers */}
      <div className="space-y-2">
        <Label htmlFor="kafka-brokers">
          Kafka Brokers <span className="text-error-600">*</span>
        </Label>
        <Textarea
          id="kafka-brokers"
          value={brokersString}
          onChange={(e) => handleBrokersChange(e.target.value)}
          placeholder="localhost:9092&#10;broker2:9092&#10;broker3:9092"
          className={cn(
            "font-mono text-sm min-h-[100px]",
            hasBrokerErrors && "border-error-500 focus-visible:ring-error-500",
            !hasBrokers && "border-error-500 focus-visible:ring-error-500"
          )}
          rows={4}
        />
        {hasBrokerErrors && (
          <div className="space-y-1">
            {Array.from(brokerErrors.entries()).map(([index, error]) => (
              <p key={index} className="text-sm text-error-600">
                Line {index + 1}: {error}
              </p>
            ))}
          </div>
        )}
        {!hasBrokers && (
          <p className="text-sm text-error-600">At least one broker is required</p>
        )}
        {!hasBrokerErrors && hasBrokers && (
          <p className="text-xs text-secondary-500">
            {brokers.length} broker{brokers.length !== 1 ? "s" : ""} configured
          </p>
        )}
        <p className="text-xs text-secondary-500">
          Enter one broker address per line in format "host:port" (e.g., "localhost:9092")
        </p>
      </div>

      {/* Topic */}
      <MvelInputEditor
        value={watchedValues.kafka?.topic || ""}
        onChange={(value) => handleKafkaFieldChange("topic", value)}
        inputSchema={inputSchema}
        label="Topic"
        required
        placeholder="user-events or @{input.topic}"
        description="Kafka topic name. Type @{ to see available input fields."
        errors={errors.kafka?.topic?.message as string}
      />

      {/* Key */}
      <MvelInputEditor
        value={watchedValues.kafka?.key || ""}
        onChange={(value) => handleKafkaFieldChange("key", value)}
        inputSchema={inputSchema}
        label="Message Key (Optional)"
        placeholder="message-key or @{input.key}"
        description="Optional message key for partitioning. Type @{ to see available input fields."
      />

      {/* Headers */}
      <div className="space-y-2">
        <Label>Kafka Headers</Label>
        <KeyValuePairsEditor
          pairs={watchedValues.kafka?.headers || {}}
          onChange={handleHeadersChange}
          keyPlaceholder="Header name"
          valuePlaceholder="Header value or @{input.headerValue}"
          emptyMessage="No headers. Click 'Add Pair' to add Kafka message headers."
          inputSchema={inputSchema}
        />
        <p className="text-xs text-secondary-500">
          Optional Kafka message headers. Type @{`{`} in value field to see available input fields.
        </p>
      </div>

      {/* Message */}
      <div className="space-y-2">
        <JsonEditor
          value={watchedValues.message || {}}
          onChange={handleMessageChange}
          label="Message Payload"
          description="Optional message payload. Can be JSON object or template variable."
          placeholder='{"key": "value"} or use ${variable}'
        />
      </div>
    </div>
  )
}

