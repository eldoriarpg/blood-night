/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {biomeTagName, biomeWords} from '~/util/settings/biomeNames'

describe('biomeTagName', () => {
    it('reads tags as words', () => {
        expect(biomeTagName('#minecraft:is_deep_ocean')).toEqual({kind: 'all', name: 'deep ocean'})
        expect(biomeTagName('#minecraft:has_structure/village_desert')).toEqual({kind: 'structure', name: 'desert village'})
        expect(biomeTagName('#minecraft:spawns_cold_variant_frogs')).toEqual({kind: 'group', name: 'spawns cold variant frogs'})
        expect(biomeTagName('minecraft:desert')).toBeUndefined()
    })

    it('makes words of keys without a name', () => {
        expect(biomeWords('terralith:moonlight_grove')).toBe('moonlight grove')
    })
})
