/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog, ConfigInfo, NodeType} from '~/api/generated/schema'
import type {Graph, NodeInstance} from '~/api/generated/payload'
import {isTypeField} from '~/util/graph/pickedTypes'
import {GraphChanges} from '~/util/graph/graphChanges'
import type {PatchOperation} from '~/util/patch/jsonPatch'

/**
 * Sets a config field of a node, keeping the node consistent:
 * <ul>
 *   <li>a new type pick clears the settings and typed-in values typed by the old one, such as a value's value, since
 *   they would no longer fit;</li>
 *   <li>a name naming state related nodes already share, such as an existing variable, also takes over the type
 *   those nodes hold it as, so Get and Set Variable follow the variable they name.</li>
 * </ul>
 *
 * @param base the graph's path in the payload
 * @param graph the graph
 * @param catalog the catalog
 * @param node the node's id
 * @param field the field's id
 * @param value the new value, or `undefined` to clear it
 * @returns the operations
 */
export function configEdit(base: string, graph: Graph, catalog: Catalog, node: string, field: string, value: unknown): PatchOperation[] {
    const instance = graph.nodes?.[node]
    const definition = catalog.nodes?.find(candidate => candidate.id === instance?.type)
    const editor = new GraphChanges(base, graph)
    const operations = [...editor.config(node, field, value), ...stale(editor, definition, node, instance, field)]
    const after: Graph = {...graph, nodes: {...graph.nodes, [node]: {...instance, type: instance?.type ?? '', config: {...instance?.config, [field]: value}}}}
    const shared = field === NAME && typeof value === 'string' ? sharedType(graph, catalog, node, value) : undefined
    return shared ? [...operations, ...configEdit(base, after, catalog, node, shared.field, shared.choice)] : operations
}

const NAME = 'name'

/**
 * The removals a new type pick needs: settings and typed-in values typed by the variable the field picks.
 */
function stale(editor: GraphChanges, definition: NodeType | undefined, node: string, instance: NodeInstance | undefined, field: string): PatchOperation[] {
    const selects = definition?.config?.find(candidate => candidate.id === field)?.selects
    if (selects === undefined || instance === undefined) return []
    const settings = (definition?.config ?? [])
        .filter(other => other.type?.variable === selects && other.id !== field)
        .flatMap(other => editor.config(node, other.id ?? '', undefined))
    const literals = (definition?.inputs ?? [])
        .filter(port => port.type?.variable === selects && instance.literals?.[port.id ?? ''] !== undefined)
        .flatMap(port => editor.literal(node, port.id ?? '', undefined))
    return [...settings, ...literals]
}

/**
 * The type field and choice a node should take for a name its related nodes already use: the pick of the first
 * other node of the same family (e.g. `variable.`) with that name, when it differs from the node's own.
 */
function sharedType(graph: Graph, catalog: Catalog, node: string, name: string): {field: string, choice: string} | undefined {
    const instance = graph.nodes?.[node]
    const family = familyOf(instance?.type ?? '')
    const own = typeField(catalog, instance?.type)
    if (!own?.id || family === '') return undefined
    for (const [id, other] of Object.entries(graph.nodes ?? {})) {
        if (id === node || familyOf(other.type ?? '') !== family || other.config?.[NAME] !== name) continue
        const field = typeField(catalog, other.type)
        const choice = other.config?.[field?.id ?? ''] ?? field?.defaultValue
        const current = instance?.config?.[own.id] ?? own.defaultValue
        return typeof choice === 'string' && choice !== current ? {field: own.id, choice} : undefined
    }
    return undefined
}

function typeField(catalog: Catalog, type: string | undefined): ConfigInfo | undefined {
    return catalog.nodes?.find(candidate => candidate.id === type)?.config?.find(isTypeField)
}

function familyOf(type: string): string {
    return type.slice(0, type.lastIndexOf('.') + 1)
}

/**
 * Types a colour into the red, green and blue inputs of a node at once.
 *
 * @param base the graph's path in the payload
 * @param graph the graph
 * @param node the node's id
 * @param hex the colour as `#rrggbb`
 * @returns the operations
 */
export function channelsEdit(base: string, graph: Graph, node: string, hex: string): PatchOperation[] {
    const [red, green, blue] = [1, 3, 5].map(start => Number.parseInt(hex.slice(start, start + 2), 16))
    return new GraphChanges(base, graph).literals(node, {red, green, blue})
}

/**
 * @param literals a node's typed-in values
 * @returns its red, green and blue values as `#rrggbb`, missing ones as 0
 */
export function channelsHex(literals: Readonly<Record<string, unknown>> | undefined): string {
    return `#${['red', 'green', 'blue'].map(channel => {
        const value = literals?.[channel]
        const byte = typeof value === 'number' ? Math.max(0, Math.min(255, Math.round(value))) : 0
        return byte.toString(16).padStart(2, '0')
    }).join('')}`
}
