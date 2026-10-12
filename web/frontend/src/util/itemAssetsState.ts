/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {ItemAsset} from '~/api/generated/schema'

/** Minecraft's items of the session's version, loaded once and shared by every item picker. */
export interface ItemAssetsData {
    version: string
    status: 'idle' | 'loading' | 'ready' | 'failed'
    items: Record<string, ItemAsset>
    /** Minecraft's names by language, by key, by registry, e.g. `names.entity_type['minecraft:creeper'].de_de`. */
    names: Record<string, Record<string, Record<string, string>>>
}

export function itemAssetsState() {
    return useState<ItemAssetsData>('itemAssetsState', () => ({version: '', status: 'idle', items: {}, names: {}}))
}
