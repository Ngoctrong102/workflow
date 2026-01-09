import { useState } from "react"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { X, Plus, Mail } from "lucide-react"

interface RecipientsInputProps {
  value: string[]
  onChange: (recipients: string[]) => void
  error?: string
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function RecipientsInput({ value, onChange, error }: RecipientsInputProps) {
  const [inputValue, setInputValue] = useState("")
  const [inputError, setInputError] = useState<string | null>(null)

  const validateEmail = (email: string): boolean => {
    return EMAIL_REGEX.test(email.trim())
  }

  const handleAdd = () => {
    const email = inputValue.trim()
    
    if (!email) {
      setInputError("Email is required")
      return
    }

    if (!validateEmail(email)) {
      setInputError("Invalid email format")
      return
    }

    if (value.includes(email)) {
      setInputError("Email already added")
      return
    }

    onChange([...value, email])
    setInputValue("")
    setInputError(null)
  }

  const handleRemove = (email: string) => {
    onChange(value.filter((e) => e !== email))
  }

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      e.preventDefault()
      handleAdd()
    }
  }

  return (
    <div className="space-y-2">
      <Label htmlFor="recipients">Recipients *</Label>
      <div className="flex gap-2">
        <div className="flex-1 relative">
          <Mail className="absolute left-2.5 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
          <Input
            id="recipients"
            type="email"
            value={inputValue}
            onChange={(e) => {
              setInputValue(e.target.value)
              setInputError(null)
            }}
            onKeyPress={handleKeyPress}
            placeholder="Enter email address"
            className="pl-8"
          />
        </div>
        <Button type="button" onClick={handleAdd} size="sm">
          <Plus className="h-4 w-4 mr-1" />
          Add
        </Button>
      </div>
      {(inputError || error) && (
        <p className="text-sm text-error-600">{inputError || error}</p>
      )}
      {value.length > 0 && (
        <div className="flex flex-wrap gap-2 mt-2">
          {value.map((email) => (
            <Badge key={email} variant="secondary" className="flex items-center gap-1">
              {email}
              <button
                type="button"
                onClick={() => handleRemove(email)}
                className="ml-1 hover:text-error-600"
              >
                <X className="h-3 w-3" />
              </button>
            </Badge>
          ))}
        </div>
      )}
      <p className="text-xs text-secondary-500">
        Add at least one recipient email address. Press Enter or click Add to add an email.
      </p>
    </div>
  )
}

