/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {WorldPayload} from '~/api/generated/payload'
import {plainText} from '~/util/items/itemChoices'

/**
 * The name players see for a world, as the plugin picks it: the world's own display name, else its name in
 * Multiverse-Core or Hyperverse, else its folder name.
 *
 * @param world the world's entry in the payload
 * @returns its name for players, without colours
 */
export function worldLabel(world: WorldPayload): string {
    const own = plainText(world.settings?.displayName ?? '')
    if (own !== '') return own
    return world.alias?.trim() || world.name
}
