/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {SoundAsset} from '~/api/generated/schema'
import {translated} from '~/util/items/itemChoices'

/** A sound as the sound picker lists it. */
export interface SoundChoice {
    /** The key the settings store, e.g. `minecraft:entity.zombie.ambient`. */
    key: string
    /** What it sounds like, in the editor's language, e.g. `Zombie groans`. */
    name: string
    /** What kind of sound it is, e.g. `entity` or `ambient`. */
    group: string
    playable: boolean
}

/** The groups in the order the picker offers them, the ones a blood night needs most first. */
export const SOUND_GROUPS = ['ambient', 'entity', 'weather', 'event', 'block', 'item', 'music', 'music_disc', 'enchant', 'particle', 'ui'] as const

/**
 * @param key a sound's key with or without namespace, e.g. `minecraft:entity.zombie.ambient`
 * @returns the key without the `minecraft:` namespace, as Minecraft's sound list names it
 */
export function bareSoundKey(key: string): string {
    return key.startsWith('minecraft:') ? key.slice('minecraft:'.length) : key
}

/**
 * @param key a sound's key, e.g. `ambient.basalt_deltas.additions`
 * @returns a readable name for a sound without a subtitle, e.g. `Basalt deltas additions`
 */
export function keyName(key: string): string {
    const parts = bareSoundKey(key).split('.')
    const words = (parts.length > 1 ? parts.slice(1) : parts).join(' ').replaceAll('_', ' ')
    return words.charAt(0).toUpperCase() + words.slice(1)
}

/**
 * @param sounds the sounds of a Minecraft version by their key
 * @param locale the editor language, e.g. `de`
 * @returns every sound by its name in that language, sorted by group, then name
 */
export function soundChoices(sounds: Readonly<Record<string, SoundAsset>>, locale: string): SoundChoice[] {
    const order = (group: string) => {
        const index = (SOUND_GROUPS as readonly string[]).indexOf(group)
        return index < 0 ? SOUND_GROUPS.length : index
    }
    return Object.entries(sounds)
        .map(([key, asset]) => ({
            key: `minecraft:${key}`,
            name: translated(asset.subtitles, locale) ?? keyName(key),
            group: asset.group ?? key.split('.')[0] ?? '',
            playable: asset.playable ?? false,
        }))
        .sort((a, b) => order(a.group) - order(b.group) || a.name.localeCompare(b.name) || a.key.localeCompare(b.key))
}

/**
 * @param choices the sounds
 * @param query words that must all appear in the name or the key, in any case
 * @param group the group to keep, or none for all
 * @returns the matching sounds
 */
export function searchSounds(choices: readonly SoundChoice[], query: string, group?: string): SoundChoice[] {
    const words = query.toLowerCase().split(/\s+/).filter(word => word !== '')
    return choices.filter(choice => (!group || choice.group === group)
        && words.every(word => choice.name.toLowerCase().includes(word) || choice.key.includes(word)))
}
