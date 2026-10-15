/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {randomIn, rangeOf} from './soundRanges'

describe('sound ranges', () => {
    it('reads a stored range lower end first, a missing end as normal', () => {
        expect(rangeOf({min: 1.2, max: 0.8})).toEqual([0.8, 1.2])
        expect(rangeOf({min: 0.5})).toEqual([0.5, 1])
        expect(rangeOf(undefined)).toEqual([1, 1])
    })

    it('picks within the range', () => {
        for (let i = 0; i < 50; i++) {
            const value = randomIn([0.8, 1.2])
            expect(value).toBeGreaterThanOrEqual(0.8)
            expect(value).toBeLessThanOrEqual(1.2)
        }
        expect(randomIn([0.7, 0.7])).toBe(0.7)
    })
})
