/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {litPath, MOON_PHASES} from './moonPhases'

describe('moon phases', () => {
    it('goes from full through new and back', () => {
        expect(MOON_PHASES[0]).toBe('FULL_MOON')
        expect(MOON_PHASES[4]).toBe('NEW_MOON')
        expect(MOON_PHASES).toHaveLength(8)
    })

    it('lights nothing at the new moon and everything at the full moon', () => {
        expect(litPath('NEW_MOON', 10)).toBe('')
        expect(litPath('FULL_MOON', 10)).toContain('A 10 10 0 1 1')
    })

    it('lights the right half at the first quarter and the left at the last', () => {
        expect(litPath('FIRST_QUARTER', 10)).toBe('M 10 0 A 10 10 0 0 1 10 20 A 0 10 0 0 0 10 0 Z')
        expect(litPath('LAST_QUARTER', 10)).toBe('M 10 0 A 10 10 0 0 0 10 20 A 0 10 0 0 1 10 0 Z')
    })

    it('bulges the terminator into the dark side when gibbous and into the lit side when crescent', () => {
        expect(litPath('WAXING_GIBBOUS', 10)).toContain('A 5 10 0 0 1 10 0')
        expect(litPath('WAXING_CRESCENT', 10)).toContain('A 5 10 0 0 0 10 0')
    })
})
