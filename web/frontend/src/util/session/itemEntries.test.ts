/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {EditorPayload} from '~/api/generated/payload'
import {addItem, isItemField, itemEntries, nameItem, newItem} from '~/util/session/itemEntries'
import {applyPatch} from '~/util/patch/jsonPatch'

describe('item entries', () => {
    const payload = {
        items: {b: newItem('minecraft:stick'), a: {...newItem('minecraft:stone'), kind: 'REGISTERED'}},
        choices: {materials: ['minecraft:stone'], enchantments: {}},
    } as unknown as EditorPayload

    it('lists items by id', () => {
        expect(itemEntries(payload).map(entry => entry.id)).toEqual(['a', 'b'])
        expect(itemEntries(undefined)).toEqual([])
    })

    it('adds items to a payload without any', () => {
        const empty = {} as EditorPayload
        const added = applyPatch(empty, addItem(empty, 'axe', newItem('minecraft:golden_axe')))
        expect(added.items?.axe).toEqual({kind: 'DEFINED', material: 'minecraft:golden_axe', lore: [], enchantments: {}, unbreakable: false, attributes: [], hidden: []})
    })

    it('names items and removes empty names', () => {
        const item = {...newItem('minecraft:stick'), name: '<red>Stick'}
        expect(nameItem('b', item, ' <red>Stick ')).toEqual([])
        expect(nameItem('b', item, '')).toEqual([{op: 'remove', path: '/items/b/name'}])
        expect(nameItem('b', newItem('minecraft:stick'), '<gold>Rod')).toEqual([{op: 'add', path: '/items/b/name', value: '<gold>Rod'}])
    })

    it('knows the fields that name an item', () => {
        expect(isItemField('mainHand')).toBe(true)
        expect(isItemField('entityType')).toBe(false)
    })
})
