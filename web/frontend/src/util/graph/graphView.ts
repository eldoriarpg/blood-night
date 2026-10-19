/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog, NodeType, PortInfo, TypeInfo, TypeRef} from '~/api/generated/schema'
import type {Graph, Macro, NodeInstance} from '~/api/generated/payload'
import {layout, type Position} from '~/util/graph/graphLayout'
import {nodeTone, pinKind, takesExecution, type NodeTone, type PinKind} from '~/util/graph/pins'
import {choiceName, isTypeField, pickedTitle, pickedTypes, resolvePicked, type PickedTypes} from '~/util/graph/pickedTypes'
import {changingSibling} from '~/util/graph/mutate'
import {boundType, type GraphBindings} from '~/util/graph/graphBindings'

/** A data port of a drawn node. */
export interface PortView {
    id: string
    name: string
    /** What the port carries, from the API's documentation; empty when there is none. */
    description: string
    /** The editor name of the port's type, e.g. `Location`; empty when unknown. */
    typeName: string
    /** Whether the node works without this input, e.g. one property of a Set node. */
    optional: boolean
    pin: PinKind
    connected: boolean
    literal: unknown
    literalAllowed: boolean
    options: string[]
    /** The catalog id of the port's type, which names its choices, e.g. `org.bukkit.entity.EntityType`. */
    typeId: string
    /** How a value is typed in when not by its shape, e.g. `color`. */
    format: string | undefined
    /** The part of a large node the port is drawn in, e.g. the type declaring a property. */
    group: string | undefined
}

/** A value a text field offers, e.g. an existing variable, with what it holds, e.g. `Location`. */
export interface Suggestion {
    value: string
    hint: string
}

/** A config field of a drawn node, edited on the node. */
export interface ConfigView {
    id: string
    name: string
    pin: PinKind
    options: string[]
    /** Names of the options where the catalog gives them, e.g. the types a type field picks from. */
    labels: Record<string, string>
    /** The catalog id of the field's type, which names its choices. */
    typeId: string
    format: string | undefined
    /** The type variable the field picks, for a type field. */
    selects: string | undefined
    /** The input or execution pin the field is drawn beside, e.g. the case of a switch whose value it holds. */
    beside: string | undefined
    /** Values the same field holds on related nodes of the graph, e.g. the variable names already in use. */
    suggestions: Suggestion[]
    value: unknown
}

/** An execution output of a drawn node. */
export interface ExecutionView {
    id: string
    name: string
    connected: boolean
}

/** A node as the canvas draws it. */
export interface NodeView {
    id: string
    type: string
    title: string
    tone: NodeTone
    /** What the node does, from the catalog; empty when unknown. */
    description: string
    known: boolean
    /** Whether the node starts a chain, such as an event, so its run can be played. */
    entry: boolean
    /** Whether the node only reroutes a wire, drawn as a dot. */
    reroute: boolean
    /** Whether the node takes a colour as red, green and blue, which a colour picker can fill at once. */
    channels: boolean
    config: ConfigView[]
    execIn: boolean
    inputs: PortView[]
    outputs: PortView[]
    executions: ExecutionView[]
    position: Position
}

/** Choices of config fields that come from the session rather than the catalog, by `<node type>|<field>`. */
export type SessionChoices = Readonly<Record<string, {options: readonly string[], labels: Readonly<Record<string, string>>}>>

/** A wire between two pins. */
export interface EdgeView {
    id: string
    source: string
    sourceHandle: string
    target: string
    targetHandle: string
    pin: PinKind
    /** For an execution wire: its position in the list of nodes its output runs. */
    position?: number
}

/** Handle ids of the pins a node draws, shared with the node component. */
export const handles = {
    execIn: 'exec-in',
    execOut: (port: string) => `exec:${port}`,
    input: (port: string) => `in:${port}`,
    output: (port: string) => `out:${port}`,
}

/**
 * The nodes and wires of a graph as the canvas draws them. Node types the catalog does not know (macro and routine
 * uses, nodes of a missing extension) are drawn with the ports their instance names.
 *
 * @param graph the graph
 * @param catalog the catalog of the server the session came from
 * @param macros the session's macros, for the names of macro uses
 * @param choices choices of config fields the catalog cannot know, by node type and field, e.g. the session's
 *     particle effects for `world.play_effect|effect`
 * @returns the nodes and wires
 */
export function graphView(
    graph: Graph, catalog: Catalog, macros: readonly Macro[], bindings: GraphBindings = {}, choices: SessionChoices = {},
): {nodes: NodeView[], edges: EdgeView[]} {
    const types = new Map((catalog.nodes ?? []).map(node => [node.id ?? '', node]))
    const values = new Map((catalog.types ?? []).map((type: TypeInfo) => [type.id ?? '', type.values ?? []]))
    const typeNames = new Map((catalog.types ?? []).map((type: TypeInfo) => [type.id ?? '', type.name ?? type.id ?? '']))
    const formats = new Map((catalog.types ?? []).map((type: TypeInfo) => [type.id ?? '', type.format]))
    const positions = layout(graph)
    const connectedOutputs = outputsInUse(graph)
    const names = namesInUse(graph, catalog)
    const nodes = Object.entries(graph.nodes ?? {}).map(([id, instance]) => nodeView(id, instance, {
        catalog,
        bindings,
        definition: types.get(instance.type ?? ''),
        macros,
        values,
        typeNames,
        formats,
        names,
        choices,
        rerouted: reroutedType(graph, catalog, bindings, id),
        connected: connectedOutputs.get(id) ?? new Set(),
        position: positions.get(id) ?? {x: 0, y: 0},
    }))
    const pinOf = outputPins(nodes)
    return {nodes, edges: edges(graph, pinOf)}
}

interface NodeContext {
    catalog: Catalog
    bindings: GraphBindings
    definition: NodeType | undefined
    macros: readonly Macro[]
    values: Map<string, string[]>
    typeNames: Map<string, string>
    formats: Map<string, string | undefined>
    /** Text config values in use, by node family and field; see {@link namesInUse}. */
    names: Map<string, Suggestion[]>
    choices: SessionChoices
    /** The type a reroute passes on, from what is wired into it. */
    rerouted: TypeRef | undefined
    connected: Set<string>
    position: Position
}

function nodeView(id: string, instance: NodeInstance, context: NodeContext): NodeView {
    return asBooleanSelect(plainNodeView(id, instance, context), pickedTypes(context.definition, instance, context.catalog))
}

function plainNodeView(id: string, instance: NodeInstance, context: NodeContext): NodeView {
    const type = instance.type ?? ''
    const definition = context.definition
    const picked = pickedTypes(definition, instance, context.catalog)
    const inputs = (changingSibling(context.catalog, type)?.inputs ?? definition?.inputs ?? Object.keys({...instance.inputs, ...instance.literals}).map(port => ({id: port, name: port, literal: true})))
        .map(port => 'type' in port ? {...port, type: resolvePicked(boundType(port.type, context.catalog, context.bindings), picked)} : port)
    const outputs = (definition?.outputs ?? []).map(port => ({...port, type: context.rerouted ?? resolvePicked(boundType(port.type, context.catalog, context.bindings), picked)}))
    const executions = definition?.executions ?? Object.keys(instance.executions ?? {}).map(port => ({id: port, name: port}))
    return {
        id,
        type,
        title: familyTitle(definition, context.catalog)
            ?? boundTitle(definition, outputs, context.typeNames)
            ?? pickedTitle(definition, picked, context.catalog)
            ?? title(type, definition, context.macros),
        tone: nodeTone(definition?.kind, type),
        description: definition?.description ?? '',
        known: definition !== undefined,
        entry: definition?.kind === 'entry',
        reroute: isReroute(type),
        channels: takesChannels(definition, context.formats),
        config: configViews(definition, instance, picked, context),
        execIn: definition ? takesExecution(definition.kind) : !type.startsWith('value.'),
        inputs: inputs.map(port => ({
            id: port.id ?? '',
            name: port.name ?? port.id ?? '',
            description: ('description' in port ? port.description : undefined) ?? '',
            typeName: typeName('type' in port ? port.type : undefined, context.typeNames),
            optional: ('optional' in port ? port.optional : false) ?? false,
            pin: pinKind('type' in port ? port.type : undefined),
            connected: instance.inputs?.[port.id ?? ''] !== undefined,
            literal: instance.literals?.[port.id ?? ''],
            literalAllowed: 'type' in port ? port.literal === true : true,
            options: context.values.get(('type' in port ? port.type?.id : undefined) ?? '') ?? [],
            typeId: ('type' in port ? port.type?.id : undefined) ?? '',
            format: context.formats.get(('type' in port ? port.type?.id : undefined) ?? ''),
            group: 'group' in port ? port.group : undefined,
        })),
        outputs: outputs.map(port => ({id: port.id ?? '', name: port.name ?? port.id ?? '', description: port.description ?? '', typeName: typeName(port.type, context.typeNames), optional: false, pin: pinKind(port.type), connected: context.connected.has(port.id ?? ''), literal: undefined, literalAllowed: false, options: [], typeId: port.type?.id ?? '', format: undefined, group: port.group})),
        executions: executions.map(port => ({id: port.id ?? '', name: port.name ?? '', connected: (instance.executions?.[port.id ?? ''] ?? []).length > 0})),
        position: context.position,
    }
}

/** Ports drawn under one group header, e.g. the properties a type declares. */
export interface PortGroup {
    /** The group's name; undefined when the node's ports are not grouped. */
    name: string | undefined
    ports: PortView[]
}

/**
 * @param node a drawn node
 * @returns whether its ports fall into at least two groups, so they are drawn under group headers
 */
export function isGrouped(node: NodeView): boolean {
    return new Set([...node.inputs, ...node.outputs].map(port => port.group).filter(group => group !== undefined)).size >= 2
}

/**
 * Splits ports into runs of the same group, in their order; ports of a node that is not grouped form one unnamed run.
 *
 * @param ports the ports to draw, ordered by group as the catalog lists them
 * @param grouped whether the node draws group headers, see {@link isGrouped}
 * @returns the runs
 */
export function portGroups(ports: readonly PortView[], grouped: boolean): PortGroup[] {
    if (!grouped) return [{name: undefined, ports: [...ports]}]
    const groups: PortGroup[] = []
    for (const port of ports) {
        const last = groups.at(-1)
        if (last && last.name === port.group) last.ports.push(port)
        else groups.push({name: port.group, ports: [port]})
    }
    return groups
}

/** Node types that only reroute a wire. */
export const REROUTES: ReadonlySet<string> = new Set(['reroute', 'reroute.execution'])

function isReroute(type: string): boolean {
    return REROUTES.has(type)
}

const CHANNELS = ['red', 'green', 'blue']

function takesChannels(definition: NodeType | undefined, formats: Map<string, string | undefined>): boolean {
    const inputs = new Set((definition?.inputs ?? []).filter(port => port.literal).map(port => port.id))
    const colour = (definition?.outputs ?? []).some(port => formats.get(port.type?.id ?? '') === 'color')
    return colour && CHANNELS.every(channel => inputs.has(channel))
}

function configViews(definition: NodeType | undefined, instance: NodeInstance, picked: PickedTypes, context: NodeContext): ConfigView[] {
    return (definition?.config ?? []).map(field => {
        const type = resolvePicked(field.type, picked)
        const typeField = isTypeField(field)
        const session = context.choices[`${instance.type ?? ''}|${field.id ?? ''}`]
        const options = session ? [...session.options] : (typeField ? field.choices ?? [] : context.values.get(type?.id ?? '') ?? [])
        return {
            id: field.id ?? '',
            name: field.name ?? field.id ?? '',
            pin: typeField ? 'other' : pinKind(type),
            options,
            labels: session ? {...session.labels} : (typeField ? Object.fromEntries(options.map(choice => [choice, choiceName(choice, context.catalog)])) : {}),
            typeId: typeField ? '' : type?.id ?? '',
            format: typeField ? undefined : context.formats.get(type?.id ?? ''),
            selects: field.selects,
            beside: field.beside,
            suggestions: typeField ? [] : context.names.get(nameKey(instance.type ?? '', field.id ?? '')) ?? [],
            value: instance.config?.[field.id ?? ''] ?? field.defaultValue,
        }
    })
}

function nameKey(type: string, field: string): string {
    return type === ROUTINE_DEFINE && field === 'name' ? ROUTINE_NAMES : `${type.slice(0, type.lastIndexOf('.') + 1)}|${field}`
}

const ROUTINE_DEFINE = 'routine.define'

/** Where routine names are suggested: a routine's own name and the routine a parameter belongs to share them. */
const ROUTINE_NAMES = 'routine.|routine'

/**
 * The text config values of a graph, by node family (the type up to its last dot, e.g. `variable.`) and field, so a
 * name field suggests the variable, gate or timer names its related nodes already use, each with the type it is
 * held as where the node picks one.
 */
function namesInUse(graph: Graph, catalog: Catalog): Map<string, Suggestion[]> {
    const names = new Map<string, Map<string, string>>()
    for (const instance of Object.values(graph.nodes ?? {})) {
        const definition = catalog.nodes?.find(node => node.id === instance.type)
        const typeField = definition?.config?.find(isTypeField)
        const choice = instance.config?.[typeField?.id ?? ''] ?? typeField?.defaultValue
        const hint = typeof choice === 'string' ? choiceName(choice, catalog) : ''
        for (const [field, value] of Object.entries(instance.config ?? {})) {
            if (typeof value !== 'string' || value === '' || field === typeField?.id) continue
            const key = nameKey(instance.type ?? '', field)
            const found = names.get(key) ?? new Map<string, string>()
            if (!found.has(value)) found.set(value, hint)
            names.set(key, found)
        }
    }
    return new Map([...names].map(([key, values]) => [key, [...values]
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([value, hint]) => ({value, hint}))]))
}

const SELECT = 'select'
const SELECT_BOOLEAN_OPTIONS = ['option_0', 'option_1']

/**
 * A select keyed by a boolean needs no keys: its first option is given for true, its second for false, so it shows
 * just those two, named so, and none of its key fields.
 */
function asBooleanSelect(view: NodeView, picked: PickedTypes): NodeView {
    if (view.type !== SELECT || picked.get('K')?.choice !== 'boolean') return view
    return {
        ...view,
        inputs: view.inputs
            .filter(port => port.id === 'key' || SELECT_BOOLEAN_OPTIONS.includes(port.id))
            .map(port => port.id === 'option_0' ? {...port, name: 'True'} : port.id === 'option_1' ? {...port, name: 'False'} : port),
        config: view.config.filter(field => field.beside === undefined),
    }
}

/**
 * The type a data reroute passes on: the type of the output wired into it, followed through further reroutes.
 */
function reroutedType(graph: Graph, catalog: Catalog, bindings: GraphBindings, id: string, depth = 0): TypeRef | undefined {
    const instance = graph.nodes?.[id]
    if (instance?.type !== 'reroute' || depth > 32) return undefined
    const source = instance.inputs?.value
    if (!source) return undefined
    const upstream = graph.nodes?.[source.node]
    if (upstream?.type === 'reroute') return reroutedType(graph, catalog, bindings, source.node, depth + 1)
    const definition = catalog.nodes?.find(node => node.id === upstream?.type)
    const port = definition?.outputs?.find(output => output.id === source.port)
    return resolvePicked(boundType(port?.type, catalog, bindings), pickedTypes(definition, upstream, catalog))
}

/**
 * The title of a node giving a graph-wide value, with the type it has in this graph, e.g. `Self: Creeper`.
 */
function boundTitle(definition: NodeType | undefined, outputs: readonly PortInfo[], names: Map<string, string>): string | undefined {
    const variable = definition?.outputs?.length === 1 ? definition.outputs[0]?.type?.variable : undefined
    const id = outputs[0]?.type?.id
    if (!definition?.name || !variable || !id || (definition.inputs?.length ?? 0) > 0) return undefined
    return `${definition.name}: ${names.get(id) ?? id.split('.').at(-1)}`
}

/**
 * A variant's title: its family's name and the type it works on, e.g. `Mutate: Creeper`.
 */
function familyTitle(definition: NodeType | undefined, catalog: Catalog): string | undefined {
    if (!definition?.family) return undefined
    const family = (catalog.nodes ?? []).find(node => node.id === definition.family)?.name ?? definition.family
    const target = definition.inputs?.find(port => port.id === 'target')?.type?.id ?? ''
    const name = (catalog.types ?? []).find(type => type.id === target)?.name ?? target.split('.').at(-1)
    return `${family}: ${name}`
}

function typeName(type: TypeRef | undefined, names: Map<string, string>): string {
    if (!type) return ''
    if (type.numeric) return 'Number'
    if (type.variable) return type.variable
    return names.get(type.id ?? '') ?? (type.raw ?? type.id ?? '').split('.').at(-1) ?? ''
}

function title(type: string, definition: NodeType | undefined, macros: readonly Macro[]): string {
    if (definition?.name) return definition.name
    if (type.startsWith('macro.')) return macros.find(macro => `macro.${macro.id}` === type)?.name ?? type.slice(6)
    if (type.startsWith('routine.')) return type.slice(8)
    return type
}

function outputsInUse(graph: Graph): Map<string, Set<string>> {
    const used = new Map<string, Set<string>>()
    for (const instance of Object.values(graph.nodes ?? {})) {
        for (const edge of Object.values(instance.inputs ?? {})) {
            used.set(edge.node, (used.get(edge.node) ?? new Set()).add(edge.port))
        }
    }
    return used
}

function outputPins(nodes: NodeView[]): Map<string, PinKind> {
    const pins = new Map<string, PinKind>()
    for (const node of nodes) {
        for (const port of node.outputs) pins.set(`${node.id}:${port.id}`, port.pin)
    }
    return pins
}

function edges(graph: Graph, pinOf: Map<string, PinKind>): EdgeView[] {
    const result: EdgeView[] = []
    for (const [id, instance] of Object.entries(graph.nodes ?? {})) {
        for (const [port, edge] of Object.entries(instance.inputs ?? {})) {
            result.push({
                id: `data:${id}:${port}`,
                source: edge.node,
                sourceHandle: handles.output(edge.port),
                target: id,
                targetHandle: handles.input(port),
                pin: pinOf.get(`${edge.node}:${edge.port}`) ?? 'other',
            })
        }
        for (const [port, targets] of Object.entries(instance.executions ?? {})) {
            targets.forEach((target, index) => result.push({
                id: `exec:${id}:${port}:${index}`,
                source: id,
                sourceHandle: handles.execOut(port),
                target,
                targetHandle: handles.execIn,
                pin: 'exec',
                position: index,
            }))
        }
    }
    return result
}
