/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {isTranslated, withFallbacks} from '~/util/minimessage/translations'

describe('withFallbacks', () => {
    it('replaces texts in the player\'s language by their fallback', () => {
        expect(withFallbacks('<dark_red><lang_or:bloodnight.mob.armored_zombie:\'Armored Zombie\'>')).toBe('<dark_red>Armored Zombie')
        expect(withFallbacks('<lang_or:bloodnight.mob.x:\'Dragon\\\'s Heir\'>')).toBe('Dragon\'s Heir')
    })

    it('tells translated texts apart', () => {
        expect(isTranslated('<lang_or:a.b:\'A\'>')).toBe(true)
        expect(isTranslated('<red>Zombie')).toBe(false)
    })
})
