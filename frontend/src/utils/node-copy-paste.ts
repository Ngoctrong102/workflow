/**
 * Node copy/paste utilities
 */

import type { Node, Edge } from "reactflow"

export interface CopiedNodes {
  nodes: Node[]
  edges: Edge[]
  offset: { x: number; y: number }
}

/**
 * Copy selected nodes and their internal edges
 */
export function copyNodes(nodes: Node[], edges: Edge[], selectedNodeIds: string[]): CopiedNodes | null {
  if (selectedNodeIds.length === 0) {
    return null
  }

  const selectedNodes = nodes.filter((node) => selectedNodeIds.includes(node.id))
  
  if (selectedNodes.length === 0) {
    return null
  }

  // Calculate bounding box to determine offset
  const minX = Math.min(...selectedNodes.map((n) => n.position.x))
  const minY = Math.min(...selectedNodes.map((n) => n.position.y))

  // Copy nodes with relative positions
  const copiedNodes = selectedNodes.map((node) => ({
    ...node,
    position: {
      x: node.position.x - minX,
      y: node.position.y - minY,
    },
  }))

  // Copy edges that connect selected nodes (internal edges)
  const copiedEdges = edges.filter(
    (edge) =>
      selectedNodeIds.includes(edge.source) && selectedNodeIds.includes(edge.target)
  ).map((edge) => ({ ...edge }))

  return {
    nodes: copiedNodes,
    edges: copiedEdges,
    offset: { x: minX, y: minY },
  }
}

/**
 * Paste nodes with new IDs and offset
 */
export function pasteNodes(
  copied: CopiedNodes,
  pasteOffset: { x: number; y: number } = { x: 50, y: 50 }
): { nodes: Node[]; edges: Edge[] } {
  const idMap = new Map<string, string>()

  // Generate new IDs for nodes
  const pastedNodes = copied.nodes.map((node) => {
    const newId = `${node.id}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    idMap.set(node.id, newId)
    return {
      ...node,
      id: newId,
      position: {
        x: node.position.x + pasteOffset.x,
        y: node.position.y + pasteOffset.y,
      },
    }
  })

  // Update edge source/target with new IDs
  const pastedEdges = copied.edges.map((edge) => ({
    ...edge,
    id: `edge-${idMap.get(edge.source)}-${idMap.get(edge.target)}-${Date.now()}`,
    source: idMap.get(edge.source)!,
    target: idMap.get(edge.target)!,
  }))

  return {
    nodes: pastedNodes,
    edges: pastedEdges,
  }
}

/**
 * Store copied nodes in clipboard (localStorage as fallback)
 */
export function storeCopiedNodes(copied: CopiedNodes): void {
  try {
    const data = JSON.stringify(copied)
    localStorage.setItem("workflow-copied-nodes", data)
  } catch (error) {
    console.error("Failed to store copied nodes:", error)
  }
}

/**
 * Retrieve copied nodes from clipboard
 */
export function getCopiedNodes(): CopiedNodes | null {
  try {
    const data = localStorage.getItem("workflow-copied-nodes")
    if (!data) return null
    return JSON.parse(data) as CopiedNodes
  } catch (error) {
    console.error("Failed to retrieve copied nodes:", error)
    return null
  }
}

