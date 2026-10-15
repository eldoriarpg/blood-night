/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {InjectionKey} from 'vue'
import type {Field} from '~/util/settings/schemaFields'

/** What kind of world settings are edited for, which decides the settings that matter. */
export interface WorldKind {
    /** Whether the world's time goes on: night times there, timed blood nights otherwise. */
    dayCycle: boolean
    /** `NORMAL`, `NETHER`, `THE_END` or `CUSTOM`. */
    environment: string
}

/** Hands the edited world's kind down to every form of its settings. */
export const WORLD_KIND: InjectionKey<Ref<WorldKind | undefined>> = Symbol('worldKind')

const DAY_CYCLE_ONLY: ReadonlySet<string> = new Set(['begin', 'end', 'skippable', 'length'])

/**
 * @param field a field of a world's settings
 * @param parent the key of the object it belongs to, e.g. `nightSettings`
 * @param world the world's kind, or none outside world settings
 * @returns whether the field matters in that world: night times only with a day cycle, timed blood nights only
 *     without, the dragon fight only in the End
 */
export function worldShows(field: Field, parent: string | undefined, world: WorldKind | undefined): boolean {
    if (!world) return true
    if (parent === 'nightSettings') {
        if (DAY_CYCLE_ONLY.has(field.key)) return world.dayCycle
        if (field.key === 'timed') return !world.dayCycle
    }
    if (parent === 'timed' && (field.key === 'dragonFight' || field.key === 'dragon')) return world.environment === 'THE_END'
    return true
}

/**
 * @param section a tab of the world settings, e.g. `selection`
 * @param world the world's kind
 * @returns whether the tab matters in that world: the night selection only with a day cycle
 */
export function worldSectionShows(section: string, world: WorldKind | undefined): boolean {
    return !(world && !world.dayCycle && section === 'selection')
}
