/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog, TypeRef} from '~/api/generated/schema'
import type {EditorPayload} from '~/api/generated/payload'

/** Graph-wide type variables and the type id each has in the graph being edited, e.g. `SELF` to the Creeper type. */
export type GraphBindings = Readonly<Record<string, string>>

/**
 * A port type with a graph-wide variable replaced by the type it has in this graph: the graph's own binding, else the
 * catalog's fallback.
 *
 * @param type a port type
 * @param catalog the catalog with the fallbacks
 * @param bindings the graph's bindings
 * @returns the resolved type, or the type itself when it names no graph-wide variable
 */
export function boundType(type: TypeRef | undefined, catalog: Catalog, bindings: GraphBindings): TypeRef | undefined {
    const variable = type?.variable
    if (!variable) return type
    const id = bindings[variable] ?? catalog.bindings?.[variable]
    return id ? {id, arguments: []} : type
}

/**
 * The bindings of a mob's graph: Self as the mob's entity type, Extension as its extension's, as the server compiles
 * it; macros have none and keep the fallbacks.
 *
 * @param payload the payload with the server's entity classes
 * @param mob the mob's id
 * @returns the bindings
 */
export function mobBindings(payload: EditorPayload | null | undefined, mob: string): GraphBindings {
    const classes = payload?.choices?.entityClasses ?? {}
    const definition = payload?.mobs?.[mob]
    const bindings: Record<string, string> = {}
    const self = classes[definition?.attributes?.entityType ?? '']
    if (self) bindings.SELF = self
    const extension = classes[definition?.extension?.entityType ?? '']
    if (extension) bindings.EXTENSION = extension
    return bindings
}
