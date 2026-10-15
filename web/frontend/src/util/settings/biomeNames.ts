/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/** How a biome tag reads: all biomes of a kind, those a structure generates in, or a group by its name. */
export interface BiomeTagName {
    kind: 'all' | 'structure' | 'group'
    name: string
}

/**
 * What a biome tag stands for, in words made from its key, since Minecraft has no names for tags:
 * `#minecraft:is_ocean` is all ocean biomes, `#minecraft:has_structure/village_desert` those desert villages generate
 * in, any other tag a group named by its key.
 *
 * @param tag a biome tag, e.g. `#minecraft:is_nether`
 * @returns its kind and name, or undefined where it is not a tag
 */
export function biomeTagName(tag: string): BiomeTagName | undefined {
    if (!tag.startsWith('#')) return undefined
    const path = tag.slice(1).replace(/^minecraft:/, '')
    if (path.startsWith('is_')) return {kind: 'all', name: words(path.slice('is_'.length))}
    if (path.startsWith('has_structure/')) return {kind: 'structure', name: words(path.slice('has_structure/'.length).split('_').reverse().join('_'))}
    return {kind: 'group', name: words(path)}
}

/**
 * @param key a biome key without a name of Minecraft's, e.g. `terralith:moonlight_grove`
 * @returns words made from it, e.g. `moonlight grove`
 */
export function biomeWords(key: string): string {
    return words(key.replace(/^[^:]*:/, ''))
}

function words(path: string): string {
    return path.split(/[/_.]/).filter(Boolean).join(' ')
}
