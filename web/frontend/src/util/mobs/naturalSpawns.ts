/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

const NORMAL: ReadonlySet<string> = new Set([
    'ZOMBIE', 'HUSK', 'DROWNED', 'ZOMBIE_VILLAGER', 'SKELETON', 'STRAY', 'BOGGED', 'CREEPER', 'SPIDER', 'CAVE_SPIDER',
    'ENDERMAN', 'WITCH', 'SLIME', 'PHANTOM', 'GUARDIAN', 'ELDER_GUARDIAN', 'PILLAGER', 'VINDICATOR', 'EVOKER',
    'RAVAGER', 'VEX', 'SILVERFISH', 'WARDEN', 'BREEZE', 'CREAKING',
])
const NETHER: ReadonlySet<string> = new Set([
    'ZOMBIFIED_PIGLIN', 'PIGLIN', 'PIGLIN_BRUTE', 'HOGLIN', 'GHAST', 'MAGMA_CUBE', 'BLAZE', 'WITHER_SKELETON',
    'SKELETON', 'ENDERMAN', 'STRIDER',
])
const THE_END: ReadonlySet<string> = new Set(['ENDERMAN', 'SHULKER', 'ENDER_DRAGON'])
/**
 * The hostile mobs a world of each kind spawns by itself, from its biomes, structures and spawners, by entity type.
 * Mobs only brought in by players, e.g. through portals, are left out.
 */
const NATURAL: Readonly<Record<string, ReadonlySet<string>>> = {NORMAL, NETHER, THE_END}

/** The entity types a mob of a type also takes the place of unless it is limited to its exact type. */
const SUBTYPES: Readonly<Record<string, readonly string[]>> = {
    ZOMBIE: ['HUSK', 'DROWNED', 'ZOMBIE_VILLAGER', 'ZOMBIFIED_PIGLIN'],
    SPIDER: ['CAVE_SPIDER'],
    SLIME: ['MAGMA_CUBE'],
    GUARDIAN: ['ELDER_GUARDIAN'],
}

/**
 * Whether a mob can take the place of anything spawning by itself in a world of a kind: its type or, unless it is
 * limited to its exact type, one of its subtypes spawns there. Worlds of an unknown kind are given the benefit of the
 * doubt.
 *
 * @param entityType the mob's entity type, e.g. `ZOMBIE`
 * @param exactType whether the mob is limited to exactly that type
 * @param environment the world's kind: `NORMAL`, `NETHER`, `THE_END` or `CUSTOM`
 */
export function spawnsNaturally(entityType: string, exactType: boolean, environment: string): boolean {
    const natural = NATURAL[environment]
    if (!natural) return true
    const fitting = exactType ? [entityType] : [entityType, ...SUBTYPES[entityType] ?? []]
    return fitting.some(type => natural.has(type))
}
