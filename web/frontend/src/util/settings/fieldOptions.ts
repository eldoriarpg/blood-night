/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Field} from '~/util/settings/schemaFields'

/**
 * The choices a settings field offers. Entity type fields offer only the types the server lets a mob be (no
 * projectiles, vehicles or armor stands), plus the value already set, so a stored value never silently disappears.
 *
 * @param field the field
 * @param mobTypes the entity types a mob can be, from the server; empty when unknown
 * @param current the field's value
 * @returns the choices, in the schema's order
 */
export function fieldOptions(field: Field, mobTypes: readonly string[], current: unknown): string[] {
    if (field.enumName !== 'EntityType' || mobTypes.length === 0) return field.options
    const allowed = new Set(mobTypes)
    return field.options.filter(option => allowed.has(option) || option === current)
}

/**
 * The spawn reasons a mob's spawn rules offer: only those its entity type can spawn by, e.g. a slime splitting or an
 * animal being bred, plus those already chosen, so a stored reason never silently disappears.
 *
 * @param options every spawn reason
 * @param fitting the reasons the mob's type can spawn by, from the server; none when unknown
 * @param chosen the reasons chosen
 * @returns the reasons to offer, in the schema's order
 */
export function spawnReasonOptions(options: readonly string[], fitting: readonly string[] | undefined, chosen: unknown): string[] {
    if (!fitting) return [...options]
    const kept = new Set([...fitting, ...(Array.isArray(chosen) ? chosen : [])])
    return options.filter(option => kept.has(option))
}
