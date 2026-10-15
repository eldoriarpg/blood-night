/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Field} from '~/util/settings/schemaFields'

/**
 * Fields whose value means something else depending on another field of the same form: `xpValue` is a factor under
 * `MULTIPLY` and the exact amount under `VALUE`. Their help text is looked up per value of that field.
 */
const MEANING_DEPENDS_ON: Readonly<Record<string, string>> = {
    xpValue: 'xpModifier',
    health: 'healthModifier',
    damage: 'damageModifier',
    seconds: 'mode',
    fleeBelow: 'preset',
    range: 'preset',
    moonPhase: 'type',
}

/**
 * The translation key of a field's help text, the line under it saying what it does where that is not obvious, or
 * where it depends on another setting. A field whose meaning depends on another field of the form gets the text for
 * that field's current value (`settings.help.xpValue.MULTIPLY`); any other field its own (`settings.help.lootMultiplier`).
 *
 * @param field the field
 * @param fields the form's fields
 * @param value the form's object
 * @returns the key to look up, which may have no translation
 */
export function fieldHelpKey(field: Field, fields: readonly Field[], value: Readonly<Record<string, unknown>> | undefined): string {
    const controller = MEANING_DEPENDS_ON[field.key]
    const source = controller ? fields.find(candidate => candidate.key === controller) : undefined
    if (!source) return `settings.help.${field.key}`
    return `settings.help.${field.key}.${String(value?.[controller ?? ''] ?? source.defaultValue)}`
}
