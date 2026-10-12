/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Graph, NodeInstance} from '~/api/generated/payload'
import type {PatchOperation} from '~/util/patch/jsonPatch'
import type {Position} from '~/util/graph/graphLayout'

/**
 * Builds the patch operations for edits to one graph of the payload, e.g. a mob's behaviour at
 * `/mobs/speed_creeper/behaviour`. Maps a node leaves empty are missing in the payload, so operations that write
 * into one create it first.
 */
/** An execution wire by where it starts: the source node, its output port and the wire's position in that list. */
export interface ExecutionWire {
    source: string
    port: string
    position: number
}

export class GraphChanges {
    /**
     * @param base the pointer of the graph in the payload
     * @param graph the graph as it is now
     */
    constructor(private readonly base: string, private readonly graph: Graph) {}

    private node(id: string): string {
        return `${this.base}/nodes/${escape(id)}`
    }

    private instance(id: string): NodeInstance {
        const instance = this.graph.nodes?.[id]
        if (!instance) throw new Error(`No node ${id}`)
        return instance
    }

    /** Moves a node, keeping what else the editor stored with it. */
    move(id: string, position: Position): PatchOperation[] {
        const editor = (this.instance(id).editor ?? {}) as Record<string, unknown>
        return [{op: 'add', path: `${this.node(id)}/editor`, value: {...editor, x: Math.round(position.x), y: Math.round(position.y)}}]
    }

    /** Adds a node of a type at a position, with a fresh id and, if given, the wires leaving or feeding it and its config. */
    add(type: string, position: Position, wires: Pick<NodeInstance, 'inputs' | 'executions' | 'config'> = {}): {id: string, operations: PatchOperation[]} {
        const id = freshId(type, Object.keys(this.graph.nodes ?? {}))
        const operations: PatchOperation[] = this.graph.nodes ? [] : [{op: 'add', path: `${this.base}/nodes`, value: {}}]
        operations.push({op: 'add', path: this.node(id), value: {type, ...wires, editor: {x: Math.round(position.x), y: Math.round(position.y)}}})
        return {id, operations}
    }

    /**
     * Adds a node of a type at a position and wires it to the pin a wire was pulled from, as a node picked at the
     * end of a pulled wire is added.
     *
     * @param type the new node's type
     * @param position where it goes
     * @param pulled the node and handle the wire was pulled from
     * @param port the new node's handle the wire goes to
     * @param config config to add the node with, such as a picked type
     * @returns the new node's id and the operations
     */
    addWired(
        type: string,
        position: Position,
        pulled: {node: string, handle: string},
        port: string,
        config: Record<string, string> = {},
    ): {id: string, operations: PatchOperation[]} {
        const configured = Object.keys(config).length > 0 ? {config} : {}
        if (pulled.handle.startsWith('out:') && port.startsWith('in:')) {
            return this.add(type, position, {...configured, inputs: {[port.slice(3)]: {node: pulled.node, port: pulled.handle.slice(4)}}})
        }
        if (pulled.handle === 'exec-in' && port.startsWith('exec:')) {
            return this.add(type, position, {...configured, executions: {[port.slice(5)]: [pulled.node]}})
        }
        const {id, operations} = this.add(type, position, configured)
        if (pulled.handle.startsWith('in:') && port.startsWith('out:')) {
            operations.push(...this.connectData(pulled.node, pulled.handle.slice(3), id, port.slice(4)))
        } else if (pulled.handle.startsWith('exec:') && port === 'exec-in') {
            operations.push(...this.connectExecution(pulled.node, pulled.handle.slice(5), id))
        }
        return {id, operations}
    }

    /**
     * Removes nodes and every wire to them, and further execution wires removed at the same time, all of one output's
     * list highest position first, so no removal shifts another.
     *
     * @param ids the nodes
     * @param executions further execution wires to remove, by source, output port and position
     * @returns the operations
     */
    remove(ids: readonly string[], executions: readonly ExecutionWire[] = []): PatchOperation[] {
        const removed = new Set(ids)
        const inputs: PatchOperation[] = []
        const wires = new Map(executions.map(wire => [`${wire.source}|${wire.port}|${wire.position}`, wire]))
        for (const [id, instance] of Object.entries(this.graph.nodes ?? {})) {
            if (removed.has(id)) continue
            for (const [port, edge] of Object.entries(instance.inputs ?? {})) {
                if (removed.has(edge.node)) inputs.push({op: 'remove', path: `${this.node(id)}/inputs/${escape(port)}`})
            }
            for (const [port, targets] of Object.entries(instance.executions ?? {})) {
                targets.forEach((target, position) => {
                    if (removed.has(target)) wires.set(`${id}|${port}|${position}`, {source: id, port, position})
                })
            }
        }
        return [
            ...inputs,
            ...this.disconnectExecutions([...wires.values()].filter(wire => !removed.has(wire.source))),
            ...ids.map(id => ({op: 'remove' as const, path: this.node(id)})),
        ]
    }

    /** Feeds a data input from another node's output, replacing a literal typed into it. */
    connectData(target: string, input: string, source: string, output: string): PatchOperation[] {
        const instance = this.instance(target)
        const operations = this.ensureMap(target, 'inputs')
        if (instance.literals?.[input] !== undefined) operations.push({op: 'remove', path: `${this.node(target)}/literals/${escape(input)}`})
        operations.push({op: 'add', path: `${this.node(target)}/inputs/${escape(input)}`, value: {node: source, port: output}})
        return operations
    }

    /** Chains a node after an execution output. */
    connectExecution(source: string, port: string, target: string): PatchOperation[] {
        const targets = this.instance(source).executions?.[port]
        if (targets?.includes(target)) return []
        if (targets) return [{op: 'add', path: `${this.node(source)}/executions/${escape(port)}/-`, value: target}]
        return [...this.ensureMap(source, 'executions'), {op: 'add', path: `${this.node(source)}/executions/${escape(port)}`, value: [target]}]
    }

    /** Chains another node in place of one after an execution output, keeping its place in the order. */
    redirectExecution(source: string, port: string, from: string, to: string): PatchOperation[] {
        const index = this.instance(source).executions?.[port]?.indexOf(from) ?? -1
        return index < 0 ? [] : [{op: 'replace', path: `${this.node(source)}/executions/${escape(port)}/${index}`, value: to}]
    }

    /**
     * Removes execution wires by their position in their output's list, the highest first in each list, so one
     * removal does not shift the positions of the next.
     *
     * @param wires each wire's source node, output port and position
     * @returns the operations
     */
    disconnectExecutions(wires: readonly ExecutionWire[]): PatchOperation[] {
        return [...wires]
            .sort((a, b) => a.source.localeCompare(b.source) || a.port.localeCompare(b.port) || b.position - a.position)
            .map(wire => ({op: 'remove' as const, path: `${this.node(wire.source)}/executions/${escape(wire.port)}/${wire.position}`}))
    }

    /** Removes the wire feeding a data input. */
    disconnectData(target: string, input: string): PatchOperation[] {
        return [{op: 'remove', path: `${this.node(target)}/inputs/${escape(input)}`}]
    }

    /** Removes a chain from an execution output to a node. */
    disconnectExecution(source: string, port: string, target: string): PatchOperation[] {
        const index = this.instance(source).executions?.[port]?.indexOf(target) ?? -1
        return index < 0 ? [] : [{op: 'remove', path: `${this.node(source)}/executions/${escape(port)}/${index}`}]
    }

    /** Types a value into an input, or clears it with `undefined`. */
    literal(id: string, input: string, value: unknown): PatchOperation[] {
        return this.value(id, 'literals', input, value)
    }

    /** Types several values into inputs at once, e.g. the red, green and blue of a colour. */
    literals(id: string, values: Readonly<Record<string, unknown>>): PatchOperation[] {
        const set = Object.entries(values).map(([input, value]) => ({op: 'add' as const, path: `${this.node(id)}/literals/${escape(input)}`, value}))
        return set.length === 0 ? [] : [...this.ensureMap(id, 'literals'), ...set]
    }

    /** Sets a config field, or clears it with `undefined`. */
    config(id: string, field: string, value: unknown): PatchOperation[] {
        return this.value(id, 'config', field, value)
    }

    private value(id: string, map: 'literals' | 'config', key: string, value: unknown): PatchOperation[] {
        const present = this.instance(id)[map]?.[key] !== undefined
        if (value === undefined) return present ? [{op: 'remove', path: `${this.node(id)}/${map}/${escape(key)}`}] : []
        return [...this.ensureMap(id, map), {op: 'add', path: `${this.node(id)}/${map}/${escape(key)}`, value}]
    }

    private ensureMap(id: string, map: 'inputs' | 'executions' | 'literals' | 'config'): PatchOperation[] {
        return this.instance(id)[map] ? [] : [{op: 'add', path: `${this.node(id)}/${map}`, value: {}}]
    }
}

function escape(segment: string): string {
    return segment.replaceAll('~', '~0').replaceAll('/', '~1')
}

/**
 * @param type a node type, e.g. `entity.set_fire_ticks`
 * @param taken ids already in the graph
 * @returns an id from the type's last segment and the lowest free number, e.g. `set_fire_ticks_3`
 */
export function freshId(type: string, taken: readonly string[]): string {
    const stem = (type.split('.').at(-1) ?? 'node').replaceAll(/[^a-z0-9_]/gi, '_')
    const used = new Set(taken)
    let number = 1
    while (used.has(`${stem}_${number}`)) number++
    return `${stem}_${number}`
}
