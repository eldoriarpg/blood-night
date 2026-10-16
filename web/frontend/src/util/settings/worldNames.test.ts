/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {WorldPayload} from '~/api/generated/payload'
import {worldLabel} from '~/util/settings/worldNames'

function world(displayName: string, alias: string): WorldPayload {
    return {name: 'world_nether', environment: 'NETHER', dayCycle: false, alias, settings: {displayName}} as unknown as WorldPayload
}

describe('worldLabel', () => {
    it('takes the display name, else the world plugin\'s alias, else the folder name', () => {
        expect(worldLabel(world('<red>Hell', 'Nether'))).toBe('Hell')
        expect(worldLabel(world('', 'Nether'))).toBe('Nether')
        expect(worldLabel(world('', ''))).toBe('world_nether')
    })
})
