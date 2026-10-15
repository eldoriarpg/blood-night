/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Field} from '~/util/settings/schemaFields'
import {shownFields} from '~/util/settings/modifiedValues'

function field(key: string, kind: Field['kind'], defaultValue?: unknown): Field {
    return {key, kind, options: [], children: [], item: undefined, defaultValue, enumName: undefined, definition: undefined}
}

const fields = [field('healthModifier', 'choice', 'DEFAULT'), field('health', 'number'), field('xpModifier', 'choice'), field('xpValue', 'number')]

describe('modified values', () => {
    it('hides a value its modifier ignores', () => {
        expect(shownFields(fields, {healthModifier: 'DEFAULT', xpModifier: 'MULTIPLY'}).map(entry => entry.key))
            .toEqual(['healthModifier', 'xpModifier', 'xpValue'])
    })

    it('reads the modifier default when the value has none', () => {
        expect(shownFields(fields, {xpModifier: 'DEFAULT'}).map(entry => entry.key)).toEqual(['healthModifier', 'xpModifier'])
    })

    it('keeps a number named like a modifier that is no choice', () => {
        const world = [field('damageModifier', 'number'), field('damage', 'number')]
        expect(shownFields(world, {damageModifier: 2}).map(entry => entry.key)).toEqual(['damageModifier', 'damage'])
    })

    it('shows only the numbers a movement preset reads', () => {
        const movement = [{...field('preset', 'choice', 'VANILLA'), enumName: 'MovementPreset'}, field('range', 'number'), field('closest', 'number'), field('farthest', 'number')]
        expect(shownFields(movement, {preset: 'KEEP_DISTANCE'}).map(entry => entry.key)).toEqual(['preset', 'closest', 'farthest'])
        expect(shownFields(movement, {}).map(entry => entry.key)).toEqual(['preset'])
    })

    it('hides what a switched off part does not use', () => {
        const bar = [field('enabled', 'boolean', true), field('title', 'text'), field('color', 'choice')]
        expect(shownFields(bar, {enabled: false}).map(entry => entry.key)).toEqual(['enabled'])
        expect(shownFields(bar, {}).map(entry => entry.key)).toEqual(['enabled', 'title', 'color'])
    })

    it('keeps the loot multiplier of switched off vanilla drops, which special mobs still use', () => {
        const vanilla = [field('enabled', 'boolean'), field('lootMultiplier', 'number'), field('extraDrops', 'boolean')]
        expect(shownFields(vanilla, {enabled: false}).map(entry => entry.key)).toEqual(['enabled', 'lootMultiplier'])
    })

    it('shows only the settings of the picked night selection', () => {
        const selection = [field('type', 'choice', 'INTERVAL'), field('interval', 'group'), field('random', 'group'), field('moonPhase', 'group')]
        expect(shownFields(selection, {}).map(entry => entry.key)).toEqual(['type', 'interval'])
        expect(shownFields(selection, {type: 'REAL_MOON_PHASE'}).map(entry => entry.key)).toEqual(['type', 'moonPhase'])
    })

    it('rules a shared key only where it means the same', () => {
        const length = [field('mode', 'choice', 'NORMAL'), field('seconds', 'number'), field('maxSeconds', 'number')]
        const effect = [field('enabled', 'boolean'), field('seconds', 'number')]
        expect(shownFields(length, {}).map(entry => entry.key)).toEqual(['mode'])
        expect(shownFields(length, {mode: 'EXTENDED'}).map(entry => entry.key)).toEqual(['mode', 'seconds'])
        expect(shownFields(effect, {}).map(entry => entry.key)).toEqual(['enabled', 'seconds'])
    })

    it('hides world particles around players and the outfit of a MythicMobs mob', () => {
        const atmosphere = [field('worldParticles', 'boolean', true), field('particles', 'list')]
        expect(shownFields(atmosphere, {}).map(entry => entry.key)).toEqual(['worldParticles'])
        const attributes = [field('mythicType', 'text', ''), field('name', 'text'), field('mythicLevel', 'integer')]
        expect(shownFields(attributes, {}).map(entry => entry.key)).toEqual(['mythicType', 'name'])
        expect(shownFields(attributes, {mythicType: 'SkeletonKing'}).map(entry => entry.key)).toEqual(['mythicType', 'mythicLevel'])
    })

    it('hides replacing items where no broader loot joins', () => {
        const loot = [field('tables', 'texts'), field('inherit', 'boolean', true), field('replaceSame', 'boolean', true)]
        expect(shownFields(loot, {}).map(entry => entry.key)).toEqual(['tables', 'inherit', 'replaceSame'])
        expect(shownFields(loot, {inherit: false}).map(entry => entry.key)).toEqual(['tables', 'inherit'])
    })
})
