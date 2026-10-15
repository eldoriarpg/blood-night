/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {curveChances} from '~/util/nights/curve'

describe('curveChances', () => {
    it('rises from min to max halfway and falls back, repeating each period', () => {
        expect(curveChances({min: 20, max: 80, period: 4}, 8)).toEqual([20, 50, 80, 50, 20, 50, 80, 50])
    })

    it('counts a period below 2 as 2', () => {
        expect(curveChances({min: 0, max: 100, period: 0}, 3)).toEqual([0, 100, 0])
    })
})
