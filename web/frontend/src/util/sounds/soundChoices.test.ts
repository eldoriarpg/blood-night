/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {bareSoundKey, keyName, searchSounds, soundChoices} from './soundChoices'

const sounds = {
    'entity.zombie.ambient': {group: 'entity', subtitles: {en_us: 'Zombie groans', de_de: 'Zombie stöhnt'}, playable: true},
    'ambient.basalt_deltas.additions': {group: 'ambient', subtitles: {}, playable: true},
    'ui.button.click': {group: 'ui', subtitles: {en_us: 'Button clicks'}, playable: false},
}

describe('sound choices', () => {
    it('names sounds by their subtitle in the editor language, else by their key', () => {
        const choices = soundChoices(sounds, 'de')
        expect(choices.map(choice => choice.name)).toEqual(['Basalt deltas additions', 'Zombie stöhnt', 'Button clicks'])
        expect(choices[1]).toEqual({key: 'minecraft:entity.zombie.ambient', name: 'Zombie stöhnt', group: 'entity', playable: true})
    })

    it('finds sounds by every word of the name or key, within a group', () => {
        const choices = soundChoices(sounds, 'en')
        expect(searchSounds(choices, 'zombie GROANS').map(choice => choice.key)).toEqual(['minecraft:entity.zombie.ambient'])
        expect(searchSounds(choices, 'basalt').map(choice => choice.name)).toEqual(['Basalt deltas additions'])
        expect(searchSounds(choices, '', 'ui').map(choice => choice.name)).toEqual(['Button clicks'])
    })

    it('reads keys with or without namespace', () => {
        expect(bareSoundKey('minecraft:entity.zombie.ambient')).toBe('entity.zombie.ambient')
        expect(keyName('minecraft:music.overworld.deep_dark')).toBe('Overworld deep dark')
    })
})
