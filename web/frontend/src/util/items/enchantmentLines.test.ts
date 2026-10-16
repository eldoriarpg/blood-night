/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {enchantmentLines, roman} from '~/util/items/enchantmentLines'

describe('enchantment lines', () => {
    it('writes levels in Roman numerals', () => {
        expect([1, 2, 4, 5, 9, 10, 255].map(roman)).toEqual(['I', 'II', 'IV', 'V', 'IX', 'X', 'CCLV'])
    })

    it('reads like a tooltip, single-level ones without a level and curses apart', () => {
        const names: Record<string, string> = {'minecraft:sharpness': 'Sharpness', 'minecraft:mending': 'Mending', 'minecraft:vanishing_curse': 'Curse of Vanishing'}
        const maxLevels = {'minecraft:sharpness': 5, 'minecraft:mending': 1, 'minecraft:vanishing_curse': 1}
        expect(enchantmentLines({'minecraft:sharpness': 5, 'minecraft:mending': 1, 'minecraft:vanishing_curse': 1}, key => names[key] ?? key, maxLevels)).toEqual([
            {text: 'Mending', curse: false},
            {text: 'Sharpness V', curse: false},
            {text: 'Curse of Vanishing', curse: true},
        ])
    })
})
