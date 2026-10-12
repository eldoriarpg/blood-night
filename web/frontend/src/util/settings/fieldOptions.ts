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
