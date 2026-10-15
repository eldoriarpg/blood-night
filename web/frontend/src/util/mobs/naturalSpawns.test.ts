/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {spawnsNaturally} from '~/util/mobs/naturalSpawns'

describe('spawnsNaturally', () => {
    it('knows what each kind of world spawns', () => {
        expect(spawnsNaturally('CREEPER', true, 'NORMAL')).toBe(true)
        expect(spawnsNaturally('CREEPER', true, 'NETHER')).toBe(false)
        expect(spawnsNaturally('ENDERMAN', true, 'THE_END')).toBe(true)
    })

    it('counts subtypes unless the mob is limited to its exact type', () => {
        expect(spawnsNaturally('ZOMBIE', false, 'NETHER')).toBe(true)
        expect(spawnsNaturally('ZOMBIE', true, 'NETHER')).toBe(false)
    })

    it('gives unknown worlds the benefit of the doubt', () => {
        expect(spawnsNaturally('CREEPER', true, 'CUSTOM')).toBe(true)
    })
})
