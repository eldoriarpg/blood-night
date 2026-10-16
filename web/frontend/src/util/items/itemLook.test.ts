/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {EditorItem, EditorPayload, ItemAttribute, MaterialInfo} from '~/api/generated/payload'
import {addAttribute, attributeLines, hideableParts, keyWords, materialInfo, trimLines} from '~/util/items/itemLook'

const damage: ItemAttribute = {attribute: 'minecraft:attack_damage', amount: 7, operation: 'ADD_NUMBER', slot: 'MAINHAND'}
const sword: MaterialInfo = {stackSize: 1, durability: 1561, dyeable: false, trimmable: false, attributes: [damage]}
const item = {kind: 'DEFINED', material: 'minecraft:diamond_sword', lore: [], enchantments: {}, unbreakable: false, attributes: [], hidden: []} as unknown as EditorItem

describe('item look', () => {
    it('knows what the server said about a material', () => {
        const payload = {choices: {materialInfo: {'minecraft:diamond_sword': sword}}} as unknown as EditorPayload
        expect(materialInfo('minecraft:diamond_sword', payload)).toBe(sword)
        expect(materialInfo('minecraft:stone', payload).stackSize).toBe(64)
    })

    it('keeps the material\'s own attributes when the first one is added', () => {
        const speed: ItemAttribute = {attribute: 'minecraft:movement_speed', amount: 0.1, operation: 'ADD_SCALAR', slot: 'MAINHAND'}
        expect(addAttribute(item, sword, speed)).toEqual([damage, speed])
        expect(addAttribute({...item, attributes: [speed]}, sword, damage)).toEqual([speed, damage])
    })

    it('offers to hide only what the item shows', () => {
        expect(hideableParts(item, sword)).toEqual(['ENCHANTMENTS', 'ATTRIBUTES'])
        expect(hideableParts({...item, unbreakable: true}, {...sword, dyeable: true, trimmable: true})).toEqual(['ENCHANTMENTS', 'ATTRIBUTES', 'UNBREAKABLE', 'DYE', 'TRIM'])
    })

    it('draws trims and attributes like the game', () => {
        expect(keyWords('minecraft:wayfinder')).toBe('Wayfinder')
        expect(trimLines({pattern: 'minecraft:silence', material: 'minecraft:iron'}, {upgrade: 'Upgrade:', pattern: name => `${name} Armor Trim`, material: name => `${name} Material`}).map(line => line.text))
            .toEqual(['Upgrade:', ' Silence Armor Trim', ' Iron Material'])
        const slow: ItemAttribute = {attribute: 'minecraft:movement_speed', amount: -0.15, operation: 'ADD_SCALAR', slot: 'MAINHAND'}
        expect(attributeLines([damage, slow], slot => `When in ${slot}:`, keyWords)).toEqual([
            {text: 'When in MAINHAND:', color: '#aaaaaa'},
            {text: ' +7 Attack Damage', color: '#5555ff'},
            {text: ' -15% Movement Speed', color: '#ff5555'},
        ])
    })
})
