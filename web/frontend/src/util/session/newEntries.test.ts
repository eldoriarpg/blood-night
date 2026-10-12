/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import type {EditorPayload} from '~/api/generated/payload'
import {addMacro, addMob, idProblem, newMacro, newMob} from '~/util/session/newEntries'
import {applyPatch} from '~/util/patch/jsonPatch'

describe('new entries', () => {
    it('checks ids', () => {
        expect(idProblem('speed_zombie', [])).toBeNull()
        expect(idProblem('Speed', [])).toBe('editor.create.badId')
        expect(idProblem('a', ['a'])).toBe('editor.create.takenId')
    })

    it('adds mobs and macros to a payload without any', () => {
        const payload = {} as EditorPayload
        const withMob = applyPatch(payload, addMob(payload, newMob('m', 'ZOMBIE')))
        expect(withMob.mobs?.m?.attributes.entityType).toBe('ZOMBIE')
        const withMacro = applyPatch(withMob, addMacro(withMob, newMacro('burst', '')))
        expect(withMacro.macros?.[0]).toMatchObject({id: 'burst', name: 'burst'})
    })
})
