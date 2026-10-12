/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import axios from 'axios'
import type {ItemIndex} from '~/api/generated/schema'

const BASE = '/api/v1/assets'

/**
 * Every item of a Minecraft version with its icon and names. The first request for a version may take a while, the
 * web backend fetches it from Mojang then.
 *
 * @param version the Minecraft version, e.g. `26.3`
 * @returns the items
 */
export function itemIndex(version: string): Promise<ItemIndex> {
    return axios.get<ItemIndex>(`${BASE}/${encodeURIComponent(version)}/items`, {timeout: 120000}).then(response => response.data)
}

/**
 * @param version the Minecraft version
 * @param texture an item's texture, e.g. `item/diamond_sword`
 * @returns the URL of its icon
 */
export function textureUrl(version: string, texture: string): string {
    return `${BASE}/${encodeURIComponent(version)}/textures/${texture}.png`
}
