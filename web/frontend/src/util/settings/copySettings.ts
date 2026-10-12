/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {SettingsPayload} from '~/api/generated/payload'
import {pointer} from '~/util/patch/jsonPointer'
import {jsonClone, type PatchOperation} from '~/util/patch/jsonPatch'
import {WORLD_SECTIONS} from '~/util/settings/settingsSections'

/**
 * The operations copying sections of one world's settings onto other worlds: each chosen section's properties are
 * replaced in every target, one change for all of them.
 *
 * @param settings the settings of the payload
 * @param from the source world's UID
 * @param targets the target worlds' UIDs
 * @param sections the keys of the sections to copy, see {@link WORLD_SECTIONS}
 * @returns the operations
 */
export function copyOperations(settings: SettingsPayload, from: string, targets: readonly string[], sections: readonly string[]): PatchOperation[] {
    const source = settings.worlds?.[from]?.settings as Record<string, unknown> | undefined
    if (!source) return []
    const properties = WORLD_SECTIONS.filter(section => sections.includes(section.key)).flatMap(section => section.properties)
    return targets
        .filter(target => target !== from && settings.worlds?.[target])
        .flatMap(target => properties
            .filter(property => source[property] !== undefined)
            .map(property => ({op: 'add', path: pointer('settings', 'worlds', target, 'settings', property), value: jsonClone(source[property])}) as PatchOperation))
}
