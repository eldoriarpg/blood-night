/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {EditorPayload} from '~/api/generated/payload'
import {attributeLabels, suggestionsFor} from '~/util/settings/serverSuggestions'

const payload = {
    settings: {worlds: {b: {name: 'world_nether'}, a: {name: 'world'}}},
    choices: {biomes: ['#minecraft:is_forest', 'minecraft:desert']},
} as unknown as EditorPayload

describe('server suggestions', () => {
    it('suggests world names and biomes, nothing for other lists', () => {
        expect(suggestionsFor('worlds', payload)).toEqual(['world', 'world_nether'])
        expect(suggestionsFor('biomes', payload)).toEqual(['#minecraft:is_forest', 'minecraft:desert'])
        expect(suggestionsFor('beginCommands', payload)).toBeNull()
    })

    it('names attributes without their namespace', () => {
        expect(attributeLabels(['minecraft:max_health', 'mod:jump.strength'])).toEqual({'minecraft:max_health': 'Max Health'})
    })
})
