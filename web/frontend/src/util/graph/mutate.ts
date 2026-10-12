/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog, NodeType, TypeRef} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'
import {applyPatch, type PatchOperation} from '~/util/patch/jsonPatch'
import {pointer, segments} from '~/util/patch/jsonPointer'
import {pulledPin} from '~/util/graph/nodeMatches'
import type {GraphBindings} from '~/util/graph/graphBindings'

const TARGET = 'target'

/**
 * @param catalog the catalog
 * @param type a node type id
 * @returns whether the editor shows the node as a family of variants, such as Mutate
 */
export function isFamilyRoot(catalog: Catalog, type: string): boolean {
    return (catalog.nodes ?? []).some(node => node.family === type)
}

/**
 * @param catalog the catalog
 * @param type a node type id
 * @returns the family the node type is a variant of, if any
 */
export function familyOf(catalog: Catalog, type: string): string | undefined {
    if (isFamilyRoot(catalog, type)) return type
    return (catalog.nodes ?? []).find(node => node.id === type)?.family
}

function targetType(node: NodeType): string | undefined {
    return node.inputs?.find(port => port.id === TARGET)?.type?.id
}

function ports(node: NodeType): number {
    return (node.inputs?.length ?? 0) + (node.outputs?.length ?? 0)
}

/** The ports a node uses: the inputs wired or typed in, and the outputs other nodes read. */
export interface UsedPorts {
    inputs: readonly string[]
    outputs: readonly string[]
}

/**
 * The variant of a family for a type wired into it: of the variants having every port the node uses, the one of
 * exactly that type, else the most specific one the type can be passed to; the reading variant unless something is
 * changed, or when the type has none.
 *
 * @param catalog the catalog
 * @param family the family's root node id
 * @param source the type wired into `target`
 * @param changes whether properties are changed
 * @param used the ports the node uses, which the variant must have
 * @returns the variant's node type id, if any fits
 */
export function variantFor(
    catalog: Catalog,
    family: string,
    source: TypeRef | undefined,
    changes: boolean,
    used: UsedPorts = {inputs: [], outputs: []},
): string | undefined {
    const id = source?.id
    if (!id) return undefined
    const assignable = new Set([id, ...((catalog.types ?? []).find(type => type.id === id)?.assignableTo ?? [])])
    const has = (node: NodeType) => used.inputs.every(port => node.inputs?.some(candidate => candidate.id === port))
        && used.outputs.every(port => node.outputs?.some(candidate => candidate.id === port))
    const fitting = (catalog.nodes ?? [])
        .filter(node => node.family === family && assignable.has(targetType(node) ?? ''))
        .filter(node => node.kind === 'action' ? has(node) : used.outputs.every(port => node.outputs?.some(candidate => candidate.id === port)))
    const exact = fitting.filter(node => targetType(node) === id)
    const pool = exact.length > 0 ? exact : fitting
    const ranked = [...pool].sort((a, b) => ports(b) - ports(a))
    const kind = changes ? 'action' : 'pure'
    return (ranked.find(node => node.kind === kind) ?? ranked.find(node => node.kind === 'action'))?.id
}

/**
 * @param catalog the catalog
 * @param type a variant's node type id
 * @returns the variant of the same family and type that changes properties, if the given one only reads
 */
export function changingSibling(catalog: Catalog, type: string): NodeType | undefined {
    const node = (catalog.nodes ?? []).find(candidate => candidate.id === type)
    if (!node?.family || node.kind === 'action') return undefined
    return (catalog.nodes ?? []).find(candidate =>
        candidate.family === node.family && candidate.kind === 'action' && targetType(candidate) === targetType(node))
}

/**
 * The operations that keep every family node the right variant after a change to a graph: a node whose `target` is
 * wired turns into the variant of the wired type, changing when a property besides `target` is wired or typed in and
 * reading otherwise; a node turning from changing to reading loses its execution wires.
 *
 * @param base the graph's pointer in the payload
 * @param graph the graph before the change
 * @param catalog the catalog
 * @param operations the change
 * @param bindings the graph's bindings of graph-wide type variables, such as the mob's own type
 * @returns the change followed by the operations fixing the variants
 */
export function withVariants(
    base: string,
    graph: Graph,
    catalog: Catalog,
    operations: readonly PatchOperation[],
    bindings: GraphBindings = {},
): PatchOperation[] {
    const path = segments(base)
    const document: Record<string, unknown> = {}
    let cursor = document
    path.slice(0, -1).forEach(segment => {
        const next: Record<string, unknown> = {}
        cursor[segment] = next
        cursor = next
    })
    cursor[path.at(-1) ?? ''] = graph
    const after = path.reduce<unknown>((value, segment) => (value as Record<string, unknown>)[segment], applyPatch(document, operations)) as Graph
    const fixes: PatchOperation[] = []
    for (const [id, instance] of Object.entries(after.nodes ?? {})) {
        const family = familyOf(catalog, instance.type ?? '')
        if (!family || !instance.inputs?.[TARGET]) continue
        const source = pulledPin(after, catalog, instance.inputs[TARGET].node, `out:${instance.inputs[TARGET].port}`, bindings).type
        const inputs = [...Object.keys(instance.inputs ?? {}), ...Object.keys(instance.literals ?? {})].filter(port => port !== TARGET)
        const outputs = Object.values(after.nodes ?? {})
            .flatMap(other => Object.values(other.inputs ?? {}))
            .filter(edge => edge.node === id)
            .map(edge => edge.port)
        const wanted = variantFor(catalog, family, source, inputs.length > 0, {inputs, outputs})
        if (!wanted || wanted === instance.type) continue
        fixes.push({op: 'replace', path: `${base}${pointer('nodes', id, 'type')}`, value: wanted})
        const kind = (catalog.nodes ?? []).find(node => node.id === wanted)?.kind
        if (kind === 'action') continue
        if (instance.executions) fixes.push({op: 'remove', path: `${base}${pointer('nodes', id, 'executions')}`})
        for (const [parent, other] of Object.entries(after.nodes ?? {})) {
            for (const [port, targets] of Object.entries(other.executions ?? {})) {
                targets.map((target, index) => ({target, index}))
                    .filter(({target}) => target === id)
                    .reverse()
                    .forEach(({index}) => fixes.push({op: 'remove', path: `${base}${pointer('nodes', parent, 'executions', port, index)}`}))
            }
        }
    }
    return [...operations, ...fixes]
}
