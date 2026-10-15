/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Field} from '~/util/settings/schemaFields'
import {fieldOptions, spawnReasonOptions} from '~/util/settings/fieldOptions'

const field = (enumName: string): Field => ({
    key: 'entityType', kind: 'choice', options: ['ARMOR_STAND', 'CREEPER', 'LLAMA_SPIT', 'ZOMBIE'], children: [], item: undefined, defaultValue: undefined, enumName, definition: undefined,
})

describe('fieldOptions', () => {
    it('offers only the mob types for an entity type', () => {
        expect(fieldOptions(field('EntityType'), ['CREEPER', 'ZOMBIE'], 'CREEPER')).toEqual(['CREEPER', 'ZOMBIE'])
    })

    it('keeps the value already set', () => {
        expect(fieldOptions(field('EntityType'), ['CREEPER', 'ZOMBIE'], 'LLAMA_SPIT')).toEqual(['CREEPER', 'LLAMA_SPIT', 'ZOMBIE'])
    })

    it('leaves other fields and unknown mob types alone', () => {
        expect(fieldOptions(field('Surface'), ['CREEPER'], undefined)).toHaveLength(4)
        expect(fieldOptions(field('EntityType'), [], undefined)).toHaveLength(4)
    })

    it('offers a mob only the spawn reasons its type can spawn by, keeping chosen ones', () => {
        const all = ['NATURAL', 'BREEDING', 'SLIME_SPLIT', 'SPAWNER']
        expect(spawnReasonOptions(all, ['NATURAL', 'SPAWNER'], ['BREEDING'])).toEqual(['NATURAL', 'BREEDING', 'SPAWNER'])
        expect(spawnReasonOptions(all, undefined, [])).toEqual(all)
    })
})
