/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Field} from '~/util/settings/schemaFields'
import {shownFields} from '~/util/settings/modifiedValues'

function field(key: string, kind: Field['kind'], defaultValue?: unknown): Field {
    return {key, kind, options: [], children: [], item: undefined, defaultValue, enumName: undefined}
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
})
