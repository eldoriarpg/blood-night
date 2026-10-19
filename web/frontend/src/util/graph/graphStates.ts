/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog} from '~/api/generated/schema'
import type {Graph, NodeInstance, State} from '~/api/generated/payload'
import type {SessionChoices} from '~/util/graph/graphView'
import type {PatchOperation} from '~/util/patch/jsonPatch'
import {pointer} from '~/util/patch/jsonPointer'

/** The node types of states, as the server names them. */
export const STATE_NODES = {
    enter: 'state.enter',
    exit: 'state.exit',
    canEnter: 'state.can_enter',
    goTo: 'state.go_to',
    goToOneOf: 'state.go_to_one_of',
    in: 'state.in',
} as const
/** How many states one Go To One Of picks from. */
export const CHOICES = 4
/** Names of states: lower case letters, digits and underscores, starting with a letter. */
export const STATE_NAME = /^[a-z][a-z0-9_]*$/
const ONLY_IN_STATES: ReadonlySet<string> = new Set([STATE_NODES.enter, STATE_NODES.exit, STATE_NODES.canEnter])

/** A state with how deep it lies, for lists that show nesting by indent. */
export interface StateRow {
    name: string
    parent: string | undefined
    initial: boolean
    depth: number
}

/**
 * @returns the graph's states, each followed by the states inside it, with their depth
 */
export function stateRows(graph: Graph): StateRow[] {
    const states = graph.states ?? []
    const rows: StateRow[] = []
    const visit = (parent: string | undefined, depth: number): void => {
        for (const state of states.filter(candidate => candidate.parent === parent)) {
            rows.push({name: state.name, parent: state.parent, initial: state.initial ?? false, depth})
            visit(state.name, depth + 1)
        }
    }
    visit(undefined, 0)
    return rows
}

/**
 * @returns the state and every state it lies in, innermost first
 */
export function stateChain(graph: Graph, state: string | undefined): string[] {
    const parents = new Map((graph.states ?? []).map(entry => [entry.name, entry.parent]))
    const chain: string[] = []
    for (let current = state; current !== undefined && !chain.includes(current); current = parents.get(current)) {
        chain.push(current)
    }
    return chain
}

/**
 * What a canvas shows for one state: the nodes in it, and the nodes of the part that always runs or of a state it lies
 * in whose values they read, which are shown too, so no wire leads nowhere.
 *
 * @param state the state's name, or undefined for the part that always runs
 * @returns the graph with only those nodes and the state's comment frames, and the ids of the nodes shown only for
 *     their values
 */
export function stateGraph(graph: Graph, state: string | undefined): {graph: Graph, borrowed: ReadonlySet<string>} {
    const nodes = graph.nodes ?? {}
    const own = Object.entries(nodes).filter(([, node]) => node.state === state)
    const reachable = new Set(stateChain(graph, state).slice(1))
    const borrowed = new Set<string>()
    for (const [, node] of own) {
        for (const edge of Object.values(node.inputs ?? {})) {
            const source = nodes[edge.node]
            if (source && source.state !== state && (source.state === undefined || reachable.has(source.state))) borrowed.add(edge.node)
        }
    }
    const shown: Record<string, NodeInstance> = Object.fromEntries(own)
    for (const id of borrowed) shown[id] = {...nodes[id], executions: {}}
    const comments = Object.fromEntries(Object.entries(graph.comments ?? {}).filter(([, comment]) => comment.state === state))
    return {graph: {...graph, nodes: shown, comments}, borrowed}
}

/**
 * @returns the catalog without the state nodes that do not fit: none while the graph has no states, and no enter, exit
 *     or enter condition outside a state
 */
export function stateCatalog(catalog: Catalog, graph: Graph, state: string | undefined): Catalog {
    const hasStates = (graph.states ?? []).length > 0
    const fits = (id: string): boolean => !id.startsWith('state.') || (hasStates && (state !== undefined || !ONLY_IN_STATES.has(id)))
    return {...catalog, nodes: (catalog.nodes ?? []).filter(node => fits(node.id ?? ''))}
}

/**
 * @returns the state pickers of the state nodes: their config fields offering the graph's states
 */
export function stateChoices(graph: Graph): SessionChoices {
    const names = (graph.states ?? []).map(state => state.name)
    const choice = {options: names, labels: Object.fromEntries(names.map(name => [name, name]))}
    const choices: Record<string, typeof choice> = {
        [`${STATE_NODES.goTo}|state`]: choice,
        [`${STATE_NODES.in}|state`]: choice,
    }
    for (let index = 1; index <= CHOICES; index++) choices[`${STATE_NODES.goToOneOf}|state_${index}`] = choice
    return choices
}

/**
 * @returns the states moved to from each state, by every Go To and Go To One Of in it, for the overview
 */
export function transitions(graph: Graph): {from: string | undefined, to: string}[] {
    const found: {from: string | undefined, to: string}[] = []
    for (const node of Object.values(graph.nodes ?? {})) {
        const targets = node.type === STATE_NODES.goTo
            ? [node.config?.state]
            : node.type === STATE_NODES.goToOneOf
                ? Array.from({length: CHOICES}, (_, index) => node.config?.[`state_${index + 1}`])
                : []
        for (const target of targets) {
            if (typeof target === 'string' && target !== '' && !found.some(seen => seen.from === node.state && seen.to === target)) {
                found.push({from: node.state, to: target})
            }
        }
    }
    return found
}

/**
 * Edits of a graph's states, as operations on the payload.
 *
 * @param base the graph's pointer
 */
export class StateChanges {
    constructor(private readonly base: string, private readonly graph: Graph) {}

    private replaced(states: State[]): PatchOperation {
        return {op: 'add', path: `${this.base}/states`, value: states}
    }

    private states(): State[] {
        return this.graph.states ?? []
    }

    /** Adds a state, initial when it is the first among its siblings. */
    add(name: string, parent: string | undefined): PatchOperation[] {
        const siblings = this.states().filter(state => state.parent === parent)
        const added: State = {name, ...(parent ? {parent} : {}), ...(siblings.length === 0 ? {initial: true} : {})}
        const states = [...this.states()]
        const after = parent ? lastInside(states, parent) : states.length - 1
        states.splice(after + 1, 0, added)
        return [this.replaced(states)]
    }

    /** Renames a state, and every node, frame and state node naming it. */
    rename(from: string, to: string): PatchOperation[] {
        const states = this.states().map(state => ({
            ...state,
            ...(state.name === from ? {name: to} : {}),
            ...(state.parent === from ? {parent: to} : {}),
        }))
        const operations: PatchOperation[] = [this.replaced(states)]
        for (const [id, node] of Object.entries(this.graph.nodes ?? {})) {
            const config = renamedConfig(node, from, to)
            if (node.state !== from && config === node.config) continue
            operations.push({op: 'add', path: this.base + pointer('nodes', id), value: {...node, config, ...(node.state === from ? {state: to} : {})}})
        }
        for (const [id, comment] of Object.entries(this.graph.comments ?? {})) {
            if (comment.state === from) operations.push({op: 'add', path: this.base + pointer('comments', id, 'state'), value: to})
        }
        return operations
    }

    /** Makes a state the initial one among its siblings. */
    initial(name: string): PatchOperation[] {
        const parent = this.states().find(state => state.name === name)?.parent
        return [this.replaced(this.states().map(state => {
            if (state.parent !== parent) return state
            const {initial: _, ...rest} = state
            return state.name === name ? {...rest, initial: true} : rest
        }))]
    }

    /** Removes an empty state without states inside; the next sibling becomes initial when it was. */
    remove(name: string): PatchOperation[] {
        const removed = this.states().find(state => state.name === name)
        const left = this.states().filter(state => state.name !== name)
        if (removed?.initial) {
            const next = left.find(state => state.parent === removed.parent)
            if (next) next.initial = true
        }
        return [this.replaced(left)]
    }
}

/**
 * @returns whether a state can be removed: nothing lies in it, no node, frame or other state
 */
export function removable(graph: Graph, name: string): boolean {
    return !Object.values(graph.nodes ?? {}).some(node => node.state === name)
        && !Object.values(graph.comments ?? {}).some(comment => comment.state === name)
        && !(graph.states ?? []).some(state => state.parent === name)
}

function lastInside(states: readonly State[], parent: string): number {
    let last = states.findIndex(state => state.name === parent)
    for (let index = last + 1; index < states.length; index++) {
        if (stateChainOf(states, states[index]!.name).includes(parent)) last = index
    }
    return last
}

function stateChainOf(states: readonly State[], name: string): string[] {
    return stateChain({nodes: {}, states: [...states]}, name)
}

function renamedConfig(node: NodeInstance, from: string, to: string): NodeInstance['config'] {
    if (!node.type?.startsWith('state.') || !node.config) return node.config
    const entries = Object.entries(node.config)
    if (!entries.some(([, value]) => value === from)) return node.config
    return Object.fromEntries(entries.map(([key, value]) => [key, value === from ? to : value]))
}
