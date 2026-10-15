/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Field} from '~/util/settings/schemaFields'

/**
 * The fields a settings form draws its own way rather than by their kind alone, because typing their value would ask
 * for knowledge an everyday user does not have:
 * <ul>
 *     <li>`percent`: a chance, as a slider from 0 to 100 %</li>
 *     <li>`dayTime`: a time of the Minecraft day in ticks after sunrise, as a clock time</li>
 *     <li>`unchanged`: a number where -1 leaves the game's own value, as a switch and a number</li>
 *     <li>`unchangedSeconds`: the same, stored in ticks and shown in seconds</li>
 *     <li>`effect`: a potion effect, picked by name</li>
 *     <li>`mob`: a mob of the session, picked by name</li>
 *     <li>`mobMap`: values by mob, the mobs picked by name</li>
 *     <li>`worldMobs`: which mobs spawn in a world, and their weight there</li>
 *     <li>`dropWeights`: weights by number of drops</li>
 *     <li>`gameRules`: game rule values by rule, the rules picked by name</li>
 *     <li>`miniMessage`: a MiniMessage text, with a preview</li>
 *     <li>`moonPhases`: a chance for every phase of the moon</li>
 *     <li>`phaseCycle`: a cycle of nights with a chance each, as an equalizer</li>
 *     <li>`curve`: the curve night selection with a graph of its chances</li>
 * </ul>
 */
export type SpecialField = 'percent' | 'dayTime' | 'unchanged' | 'unchangedSeconds' | 'effect' | 'mob' | 'mobMap' | 'worldMobs' | 'dropWeights' | 'gameRules' | 'miniMessage' | 'moonPhases' | 'phaseCycle' | 'curve'

const PERCENT: ReadonlySet<string> = new Set(['chance', 'chancePerMinute', 'lightningChance', 'thunderChance', 'loseExperienceChance', 'loseInventoryChance', 'probability', 'spawnPercentage'])
const MINI_MESSAGE: ReadonlySet<string> = new Set(['title', 'prompt', 'prefix', 'displayName'])

/**
 * @param field a field of a settings form
 * @param parent the key of the object the field belongs to, e.g. `nightSettings`
 * @returns how the field is drawn, if not by its kind alone
 */
export function specialField(field: Field, parent: string | undefined): SpecialField | undefined {
    switch (field.kind) {
        case 'integer':
            if (PERCENT.has(field.key)) return 'percent'
            if (parent === 'nightSettings' && (field.key === 'begin' || field.key === 'end')) return 'dayTime'
            if (field.key === 'monsterSpawnLimit') return 'unchanged'
            if (field.key === 'monsterTicksPerSpawn') return 'unchangedSeconds'
            return undefined
        case 'text':
            if (field.key === 'effect') return 'effect'
            if (parent === 'timed' && field.key === 'dragon') return 'mob'
            if (MINI_MESSAGE.has(field.key)) return 'miniMessage'
            return undefined
        case 'map':
            if (parent === 'moonPhase' && field.key === 'probabilities') return 'moonPhases'
            if (field.key === 'weights') return 'dropWeights'
            if (field.key === 'gameRules') return 'gameRules'
            if (parent === 'spawning' && field.key === 'mobs') return 'worldMobs'
            if (field.key === 'overrides') return 'mobMap'
            return undefined
        case 'group':
            return parent === 'nightSelection' && field.key === 'curve' ? 'curve' : undefined
        case 'any':
            return parent === 'phase' && field.key === 'probabilities' ? 'phaseCycle' : undefined
        default:
            return undefined
    }
}

/**
 * @param path a field's JSON pointer, e.g. `/settings/worlds/w1/settings/nightSettings/begin`
 * @returns the key of the object the field belongs to, e.g. `nightSettings`
 */
export function parentKey(path: string): string | undefined {
    const parts = path.split('/')
    return parts.length >= 2 ? parts.at(-2) : undefined
}
