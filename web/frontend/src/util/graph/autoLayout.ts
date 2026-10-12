/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {ElkExtendedEdge, ElkNode} from 'elkjs/lib/elk-api'
import type {Position} from '~/util/graph/graphLayout'

/** A drawn node as the layout sees it: its measured size and where its pins sit on it. */
export interface LayoutNode {
    id: string
    width: number
    height: number
    /** Pins by handle id, relative to the node's top left corner; inputs on the west side, outputs on the east. */
    ports: {id: string, x: number, y: number, side: 'WEST' | 'EAST'}[]
}

/** A wire between two pins. */
export interface LayoutEdge {
    source: string
    sourceHandle: string
    target: string
    targetHandle: string
}

const OPTIONS: Readonly<Record<string, string>> = {
    'elk.algorithm': 'layered',
    'elk.direction': 'RIGHT',
    'elk.edgeRouting': 'SPLINES',
    'elk.spacing.nodeNode': '40',
    'elk.layered.spacing.nodeNodeBetweenLayers': '90',
    'elk.layered.nodePlacement.strategy': 'NETWORK_SIMPLEX',
    'elk.layered.considerModelOrder.strategy': 'NODES_AND_EDGES',
}

function portId(node: string, handle: string): string {
    return `${node}::${handle}`
}

/**
 * The graph ELK lays out: every node with its size and its pins as ports fixed where they are drawn, every wire as an
 * edge between those ports, flowing to the right.
 *
 * @param nodes the drawn nodes
 * @param edges the wires
 * @returns ELK's input
 */
export function elkGraph(nodes: readonly LayoutNode[], edges: readonly LayoutEdge[]): ElkNode {
    const ports = new Set(nodes.flatMap(node => node.ports.map(port => portId(node.id, port.id))))
    const wires: ElkExtendedEdge[] = edges
        .filter(edge => ports.has(portId(edge.source, edge.sourceHandle)) && ports.has(portId(edge.target, edge.targetHandle)))
        .map((edge, index) => ({
            id: `edge_${index}`,
            sources: [portId(edge.source, edge.sourceHandle)],
            targets: [portId(edge.target, edge.targetHandle)],
        }))
    return {
        id: 'root',
        layoutOptions: {...OPTIONS},
        children: nodes.map(node => ({
            id: node.id,
            width: node.width,
            height: node.height,
            layoutOptions: {'elk.portConstraints': 'FIXED_POS'},
            ports: node.ports.map(port => ({
                id: portId(node.id, port.id),
                x: port.x,
                y: port.y,
                width: 1,
                height: 1,
                layoutOptions: {'elk.port.side': port.side},
            })),
        })),
        edges: wires,
    }
}

/**
 * Lays a graph out left to right, execution and data flowing from one layer of nodes to the next, with few crossings.
 * ELK is loaded on first use.
 *
 * @param nodes the drawn nodes with their sizes and pins
 * @param edges the wires
 * @returns the top left corner of every node, rounded
 */
export async function arrange(nodes: readonly LayoutNode[], edges: readonly LayoutEdge[]): Promise<Map<string, Position>> {
    const {default: Elk} = await import('elkjs/lib/elk.bundled.js')
    const laid = await new Elk().layout(elkGraph(nodes, edges))
    return new Map((laid.children ?? []).map(child => [child.id, {x: Math.round(child.x ?? 0), y: Math.round(child.y ?? 0)}]))
}
