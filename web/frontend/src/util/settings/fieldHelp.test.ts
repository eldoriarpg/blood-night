/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Field} from '~/util/settings/schemaFields'
import {fieldHelpKey} from '~/util/settings/fieldHelp'

function field(key: string, defaultValue?: unknown): Field {
    return {key, kind: 'any', options: [], children: [], item: undefined, defaultValue, enumName: undefined}
}

describe('field help', () => {
    it('follows the field that decides what a value means', () => {
        const fields = [field('xpModifier', 'MULTIPLY'), field('xpValue')]
        expect(fieldHelpKey(fields[1]!, fields, {xpModifier: 'VALUE'})).toBe('settings.help.xpValue.VALUE')
        expect(fieldHelpKey(fields[1]!, fields, {})).toBe('settings.help.xpValue.MULTIPLY')
    })

    it('gives other fields their own text', () => {
        const fields = [field('lootMultiplier')]
        expect(fieldHelpKey(fields[0]!, fields, {})).toBe('settings.help.lootMultiplier')
    })

    it('keeps a shared key without the deciding field out of the per-value texts', () => {
        const effect = [field('enabled'), field('seconds')]
        expect(fieldHelpKey(effect[1]!, effect, {})).toBe('settings.help.seconds')
    })

    it('leaves a key that means something else in another object without help', () => {
        const sounds = [field('begin'), field('end'), field('ambience')]
        const night = [field('begin'), field('skippable')]
        expect(fieldHelpKey(sounds[0]!, sounds, {})).toBeUndefined()
        expect(fieldHelpKey(night[0]!, night, {})).toBe('settings.help.begin')
    })
})
