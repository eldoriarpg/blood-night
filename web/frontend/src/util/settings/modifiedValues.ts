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

/** The numbers each movement preset reads; the others are hidden while it is picked. */
const PRESET_FIELDS: Readonly<Record<string, readonly string[]>> = {
    VANILLA: [],
    AGGRESSIVE: ['range'],
    KEEP_DISTANCE: ['closest', 'farthest'],
    HIT_AND_RUN: ['seconds'],
    STALKER: ['closest', 'farthest', 'fleeBelow'],
    COWARD: ['fleeBelow'],
    PACK: ['range'],
    GUARD: ['range'],
}

function presetShows(field: Field, fields: readonly Field[], value: Readonly<Record<string, unknown>> | undefined): boolean {
    const preset = fields.find(candidate => candidate.key === 'preset' && candidate.enumName === 'MovementPreset')
    if (!preset || field === preset) return true
    return PRESET_FIELDS[String(value?.preset ?? preset.defaultValue ?? 'VANILLA')]?.includes(field.key) ?? true
}

/** Reads a field of the form, its default when the form has no value for it. */
type Read = (key: string) => unknown

/**
 * A setting that only matters when others have certain values: its fields are hidden otherwise. A rule only applies
 * to a form that has every field it reads, so a key shared by several objects (`seconds`, `range`, `type`) is ruled
 * only where it means the same.
 */
interface ShowRule {
    /** The fields the rule hides; `others` hides every field but the ones it reads and its exceptions. */
    fields: readonly string[] | 'others'
    /** The fields it reads. */
    reads: readonly string[]
    /** Fields `others` leaves alone. */
    except?: readonly string[]
    /** Whether the fields matter. */
    shows: (read: Read) => boolean
}

const blank = (value: unknown): boolean => typeof value !== 'string' || value.trim() === ''

/** The settings that only matter while others have certain values, see each class's documentation in the plugin. */
const SHOW_RULES: readonly ShowRule[] = [
    {fields: 'others', reads: ['enabled'], except: ['lootMultiplier', 'displayName'], shows: read => read('enabled') !== false},
    {fields: ['random'], reads: ['type', 'random'], shows: read => read('type') === 'RANDOM'},
    {fields: ['moonPhase'], reads: ['type', 'moonPhase'], shows: read => read('type') === 'MOON_PHASE' || read('type') === 'REAL_MOON_PHASE'},
    {fields: ['interval'], reads: ['type', 'interval'], shows: read => read('type') === 'INTERVAL'},
    {fields: ['phase'], reads: ['type', 'phase'], shows: read => read('type') === 'PHASE'},
    {fields: ['curve'], reads: ['type', 'curve'], shows: read => read('type') === 'CURVE'},
    {fields: ['seconds'], reads: ['mode', 'maxSeconds'], shows: read => read('mode') === 'EXTENDED' || read('mode') === 'RANGE'},
    {fields: ['maxSeconds'], reads: ['mode'], shows: read => read('mode') === 'RANGE'},
    {fields: ['dragon'], reads: ['dragonFight'], shows: read => read('dragonFight') === true},
    {fields: ['particles'], reads: ['worldParticles'], shows: read => read('worldParticles') === false},
    {fields: ['heartbeatRange', 'heartbeatLoudestWithin'], reads: ['heartbeat'], shows: read => read('heartbeat') !== false},
    {fields: ['lightningChance'], reads: ['lightning'], shows: read => read('lightning') !== false},
    {fields: ['power', 'range', 'minSeconds', 'effects'], reads: ['chance', 'power'], shows: read => Number(read('chance') ?? 0) > 0},
    {fields: ['always'], reads: ['blockDamage'], shows: read => read('blockDamage') !== true},
    {fields: ['name', 'healthModifier', 'health', 'damageModifier', 'damage', 'changes'], reads: ['mythicType'], shows: read => blank(read('mythicType'))},
    {fields: ['mythicLevel'], reads: ['mythicType'], shows: read => !blank(read('mythicType'))},
    {fields: ['entityType', 'equipment'], reads: ['type', 'entityType'], shows: read => read('type') !== 'NONE'},
    {fields: ['method'], reads: ['reach'], shows: read => read('reach') !== 'NONE'},
    {
        fields: ['prefix'],
        reads: ['reach', 'method', 'personalMethod'],
        shows: read => (read('reach') !== 'NONE' && read('method') === 'CHAT') || read('personalMethod') === 'CHAT',
    },
    {fields: ['url', 'required', 'prompt'], reads: ['offer'], shows: read => read('offer') !== false},
    {fields: ['markerKeys'], reads: ['wrap', 'rules'], shows: read => !(read('wrap') === true && read('rules') === true)},
    {fields: ['replaceSame'], reads: ['inherit'], shows: read => read('inherit') !== false},
    {fields: ['minInterval', 'maxInterval'], reads: ['ambience'], shows: read => !Array.isArray(read('ambience')) || (read('ambience') as unknown[]).length > 0},
]

function rulesShow(field: Field, fields: readonly Field[], value: Readonly<Record<string, unknown>> | undefined): boolean {
    const byKey = new Map(fields.map(entry => [entry.key, entry]))
    const read: Read = key => value?.[key] ?? byKey.get(key)?.defaultValue
    return SHOW_RULES.every(rule => {
        if (!rule.reads.every(key => byKey.has(key))) return true
        const ruled = rule.fields === 'others'
            ? !rule.reads.includes(field.key) && !(rule.except ?? []).includes(field.key)
            : rule.fields.includes(field.key)
        return !ruled || rule.shows(read)
    })
}

/**
 * The fields of a form without the ones that do not matter right now: a value under the `DEFAULT` modifier is never
 * used, a movement shows only the numbers its preset reads, and every {@link SHOW_RULES} rule hides settings that only
 * matter while others have certain values.
 *
 * @param fields the form's fields
 * @param value the form's object
 * @returns the fields to show
 */
export function shownFields(fields: readonly Field[], value: Readonly<Record<string, unknown>> | undefined): Field[] {
    const byKey = new Map(fields.map(field => [field.key, field]))
    return fields.filter(field => {
        if (!presetShows(field, fields, value)) return false
        if (!rulesShow(field, fields, value)) return false
        const modifier = byKey.get(modifierOf(field.key))
        if (!modifier || modifier.kind !== 'choice' || field === modifier) return true
        return (value?.[modifier.key] ?? modifier.defaultValue) !== 'DEFAULT'
    })
}
