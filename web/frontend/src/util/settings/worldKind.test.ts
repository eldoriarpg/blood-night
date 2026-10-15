/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import type {Field} from '~/util/settings/schemaFields'
import {worldSectionShows, worldShows} from './worldKind'

const field = (key: string): Field => ({key, kind: 'integer', options: [], children: [], item: undefined, defaultValue: undefined, enumName: undefined, definition: undefined})
const overworld = {dayCycle: true, environment: 'NORMAL'}
const nether = {dayCycle: false, environment: 'NETHER'}
const end = {dayCycle: false, environment: 'THE_END'}

describe('settings by world kind', () => {
    it('shows night times only with a day cycle, timed nights only without', () => {
        expect(worldShows(field('begin'), 'nightSettings', overworld)).toBe(true)
        expect(worldShows(field('begin'), 'nightSettings', nether)).toBe(false)
        expect(worldShows(field('timed'), 'nightSettings', overworld)).toBe(false)
        expect(worldShows(field('timed'), 'nightSettings', nether)).toBe(true)
        expect(worldShows(field('beginCommands'), 'nightSettings', nether)).toBe(true)
    })

    it('shows the dragon fight only in the End', () => {
        expect(worldShows(field('dragonFight'), 'timed', end)).toBe(true)
        expect(worldShows(field('dragon'), 'timed', nether)).toBe(false)
        expect(worldShows(field('everyMinutes'), 'timed', nether)).toBe(true)
    })

    it('shows everything without a world, and the night selection only with a day cycle', () => {
        expect(worldShows(field('begin'), 'nightSettings', undefined)).toBe(true)
        expect(worldSectionShows('selection', nether)).toBe(false)
        expect(worldSectionShows('selection', overworld)).toBe(true)
        expect(worldSectionShows('mobs', nether)).toBe(true)
    })
})
