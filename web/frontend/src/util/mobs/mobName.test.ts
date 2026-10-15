/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {mobLabel} from '~/util/mobs/mobName'

describe('mobLabel', () => {
    it('prefers the language, then English, then any', () => {
        const name = {style: '<red>', texts: {en: 'Armored Zombie', de: 'Gepanzerter Zombie'}}
        expect(mobLabel(name, 'de')).toBe('Gepanzerter Zombie')
        expect(mobLabel(name, 'fr')).toBe('Armored Zombie')
        expect(mobLabel({style: '', texts: {tr: 'Zırhlı', fr: 'Zombie'}}, 'de')).toBe('Zombie')
        expect(mobLabel(undefined, 'en')).toBe('')
    })
})
