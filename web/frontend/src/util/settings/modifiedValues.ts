/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Field} from '~/util/settings/schemaFields'

/**
 * The modifier field that decides whether a value field means anything: `health` by `healthModifier`, `xpValue` by
 * `xpModifier`.
 *
 * @param key a field's key
 * @returns the key of its modifier
 */
function modifierOf(key: string): string {
    return `${key.replace(/Value$/, '')}Modifier`
}

/**
 * The fields of a form without the values their modifier ignores: a value under the `DEFAULT` modifier is never used,
 * so showing it would mislead.
 *
 * @param fields the form's fields
 * @param value the form's object
 * @returns the fields to show
 */
export function shownFields(fields: readonly Field[], value: Readonly<Record<string, unknown>> | undefined): Field[] {
    const byKey = new Map(fields.map(field => [field.key, field]))
    return fields.filter(field => {
        const modifier = byKey.get(modifierOf(field.key))
        if (!modifier || modifier.kind !== 'choice' || field === modifier) return true
        return (value?.[modifier.key] ?? modifier.defaultValue) !== 'DEFAULT'
    })
}
