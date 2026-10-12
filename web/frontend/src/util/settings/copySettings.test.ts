/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import type {SettingsPayload} from '~/api/generated/payload'
import {copyOperations} from '~/util/settings/copySettings'
import {applyPatch} from '~/util/patch/jsonPatch'

const settings = {
    worlds: {
        a: {name: 'a', settings: {enabled: true, nightSettings: {begin: 12000}, mobSettings: {forcePhantoms: false}}},
        b: {name: 'b', settings: {enabled: false, nightSettings: {begin: 13000}, mobSettings: {forcePhantoms: true}}},
    },
} as unknown as SettingsPayload

describe('copyOperations', () => {
    it('replaces the chosen sections in the targets only', () => {
        const copied = applyPatch({settings}, copyOperations(settings, 'a', ['b', 'a'], ['night', 'general'])).settings
        expect(copied.worlds?.b?.settings).toEqual({enabled: true, nightSettings: {begin: 12000}, mobSettings: {forcePhantoms: true}})
        expect(copied.worlds?.a).toEqual(settings.worlds?.a)
    })
})
