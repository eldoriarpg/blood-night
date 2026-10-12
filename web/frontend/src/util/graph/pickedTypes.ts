/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog, ConfigInfo, NodeType, TypeRef} from '~/api/generated/schema'
import type {NodeInstance} from '~/api/generated/payload'

/** The type a node's type field picks, by the variable it binds. */
export type PickedTypes = ReadonlyMap<string, {choice: string, type: TypeRef}>

/**
 * @param field a config field of a node type
 * @returns whether it picks the type of a variable, such as the type of a cast or a value
 */
export function isTypeField(field: ConfigInfo): boolean {
    return field.selects !== undefined && (field.choices?.length ?? 0) > 0
}

/**
 * The types a node's type fields pick, as the compiler binds them: the stored choice, or the field's default.
 *
 * @param definition the node's type
 * @param instance the node
 * @param catalog the catalog with the selectable types
 * @returns the picked types by variable
 */
export function pickedTypes(definition: NodeType | undefined, instance: NodeInstance | undefined, catalog: Catalog): PickedTypes {
    const picked = new Map<string, {choice: string, type: TypeRef}>()
    for (const field of (definition?.config ?? []).filter(isTypeField)) {
        const stored = instance?.config?.[field.id ?? '']
        const choice = typeof stored === 'string' ? stored : String(field.defaultValue ?? field.choices?.[0] ?? '')
        const id = catalog.selectable?.[choice]
        if (id && field.selects) picked.set(field.selects, {choice, type: {id, arguments: []}})
    }
    return picked
}

/**
 * @param type a port or config type
 * @param picked the node's picked types
 * @returns the type with a picked variable replaced by the type picked for it
 */
export function resolvePicked(type: TypeRef | undefined, picked: PickedTypes): TypeRef | undefined {
    return type?.variable ? picked.get(type.variable)?.type ?? type : type
}

/**
 * @param choice a selectable type id, e.g. `living_entity`
 * @param catalog the catalog
 * @returns its editor name, e.g. `Living Entity`
 */
export function choiceName(choice: string, catalog: Catalog): string {
    const id = catalog.selectable?.[choice]
    return (catalog.types ?? []).find(type => type.id === id)?.name ?? choice
}

/**
 * @param definition a node type
 * @param picked its picked types
 * @param catalog the catalog
 * @returns the node's title with the picked type, e.g. `As: Mob`
 */
export function pickedTitle(definition: NodeType | undefined, picked: PickedTypes, catalog: Catalog): string | undefined {
    const first = [...picked.values()][0]
    return first && definition?.name ? `${definition.name}: ${choiceName(first.choice, catalog)}` : undefined
}
