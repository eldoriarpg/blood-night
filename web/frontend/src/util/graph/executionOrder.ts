/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'

/** One node reached while a chain runs, when, and the wire it was reached through, if any. */
export interface Step {
    node: string
    /** The id of the wire the node was reached through, as the canvas names wires, e.g. `exec:tick:next:0`. */
    wire: string | undefined
    /** When the node is reached, in milliseconds after the start. */
    at: number
}

/** How long one node takes to show. */
export const STEP_MILLIS = 350
/** The longest a delay holds its chain back in the playback. */
export const LONGEST_DELAY = 10_000

const DEFINE = 'routine.define'
const DELAYS: ReadonlySet<string> = new Set(['flow.delay', 'flow.retriggerable_delay'])
const UNIT_MILLIS: Readonly<Record<string, number>> = {MILLISECONDS: 1, TICKS: 50, SECONDS: 1000, MINUTES: 60_000}

/**
 * The order in which the nodes of a chain are reached when it runs, as the compiled code reaches them, timed for a
 * playback: a node's wired inputs first, each value computed before what reads it and only once, then the node
 * itself, then what its execution outputs run, output by output in the order the node declares them and each
 * output's nodes in order. A routine call runs the routine before going on. A delay holds the rest of its own chain
 * back for its duration (a typed-in one in its unit, a wired one counted as a second, at most
 * {@link LONGEST_DELAY}) while everything after it goes on, as in the plugin. Conditions are not known in the editor,
 * so every arm of a branch, every case of a switch and the body of a loop are shown, one after another.
 *
 * @param graph the graph
 * @param catalog the catalog, with the graph's routine calls
 * @param start the node the chain starts at, e.g. an event
 * @returns the nodes in the order they are reached, by time
 */
export function executionOrder(graph: Graph, catalog: Catalog, start: string): Step[] {
    const nodes = graph.nodes ?? {}
    const definitions = new Map((catalog.nodes ?? []).map(node => [node.id ?? '', node]))
    const steps: Step[] = []
    const reached = new Set<string>()
    const routines = new Set<string>()

    function value(id: string, wire: string, at: number): number {
        if (reached.has(id) || !nodes[id]) return at
        reached.add(id)
        const next = inputs(id, at)
        steps.push({node: id, wire, at: next})
        return next + STEP_MILLIS
    }

    function inputs(id: string, at: number): number {
        const instance = nodes[id]
        const declared = (definitions.get(instance?.type ?? '')?.inputs ?? []).map(port => port.id ?? '')
        const wired = Object.keys(instance?.inputs ?? {})
        let time = at
        for (const port of [...declared.filter(name => wired.includes(name)), ...wired.filter(name => !declared.includes(name))]) {
            const edge = instance?.inputs?.[port]
            if (edge) time = value(edge.node, `data:${id}:${port}`, time)
        }
        return time
    }

    function statement(id: string, wire: string | undefined, at: number): number {
        const instance = nodes[id]
        if (!instance || reached.has(id)) return at
        reached.add(id)
        let time = inputs(id, at)
        steps.push({node: id, wire, at: time})
        time = routine(instance.type ?? '', time + STEP_MILLIS)
        const delayed = DELAYS.has(instance.type ?? '')
        const chainStart = delayed ? time + delayMillis(instance) : time
        let chainTime = chainStart
        for (const port of executionPorts(id)) {
            (instance.executions?.[port] ?? []).forEach((target, index) => {
                chainTime = statement(target, `exec:${id}:${port}:${index}`, chainTime)
            })
        }
        return delayed ? time : chainTime
    }

    function executionPorts(id: string): string[] {
        const instance = nodes[id]
        const declared = (definitions.get(instance?.type ?? '')?.executions ?? []).map(port => port.id ?? '')
        const used = Object.keys(instance?.executions ?? {})
        return [...declared.filter(name => used.includes(name)), ...used.filter(name => !declared.includes(name))]
    }

    function routine(type: string, at: number): number {
        if (!type.startsWith('routine.') || type === DEFINE || routines.has(type)) return at
        routines.add(type)
        const name = type.slice('routine.'.length)
        const define = Object.entries(nodes).find(([, node]) => node.type === DEFINE && node.config?.name === name)?.[0]
        return define ? statement(define, undefined, at) : at
    }

    statement(start, undefined, 0)
    return steps.sort((a, b) => a.at - b.at)
}

function delayMillis(instance: {literals?: Record<string, unknown>, config?: Record<string, unknown>}): number {
    const duration = instance.literals?.duration
    const unit = UNIT_MILLIS[typeof instance.config?.unit === 'string' ? instance.config.unit : 'SECONDS'] ?? 1000
    const millis = typeof duration === 'number' ? duration * unit : 1000
    return Math.min(Math.max(millis, 0), LONGEST_DELAY)
}
