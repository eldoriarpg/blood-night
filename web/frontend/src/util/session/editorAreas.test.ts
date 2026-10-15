/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {areaOf, mayChange} from '~/util/session/editorAreas'

describe('editor areas', () => {
    it('sorts sections into areas like the service', () => {
        expect(areaOf('mobs/speed_zombie')).toBe('MOBS')
        expect(areaOf('macros/kick')).toBe('MACROS')
        expect(areaOf('items/abc')).toBe('ITEMS')
        expect(areaOf('settings/worlds/1234')).toBe('WORLDS')
        expect(areaOf('settings/main')).toBe('SERVER')
        expect(areaOf('lootTables/gems')).toBe('LOOT')
        expect(areaOf('choices')).toBeNull()
    })

    it('lets a participant change only their areas', () => {
        expect(mayChange('mobs/a', ['MOBS'])).toBe(true)
        expect(mayChange('settings/main', ['MOBS', 'WORLDS'])).toBe(false)
    })
})
