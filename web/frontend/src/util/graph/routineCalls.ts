/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog, NodeType, PortInfo} from '~/api/generated/schema'
import type {Graph, NodeInstance} from '~/api/generated/payload'

const DEFINE = 'routine.define'
const PARAMETER = 'routine.parameter'

/**
 * The catalog with a call node for every routine the graph defines, as the compiler derives them: `routine.<name>`,
 * "Call <name>", one input per parameter of that routine (named and typed by its Parameter node, the first one of a
 * name winning), then `next`. Routines are part of the graph, not of the server's catalog, so without this they could
 * be neither added nor drawn with their inputs.
 *
 * @param catalog the server's catalog
 * @param graph the graph
 * @returns the catalog with the graph's routine calls
 */
export function withRoutines(catalog: Catalog, graph: Graph): Catalog {
    const nodes = Object.values(graph.nodes ?? {})
    const known = new Set((catalog.nodes ?? []).map(node => node.id))
    const calls = [...new Set(nodes.filter(node => node.type === DEFINE).map(node => text(node, 'name')))]
        .filter((name): name is string => name !== undefined && !known.has(`routine.${name}`))
        .map(name => call(name, parameters(nodes, name, catalog)))
    return calls.length === 0 ? catalog : {...catalog, nodes: [...catalog.nodes ?? [], ...calls]}
}

/**
 * @param graph the graph
 * @returns the names of the routines the graph defines, sorted
 */
export function routineNames(graph: Graph): string[] {
    return [...new Set(Object.values(graph.nodes ?? {})
        .filter(node => node.type === DEFINE)
        .map(node => text(node, 'name'))
        .filter((name): name is string => name !== undefined))].sort()
}

function call(name: string, inputs: PortInfo[]): NodeType {
    return {
        id: `routine.${name}`,
        kind: 'action',
        category: 'routine',
        name: `Call ${name}`,
        description: `Runs the routine ${name}, then continues`,
        macroOnly: false,
        inputs,
        executions: [{id: 'next', name: ''}],
    }
}

function parameters(nodes: readonly NodeInstance[], routine: string, catalog: Catalog): PortInfo[] {
    const first = Object.keys(catalog.selectable ?? {})[0] ?? ''
    const found = new Map<string, PortInfo>()
    for (const node of nodes) {
        const name = text(node, 'name')
        if (node.type !== PARAMETER || text(node, 'routine') !== routine || name === undefined || found.has(name)) continue
        const id = catalog.selectable?.[text(node, 'type') ?? first]
        if (!id) continue
        const literal = (catalog.types ?? []).find(type => type.id === id)?.literal === true
        found.set(name, {id: name, name, description: '', type: {id, arguments: []}, optional: false, literal})
    }
    return [...found.values()]
}

function text(node: NodeInstance, field: string): string | undefined {
    const value = node.config?.[field]
    return typeof value === 'string' && value !== '' ? value : undefined
}
