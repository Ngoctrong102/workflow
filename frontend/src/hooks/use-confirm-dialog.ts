import { useState, useCallback } from "react"

interface ConfirmDialogOptions {
  title: string
  description: string
  confirmText?: string
  cancelText?: string
  variant?: "default" | "destructive"
}

interface ConfirmDialogState {
  isOpen: boolean
  options: ConfirmDialogOptions | null
  resolve: ((value: boolean) => void) | null
}

export function useConfirmDialog() {
  const [state, setState] = useState<ConfirmDialogState>({
    isOpen: false,
    options: null,
    resolve: null,
  })

  const confirm = useCallback(
    (options: ConfirmDialogOptions): Promise<boolean> => {
      return new Promise((resolve) => {
        setState({
          isOpen: true,
          options: {
            confirmText: "Confirm",
            cancelText: "Cancel",
            variant: "default",
            ...options,
          },
          resolve,
        })
      })
    },
    []
  )

  const handleConfirm = useCallback(() => {
    if (state.resolve) {
      state.resolve(true)
    }
    setState({
      isOpen: false,
      options: null,
      resolve: null,
    })
  }, [state.resolve])

  const handleCancel = useCallback(() => {
    if (state.resolve) {
      state.resolve(false)
    }
    setState({
      isOpen: false,
      options: null,
      resolve: null,
    })
  }, [state.resolve])

  return {
    confirm,
    isOpen: state.isOpen,
    options: state.options,
    onConfirm: handleConfirm,
    onCancel: handleCancel,
  }
}

