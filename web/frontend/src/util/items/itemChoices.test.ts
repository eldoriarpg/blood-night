/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {enchantmentName, itemChoices, keyName, materialName, plainText, searchChoices} from '~/util/items/itemChoices'

const names = {
    items: {
        'minecraft:diamond_sword': {texture: 'item/diamond_sword', names: {en_us: 'Diamond Sword', de_de: 'Diamantschwert'}},
        'minecraft:stone': {texture: 'block/stone', names: {en_us: 'Stone', de_de: 'Stein'}},
    },
    enchantments: {'minecraft:sharpness': {en_us: 'Sharpness', de_de: 'Schärfe'}},
}
const knight = {kind: 'DEFINED' as const, material: 'minecraft:diamond_sword', name: '<gold>Knight', lore: ['<gray>Forged in blood'], enchantments: {'minecraft:sharpness': 5}}

describe('item choices', () => {
    it('names materials and enchantments in the editor language, falling back to English and the key', () => {
        expect(materialName('minecraft:stone', names.items['minecraft:stone'], 'de')).toBe('Stein')
        expect(materialName('minecraft:stone', names.items['minecraft:stone'], 'fr')).toBe('Stone')
        expect(materialName('minecraft:golden_axe', undefined, 'de')).toBe('Golden Axe')
        expect(enchantmentName('minecraft:sharpness', names.enchantments['minecraft:sharpness'], 'de')).toBe('Schärfe')
        expect(enchantmentName('pack:bleeding', undefined, 'de')).toBe('Bleeding')
        expect(keyName('minecraft:oak_planks')).toBe('Oak Planks')
        expect(plainText('<red>Blood <bold>Blade')).toBe('Blood Blade')
    })

    it('offers the payload items first, with their own name, material icon and details', () => {
        const choices = itemChoices(['minecraft:diamond_sword', 'minecraft:stone'], {knight_sword: knight}, names, 'de')
        expect(choices.map(choice => [choice.key, choice.name, choice.texture, choice.custom, choice.details])).toEqual([
            ['knight_sword', 'Knight', 'item/diamond_sword', true, 'Diamantschwert · Schärfe 5'],
            ['minecraft:diamond_sword', 'Diamantschwert', 'item/diamond_sword', false, ''],
            ['minecraft:stone', 'Stein', 'block/stone', false, ''],
        ])
    })

    it('finds items by type, name, lore and enchantment, every word matching something', () => {
        const choices = itemChoices(['minecraft:diamond_sword', 'minecraft:stone'], {knight_sword: knight}, names, 'de')
        const keys = (query: string) => searchChoices(choices, query).map(choice => choice.key)
        expect(keys('stein')).toEqual(['minecraft:stone'])
        expect(keys('schwert')).toEqual(['knight_sword', 'minecraft:diamond_sword'])
        expect(keys('blood')).toEqual(['knight_sword'])
        expect(keys('sharpness')).toEqual(['knight_sword'])
        expect(keys('schärfe diamant')).toEqual(['knight_sword'])
        expect(keys('schärfe stein')).toEqual([])
        expect(keys('minecraft:st')).toEqual(['minecraft:stone'])
        expect(keys('s')).toEqual(['minecraft:stone', 'knight_sword', 'minecraft:diamond_sword'])
    })
})
