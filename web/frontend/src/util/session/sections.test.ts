/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import {sectionsOf} from '~/util/session/sections'
import {PatchError} from '~/util/patch/jsonPointer'

const payload = {mobs: {a: {id: 'a'}}, macros: [{id: 'burst'}, {id: 'jump'}], settings: {main: {}, globalDrops: {}, worlds: {w: {}}}, items: {}}

describe('sectionsOf', () => {
    it('names the touched sections', () => {
        expect([...sectionsOf(payload, [{op: 'replace', path: '/mobs/a/id', value: 'a'}]).sections]).toEqual(['mobs/a'])
        expect([...sectionsOf(payload, [{op: 'remove', path: '/macros/1'}]).sections]).toEqual(['macros/jump'])
        expect([...sectionsOf(payload, [{op: 'add', path: '/macros/-', value: {id: 'dash'}}]).sections]).toEqual(['macros/dash'])
        expect([...sectionsOf(payload, [{op: 'add', path: '/settings/worlds/w/x', value: 1}]).sections]).toEqual(['settings/worlds/w'])
        expect([...sectionsOf(payload, [{op: 'add', path: '/items/x', value: {}}]).sections]).toEqual(['items/x'])
        expect(sectionsOf(payload, [{op: 'replace', path: '', value: {}}]).everything).toBe(true)
    })

    it('refuses read only parts', () => {
        expect(() => sectionsOf(payload, [{op: 'add', path: '/choices/x', value: {}}])).toThrow(PatchError)
    })
})
