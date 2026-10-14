/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorPayload} from '~/api/generated/payload'

/**
 * @param key a list field's key
 * @param payload the session's payload
 * @returns what the server suggests for it: its world names for `worlds`, its biomes and biome tags for `biomes`; null
 *     for any other list
 */
export function suggestionsFor(key: string, payload: EditorPayload | null | undefined): string[] | null {
    if (key === 'worlds') return Object.values(payload?.settings.worlds ?? {}).map(world => world.name).sort()
    if (key === 'biomes') return [...payload?.choices.biomes ?? []]
    return null
}

/**
 * @param attributes attribute keys, e.g. `minecraft:max_health`
 * @returns their names to show, e.g. `Max Health`, by key; keys of other namespaces are shown as they are
 */
export function attributeLabels(attributes: readonly string[]): Record<string, string> {
    return Object.fromEntries(attributes.filter(key => key.startsWith('minecraft:')).map(key => [key, key.slice('minecraft:'.length)
        .split(/[._]/).filter(Boolean).map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')]))
}
