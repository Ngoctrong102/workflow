/**
 * Undo/Redo utility for managing state history
 */

export interface HistoryState<T> {
  past: T[]
  present: T
  future: T[]
}

export interface UndoRedoActions<T> {
  undo: () => T | null
  redo: () => T | null
  canUndo: () => boolean
  canRedo: () => boolean
  clear: () => void
}

/**
 * Create undo/redo manager
 */
export function createUndoRedo<T>(initialState: T, maxHistory: number = 50): {
  state: HistoryState<T>
  actions: UndoRedoActions<T>
  setState: (newState: T) => void
} {
  let history: HistoryState<T> = {
    past: [],
    present: initialState,
    future: [],
  }

  const setState = (newState: T) => {
    // Don't add to history if state hasn't changed
    if (JSON.stringify(history.present) === JSON.stringify(newState)) {
      return
    }

    history = {
      past: [...history.past.slice(-maxHistory + 1), history.present],
      present: newState,
      future: [],
    }
  }

  const undo = (): T | null => {
    if (history.past.length === 0) {
      return null
    }

    const previous = history.past[history.past.length - 1]
    history = {
      past: history.past.slice(0, -1),
      present: previous,
      future: [history.present, ...history.future],
    }

    return history.present
  }

  const redo = (): T | null => {
    if (history.future.length === 0) {
      return null
    }

    const next = history.future[0]
    history = {
      past: [...history.past, history.present],
      present: next,
      future: history.future.slice(1),
    }

    return history.present
  }

  const canUndo = (): boolean => history.past.length > 0
  const canRedo = (): boolean => history.future.length > 0

  const clear = () => {
    history = {
      past: [],
      present: history.present,
      future: [],
    }
  }

  return {
    state: history,
    actions: {
      undo,
      redo,
      canUndo,
      canRedo,
      clear,
    },
    setState,
  }
}

