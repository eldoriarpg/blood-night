/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Field, FieldKind} from '~/util/settings/schemaFields'
import {groupKey, parentKey, specialField} from './specialFields'

function field(key: string, kind: FieldKind): Field {
    return {key, kind, options: [], children: [], item: undefined, defaultValue: undefined, enumName: undefined, definition: undefined}
}

describe('special fields', () => {
    it('knows the fields drawn their own way', () => {
        expect(specialField(field('lightningChance', 'integer'), 'lightning')).toBe('percent')
        expect(specialField(field('begin', 'integer'), 'nightSettings')).toBe('dayTime')
        expect(specialField(field('monsterSpawnLimit', 'integer'), 'nightRules')).toBe('unchanged')
        expect(specialField(field('monsterTicksPerSpawn', 'integer'), 'nightRules')).toBe('unchangedSeconds')
        expect(specialField(field('effect', 'text'), 'effect')).toBe('effect')
        expect(specialField(field('dragon', 'text'), 'timed')).toBe('mob')
        expect(specialField(field('title', 'text'), 'bossBar')).toBe('miniMessage')
        expect(specialField(field('slots', 'map'), 'loot')).toBe('dropWeights')
        expect(specialField(field('tables', 'texts'), 'ordinary')).toBe('lootTables')
        expect(specialField({...field('experience', 'group'), definition: 'Experience'}, 'loot')).toBe('experience')
        expect(specialField({...field('money', 'group'), definition: 'Money'}, 'ordinary')).toBe('money')
        expect(specialField(field('gameRules', 'map'), 'nightRules')).toBe('gameRules')
        expect(specialField(field('mobs', 'map'), 'spawning')).toBe('worldMobs')
        expect(specialField(field('overrides', 'map'), 'mobSettings')).toBe('mobMap')
        expect(specialField(field('withers', 'map'), 'mobSettings')).toBe('mobMap')
        expect(specialField(field('probabilities', 'map'), 'moonPhase')).toBe('moonPhases')
        expect(specialField(field('probabilities', 'any'), 'phase')).toBe('phaseCycle')
        expect(specialField(field('curve', 'group'), 'nightSelection')).toBe('curve')
        expect(specialField({...field('name', 'group'), definition: 'MobName'}, 'attributes')).toBe('mobName')
    })

    it('leaves alike named fields elsewhere alone', () => {
        expect(specialField(field('begin', 'list'), 'soundSettings')).toBeUndefined()
        expect(specialField(field('begin', 'integer'), 'something')).toBeUndefined()
        expect(specialField(field('dragon', 'text'), 'other')).toBeUndefined()
        expect(specialField(field('enabled', 'boolean'), 'bossBar')).toBeUndefined()
    })

    it('reads a mob\'s loot apart from a world\'s', () => {
        expect(groupKey('/mobs/zombie/loot')).toBe('mobLoot')
        expect(groupKey('/settings/worlds/w/settings/loot')).toBe('loot')
    })

    it('finds the parent of a field', () => {
        expect(parentKey('/settings/worlds/w1/settings/nightSettings/begin')).toBe('nightSettings')
        expect(parentKey('/x')).toBe('')
    })
})
