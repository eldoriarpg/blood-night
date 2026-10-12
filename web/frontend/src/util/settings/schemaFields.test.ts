/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import {fieldsOf, type SchemaDocument} from '~/util/settings/schemaFields'
import schema from '~/api/generated/payload.schema.json'

const document = schema as unknown as SchemaDocument

describe('fieldsOf', () => {
    it('builds the night settings', () => {
        const fields = fieldsOf(document, 'NightSettings')
        const byKey = new Map(fields.map(field => [field.key, field]))
        expect(byKey.get('begin')).toMatchObject({kind: 'integer', defaultValue: 13000})
        expect(byKey.get('skippable')).toMatchObject({kind: 'boolean', defaultValue: false})
        expect(byKey.get('beginCommands')?.kind).toBe('texts')
        expect(byKey.get('length')?.kind).toBe('group')
        expect(byKey.get('length')?.children.find(child => child.key === 'mode')).toMatchObject({kind: 'choice', options: ['NORMAL', 'EXTENDED', 'RANGE'], enumName: 'LengthMode'})
    })

    it('builds maps, sets of choices and lists of settings', () => {
        const spawning = fieldsOf(document, 'MobSpawning')
        expect(spawning.find(field => field.key === 'ignoredReasons')).toMatchObject({kind: 'choices', enumName: 'SpawnReason'})
        expect(spawning.find(field => field.key === 'mobs')).toMatchObject({kind: 'map', item: {kind: 'integer'}})
        const sounds = fieldsOf(document, 'SoundSettings')
        expect(sounds.find(field => field.key === 'ambience')).toMatchObject({kind: 'list', item: {kind: 'group'}})
    })
})
