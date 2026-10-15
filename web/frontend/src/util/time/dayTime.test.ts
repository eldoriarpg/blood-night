/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {clockTime, ticksOf} from './dayTime'

describe('Minecraft clock time', () => {
    it('shows ticks after sunrise as the clock does', () => {
        expect(clockTime(0)).toBe('06:00')
        expect(clockTime(6000)).toBe('12:00')
        expect(clockTime(13000)).toBe('19:00')
        expect(clockTime(13500)).toBe('19:30')
        expect(clockTime(18000)).toBe('00:00')
        expect(clockTime(23000)).toBe('05:00')
    })

    it('reads a clock time back into ticks', () => {
        expect(ticksOf('19:30')).toBe(13500)
        expect(ticksOf('06:00')).toBe(0)
        expect(ticksOf('05:00')).toBe(23000)
        expect(ticksOf('25:00')).toBeUndefined()
        expect(ticksOf('evening')).toBeUndefined()
    })
})
