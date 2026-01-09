/**
 * Node grouping utilities
 */

import type { Node, Edge } from "reactflow"

export interface NodeGroup {
  id: string
  name: string
  nodeIds: string[]
  collapsed: boolean
  position: { x: number; y: number }
  size: { width: number; height: number }
}

/**
 * Create a group from selected nodes
 */
export function createGroup(
  selectedNodeIds: string[],
  nodes: Node[],
  name: string = "Group"
): NodeGroup | null {
  if (selectedNodeIds.length === 0) {
    return null
  }

  const selectedNodes = nodes.filter((node) => selectedNodeIds.includes(node.id))
  
  if (selectedNodes.length === 0) {
    return null
  }

  // Calculate bounding box
  const minX = Math.min(...selectedNodes.map((n) => n.position.x))
  const minY = Math.min(...selectedNodes.map((n) => n.position.y))
  const maxX = Math.max(...selectedNodes.map((n) => n.position.x + (n.width || 200)))
  const maxY = Math.max(...selectedNodes.map((n) => n.position.y + (n.height || 100)))

  return {
    id: `group-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    name,
    nodeIds: selectedNodeIds,
    collapsed: false,
    position: { x: minX, y: minY },
    size: {
      width: maxX - minX,
      height: maxY - minY,
    },
  }
}

/**
 * Get nodes in a group
 */
export function getGroupNodes(group: NodeGroup, nodes: Node[]): Node[] {
  return nodes.filter((node) => group.nodeIds.includes(node.id))
}

/**
 * Get edges within a group
 */
export function getGroupEdges(group: NodeGroup, edges: Edge[]): Edge[] {
  return edges.filter(
    (edge) =>
      group.nodeIds.includes(edge.source) && group.nodeIds.includes(edge.target)
  )
}

/**
 * Move a group and its nodes
 */
export function moveGroup(
  group: NodeGroup,
  nodes: Node[],
  delta: { x: number; y: number }
): Node[] {
  const groupNodes = getGroupNodes(group, nodes)
  const nodeMap = new Map(nodes.map((n) => [n.id, n]))

  groupNodes.forEach((node) => {
    const updated = nodeMap.get(node.id)
    if (updated) {
      updated.position = {
        x: updated.position.x + delta.x,
        y: updated.position.y + delta.y,
      }
    }
  })

  return Array.from(nodeMap.values())
}

/**
 * Collapse/expand group
 */
export function toggleGroupCollapse(group: NodeGroup): NodeGroup {
  return {
    ...group,
    collapsed: !group.collapsed,
  }
}

