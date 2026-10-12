/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog, NodeType, TypeRef} from '~/api/generated/schema'
import type {Graph, Macro} from '~/api/generated/payload'
import {handles, REROUTES} from '~/util/graph/graphView'
import {NUMBERS, nodeTone, takesExecution, type NodeTone} from '~/util/graph/pins'
import {isTypeField} from '~/util/graph/pickedTypes'
import {boundType, type GraphBindings} from '~/util/graph/graphBindings'

/** The pin a wire was pulled from: which node, which handle, and the type it carries when it is a data pin. */
export interface PulledPin {
    node: string
    handle: string
    type: TypeRef | undefined
}

/** A node that can take the pulled wire, and the handle of its own the wire goes to. */
export interface NodeMatch {
    type: string
    name: string
    category: string
    tone: NodeTone
    port: string
    /** Whether the wire meets a type variable rather than a type, which takes anything. */
    wildcard: boolean
    /** Config the node is added with, such as the type a cast picks so it fits the wire. */
    config: Record<string, string>
}

/**
 * The pin a handle of a node in the graph stands for, with its type from the catalog. Nodes the catalog does not know
 * (macro and routine uses) give no type.
 *
 * @param graph the graph
 * @param catalog the catalog
 * @param node the node's id
 * @param handle the handle's id, e.g. `out:value`
 * @param bindings the graph's bindings of graph-wide type variables
 * @returns the pin
 */
export function pulledPin(graph: Graph, catalog: Catalog, node: string, handle: string, bindings: GraphBindings = {}): PulledPin {
    const definition = catalog.nodes?.find(candidate => candidate.id === graph.nodes?.[node]?.type)
    const ports = handle.startsWith('in:') ? definition?.inputs : handle.startsWith('out:') ? definition?.outputs : undefined
    const port = handle.slice(handle.indexOf(':') + 1)
    return {node, handle, type: boundType(ports?.find(candidate => candidate.id === port)?.type, catalog, bindings)}
}

/**
 * Whether a value of one type can be passed where another is expected, as the compiler decides it: equal types, a
 * type the catalog lists as assignable, and type variables on either side, which take whatever they are bound to;
 * numeric variables take numbers only.
 * A generic type with variables matches by its class.
 *
 * @param catalog the catalog with its types
 * @param target the type expected
 * @param source the type of the value
 * @returns whether the wire fits
 */
export function accepts(catalog: Catalog, target: TypeRef | undefined, source: TypeRef | undefined): boolean {
    if (target?.numeric) return !source || source.variable !== undefined || NUMBERS.has(source.id ?? '')
    if (!target || !source || target.variable || source.variable) return true
    const sourceId = source.id ?? source.raw ?? ''
    const targetId = target.id ?? ''
    if (sourceId === targetId) return true
    const assignable = catalog.types?.find(type => type.id === sourceId)?.assignableTo ?? []
    if (targetId !== '') return assignable.includes(targetId)
    const raw = target.raw ?? ''
    return rawOf(sourceId) === raw || assignable.some(id => rawOf(id) === raw)
}

function rawOf(id: string): string {
    const bracket = id.indexOf('<')
    return bracket < 0 ? id : id.slice(0, bracket)
}

/**
 * The catalog's nodes that can take a wire pulled from a pin, each with the handle the wire goes to: an execution
 * output wants a node that takes execution, an execution input a node with an execution output, a data output a node
 * with an input that accepts its type, a data input a node with an output of a type it accepts.
 *
 * @param catalog the catalog
 * @param pin the pulled pin
 * @param inMacro whether the graph is a macro's, which may hold nodes others may not
 * @returns the matching nodes: reroutes first, then those meeting a type, then those meeting only a type variable
 */
export function nodeMatches(catalog: Catalog, pin: PulledPin, inMacro: boolean): NodeMatch[] {
    return (catalog.nodes ?? [])
        .filter(node => (inMacro || !node.macroOnly) && !node.family)
        .flatMap(node => {
            const match = matchingPort(catalog, node, pin)
            if (match === undefined) return []
            const type = node.id ?? ''
            return [{type, name: node.name ?? type, category: node.category ?? '', tone: nodeTone(node.kind, type), ...match}]
        })
        .sort((a, b) => Number(REROUTES.has(b.type)) - Number(REROUTES.has(a.type)) || Number(a.wildcard) - Number(b.wildcard))
}

/**
 * Every node that can be added to a graph without a wire, as the palette lists them: the catalog's nodes except
 * variants and reroutes, and the session's macros.
 *
 * @param catalog the catalog
 * @param macros the session's macros
 * @param inMacro whether the graph is a macro's, which may hold nodes others may not
 * @returns the nodes, in catalog order, macros last
 */
export function addableNodes(catalog: Catalog, macros: readonly Macro[], inMacro: boolean): NodeMatch[] {
    return [
        ...(catalog.nodes ?? [])
            .filter(node => (inMacro || !node.macroOnly) && !node.family && !REROUTES.has(node.id ?? ''))
            .map(node => ({type: node.id ?? '', name: node.name ?? node.id ?? '', category: node.category ?? '', tone: nodeTone(node.kind, node.id ?? '')})),
        ...macros.map(macro => ({type: `macro.${macro.id}`, name: macro.name ?? macro.id ?? '', category: 'macro', tone: nodeTone(undefined, 'macro.')})),
    ].map(entry => ({...entry, port: '', wildcard: false, config: {}}))
}

type PortMatch = Pick<NodeMatch, 'port' | 'wildcard' | 'config'>

function matchingPort(catalog: Catalog, node: NodeType, pin: PulledPin): PortMatch | undefined {
    if (pin.handle.startsWith('exec:')) return takesExecution(node.kind) ? {port: handles.execIn, wildcard: false, config: {}} : undefined
    if (pin.handle === handles.execIn) {
        const execution = node.executions?.[0]?.id
        return execution === undefined ? undefined : {port: handles.execOut(execution), wildcard: false, config: {}}
    }
    const pullsOutput = pin.handle.startsWith('out:')
    const fit = (type: TypeRef | undefined) => pullsOutput ? accepts(catalog, type, pin.type) : accepts(catalog, pin.type, type)
    for (const port of (pullsOutput ? node.inputs : node.outputs) ?? []) {
        const picked = pickFor(catalog, node, port.type, pin.type, fit)
        if (picked && port.id !== undefined) {
            return {port: pullsOutput ? handles.input(port.id) : handles.output(port.id), wildcard: false, config: picked}
        }
    }
    const fits = ((pullsOutput ? node.inputs : node.outputs) ?? []).filter(port => !pickedBy(node, port.type) && fit(port.type))
    const port = fits.find(candidate => !isWildcard(candidate.type)) ?? fits[0]
    if (port?.id === undefined) return undefined
    return {port: pullsOutput ? handles.input(port.id) : handles.output(port.id), wildcard: isWildcard(port.type), config: {}}
}

function pickedBy(node: NodeType, type: TypeRef | undefined) {
    return type?.variable === undefined ? undefined : (node.config ?? []).filter(isTypeField).find(field => field.selects === type.variable)
}

/**
 * For a port whose type a type field picks: the pick that lets the wire fit, the pulled pin's own type first.
 */
function pickFor(
    catalog: Catalog,
    node: NodeType,
    type: TypeRef | undefined,
    wanted: TypeRef | undefined,
    fit: (type: TypeRef) => boolean,
): Record<string, string> | undefined {
    const field = pickedBy(node, type)
    if (!field?.id || !wanted?.id) return undefined
    const candidates = (field.choices ?? [])
        .map(choice => ({choice, type: {id: catalog.selectable?.[choice], arguments: []}}))
        .filter(candidate => candidate.type.id !== undefined)
    const choice = candidates.find(candidate => candidate.type.id === wanted.id) ?? candidates.find(candidate => fit(candidate.type))
    return choice ? {[field.id]: choice.choice} : undefined
}

function isWildcard(type: TypeRef | undefined): boolean {
    return type?.variable !== undefined && !type.numeric
}
