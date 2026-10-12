/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {TypeRef} from '~/api/generated/schema'

/** How a pin is drawn: the execution pin, or a data pin coloured by the kind of value it carries. */
export type PinKind = 'exec' | 'entity' | 'number' | 'boolean' | 'text' | 'location' | 'item' | 'other'

/** How a node's header is coloured, from what the node does. */
export type NodeTone = 'event' | 'action' | 'value' | 'flow' | 'macro'

/** The Java number types, primitive and boxed. */
export const NUMBERS: ReadonlySet<string> = new Set(['int', 'long', 'short', 'byte', 'float', 'double', 'java.lang.Integer', 'java.lang.Long', 'java.lang.Double', 'java.lang.Float', 'java.lang.Short', 'java.lang.Byte', 'java.lang.Number'])

/**
 * Whether a type is one of Bukkit's entity interfaces, such as `org.bukkit.entity.Creeper`; the enums and nested types
 * of that package, such as `EntityType` or `Pose`, are not.
 */
function isEntityClass(id: string): boolean {
    if (!id.startsWith('org.bukkit.entity.')) return false
    const name = id.slice('org.bukkit.entity.'.length)
    return !name.includes('$') && !name.endsWith('Type') && !['Pose', 'SpawnCategory'].includes(name)
}

/**
 * The pin kind of a port's type, from the Java type it names.
 *
 * @param type the port's type
 * @returns the kind its pin is drawn as
 */
export function pinKind(type: TypeRef | undefined): PinKind {
    if (type?.numeric) return 'number'
    const id = type?.id ?? type?.raw ?? ''
    if (id === 'boolean' || id === 'java.lang.Boolean') return 'boolean'
    if (NUMBERS.has(id)) return 'number'
    if (id === 'java.lang.String' || id.endsWith('.Component')) return 'text'
    if (isEntityClass(id) || /Entity|Player|Mob\b|Creature|Monster/.test(id)) return 'entity'
    if (/Location|Vector|Block\b/.test(id)) return 'location'
    if (/ItemStack|Material/.test(id)) return 'item'
    return 'other'
}

/**
 * The tone of a node, from its catalog kind and type.
 *
 * @param kind the catalog kind: entry, action, control_flow, pure or value
 * @param type the node's type, e.g. `macro.knockback`
 * @returns the tone its header is drawn in
 */
export function nodeTone(kind: string | undefined, type: string): NodeTone {
    if (type.startsWith('macro.') || type.startsWith('routine.')) return 'macro'
    switch (kind) {
        case 'entry': return 'event'
        case 'action': return 'action'
        case 'control_flow': return 'flow'
        default: return 'value'
    }
}

/**
 * @param kind the catalog kind
 * @returns whether nodes of the kind are chained, so they have an execution input
 */
export function takesExecution(kind: string | undefined): boolean {
    return kind === 'action' || kind === 'control_flow'
}
