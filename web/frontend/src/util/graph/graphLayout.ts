/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Graph, NodeInstance} from '~/api/generated/payload'

/** A node's place on the canvas, in canvas pixels. */
export interface Position {
    x: number
    y: number
}

const COLUMN = 300
const ROW = 170

/**
 * Where each node of a graph is drawn: the position the editor stored with it, else a place in a column after the
 * nodes that chain to it and before the nodes it feeds, so a graph written without the editor still reads left to
 * right.
 *
 * @param graph the graph
 * @returns the position of every node by id
 */
export function layout(graph: Graph): Map<string, Position> {
    const nodes = graph.nodes ?? {}
    const columns = columnsOf(nodes)
    const rows = new Map<number, number>()
    const positions = new Map<string, Position>()
    for (const [id, instance] of Object.entries(nodes)) {
        const stored = storedPosition(instance)
        if (stored) {
            positions.set(id, stored)
            continue
        }
        const column = columns.get(id) ?? 0
        const row = rows.get(column) ?? 0
        rows.set(column, row + 1)
        positions.set(id, {x: column * COLUMN, y: row * ROW})
    }
    return positions
}

/**
 * @param instance a node
 * @returns the position the editor stored with it, if any
 */
export function storedPosition(instance: NodeInstance): Position | undefined {
    const editor = instance.editor as {x?: unknown, y?: unknown} | undefined
    if (typeof editor?.x === 'number' && typeof editor.y === 'number') return {x: editor.x, y: editor.y}
    return undefined
}

function columnsOf(nodes: Record<string, NodeInstance>): Map<string, number> {
    const columns = new Map<string, number>(Object.keys(nodes).map(id => [id, 0]))
    const limit = Object.keys(nodes).length
    for (let round = 0; round < limit; round++) {
        let moved = false
        for (const [id, instance] of Object.entries(nodes)) {
            const column = columns.get(id) ?? 0
            for (const targets of Object.values(instance.executions ?? {})) {
                for (const target of targets) moved = push(columns, target, column + 1) || moved
            }
            for (const edge of Object.values(instance.inputs ?? {})) {
                moved = push(columns, id, (columns.get(edge.node) ?? 0) + 1) || moved
            }
        }
        if (!moved) break
    }
    return columns
}

function push(columns: Map<string, number>, id: string, column: number): boolean {
    if (!columns.has(id) || (columns.get(id) ?? 0) >= column) return false
    columns.set(id, column)
    return true
}
