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
 * Fields whose key other objects use for something else, with a field only the right object has: `begin` and `end` of
 * the night settings, not the sounds that play when a night begins and ends.
 */
const ONLY_BESIDE: Readonly<Record<string, string>> = {
    begin: 'skippable',
    end: 'skippable',
    length: 'skippable',
    timed: 'skippable',
}

/**
 * The translation key of a field's help text, the line under it saying what it does where that is not obvious, or
 * where it depends on another setting. A field whose meaning depends on another field of the form gets the text for
 * that field's current value (`settings.help.xpValue.MULTIPLY`); any other field its own (`settings.help.lootMultiplier`).
 * Keys shared by objects with different meanings only have texts per value, so they never get a wrong one.
 *
 * @param field the field
 * @param fields the form's fields
 * @param value the form's object
 * @returns the key to look up, which may have no translation; none where the key means something else
 */
export function fieldHelpKey(field: Field, fields: readonly Field[], value: Readonly<Record<string, unknown>> | undefined): string | undefined {
    const beside = ONLY_BESIDE[field.key]
    if (beside && !fields.some(candidate => candidate.key === beside)) return undefined
    const controller = MEANING_DEPENDS_ON[field.key]
    const source = controller ? fields.find(candidate => candidate.key === controller) : undefined
    if (!source) return `settings.help.${field.key}`
    return `settings.help.${field.key}.${String(value?.[controller ?? ''] ?? source.defaultValue)}`
}
