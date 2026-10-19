/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorArea} from '~/api/generated/schema'

/**
 * The area a lockable section lies in, as the service sorts it: {@code mobs/<id>} into mobs, {@code settings/main}
 * into the server settings, and so on.
 *
 * @param section a section, e.g. `mobs/speed_zombie`
 * @returns its area, or null for a section of no area
 */
export function areaOf(section: string): EditorArea | null {
    if (section.startsWith('mobs/')) return 'MOBS'
    if (section.startsWith('macros/')) return 'MACROS'
    if (section.startsWith('items/')) return 'ITEMS'
    if (section.startsWith('lootTables/')) return 'LOOT'
    if (section.startsWith('particleEffects/')) return 'PARTICLES'
    if (section.startsWith('settings/worlds/')) return 'WORLDS'
    if (section === 'settings/main') return 'SERVER'
    return null
}

/**
 * @param section a section
 * @param areas the areas a participant may change
 * @returns whether they may change the section
 */
export function mayChange(section: string, areas: readonly EditorArea[]): boolean {
    const area = areaOf(section)
    return area !== null && areas.includes(area)
}
