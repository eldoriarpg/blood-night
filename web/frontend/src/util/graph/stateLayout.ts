/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {ElkExtendedEdge, ElkNode} from 'elkjs/lib/elk-api'
import type {Graph} from '~/api/generated/payload'
import {stateRows, transitions} from '~/util/graph/graphStates'

/** A state drawn as a box; a state with states inside it holds their boxes below its name. */
export interface StateBox {
    name: string
    x: number
    y: number
    width: number
    height: number
    depth: number
    initial: boolean
    parent: boolean
}

/** A move between states, drawn as a line through its points. */
export interface StateArrow {
    from: string
    to: string
    points: {x: number, y: number}[]
}

/** The states laid out, all in one coordinate space. */
export interface StateLayout {
    boxes: StateBox[]
    arrows: StateArrow[]
    width: number
    height: number
}

/** The name the part that always runs has in the overview, when a move starts there. */
export const ALWAYS = '\u0000always'
const LEAF_HEIGHT = 36
const CHARACTER = 8
const LABEL = 28
const OPTIONS: Readonly<Record<string, string>> = {
    'elk.algorithm': 'layered',
    'elk.direction': 'RIGHT',
    'elk.hierarchyHandling': 'INCLUDE_CHILDREN',
    'elk.edgeRouting': 'ORTHOGONAL',
    'elk.spacing.nodeNode': '28',
    'elk.layered.spacing.nodeNodeBetweenLayers': '48',
    'elk.padding': `[top=${LABEL},left=14,bottom=14,right=14]`,
}

/**
 * Lays the states of a graph out as boxes inside boxes, with an arrow for every move between them, and a box for the
 * part that always runs when a move starts there.
 *
 * @returns the boxes, parents before their children, and the arrows, all in one coordinate space
 */
export async function stateLayout(graph: Graph): Promise<StateLayout> {
    const rows = stateRows(graph)
    const moves = transitions(graph).filter(move => rows.some(row => row.name === move.to))
    const always = moves.some(move => move.from === undefined)
    const nodes = new Map<string, ElkNode>()
    const root: ElkNode = {id: 'root', layoutOptions: {...OPTIONS}, children: []}
    if (always) root.children!.push(leaf(ALWAYS, 'always'))
    for (const row of rows) {
        const node = leaf(row.name, row.name)
        nodes.set(row.name, node)
        const container = row.parent ? nodes.get(row.parent) : root
        container!.children = [...(container!.children ?? []), node]
    }
    const edges: ElkExtendedEdge[] = moves.map((move, index) => ({
        id: `move_${index}`,
        sources: [move.from ?? ALWAYS],
        targets: [move.to],
    }))
    root.edges = edges
    const {default: Elk} = await import('elkjs/lib/elk.bundled.js')
    const laid = await new Elk().layout(root)
    const boxes: StateBox[] = []
    const initial = new Map(rows.map(row => [row.name, row.initial]))
    const collect = (node: ElkNode, x: number, y: number, depth: number): void => {
        for (const child of node.children ?? []) {
            const left = x + (child.x ?? 0)
            const top = y + (child.y ?? 0)
            boxes.push({
                name: child.id,
                x: left,
                y: top,
                width: child.width ?? 0,
                height: child.height ?? 0,
                depth,
                initial: initial.get(child.id) ?? false,
                parent: (child.children ?? []).length > 0,
            })
            collect(child, left, top, depth + 1)
        }
    }
    collect(laid, 0, 0, 0)
    const arrows = (laid.edges ?? []).flatMap((edge, index) => {
        const section = (edge as ElkExtendedEdge).sections?.[0]
        const move = moves[index]
        if (!section || !move) return []
        return [{
            from: move.from ?? ALWAYS,
            to: move.to,
            points: [section.startPoint, ...(section.bendPoints ?? []), section.endPoint],
        }]
    })
    return {boxes, arrows, width: laid.width ?? 0, height: laid.height ?? 0}
}

function leaf(id: string, label: string): ElkNode {
    return {
        id,
        width: Math.max(80, label.length * CHARACTER + 32),
        height: LEAF_HEIGHT,
        layoutOptions: {'elk.padding': `[top=${LABEL},left=14,bottom=14,right=14]`},
    }
}
