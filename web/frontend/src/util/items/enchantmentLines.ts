/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/** An enchantment as an item's tooltip shows it. */
export interface EnchantmentLine {
    text: string
    curse: boolean
}

const NUMERALS: readonly [number, string][] = [[1000, 'M'], [900, 'CM'], [500, 'D'], [400, 'CD'], [100, 'C'], [90, 'XC'], [50, 'L'], [40, 'XL'], [10, 'X'], [9, 'IX'], [5, 'V'], [4, 'IV'], [1, 'I']]

/**
 * @param value a whole number from 1
 * @returns it in Roman numerals, as Minecraft writes enchantment levels
 */
export function roman(value: number): string {
    let left = Math.max(1, Math.round(value))
    let text = ''
    for (const [amount, numeral] of NUMERALS) {
        while (left >= amount) {
            text += numeral
            left -= amount
        }
    }
    return text
}

/**
 * The enchantment lines of an item's tooltip, as Minecraft shows them: the name and the level in Roman numerals,
 * without a level for enchantments that only have one, curses apart so they can be drawn red.
 *
 * @param enchantments levels by enchantment key
 * @param name the name of an enchantment by its key
 * @param maxLevels the highest level of each enchantment by its key; one without leaves out the level only when it
 *     has none above 1
 * @returns the lines, in the order of the keys
 */
export function enchantmentLines(enchantments: Readonly<Record<string, number>>, name: (key: string) => string, maxLevels: Readonly<Record<string, number>>): EnchantmentLine[] {
    return Object.entries(enchantments)
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([key, level]) => ({
            text: maxLevels[key] === 1 && level === 1 ? name(key) : `${name(key)} ${roman(level)}`,
            curse: key.endsWith('_curse'),
        }))
}
